package nl.larsdenbakker.operation.operations;

import java.util.Map;
import nl.larsdenbakker.datapath.DataPathResolveException;
import nl.larsdenbakker.registry.Registry;
import nl.larsdenbakker.storage.Storage;
import nl.larsdenbakker.operation.OperationContext;
import static nl.larsdenbakker.operation.operations.AbstractTargetedOperation.KEY_REGISTRY;
import static nl.larsdenbakker.operation.operations.AbstractTargetedOperation.KEY_TARGET;
import nl.larsdenbakker.app.InvalidInputException;

/**
 * A type of operation that has a map as target. Map key and value types can be defined.
 *
 * @author Lars den Bakker <larsdenbakker at gmail.com>
 */
public abstract class MapTargetOperation<M extends Map<K, V>, K, V> extends AbstractTargetedOperation<M> {

   private final Class<K> keyType;
   private final Class<V> valueType;
   private final M target;
   private final Registry registry;

   public MapTargetOperation(OperationContext context, Storage storage, Class<M> mapType, Class<K> keyType, Class<V> valueType) throws InvalidInputException {
      super(context, storage, mapType);
      this.keyType = keyType;
      this.valueType = valueType;

      registry = storage.get(KEY_REGISTRY, Registry.class);

      if (registry != null) {
         String key = storage.getAndAssert(KEY_TARGET, String.class);
         try {
            Object val = getContext().getOperationHandler().getDataPathModule().resolveDataPath(registry, key);
            if (val != null) {
               if (mapType.isAssignableFrom(val.getClass())) {
                  M map = (M) val;
                  int oldSize = map.size();
                  map = storage.getConversionModule().convertToMap(map, mapType, keyType, valueType, false);
                  if (map == null) {
                     throw new InvalidInputException("Collection of type " + val.getClass().getSimpleName() + " could not be converted"
                                                     + " to a map of type " + mapType.getSimpleName());
                  }
                  if (map.size() != oldSize) {
                     throw new InvalidInputException("Some elements could not be converted to key-value pair: " + keyType.getSimpleName() + " = " + valueType.getSimpleName());
                  }
                  target = map;
               } else {
                  M map = storage.getConversionModule().convertToMap(val, mapType, keyType, valueType, false);
                  if (map == null) {
                     throw new InvalidInputException("Could not convert value from registry " + registry.getKey() + " to a map of type " + mapType.getSimpleName());
                  } else {
                     target = map;
                  }
               }
            } else {
               throw new InvalidInputException("Could not find value for key " + key + " in registry " + registry.getKey());
            }
         } catch (DataPathResolveException ex) {
            throw new InvalidInputException("Could not find value for key " + key + " in registry " + registry.getKey());
         }
      } else {
         target = storage.getAndAssertMap(KEY_TARGET, mapType, keyType, valueType, 1);
      }
   }

}
