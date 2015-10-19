package nl.larsdenbakker.operation;

import nl.larsdenbakker.operation.template.OperationTemplate;
import nl.larsdenbakker.operation.template.SimpleOperationTemplate;
import nl.larsdenbakker.operation.operations.Operation;
import nl.larsdenbakker.operation.procedure.ProcedureTemplate;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import nl.larsdenbakker.app.Module;
import nl.larsdenbakker.datafile.DataFileException;
import nl.larsdenbakker.storage.MemoryStorage;
import nl.larsdenbakker.storage.Storage;
import nl.larsdenbakker.operation.procedure.ConditionalStatement;
import nl.larsdenbakker.operation.procedure.OperationSequence;
import nl.larsdenbakker.operation.procedure.ProcedureTask;
import nl.larsdenbakker.operation.variables.Variable;
import nl.larsdenbakker.util.ApplicationUtils;
import nl.larsdenbakker.util.CollectionUtils;
import nl.larsdenbakker.app.InvalidInputException;

/**
 *
 * @author Lars den Bakker <larsdenbakker at gmail.com>
 */
public class OperationFactory {

   /**
    * Register an Operation type.
    *
    * @param parentModule    The module the Operation belongs to.
    * @param operationModule The associated OperationModule.
    * @param name            The name (key) of the operation. This must be a unique value.
    * @param operationType   The class for the Operation type.
    */
   public static void registerOperations(Module parentModule, OperationModule operationModule, String name, Class<? extends Operation> operationType) {
      List<String> variableKeys = new ArrayList();
      for (Field f : operationType.getFields()) {
         if (Modifier.isStatic(f.getModifiers())) {
            if (f.getName().startsWith("KEY_")) {
               try {
                  Object obj = f.get(null);
                  if (obj instanceof String) {
                     variableKeys.add((String) obj);
                  } else {
                     throw new IllegalArgumentException("Field " + f.getName() + " in class " + operationType + " is not of type String.");
                  }
               } catch (IllegalArgumentException | IllegalAccessException ex) {
                  throw new IllegalArgumentException("Field " + f.getName() + " in class " + operationType + " could not be retreived.", ex);
               }
            }
         }
      }
      Variable[] variables = new Variable[variableKeys.size()];
      for (int i = 0; i < variableKeys.size(); i++) {
         variables[i] = new Variable(operationModule, variableKeys.get(i), null);
      }
      SimpleOperationTemplate template = new SimpleOperationTemplate(parentModule, operationModule, name, variables, operationType);
      operationModule.getOperationRegistry().register(template);
   }

   /**
    * Register Procedures from a Storage configuration.
    *
    * @param parentModule    The module the Procedures belong to.
    * @param operationModule The associated OperationModule.
    * @param storage         The storage containing configuration of the Procedures.
    *
    * @throws InvalidInputException if any of the Procedures were configured incorrectly.
    */
   public static void registerProcedures(Module parentModule, OperationModule operationModule, Storage storage) throws InvalidInputException {
      try {
         OperationRegistry registry = operationModule.getOperationRegistry();
         for (Storage node : storage.getNodes()) {
            OperationTemplate operationTemplate = createProcedure(parentModule, operationModule, node);
            if (!registry.isRegistered(operationTemplate)) {
               registry.register(operationTemplate.getKey(), operationTemplate);
            } else {
               throw new InvalidInputException("An operation called " + operationTemplate.getKey()
                                               + " is already registered under that name.").addFailedAction("registering procedures");
            }
         }
      } catch (InvalidInputException ex) {
         throw ex.addFailedAction("registering procedures");
      }
   }

   /**
    * Register Procedures from a Map configuration.
    *
    * @param parentModule    The module the Procedures belong to.
    * @param operationModule The associated OperationModule.
    * @param map             The map containing configuration of the Procedures.
    *
    * @throws InvalidInputException if any of the Procedures were configured incorrectly.
    */
   public static void registerProcedures(Module parentModule, OperationModule operationModule, Map<String, Object> map) throws InvalidInputException {
      registerProcedures(parentModule, operationModule, MemoryStorage.create(operationModule.getConversionModule(), map));
   }

   /**
    * Register Procedures from the given Module's configuration. Default Module
    * configuration (resource and configuration folder) is searched.
    *
    * @param parentModule    The module the Procedures belong to.
    * @param operationModule The associated OperationModule.
    * @param fileName        The name of the file containing configuration of the Procedures.
    *
    * @throws DataFileException if there was a problem loading the resource or file.
    * @throws InvalidInputException if any of the Procedures were configured incorrectly.
    */
   public static void registerProcedures(Module parentModule, OperationModule operationModule, String fileName) throws DataFileException, InvalidInputException {
      try {
         Map<String, Object> configuration = ApplicationUtils.loadModuleConfiguration(parentModule, fileName);
         registerProcedures(parentModule, operationModule, configuration);
      } catch (InvalidInputException ex) {
         throw ex.addFailedAction("reading file: '" + fileName + "'");
      }
   }

   private static OperationTemplate createProcedure(Module parentModule, OperationModule operationModule, Storage storage) throws InvalidInputException {
      Variable[] variables = createVariables(operationModule, storage);
      final String operationsKey = "operations";
      Storage operationsStorage = storage.getAndAssert(operationsKey, Storage.class);
      Map<String, OperationTemplate> operationTemplates = createProcedureOperations(parentModule, operationModule, operationsStorage);
      ProcedureTask[] procedureTasks = createProcedureTasks(operationModule, operationTemplates, storage);
      ProcedureTemplate procedureTemplate = new ProcedureTemplate(parentModule, operationModule, storage.getStorageKey(), variables, procedureTasks);
      return procedureTemplate;

   }

   private static Map<String, OperationTemplate> createProcedureOperations(Module parentModule, OperationModule operationModule, Storage storage) throws InvalidInputException {
      final String typeKey = "type";
      Map<String, OperationTemplate> operations = new LinkedHashMap(); //Linked in order to maintain order
      OperationRegistry registry = operationModule.getOperationRegistry();
      for (Storage node : storage.getNodes()) {
         String type = node.getAndAssert(typeKey, String.class);
         String name = node.getStorageKey();
         OperationTemplate template = registry.getByKey(type);
         if (template != null) {
            Variable[] variables = createVariables(operationModule, node);
            ProcedureTask task = new OperationSequence(template);
            operations.put(name, new ProcedureTemplate(parentModule, operationModule, name, variables, task));
         } else {
            throw new InvalidInputException("Did not find any Operation called: " + type + " specified at: '" + node.getStoragePath() + "." + typeKey + "'");
         }
      }
      return operations;
   }

   private static ProcedureTask[] createProcedureTasks(OperationModule operationModule, Map<String, OperationTemplate> operations, Storage storage) throws InvalidInputException {
      final String conditionsKey = "conditionals";

      Storage conditionsNode = storage.getStorage(conditionsKey, false);
      if (conditionsNode != null) {
         //It is a procedure with if/then/else conditionals
         List<ProcedureTask> procedures = new ArrayList();
         for (String key : conditionsNode.getKeys()) {
            ProcedureTask procedureTask = createProcedureTask(operationModule, operations, storage, key);
            procedures.add(procedureTask);
         }
         return procedures.toArray(new ProcedureTask[procedures.size()]);
      } else {
         //It is a procedure with only operationTemplatesResult, we will execute the operationTemplatesResult in order without conditionals.
         OperationTemplate[] operationTemplates = CollectionUtils.asArrayOfType(OperationTemplate.class, operations.values());
         OperationSequence operationSequence = new OperationSequence();
         return CollectionUtils.asArray(operationSequence);
      }
   }

   private static ProcedureTask createProcedureTask(OperationModule operationModule, Map<String, OperationTemplate> operations, Storage storage, String key) throws InvalidInputException {
      storage.assertSet(key);
      if (storage.isStorage(key)) {
         Storage node = storage.getStorage(key);
         ProcedureTask ifStatement = createProcedureTask(operationModule, operations, node, "if");
         ProcedureTask thenStatement = createProcedureTask(operationModule, operations, node, "then");
         ProcedureTask elseStatement = createProcedureTask(operationModule, operations, node, "else");
         ConditionalStatement conditionalStatement = new ConditionalStatement(ifStatement, thenStatement, elseStatement);
         return conditionalStatement;
      } else {
         List<String> operationNames = storage.getCollection(key, ArrayList.class, String.class);
         List<OperationTemplate> operationList = new ArrayList();
         for (String operationName : operationNames) {
            operationName = operationName.toLowerCase();
            OperationTemplate template = operations.get(operationName);
            if (template == null) {
               template = operationModule.getOperationRegistry().getByKey(operationName);
            }
            if (template != null) {
               operationList.add(template);
            } else {
               throw new InvalidInputException("Could not find operation: " + operationName + " specified at: " + storage.getStoragePath() + "." + key);
            }
         }
         OperationSequence operationSequence = new OperationSequence(CollectionUtils.asArrayOfType(OperationTemplate.class, operationList));
         return operationSequence;
      }
   }

   private static Variable[] createVariables(OperationModule operationHandler, Storage storage) {
      final String variablesKey = "variables";
      List<Variable> list = new ArrayList();
      Storage variablesStorage = storage.getStorage(variablesKey);
      if (variablesStorage != null) {
         for (String key : variablesStorage.getKeys()) {
            Object value = variablesStorage.get(key);
            list.add(new Variable(operationHandler, key, value));
         }
      }
      return list.toArray(new Variable[list.size()]);
   }
}
