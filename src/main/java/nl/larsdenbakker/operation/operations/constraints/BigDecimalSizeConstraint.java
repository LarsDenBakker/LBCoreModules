package nl.larsdenbakker.operation.operations.constraints;

import java.math.BigDecimal;
import nl.larsdenbakker.storage.Storage;
import nl.larsdenbakker.operation.OperationContext;
import nl.larsdenbakker.operation.operations.TargetedOperation;
import nl.larsdenbakker.app.InvalidInputException;
import nl.larsdenbakker.util.OperationResponse;

/**
 *
 * @author Lars den Bakker <larsdenbakker at gmail.com>
 */
public class BigDecimalSizeConstraint extends TargetedOperation<BigDecimal> {

   public static final String KEY_MIN_SIZE = "min-size".intern();
   public static final String KEY_MAX_SIZE = "max-size".intern();

   private final BigDecimal minSize;
   private final BigDecimal maxSize;

   public BigDecimalSizeConstraint(OperationContext context, Storage storage) throws InvalidInputException {
      super(context, storage, BigDecimal.class);
      this.minSize = storage.get(KEY_MIN_SIZE, BigDecimal.class, BigDecimal.valueOf(-1.0));
      this.maxSize = storage.get(KEY_MAX_SIZE, BigDecimal.class, BigDecimal.valueOf(-1.0));
      if (minSize.equals(BigDecimal.valueOf(-1)) && maxSize.equals(BigDecimal.valueOf(-1.0))) {
         throw new InvalidInputException("Neither min-size nor max-size is set.");
      } else if (!maxSize.equals(BigDecimal.valueOf(-1.0)) && maxSize.compareTo(minSize) == -1) {
         throw new InvalidInputException("Max length is smaller than min length. (min: " + minSize + " max: " + maxSize + ")");
      }
   }

   @Override
   protected OperationResponse _execute() {
      BigDecimal target = getTarget();
      if (!minSize.equals(BigDecimal.valueOf(-1)) && target.compareTo(minSize) == -1) {
         return OperationResponse.failed("Input cannot be lower than " + minSize + ".");
      }
      if (!maxSize.equals(BigDecimal.valueOf(-1)) && target.compareTo(maxSize) == 1) {
         return OperationResponse.failed("Input cannot be higher than " + maxSize + ".");
      }
      return OperationResponse.succeeded();
   }

}
