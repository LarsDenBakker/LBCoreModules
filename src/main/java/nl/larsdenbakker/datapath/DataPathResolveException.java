package nl.larsdenbakker.datapath;

/**
 * Thrown when something went wrong resolving a DataPath.
 *
 * @author Lars den Bakker<larsdenbakker@gmail.com>
 */
public class DataPathResolveException extends Exception {

   public DataPathResolveException() {
   }

   public DataPathResolveException(String message) {
      super(message);
   }

   public DataPathResolveException(Throwable cause) {
      super(cause);
   }

   public DataPathResolveException(String message, Throwable cause) {
      super(message, cause);
   }

}
