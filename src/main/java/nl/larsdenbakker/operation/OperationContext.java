package nl.larsdenbakker.operation;

import nl.larsdenbakker.operation.template.OperationTemplate;
import nl.larsdenbakker.operation.operations.Operation;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import nl.larsdenbakker.storage.MemoryStorage;
import nl.larsdenbakker.storage.Storage;
import nl.larsdenbakker.operation.variables.Variable;
import nl.larsdenbakker.util.TextUtils;
import nl.larsdenbakker.app.ApplicationUser;
import nl.larsdenbakker.app.InvalidInputException;
import nl.larsdenbakker.util.OperationResponse;

/**
 * Context Object Operation execution is started and is passed along during
 * Operation execution. Contains information about the executor of the Operation, OperationTemplate being
 * executed and the Operation's variables.
 *
 * @author Lars den Bakker <larsdenbakker at gmail.com>
 */
public class OperationContext {

   private final OperationContext parent;
   protected Storage storage;
   private final OperationTemplate template;

   private Operation operation;

   public OperationContext(OperationContext parent, OperationTemplate template) {
      this.parent = parent;
      this.template = template;
   }

   public OperationTemplate getOperationTemplate() {
      return template;
   }

   public OperationModule getOperationHandler() {
      return parent.getOperationHandler();
   }

   public ApplicationUser getExecutor() {
      return parent.getExecutor();
   }

   public Operation getOperation() {
      return operation;
   }

   protected Storage getStorage() {
      if (storage != null) {
         return storage;
      } else {
         storage = MemoryStorage.create(getOperationHandler().getConversionModule());
         return storage;
      }
   }

   public OperationContext getParent() {
      return parent;
   }

   public OperationContextRoot getRoot() {
      return parent.getRoot();
   }

   protected Map<String, Object> getRootArguments() {
      return parent.getRootArguments();
   }

   /**
    * Execute the Operation associated with this OperationContext.
    *
    * @return The response of the operation.
    */
   public OperationResponse execute() {
      try {
         Storage storage = getStorage();
         Storage parentStorage = (parent != null) ? parent.getStorage() : getStorage();
         for (Variable variable : template.getVariables()) {
            variable.mapVariableToStorage(parentStorage, storage);
         }
         registerDefaults(storage);
         try {
            operation = template.createInstance(this, storage);
            return operation.execute();
         } catch (InvalidInputException ex) {
            return OperationResponse.failed("An error occurred when executing this operation: " + ex.getMessage());
         }
      } catch (Exception ex) {
         getOperationHandler().getParentApplication().getConsole();
         return OperationResponse.failed("An unknown error occurred when executing this operation.");
      }
   }

   private void registerDefaults(Storage storage) {
      storage.set("executor", getExecutor());
      storage.set(Operation.KEY_NAME, getOperationTemplate().getKey());
   }

   /**
    * Replace any variables (prefixed with a $) in the given
    * String with variable values of this OperationContext.
    *
    * @param string The String.
    *
    * @return The String with variables replaced.
    */
   public String replaceVariables(String string) {
      String[] split = TextUtils.splitOnSpaces(string);
      StringBuilder sb = new StringBuilder(24);
      for (String str : split) {
         if (str.startsWith("$")) {
            String temp = str.substring(1);
            if (storage.isSet(temp)) {
               str = TextUtils.getDescription(storage.get(temp));
            }
         }
         sb.append(str).append(' ');
      }
      sb.deleteCharAt(sb.length() - 1);
      return sb.toString();
   }

   /**
    * @return The order in which operations have been executed in this
    *         context.
    */
   public List<Operation> getExecutionTrail() {
      OperationContext context = this;
      List<Operation> trail = new ArrayList();
      while (context != null) {
         trail.add(context.getOperation());
         context = context.getParent();
      }
      return Lists.reverse(trail);
   }

}
