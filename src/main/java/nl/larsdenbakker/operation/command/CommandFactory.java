package nl.larsdenbakker.operation.command;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import nl.larsdenbakker.app.Module;
import nl.larsdenbakker.configuration.ConfigurationTemplate;
import nl.larsdenbakker.datafile.DataFile;
import nl.larsdenbakker.datafile.DataFileException;
import nl.larsdenbakker.storage.MemoryStorage;
import nl.larsdenbakker.storage.Storage;
import nl.larsdenbakker.operation.OperationModule;
import nl.larsdenbakker.util.ApplicationUtils;
import nl.larsdenbakker.app.InvalidInputException;
import static nl.larsdenbakker.util.Message.Type.INFO;

/**
 * Factory to create and register commands.
 *
 * @author Lars den Bakker <larsdenbakker at gmail.com>
 */
public class CommandFactory {

   /**
    * Create and register commands from configuration. Configuration is read from the
    * the module's default resource (jar file) and configuration file locations.
    *
    * @param parentModule    The module the commands are registered from.
    * @param operationModule The associated OperationModule.
    * @param fileName        The name of the configuration file.
    *
    * @throws DataFileException     if anything goes wrong when loading the file.
    * @throws InvalidInputException if there were any problems parsing the configuration.
    */
   public static void createAndRegisterCommands(Module parentModule, OperationModule operationModule, String fileName) throws DataFileException, InvalidInputException {
      try {
         Map<String, Object> configuration = ApplicationUtils.loadModuleConfiguration(parentModule, fileName);
         CommandFactory.createAndRegisterCommands(parentModule, operationModule, configuration);
      } catch (InvalidInputException ex) {
         throw ex.addFailedAction("reading file: '" + fileName + "'");
      }
   }

   /**
    * Create and register commands from configuration. Configuration is read from the
    * the provided DataFile.
    *
    * @param parentModule    The module the commands are registered from.
    * @param operationModule The associated OperationModule.
    * @param dataFile        The data file containing the command configuration.
    *
    * @throws DataFileException     if anything goes wrong when loading the file.
    * @throws InvalidInputException if there were any problems parsing the configuration.
    */
   public static void createAndRegisterCommands(Module parentModule, OperationModule operationModule, DataFile dataFile) throws DataFileException, InvalidInputException {
      try {
         Map<String, Object> fileContents = dataFile.load();
         CommandFactory.createAndRegisterCommands(parentModule, operationModule, fileContents);
      } catch (InvalidInputException ex) {
         throw ex.addFailedAction("reading file '" + dataFile.getFile() + "'");
      }
   }

   /**
    * Create and register commands from configuration. Configuration is read from the
    * the provided map.
    *
    * @param parentModule    The module the commands are registered from.
    * @param operationModule The associated OperationModule.
    * @param map             The map containing the command configuration.
    *
    * @throws DataFileException     if anything goes wrong when loading the file.
    * @throws InvalidInputException if there were any problems parsing the configuration.
    */
   public static void createAndRegisterCommands(Module parentModule, OperationModule operationModule, Map<String, Object> map) throws InvalidInputException {
      CommandFactory.createAndRegisterCommands(parentModule, operationModule, MemoryStorage.create(operationModule.getConversionModule(), map));
   }

   /**
    * Create and register commands from configuration. Configuration is read from the
    * the provided storage.
    *
    * @param parentModule    The module the commands are registered from.
    * @param operationModule The associated OperationModule.
    * @param storage         The name storage containing command configuration.
    *
    * @throws InvalidInputException if there were any problems parsing the configuration.
    */
   public static void createAndRegisterCommands(Module parentModule, OperationModule operationModule, Storage storage) throws InvalidInputException {
      try {
         int count = 0;
         Storage commandsStorage = storage.getAndAssertStorage("commands");
         for (String key : commandsStorage.getKeys()) {
            if (commandsStorage.isStorage(key)) {
               Storage commandNode = commandsStorage.getStorage(key);
               Command command = createCommand(parentModule, operationModule, key, commandNode);
               operationModule.getCommandRegistry().register(command);
               count++;
            }
         }
         parentModule.getParentApplication().getConsole().message(INFO, "Registered " + count + " operations.");
      } catch (InvalidInputException ex) {
         throw ex.addFailedAction("registering commands");
      }
   }

   private static Command createCommand(Module parentModule, OperationModule operationModule, String key, Storage commandStorage) throws InvalidInputException {
      Map<String, Command> subCommands = new LinkedHashMap();

      if (commandStorage.isSet("templates", ArrayList.class)) {
         String templatesKey = "templates";
         Map<String, Object> variables = commandStorage.getMap("template-variables", LinkedHashMap.class, String.class, Object.class, true);
         List<String> templates = commandStorage.getCollection(templatesKey, ArrayList.class, String.class);
         for (String templateKey : templates) {
            ConfigurationTemplate template = operationModule.getConfigurationModule().getTemplateRegistry().getByKey(templateKey);
            if (template != null) {
               Map<String, Object> toTemplate = template.toTemplate(variables);
               if (toTemplate != null && !toTemplate.isEmpty()) {
                  Map<String, Object> map = commandStorage.getMap("sub-commands", LinkedHashMap.class, String.class, Object.class, true);
                  map.putAll((Map) toTemplate);
               } else {
                  throw new IllegalStateException("OperationTemplate returned a null template.");
               }
            } else {
               throw new InvalidInputException("Could not find configuration template '" + template + "' specified at: " + commandStorage.getStoragePath() + "." + templatesKey);
            }
         }
      }

      if (commandStorage.isStorage("sub-commands")) {
         Storage subCommandsNode = commandStorage.getStorage("sub-commands");
         for (String subCommandKey : subCommandsNode.getKeys()) {
            if (subCommandsNode.isStorage(subCommandKey)) {
               Command command = createCommand(parentModule, operationModule, subCommandKey, subCommandsNode.getStorage(subCommandKey));
               subCommands.put(command.getKey(), command);
            }
         }
      }

      boolean adminCommand = commandStorage.get("admin", Boolean.class, false);
      boolean rootCommand = commandStorage.get("root", Boolean.class, false);
      String operation = commandStorage.get("operation", String.class);
      List<CommandArgument> arguments = new ArrayList();

      boolean atOptional = false;

      if (commandStorage.isStorage("arguments")) {
         Storage argumentsNode = commandStorage.getStorage("arguments");
         for (String argumentKey : argumentsNode.getKeys()) {
            Storage argumentNode = argumentsNode.getAndAssertStorage(argumentKey);
            CommandArgument argument = createArgument(argumentNode, arguments.size() + 1);
            if (atOptional && !argument.isOptional()) {
               throw new InvalidInputException("Argument at '" + argumentNode.getStoragePath()
                                               + " was not declared as optional, but it was preceded by an optional argument.");
            }
            arguments.add(argument);
         }
      }
      Map<String, Object> variables;

      if (commandStorage.isStorage("variables")) {
         Storage variablesNode = commandStorage.getStorage("variables");
         variables = variablesNode.getContents();
      } else {
         variables = null;
      }

      Command command = new Command(parentModule, key, adminCommand, rootCommand, operation, arguments, subCommands, variables);
      return command;
   }

   private static CommandArgument createArgument(Storage argumentStorage, int index) throws InvalidInputException {
      final String keyVariable = "variable";
      final String keyOptional = "optional";

      String variable = argumentStorage.getAndAssert(keyVariable, String.class);
      String description = argumentStorage.getStorageKey();
      boolean optional = argumentStorage.get(keyOptional, Boolean.class, false);

      return new CommandArgument(variable, description, optional);

   }

}
