package nl.larsdenbakker.registry;

import java.util.Collection;
import java.util.Iterator;
import nl.larsdenbakker.app.Module;
import nl.larsdenbakker.conversion.ConversionException;
import nl.larsdenbakker.datafile.DataFile;
import nl.larsdenbakker.util.TextUtils;

/**
 * A Registry for Registrables. Contains helper methods to simplify registration
 * of values that implement the Registrable interface.
 *
 * @param <K> The key type for this Registry.
 * @param <V> The value type for this Registry, must extend a Registrable type
 * that has K as it's key.
 *
 * @author Lars den Bakker <larsdenbakker at gmail.com>
 *
 */
public abstract class RegisterableRegistry<K, V extends Registrable<K>> extends AbstractRegistry<K, V> {

   public RegisterableRegistry(Module parentModule, RegistryModule registryHandler, Class keyType, Class valueType, DataFile dataFile) {
      super(parentModule, registryHandler, keyType, valueType, dataFile);
   }

   public RegisterableRegistry(Module parentModule, RegistryModule registryHandler, Class keyType, Class valueType) {
      super(parentModule, registryHandler, keyType, valueType);
   }

   /**
    * Register all values in this Collection. Values must by of type V. Values
    * are registered individually, if a registration fails it will continue to
    * register the rest. Values are registered in the order of a for loop.
    *
    * @param values The values
    *
    * @return Whether or not all values were registered. If one registration
    * fails, it will return false.
    */
   public boolean registerAll(Collection<V> values) {
      boolean allSucceeded = true;
      for (V val : values) {
         if (!register(val)) {
            allSucceeded = false;
         }
      }
      return allSucceeded;
   }

   /**
    * Register a value of type V to this Registry. It's key is queried.
    *
    * @param val The value you want to register.
    *
    * @return Whether or not the value was able to be registered.
    */
   public boolean register(V val) {
      return register(val.getKey(), val);
   }

   @Override
   public boolean register(K key, V val) {
      if (super.register(key, val)) {
         val.setParentRegistry(this);
         return true;
      } else {
         return false;
      }
   }

   @Override
   public K getKeyFor(V val) {
      return val.getKey();
   }

   /**
    * Unregister all values that belong to the provided Module.
    *
    * @param module The Module.
    */
   public void unregisterByModule(Module module) {
      Iterator<V> it = getAll().iterator();
      while (it.hasNext()) {
         V v = it.next();
         if (v.getParentModule().equals(module)) {
            it.remove();
         }
      }
   }

   @Override
   public K convertToKey(String stringKey) {
      try {
         K k = getConversionModule().convert(stringKey, getKeyType());
         V v = getByKey(k);
         if (v != null) {
            return k;
         } else {
            return null;
         }
      } catch (ConversionException ex) {
         V v = this.getByDescription(stringKey);
         if (v != null) {
            return v.getKey();
         } else {
            return null;
         }
      }
   }

}
