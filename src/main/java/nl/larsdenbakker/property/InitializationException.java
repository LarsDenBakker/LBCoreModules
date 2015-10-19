package nl.larsdenbakker.property;

/**
 * Thrown when something went wrong when initialization an Object.
 *
 * @author Lars den Bakker <larsdenbakker at gmail.com>
 */
public class InitializationException extends Exception {

   public InitializationException() {
   }

   public InitializationException(String message) {
      super(message);
   }

   public InitializationException(Throwable cause) {
      super(cause);
   }

   public InitializationException(String message, Throwable cause) {
      super(message, cause);
   }

}
