package nl.larsdenbakker.operation.operations;

import java.util.Collection;
import nl.larsdenbakker.datapath.DataPathResolveException;
import nl.larsdenbakker.registry.Registry;
import nl.larsdenbakker.storage.Storage;
import nl.larsdenbakker.operation.OperationContext;
import nl.larsdenbakker.app.InvalidInputException;

/**
 * A type of Operation with a Collection as target. Collection element type are specified.
 *
 * @author Lars den Bakker <larsdenbakker at gmail.com>
 */
public abstract class CollectionTargetOperation<C extends Collection<E>, E> extends AbstractTargetedOperation<C> {

   private final Class<E> elementType;
   private final C target;
   private final Registry registry;

   public CollectionTargetOperation(OperationContext context, Storage storage, Class<C> collectionType, Class<E> elementType) throws InvalidInputException {
      super(context, storage, collectionType);
      this.elementType = elementType;

      registry = storage.get(KEY_REGISTRY, Registry.class);

      if (registry != null) {
         String key = storage.getAndAssert(KEY_TARGET, String.class);
         try {
            Object val = getContext().getOperationHandler().getDataPathModule().resolveDataPath(registry, key);
            if (val != null) {
               if (collectionType.isAssignableFrom(val.getClass())) {
                  C coll = (C) val;
                  int oldSize = coll.size();
                  coll = storage.getConversionModule().convertToCollection(coll, collectionType, elementType, false);
                  if (coll == null) {
                     throw new InvalidInputException("Collection of type " + val.getClass().getSimpleName() + " could not be converted"
                                                     + " to a collection of type " + collectionType.getSimpleName());
                  }
                  if (coll.size() != oldSize) {
                     throw new InvalidInputException("Some elements could not be converted to type: " + elementType);
                  }
                  target = coll;
               } else {
                  C coll = storage.getConversionModule().convertToCollection(val, collectionType, elementType, false);
                  if (coll == null) {
                     throw new InvalidInputException("Could not convert value from registry " + registry.getKey() + " to a collection of type " + collectionType.getSimpleName());
                  } else {
                     target = coll;
                  }
               }
            } else {
               throw new InvalidInputException("Could not find value for key " + key + " in registry " + registry.getKey());
            }
         } catch (DataPathResolveException ex) {
            throw new InvalidInputException("Could not find value for key " + key + " in registry " + registry.getKey());
         }
      } else {
         target = storage.getAndAssertCollection(KEY_TARGET, collectionType, elementType, 1);
      }
   }

   @Override
   public C getTarget() {
      return target;
   }

}
