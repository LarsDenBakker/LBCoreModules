package nl.larsdenbakker.console.operations;

import nl.larsdenbakker.app.Application;
import nl.larsdenbakker.storage.Storage;
import nl.larsdenbakker.operation.OperationContext;
import nl.larsdenbakker.operation.operations.Operation;
import nl.larsdenbakker.app.InvalidInputException;
import nl.larsdenbakker.util.OperationResponse;
import nl.larsdenbakker.app.UserInputException;

/**
 * Operation to trigger an Application shutdown.
 *
 * @author Lars den Bakker<larsdenbakker@gmail.com>
 */
public class ApplicationShutdownOperation extends Operation {

   private final boolean saveFirst;
   private final Application app;

   public ApplicationShutdownOperation(OperationContext context, Storage storage) throws InvalidInputException {
      super(context, storage);
      app = storage.getConversionModule().getParentApplication();
      this.saveFirst = storage.get("save-first", Boolean.class, true);
   }

   @Override
   protected OperationResponse _execute() {
      if (app.canShutdown()) {
         try {
            app.saveToDisk();
         } catch (UserInputException ex) {
            return OperationResponse.failed("Unable to shut down application " + app.getName() + ": " + ex.getUserFriendlyErrorMessage());
         }
         app.shutdown();
         return OperationResponse.succeeded("Shut down application " + app.getName());
      } else {
         return OperationResponse.failed("Unable to shut down application " + app.getName() + ".");
      }
   }

}
