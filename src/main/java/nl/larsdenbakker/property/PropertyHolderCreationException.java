package nl.larsdenbakker.property;

import nl.larsdenbakker.app.UserInputException;

/**
 * Exception to indicate that something went wrong when attempting to create
 * a PropertyHolder.
 *
 * @author Lars den Bakker<larsdenbakker@gmail.com>
 */
public class PropertyHolderCreationException extends UserInputException {

   public PropertyHolderCreationException() {
   }

   public PropertyHolderCreationException(String message) {
      super(message);
   }

   public PropertyHolderCreationException(Throwable cause) {
      super(cause);
   }

   public PropertyHolderCreationException(String message, Throwable cause) {
      super(message, cause);
   }

   @Override
   public PropertyHolderCreationException addFailedAction(String action) {
      super.addFailedAction(action);
      return this;
   }

}
