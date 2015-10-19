package nl.larsdenbakker.operation.operations;

/**
 *
 * @author Lars den Bakker<larsdenbakker@gmail.com>
 */
public class OperationConstructionException extends Exception {

   public OperationConstructionException(String message) {
      super(message);
   }

   public OperationConstructionException(Throwable cause) {
      super(cause);
   }

   public OperationConstructionException(String message, Throwable cause) {
      super(message, cause);
   }

   public OperationConstructionException() {
   }

}
