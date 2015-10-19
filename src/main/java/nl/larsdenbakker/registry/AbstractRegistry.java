package nl.larsdenbakker.registry;

import static com.google.common.base.Preconditions.checkNotNull;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import nl.larsdenbakker.app.Module;
import nl.larsdenbakker.conversion.ConversionException;
import nl.larsdenbakker.datafile.DataFile;
import nl.larsdenbakker.datafile.DataFileException;
import nl.larsdenbakker.datapath.AbstractDataHolder;

/**
 * Default implementation of the Registry interface. This type of Registry is
 * backed by a HashMap.
 *
 * @param <K> The key type.
 * @param <V> The value type.
 *
 * @author Lars den Bakker<larsdenbakker@gmail.com>
 */
public abstract class AbstractRegistry<K, V> extends AbstractDataHolder implements Registry<K, V> {

   private final Module parentModule;
   private final DataFile dataFile;
   private final RegistryModule registryModule;
   private final Map<K, V> map = new HashMap();
   private final Class<K> keyType;
   private final Class<V> valueType;

   private Registry parentRegistry;

   public AbstractRegistry(Module parentModule, RegistryModule registryHandler, Class<K> keyType, Class<V> valueType, DataFile dataFile) {
      this.parentModule = parentModule;
      this.registryModule = registryHandler;
      this.keyType = keyType;
      this.valueType = valueType;
      this.dataFile = dataFile;
   }

   public AbstractRegistry(Module parentModule, RegistryModule registryHandler, Class<K> keyType, Class<V> valueType) {
      this(parentModule, registryHandler, keyType, valueType, null);
   }

   @Override
   protected V _getDataValue(Object input) {
      try {
         K key = getConversionModule().convert(input, getKeyType());
         V val = getByKey(key);
         if (val != null) {
            return val;
         }
      } catch (ConversionException ex) {
      }
      return getByDescription(input.toString());
   }

   @Override
   public boolean register(K key, V val) {
      checkNotNull(val);

      V temp = getByKey(key);
      if (temp == null) {
         map.put(key, val);
         return true;
      }
      return false;
   }

   @Override
   public boolean unregisterByValue(V val) {
      for (Entry<K, V> entry : map.entrySet()) {
         if (entry.getValue().equals(val)) {
            unregister(entry.getKey());
            return true;
         }
      }
      return false;
   }

   @Override
   public V unregister(K key) {
      return map.remove(key);
   }

   @Override
   public V getByKey(K key) {
      return map.get(key);
   }

   @Override
   public int getSize() {
      return map.size();
   }

   @Override
   public void clear() {
      map.clear();
   }

   public DataFile getDataFile() {
      return dataFile;
   }

   @Override
   public Class<K> getKeyType() {
      return keyType;
   }

   @Override
   public Class<V> getValueType() {
      return valueType;
   }

   @Override
   public RegistryModule getRegistryModule() {
      return registryModule;
   }

   @Override
   public Collection<V> getAll() {
      return map.values();
   }

   @Override
   public Object toSerializable() {
      return map;
   }

   @Override
   public Map<String, Object> getContents() {
      return getConversionModule().convertToMap(map, HashMap.class, String.class, valueType, true);
   }

   @Override
   public void setParentRegistry(Registry parentRegistry) {
      this.parentRegistry = parentRegistry;
   }

   @Override
   public Registry getParentRegistry() {
      return parentRegistry;
   }

   public void saveToDisk() throws DataFileException {
      if (dataFile != null) {
         dataFile.save(this);
      } else {
         throw new IllegalArgumentException("Trying to save registry with no DataFile set. Class: " + getClass().getSimpleName());
      }
   }

   @Override
   public boolean equals(Object obj) {
      if (obj != null && obj.getClass().isAssignableFrom(getClass())) {
         return getKey().equals(((Registrable) obj).getKey());
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return getKey().hashCode();
   }

   @Override
   public Module getParentModule() {
      return parentModule;
   }

}
