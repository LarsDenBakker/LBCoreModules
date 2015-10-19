package nl.larsdenbakker.datafile;

import nl.larsdenbakker.app.UserInputException;

/**
 * Exception to indicate something has gone wrong loading or saving
 * a DataFile.
 *
 * @author Lars den Bakker<larsdenbakker@gmail.com>
 */
public class DataFileException extends UserInputException {

   public DataFileException() {
   }

   public DataFileException(String message) {
      super(message);
   }

   public DataFileException(Throwable cause) {
      super(cause);
   }

   public DataFileException(String message, Throwable cause) {
      super(message, cause);
   }

   @Override
   public DataFileException addFailedAction(String action) {
      super.addFailedAction(action);
      return this;
   }

}
