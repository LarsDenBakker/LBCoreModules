package nl.larsdenbakker.registry;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import nl.larsdenbakker.conversion.ConversionException;
import nl.larsdenbakker.conversion.ConversionModule;
import nl.larsdenbakker.datapath.DataHolder;
import nl.larsdenbakker.serialization.DataSerializable;
import nl.larsdenbakker.util.TextUtils;

/**
 * A type of DataHolder with key-value mappings. Provides methods to ensure data
 * integrity, serialization and de-serialization support and user interaction
 * with registered data.
 *
 * A Registry is itself a Registrable, it's key is a String.
 *
 * @author Lars den Bakker<larsdenbakker@gmail.com>
 * @param <K> The Registry key type.
 * @param <V> The Registry value type.
 */
public interface Registry<K, V> extends DataHolder, DataSerializable, Registrable<String> {

   /**
    *
    * @param key The key.
    *
    * @return The value V registered to the given key, or null if none.
    */
   public V getByKey(K key);

   @Override
   public default ConversionModule getConversionModule() {
      return getRegistryModule().getConversionModule();
   }

   public RegistryModule getRegistryModule();

   /**
    *
    * @return All values registered to this Registry. This Collection is backed
    * by the Registry itself, any changes will reflect within the Registry.
    */
   public Collection<V> getAll();

   public Class<K> getKeyType();

   public Class<V> getValueType();

   /**
    * @return The plural description of registered data values. This is used in
    * describing this Registry.
    */
   public String getPluralDataValueDescription();

   /**
    * Get a value V from this registry that matches the given description.
    * Description matching is done by comparing the given description with the
    * registered values' descriptions from TextUtils.getDescription(Object).
    * Spaces are casing is ignored. It is possible that more than one object
    * matches the given description, the first object that matches is returned.
    *
    * @param description The description of the object.
    *
    * @return The value of type V if found, otherwise null.
    */
   public default V getByDescription(String description) {
      description = description.replaceAll(" ", "_");
      for (V val : this.getAll()) {
         if (TextUtils.getDescription(val).replaceAll(" ", "_").equalsIgnoreCase(description)) {
            return val;
         }
      }
      return null;
   }

   /**
    *
    * @param val The value to check.
    *
    * @return Whether or not this value is registered to this Registry.
    */
   public default boolean isRegistered(V val) {
      for (V v : getAll()) {
         if (v.equals(val)) {
            return true;
         }
      }
      return false;
   }

   /**
    * Register given key-value mappings to this Registry. Individual key-value
    * pairs are only registered if they can be converted to the correct type for
    * this Registry.
    *
    * @param map The mappings.
    *
    * @return Whether or not it was possible to register all mappings as well as
    * any warnings or errors generated during registration.
    */
   public default boolean registerAll(Map<K, V> map) {
      boolean allSucceeded = true;
      for (Entry<K, V> entry : map.entrySet()) {
         boolean succeeded = register(entry.getKey(), entry.getValue());
         if (!succeeded && succeeded) {
            allSucceeded = false;
         }
      }
      return allSucceeded;
   }

   public default void unregisterAll(Collection<V> values) {
      for (V val : values) {
         unregisterByValue(val);
      }
   }

   public K getKeyFor(V val);

   public boolean register(K key, V val);

   public boolean unregisterByValue(V val);

   public V unregister(K key);

   public int getSize();

   /**
    * Clear this Registry.
    */
   public void clear();

   @Override
   public default Object toSerializable() {
      return DataHolder.super.toSerializable();
   }

   @Override
   public default String getDescription() {
      return getPluralDataValueDescription();
   }

   @Override
   public default String getTypeDescription() {
      return "Registry";
   }

   @Override
   public default K convertToKey(String stringKey) {
      try {
         K k = getConversionModule().convert(stringKey, getKeyType());
         if (k != null) {
            V v = getByKey(k);
            if (v != null) {
               return k;
            }
         }
      } catch (ConversionException ex) {
         V v = this.getByDescription(stringKey);
         if (v != null) {
            K k = getKeyFor(v);
            if (k != null) {
               return k;
            }
         }
      }
      return null;
   }

}
