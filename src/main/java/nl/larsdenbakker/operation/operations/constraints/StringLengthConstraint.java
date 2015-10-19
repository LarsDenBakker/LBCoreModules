package nl.larsdenbakker.operation.operations.constraints;

import nl.larsdenbakker.storage.Storage;
import nl.larsdenbakker.operation.OperationContext;
import nl.larsdenbakker.operation.operations.TargetedOperation;
import nl.larsdenbakker.app.InvalidInputException;
import nl.larsdenbakker.util.OperationResponse;

/**
 *
 * @author Lars den Bakker <larsdenbakker at gmail.com>
 */
public class StringLengthConstraint extends TargetedOperation<String> {

   public static final String KEY_MIN_LENGTH = "min-length".intern();
   public static final String KEY_MAX_LENGTH = "max-length".intern();

   private final int maxLength;
   private final int minLength;

   public StringLengthConstraint(OperationContext context, Storage storage) throws InvalidInputException {
      super(context, storage, String.class);
      this.minLength = storage.get(KEY_MIN_LENGTH, Integer.class, -1);
      this.maxLength = storage.get(KEY_MAX_LENGTH, Integer.class, -1);
      if (minLength == -1 && maxLength == -1) {
         throw new InvalidInputException("Neither min-length nor max-length is set.");
      } else if (maxLength != -1 && maxLength < minLength) {
         throw new InvalidInputException("Max length is smaller than min length. (min: " + minLength + " max: " + maxLength + ")");
      }
   }

   @Override
   protected OperationResponse _execute() {
      String target = getTarget();
      if (minLength != -1) {
         if (target.length() < minLength) {
            return OperationResponse.failed("Input must be at least " + minLength + " characters long.");
         }
      }
      if (maxLength != -1) {
         if (target.length() > maxLength) {
            return OperationResponse.failed("Input cannot be longer than " + maxLength + " characters.");
         }
      }
      return OperationResponse.succeeded();
   }

}
