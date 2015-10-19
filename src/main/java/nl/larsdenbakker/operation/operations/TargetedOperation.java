package nl.larsdenbakker.operation.operations;

import nl.larsdenbakker.conversion.ConversionException;
import nl.larsdenbakker.datapath.DataPathResolveException;
import nl.larsdenbakker.registry.Registry;
import nl.larsdenbakker.storage.Storage;
import nl.larsdenbakker.operation.OperationContext;
import nl.larsdenbakker.app.InvalidInputException;

/**
 *
 * @author Lars den Bakker <larsdenbakker at gmail.com>
 */
public abstract class TargetedOperation<T> extends AbstractTargetedOperation<T> {

   private final T target;
   private final Registry registry;

   public TargetedOperation(OperationContext context, Storage storage, Class<T> targetType) throws InvalidInputException {
      super(context, storage, targetType);
      registry = storage.get(KEY_REGISTRY, Registry.class);

      if (registry != null) {
         String key = storage.getAndAssert(KEY_TARGET, String.class);
         try {
            Object val = getContext().getOperationHandler().getDataPathModule().resolveDataPath(registry, key);
            if (val != null) {
               try {
                  target = storage.getConversionModule().convert(val, targetType);
               } catch (ConversionException ex) {
                  throw new InvalidInputException(ex);
               }
            } else {
               throw new InvalidInputException("Could not find value for key " + key + " in registry " + registry.getKey());
            }
         } catch (DataPathResolveException ex) {
            throw new InvalidInputException("Could not find value for key " + key + " in registry " + registry.getKey());
         }
      } else {
         target = storage.getAndAssert(KEY_TARGET, targetType);
      }
   }

   public T getTarget() {
      return target;
   }
}
