package nl.larsdenbakker.property;

/**
 * Exception to indicate that something has gone wrong when validating the
 * value of a Property for a PropertyHolder.
 *
 * @author Lars den Bakker <larsdenbakker at gmail.com>
 */
public class PropertyValidationException extends Exception {

   public PropertyValidationException() {
   }

   public PropertyValidationException(String message) {
      super(message);
   }

   public PropertyValidationException(Throwable cause) {
      super(cause);
   }

   public PropertyValidationException(String message, Throwable cause) {
      super(message, cause);
   }

}
