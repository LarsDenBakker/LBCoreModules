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
 * Comparison operation that is equal to ComparisonUtils.elementOf(operator, left, right).
 * Keys: KEY_LEFT, KEY_OPERATOR, KEY_RIGHT, KEY_INVERTED
 *
 * @author Lars den Bakker <larsdenbakker at gmail.com>
 */
public class ElementOfOperation extends Operation {

   /* The left hand side value */
   public static final String KEY_LEFT = ComparisonOperation.KEY_LEFT.intern();
   /* The operator used to perform the elementOf comparison */
   public static final String KEY_OPERATOR = ComparisonOperation.KEY_OPERATOR.intern();
   /* The right hand side value */
   public static final String KEY_RIGHT = ComparisonOperation.KEY_RIGHT.intern();
   /* Whether or not the result should be inverted */
   public static final String KEY_INVERTED = "inverted".intern();

   private final ComparisonOperator operator;
   private final List<Object> left;
   private final List<Object> right;
   private final boolean inverted;

   public ElementOfOperation(OperationContext context, Storage storage) throws InvalidInputException {
      super(context, storage);
      this.operator = storage.getAndAssert(KEY_OPERATOR, ComparisonOperator.class);
      this.left = storage.getAndAssertCollection(KEY_LEFT, ArrayList.class, Object.class, 1);
      this.right = storage.getAndAssertCollection(KEY_RIGHT, ArrayList.class, Object.class, 1);
      this.inverted = storage.get(KEY_INVERTED, Boolean.class, false);
   }

   @Override
   protected OperationResponse _execute() {
      boolean val;
      if (left.size() == 1) {
         if (right.size() == 1) {
            val = ComparisonUtils.elementOf(operator, left.get(0), right.get(0));
         } else {
            val = ComparisonUtils.elementOf(operator, left.get(0), right);
         }
      } else if (right.size() == 1) {
         val = ComparisonUtils.elementOf(operator, left, right.get(0));
      } else {
         val = ComparisonUtils.elementOf(operator, left, right);
      }
      return OperationResponse.of((inverted) ? !val : val);
   }

}
