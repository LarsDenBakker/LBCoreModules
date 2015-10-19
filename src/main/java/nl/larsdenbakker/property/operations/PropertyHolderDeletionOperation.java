package nl.larsdenbakker.property.operations;

import nl.larsdenbakker.property.PropertyHolder;
import nl.larsdenbakker.registry.Registry;
import nl.larsdenbakker.storage.Storage;
import nl.larsdenbakker.operation.OperationContext;
import nl.larsdenbakker.operation.operations.TargetedOperation;
import nl.larsdenbakker.app.InvalidInputException;
import nl.larsdenbakker.util.OperationResponse;
import static nl.larsdenbakker.util.TextUtils.getDescription;

/**
 * Operation to delete a PropertyHolder from a PropertyHolderRegistry. It is a type of
 * TargetedOperation, the target is a PropertyHolder.
 *
 * See TargetedOperation for more information.
 *
 * @author Lars den Bakker <larsdenbakker at gmail.com>
 */
public class PropertyHolderDeletionOperation extends TargetedOperation<PropertyHolder> {

   public PropertyHolderDeletionOperation(OperationContext context, Storage storage) throws InvalidInputException {
      super(context, storage, PropertyHolder.class);
   }

   @Override
   protected OperationResponse _execute() {
      PropertyHolder target = getTarget();
      Registry parentRegistry = target.getParentRegistry();
      if (parentRegistry.isRegistered(target)) {
         boolean succeeded = parentRegistry.unregisterByValue(target);
         return OperationResponse.of(succeeded);
      } else {
         return OperationResponse.failed("Object " + getDescription(" was not registered in registry " + getDescription(parentRegistry)));
      }
   }

}
