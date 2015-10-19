package nl.larsdenbakker.operation;

import java.util.Map;
import java.util.Map.Entry;
import nl.larsdenbakker.storage.MemoryStorage;
import nl.larsdenbakker.storage.Storage;
import nl.larsdenbakker.operation.template.OperationTemplate;
import nl.larsdenbakker.app.ApplicationUser;

/**
 * The root of a stack of one or more OperationContexts.
 *
 * @author Lars den Bakker <larsdenbakker at gmail.com>
 */
public class OperationContextRoot extends OperationContext {

   private final OperationModule operationHandler;
   private final ApplicationUser executor;
   private final Map<String, Object> arguments;

   public OperationContextRoot(OperationModule operationHandler, ApplicationUser executor, OperationTemplate template, Map<String, Object> arguments) {
      super(null, template);
      this.operationHandler = operationHandler;
      this.executor = executor;
      this.arguments = arguments;
   }

   @Override
   public Map<String, Object> getRootArguments() {
      return arguments;
   }

   @Override
   public OperationModule getOperationHandler() {
      return operationHandler;
   }

   @Override
   public ApplicationUser getExecutor() {
      return executor;
   }

   @Override
   protected Storage getStorage() {
      if (storage != null) {
         return storage;
      } else {
         storage = MemoryStorage.create(getOperationHandler().getConversionModule());
         for (Entry<String, Object> argument : arguments.entrySet()) {
            storage.set(argument.getKey(), argument.getValue());
         }
         return storage;
      }
   }

   @Override
   public OperationContextRoot getRoot() {
      return this;
   }

}
