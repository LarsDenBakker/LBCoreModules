package nl.larsdenbakker.operation.operations;

import java.util.ArrayList;
import java.util.List;
import nl.larsdenbakker.storage.Storage;
import nl.larsdenbakker.operation.OperationContext;
import nl.larsdenbakker.util.ComparisonUtils;
import nl.larsdenbakker.app.InvalidInputException;
import nl.larsdenbakker.util.ComparisonUtils.ComparisonOperator;
import nl.larsdenbakker.util.OperationResponse;

/**
 * Operation that compares two values and returns the result. ComparisonUtils is used
 * to compare the two values. Results are identical to ComparisonUtils.compare(operator, left, right).
 *
 * @author Lars den Bakker<larsdenbakker@gmail.com>
 */
public class ComparisonOperation extends Operation {

   /* The left hand side of the operation */
   public static final String KEY_LEFT = "left".intern();
   /* The operator of the operation */
   public static final String KEY_OPERATOR = "operator".intern();
   /* The right hand side of the operation */
   public static final String KEY_RIGHT = "right".intern();

   private final ComparisonOperator operator;
   private final List<Object> left;
   private final List<Object> right;

   public ComparisonOperation(OperationContext context, Storage storage) throws InvalidInputException {
      super(context, storage);
      this.operator = storage.getAndAssert(KEY_LEFT, ComparisonOperator.class);
      this.left = storage.getAndAssertCollection(KEY_OPERATOR, ArrayList.class, Object.class, 1);
      this.right = storage.getAndAssertCollection(KEY_RIGHT, ArrayList.class, Object.class, 1);
   }

   @Override
   protected OperationResponse _execute() {
      if (left.size() == 1) {
         if (right.size() == 1) {
            return OperationResponse.of(ComparisonUtils.compare(operator, left.get(0), right.get(0)));
         } else {
            return OperationResponse.of(ComparisonUtils.compare(operator, left.get(0), right));
         }
      } else if (right.size() == 1) {
         return OperationResponse.of(ComparisonUtils.compare(operator, left, right.get(0)));
      } else {
         return OperationResponse.of(ComparisonUtils.compare(operator, left, right));
      }
   }

}
