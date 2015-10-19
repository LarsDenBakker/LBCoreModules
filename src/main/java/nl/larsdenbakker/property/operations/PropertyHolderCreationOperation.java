package nl.larsdenbakker.property.operations;

import java.util.ArrayList;
import nl.larsdenbakker.property.PropertyHolder;
import nl.larsdenbakker.property.PropertyHolderCreationException;
import nl.larsdenbakker.property.PropertyHolderRegistry;
import nl.larsdenbakker.storage.Storage;
import nl.larsdenbakker.operation.OperationContext;
import nl.larsdenbakker.operation.operations.TargetedOperation;
import nl.larsdenbakker.app.InvalidInputException;
import nl.larsdenbakker.util.OperationResponse;

/**
 * Operation to create a PropertyHolder in a PropertyHolderRegistry. It is a
 * type of TargetedOperation, the target is a PropertyHolderRegistry. Arguments
 * can also be specified and can be passed as constructor parameters.
 *
 * See KEY_CONSTRUCTOR_ARGUMENTS for the constructor argument key.
 *
 * See TargetedOperation for more information.
 *
 * @author Lars den Bakker <larsdenbakker at gmail.com>
 */
public class PropertyHolderCreationOperation extends TargetedOperation<PropertyHolderRegistry> {

   public static final String KEY_CONSTRUCTOR_ARGUMENTS = "constructor-arguments".intern();

   private final Object[] arguments;

   public PropertyHolderCreationOperation(OperationContext context, Storage storage) throws InvalidInputException {
      super(context, storage, PropertyHolderRegistry.class);
      ArrayList<Object> list = storage.getCollection(KEY_CONSTRUCTOR_ARGUMENTS, ArrayList.class, Object.class, true);
      arguments = list.toArray(new Object[list.size()]);
   }

   @Override
   protected OperationResponse _execute() {
      PropertyHolderRegistry target = getTarget();
      try {
         PropertyHolder propertyHolder = target.createAndRegister(arguments);
         return OperationResponse.succeeded("You have created " + propertyHolder.getTypeAndValueDescription());
      } catch (PropertyHolderCreationException ex) {
         return OperationResponse.failed(ex.getMessage());
      }
   }

}
