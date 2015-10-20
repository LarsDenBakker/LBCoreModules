package nl.larsdenbakker.operation.operations;

import nl.larsdenbakker.storage.Storage;
import nl.larsdenbakker.operation.OperationContext;
import nl.larsdenbakker.app.ApplicationUser;
import nl.larsdenbakker.app.InvalidInputException;
import nl.larsdenbakker.util.OperationResponse;

/**
 * A class that handles safe execution of used-defined actions, returning
 * whether or not the operation has been successful as well as any errors
 * that occurred.
 *
 * @author Lars den Bakker <larsdenbakker at gmail.com>
 */
public abstract class Operation {

   /* The name of the operation. */
   public static final String KEY_NAME = "operation-name".intern();
   /* The custom error message to display instead of the regular errors. Optional. */
   public static final String KEY_ERROR_MESSAGE = "error-message".intern();
   /* The custom success message to display instead of regular success message. Optional */
   public static final String KEY_SUCCESS_MESSAGE = "success-message".intern();
   /* Whether or not to report any errors. Default true */
   public static final String KEY_REPORT_ERRORS = "report-errors".intern();

   private final OperationContext context;
   private final String name;
   private final String errorMessage;
   private final String successMessage;
   private final boolean reportErrors;

   public Operation(OperationContext context, Storage storage) throws InvalidInputException {
      this.context = context;
      this.name = storage.getAndAssert(KEY_NAME, String.class);
      this.errorMessage = storage.get(KEY_ERROR_MESSAGE, String.class);
      this.successMessage = storage.get(KEY_SUCCESS_MESSAGE, String.class);
      this.reportErrors = storage.get(KEY_REPORT_ERRORS, Boolean.class, true);
   }

   public OperationContext getContext() {
      return context;
   }

   public ApplicationUser getExecutor() {
      return context.getExecutor();
   }

   public String getName() {
      return name;
   }

   public boolean getReportErrors() {
      return reportErrors;
   }

   public String getCustomSuccessMessage() {
      return successMessage;
   }

   public String getCustomErrorMessage() {
      return errorMessage;
   }

   public boolean hasCustomSuccessMessage() {
      return getCustomSuccessMessage() != null;
   }

   public boolean hasCustomErrorMessage() {
      return getCustomErrorMessage() != null;
   }

   public OperationResponse execute() {
      OperationResponse response = _execute();
      if (response.hasSucceeded()) {
         if (hasCustomSuccessMessage()) {
            return OperationResponse.succeeded(getCustomSuccessMessage());
         } else {
            return response;
         }
      } else {
         if (hasCustomErrorMessage()) {
            return OperationResponse.failed(getCustomErrorMessage());
         } else if (!getReportErrors()) {
            return OperationResponse.failed();
         } else {
            return response;
         }
      }

   }

   protected abstract OperationResponse _execute();

}
