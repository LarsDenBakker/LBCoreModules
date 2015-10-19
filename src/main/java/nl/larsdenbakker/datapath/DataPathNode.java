package nl.larsdenbakker.datapath;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import nl.larsdenbakker.conversion.ConversionModule;
import nl.larsdenbakker.conversion.reference.DataReference;
import nl.larsdenbakker.registry.Registry;
import nl.larsdenbakker.registry.RootRegistry;
import nl.larsdenbakker.serialization.DataSerializable;
import nl.larsdenbakker.util.CollectionUtils;
import nl.larsdenbakker.util.TextUtils;

/**
 * A DataReference holding data that can be references to through a
 * DataPathNode. A full chain of DataPathNodes starts with a DataPathRoot,
 * followed by any number of DataPathValues. Both the root and value are
 * a subclass of this class. All DataPathValues hold reference to and call
 * their parent DataPathNode when needed and so only the final DataPathValue
 * is required.
 *
 * As a DataReference, DataPaths never hold reference to their actual values.
 * When the top DataPathValue is asked to provide it's value a chain of
 * calls to it's parents is dispatched with each looking up their parent's value
 * until the root is reached.
 *
 * DataPathReferences can be serialized into a String representation of
 * the DataPathNode.
 *
 * @author Lars den Bakker<larsdenbakker@gmail.com>
 */
public abstract class DataPathNode<V> implements DataReference<V>, DataSerializable {

   /**
    *
    * @return The String representation of this data path. For example: ".some-registry.some-registrable.some-property"
    */
   public abstract String getFullPath();

   protected abstract Object toKey(String stringKey);

   @Override
   public Object toSerializable() {
      return getFullPath();
   }

   public abstract ConversionModule getConversionModule();

   /**
    * Create a DataPath from the provided path as a String array.
    *
    * @param dataPathModule The associated DataPathModule.
    * @param path           The path as String array.
    *
    * @return The DataPath.
    */
   public static DataPathNode<?> of(DataPathModule dataPathModule, String[] path) {
      DataPathNode reference = dataPathModule.getDataPathRoot();
      reference.getDataValue();
      for (String str : path) {
         reference = new DataPathValue(reference, str);
         reference.getDataValue();
      }
      return reference;
   }

   /**
    * Create a DataPath from the provided path as a String.
    *
    * @param dataPathModule The associated DataPathModule.
    * @param path           The path as String.
    *
    * @return The DataPath.
    */
   public static DataPathNode<?> of(DataPathModule dataPathModule, String path) {
      if (path.startsWith(".")) {
         String[] split = TextUtils.splitOnPeriods(path);
         if (split.length >= 2) {
            return of(dataPathModule, Arrays.copyOfRange(split, 1, split.length));

         } else {
            throw new IllegalArgumentException("Invalid path: " + path + " contains only root.");
         }
      } else {
         throw new IllegalArgumentException("Invalid path: " + path + ", must start at root.");
      }
   }

   /**
    * Create a DataPath from the provided path as a String List.
    *
    * @param dataPathModule The associated DataPathModule.
    * @param path           The path as String list.
    *
    * @return The DataPath.
    */
   public static DataPathNode<?> of(DataPathModule dataPathModule, List<String> path) {
      return of(dataPathModule, CollectionUtils.asArrayOfType(String.class, path));
   }

   /**
    * Create a DataPath from the provided Registry and value.
    *
    * @param <V>            The Registry's value type.
    * @param dataPathModule The associated DataPathModule.
    * @param registry       The Registry.
    * @param val            The value, must be of type V.
    *
    * @return The DataPath.
    */
   public static <V> DataPathNode<V> of(DataPathModule dataPathModule, Registry<?, V> registry, V val) {
      if (registry.isRegistered(val)) {
         if (registry.getRootRegistry() != null) {
            ArrayList<String> history = new ArrayList();
            history.add(registry.getKeyFor(val).toString());
            Registry currentRegistry = registry;
            while (currentRegistry != null) {
               if (!(currentRegistry instanceof RootRegistry)) {
                  history.add(currentRegistry.getKey().toString());
               }
               currentRegistry = currentRegistry.getParentRegistry();
            }
            return (DataPathNode<V>) of(dataPathModule, CollectionUtils.asArrayOfType(String.class, Lists.reverse(history))); //Safe cast ensured.
         } else {
            throw new IllegalArgumentException(registry.getTypeAndValueDescription() + " does not lead to a RootRegistry.");
         }
      } else {
         throw new IllegalArgumentException("Value " + TextUtils.getTypeAndValueDescription(val) + " is not registered to " + registry.getTypeAndValueDescription());
      }
   }

}
