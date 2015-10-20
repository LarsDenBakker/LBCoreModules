package nl.larsdenbakker.operation.operations;

import nl.larsdenbakker.storage.Storage;
import nl.larsdenbakker.operation.OperationContext;
import nl.larsdenbakker.app.InvalidInputException;
import nl.larsdenbakker.util.OperationResponse;
import nl.larsdenbakker.util.TextUtils;

/**
 * Operation to read and describe the specified value.
 *
 * @author Lars den Bakker <larsdenbakker at gmail.com>
 */
public class InfoOperation extends TargetedOperation<Object> {

   public InfoOperation(OperationContext context, Storage storage) throws InvalidInputException {
      super(context, storage, Object.class);
   }

   @Override
   protected OperationResponse _execute() {
      return OperationResponse.succeeded(TextUtils.getTypeAndValueDescription(getTarget()));
   }

}
