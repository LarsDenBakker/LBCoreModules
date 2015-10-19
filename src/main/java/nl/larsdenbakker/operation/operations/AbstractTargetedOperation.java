package nl.larsdenbakker.operation.operations;

import nl.larsdenbakker.storage.Storage;
import nl.larsdenbakker.operation.OperationContext;
import nl.larsdenbakker.app.InvalidInputException;

/**
 *
 * @author Lars den Bakker <larsdenbakker at gmail.com>
 */
public abstract class AbstractTargetedOperation<T> extends Operation {

   public static final String KEY_TARGET = "target".intern();
   public static final String KEY_VALIDATE_ALL_TARGETS = "validate-all-targets".intern();
   public static final String KEY_REGISTRY = "registry".intern();

   private final Class<T> targetType;

   public AbstractTargetedOperation(OperationContext context, Storage storage, Class<T> targetType) throws InvalidInputException {
      super(context, storage);
      this.targetType = targetType;
   }

   public abstract T getTarget();

   public Class<T> getTargetType() {
      return targetType;
   }

}
