package nl.larsdenbakker.console.operations;

import nl.larsdenbakker.app.Application;
import nl.larsdenbakker.storage.Storage;
import nl.larsdenbakker.operation.OperationContext;
import nl.larsdenbakker.operation.operations.Operation;
import nl.larsdenbakker.app.InvalidInputException;
import nl.larsdenbakker.util.OperationResponse;
import nl.larsdenbakker.app.UserInputException;

/**
 * Operation to trigger an Application save.
 *
 * @author Lars den Bakker<larsdenbakker@gmail.com>
 */
public class ApplicationSaveOperation extends Operation {

   private final Application app;

   public ApplicationSaveOperation(OperationContext context, Storage storage) throws InvalidInputException {
      super(context, storage);
      app = storage.getConversionModule().getParentApplication();
   }

   @Override
   protected OperationResponse _execute() {
      try {
         app.saveToDisk();
         return OperationResponse.succeeded();
      } catch (UserInputException ex) {
         return OperationResponse.failed("Unable to save application " + app.getName() + ": " + ex.getUserFriendlyErrorMessage());
      }
   }

}
