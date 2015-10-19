package nl.larsdenbakker.operation.operations;

import java.util.ArrayList;
import java.util.List;
import nl.larsdenbakker.storage.Storage;
import nl.larsdenbakker.operation.OperationContext;
import nl.larsdenbakker.app.InvalidInputException;
import nl.larsdenbakker.util.OperationResponse;
import nl.larsdenbakker.util.TextUtils;

/**
 *
 * @author Lars den Bakker<larsdenbakker@gmail.com>
 */
public class FilterConstraint<T> extends TargetedOperation<T> {

   public static final String KEY_ALLOWED_VALUES = "allowed-values".intern();
   public static final String KEY_BLOCKED_VALUES = "blocked-values".intern();

   private final List<T> allowedValues;
   private final List<T> blockedValues;

   public FilterConstraint(OperationContext context, Storage storage, Class<T> inputType) throws InvalidInputException {
      super(context, storage, inputType);
      this.allowedValues = storage.getCollection(KEY_ALLOWED_VALUES, ArrayList.class, inputType);
      this.blockedValues = storage.getCollection(KEY_BLOCKED_VALUES, ArrayList.class, inputType);
      if (allowedValues.isEmpty() && blockedValues.isEmpty()) {
         throw new InvalidInputException("allowed-values and blocked-values are both empty");
      }
   }

   @Override
   protected OperationResponse _execute() {
      T target = getTarget();
      if (allowedValues == null || allowedValues.contains(target)) {
         if (blockedValues == null || !blockedValues.contains(target)) {
            return OperationResponse.succeeded();
         }
      }
      OperationResponse result = OperationResponse.failed(TextUtils.getDescription(target) + " is not allowed.");
      if (allowedValues != null) {
         result.addMessages("Allowed values: " + TextUtils.getDescription(allowedValues));
      }
      return result;
   }

}
