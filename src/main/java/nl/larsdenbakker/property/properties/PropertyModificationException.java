package nl.larsdenbakker.property.properties;

/**
 * Exception to indicate something went wrong when trying to modify a Property.
 *
 * @author Lars den Bakker <larsdenbakker at gmail.com>
 */
public class PropertyModificationException extends Exception {

   public PropertyModificationException() {
   }

   public PropertyModificationException(String message) {
      super(message);
   }

   public PropertyModificationException(Throwable cause) {
      super(cause);
   }

   public PropertyModificationException(String message, Throwable cause) {
      super(message, cause);
   }

}
