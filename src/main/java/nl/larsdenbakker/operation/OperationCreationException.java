package nl.larsdenbakker.operation;

/**
 * Exception to indicate that an Operation could not be created.
 *
 * @author Lars den Bakker <larsdenbakker at gmail.com>
 */
public class OperationCreationException extends Exception {

   public OperationCreationException(String message) {
      super(message);
   }

   public OperationCreationException(Throwable cause) {
      super(cause);
   }

   public OperationCreationException(String message, Throwable cause) {
      super(message, cause);
   }

}
