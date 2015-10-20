package nl.larsdenbakker.operation.operations;

import nl.larsdenbakker.storage.Storage;
import nl.larsdenbakker.operation.OperationContext;
import nl.larsdenbakker.app.InvalidInputException;

/**
 * Helper implementation for different types of targeted operations.
 *
 * @author Lars den Bakker <larsdenbakker at gmail.com>
 */
public abstract class AbstractTargetedOperation<T> extends Operation {

   /* The target of this operation */
   public static final String KEY_TARGET = "target".intern();
   /* The optional registry the target should be retreived from. In this case KEY_TARGET points to a key */
   public static final String KEY_REGISTRY = "registry".intern();

   private final Class<T> targetType;

   public AbstractTargetedOperation(OperationContext context, Storage storage, Class<T> targetType) throws InvalidInputException {
      super(context, storage);
      this.targetType = targetType;
   }

   /**
    * @return The target of this operation type T.
    */
   public abstract T getTarget();

   /**
    * @return The class for the type of the operation target.
    */
   public Class<T> getTargetType() {
      return targetType;
   }

}
