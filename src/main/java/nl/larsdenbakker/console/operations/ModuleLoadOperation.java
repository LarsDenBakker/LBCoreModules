package nl.larsdenbakker.console.operations;

import nl.larsdenbakker.app.Application;
import nl.larsdenbakker.app.Module;
import nl.larsdenbakker.storage.Storage;
import nl.larsdenbakker.operation.OperationContext;
import nl.larsdenbakker.operation.operations.TargetedOperation;
import nl.larsdenbakker.app.InvalidInputException;
import nl.larsdenbakker.util.OperationResponse;
import nl.larsdenbakker.app.UserInputException;

/**
 * Operation to load a Module.
 *
 * @author Lars den Bakker <larsdenbakker at gmail.com>
 */
public class ModuleLoadOperation extends TargetedOperation<Application> {

   public static final String KEY_MODULE = "module".intern();

   private final Class<? extends Module> moduleClass;

   public ModuleLoadOperation(OperationContext context, Storage storage) throws InvalidInputException {
      super(context, storage, Application.class);
      moduleClass = storage.getAndAssert(KEY_MODULE, Class.class);
   }

   @Override
   protected OperationResponse _execute() {
      Application target = getTarget();
      try {
         target.loadModule(moduleClass);
         return OperationResponse.succeeded("Loaded module " + target.getName() + ".");
      } catch (UserInputException ex) {
         return OperationResponse.failed(ex.getUserFriendlyErrorMessage());
      }
   }

}
