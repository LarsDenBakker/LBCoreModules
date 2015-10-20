package nl.larsdenbakker.operation.operations.constraints;

import nl.larsdenbakker.storage.Storage;
import nl.larsdenbakker.operation.OperationContext;
import nl.larsdenbakker.operation.operations.TargetedOperation;
import nl.larsdenbakker.app.InvalidInputException;
import nl.larsdenbakker.util.OperationResponse;
import nl.larsdenbakker.util.TextUtils;

/**
 * Operation that checks if the target String contains only ASCII characters.
 *
 * @author Lars den Bakker <larsdenbakker at gmail.com>
 */
public class StringOnlyASCIIConstraint extends TargetedOperation<String> {

   public StringOnlyASCIIConstraint(OperationContext context, Storage storage) throws InvalidInputException {
      super(context, storage, String.class);
   }

   @Override
   protected OperationResponse _execute() {
      String target = getTarget();
      if (TextUtils.isAllASCII(target)) {
         return OperationResponse.failed();
      } else {
         return OperationResponse.failed("Input cannot contain non-ASCII characters.");
      }
   }

}
