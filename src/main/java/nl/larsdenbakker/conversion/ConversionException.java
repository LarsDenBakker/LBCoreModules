package nl.larsdenbakker.conversion;

/**
 * An exception to signal that something has gone wrong when converting an
 * object from one data type to another.
 *
 * @author Lars den Bakker <larsdenbakker at gmail.com>
 */
public class ConversionException extends Exception {

   public ConversionException(String message) {
      super(message);
   }

   public ConversionException(Throwable cause) {
      super(cause);
   }

   public ConversionException(String message, Throwable cause) {
      super(message, cause);
   }

}
