package nl.larsdenbakker.operation.operations.constraints;

import nl.larsdenbakker.storage.Storage;
import nl.larsdenbakker.operation.OperationContext;
import nl.larsdenbakker.operation.operations.TargetedOperation;
import nl.larsdenbakker.app.InvalidInputException;
import nl.larsdenbakker.util.OperationResponse;

/**
 * A type of operation that checks if the Long target is within the given size range.
 *
 * @author Lars den Bakker <larsdenbakker at gmail.com>
 */
public class LongSizeConstraint extends TargetedOperation<Long> {

   /* The minimum size. Optional if a maximum is set. */
   public static final String KEY_MIN_SIZE = "min-size".intern();
   /* The maximum size. Optional if a minimum is set. */
   public static final String KEY_MAX_SIZE = "max-size".intern();

   private final long minSize;
   private final long maxSize;

   public LongSizeConstraint(OperationContext context, Storage storage) throws InvalidInputException {
      super(context, storage, Long.class);
      this.minSize = storage.get(KEY_MIN_SIZE, Long.class, -1l);
      this.maxSize = storage.get(KEY_MAX_SIZE, Long.class, -1l);
      if (minSize == -1 && maxSize == -1) {
         throw new InvalidInputException("Neither min-size nor max-size is set.");
      } else if (maxSize != -1 && maxSize < minSize) {
         throw new InvalidInputException("Max length is smaller than min length. (min: " + minSize + " max: " + maxSize + ")");
      }
   }

   @Override
   protected OperationResponse _execute() {
      Long target = getTarget();
      if (minSize != -1 && target < minSize) {
         return OperationResponse.failed("Input cannot be lower than " + minSize + ".");
      }
      if (maxSize != -1 && target > maxSize) {
         return OperationResponse.failed("Input cannot be higher than " + maxSize + ".");
      }
      return OperationResponse.succeeded();
   }
}
