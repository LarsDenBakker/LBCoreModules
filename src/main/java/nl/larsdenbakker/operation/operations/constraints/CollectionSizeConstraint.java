package nl.larsdenbakker.operation.operations.constraints;

import java.util.Collection;
import nl.larsdenbakker.storage.Storage;
import nl.larsdenbakker.operation.OperationContext;
import nl.larsdenbakker.operation.operations.CollectionTargetOperation;
import nl.larsdenbakker.app.InvalidInputException;
import nl.larsdenbakker.util.OperationResponse;

/**
 * A type of operation that checks if the size of the target collection is within the provided range.
 *
 * @author Lars den Bakker <larsdenbakker at gmail.com>
 */
public class CollectionSizeConstraint extends CollectionTargetOperation<Collection<Object>, Object> {

   /* The minimum size. Optional if a maximum is set. */
   public static final String KEY_MIN_SIZE = "min-size".intern();
   /* The maximum size. Optional if a minimum is set. */
   public static final String KEY_MAX_SIZE = "max-size".intern();

   private final int minSize;
   private final int maxSize;

   public CollectionSizeConstraint(OperationContext context, Storage storage) throws InvalidInputException {
      super(context, storage, (Class) Collection.class, Object.class);
      this.minSize = storage.get(KEY_MIN_SIZE, Integer.class, -1);
      this.maxSize = storage.get(KEY_MAX_SIZE, Integer.class, -1);
      if (minSize == -1 && maxSize == -1) {
         throw new InvalidInputException("Neither min-size nor max-size is set.");
      } else if (maxSize != -1 && maxSize < minSize) {
         throw new InvalidInputException("Max length is smaller than min length. (min: " + minSize + " max: " + maxSize + ")");
      }
   }

   @Override
   protected OperationResponse _execute() {
      Collection<?> target = getTarget();
      if (minSize != -1) {
         if (target.size() < minSize) {
            return OperationResponse.failed("Input must have at least " + minSize + " elements.");
         }
      }
      if (maxSize != -1) {
         if (target.size() > maxSize) {
            return OperationResponse.failed("Input cannot have more than " + maxSize + " elements.");
         }
      }
      return OperationResponse.succeeded();
   }

}
