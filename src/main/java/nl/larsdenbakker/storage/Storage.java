package nl.larsdenbakker.storage;

import static com.google.common.base.Preconditions.checkNotNull;
import com.google.common.collect.Lists;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javafx.util.Pair;
import nl.larsdenbakker.datapath.AbstractDataHolder;
import nl.larsdenbakker.conversion.ConversionException;
import nl.larsdenbakker.conversion.reference.DataReferencable;
import nl.larsdenbakker.app.InvalidInputException;
import nl.larsdenbakker.util.TextUtils;

/**
 * A type of DataHolder that handles key-value mappings for String
 * keys. Provides many utility methods for type conversion, safety
 * and assertion.
 *
 * @author Lars den Bakker <larsdenbakker at gmail.com>
 */
public abstract class Storage extends AbstractDataHolder {

   @Override
   protected Object _getDataValue(Object key) {
      checkNotNull(key);
      return get(key.toString());
   }

   /**
    *
    * @param key The key.
    *
    * @return The raw data associated with the given key.
    */
   public abstract Object get(String key);

   /**
    *
    * @param <T>   The desired value type.
    * @param key   The key.
    * @param clazz The value type.
    * @param or    A fallback value of type T.
    *
    * @return The data associated with the given key. The data is converted using the
    * Storage's ConversionModule to the given type. If there was no data associated
    * with the given key, or if conversion was unsuccessful the given fallback value
    * is returned instead.
    */
   public <T> T get(String key, Class<T> clazz, T or) {
      T t = get(key, clazz);
      return (t != null) ? t : or;
   }

   /**
    *
    * @param <T> The desired value type.
    * @param key The key.
    * @param or  A fallback value of type T.
    *
    * @return The data associated with the given key. The data is converted using the
    * Storage's ConversionModule to the given type. If there was no data associated
    * with the given key, or if conversion was unsuccessful the given fallback value
    * is returned instead.
    */
   public <T> T get(String key, T or) {
      T t = get(key, (Class<T>) or.getClass()); //Safe cast garaunteed
      return (t != null) ? t : or;
   }

   /**
    *
    * @param <T>   The desired value type.
    * @param key   The key.
    * @param clazz The desired value type.
    *
    * @return The data associated with the given key. The data is converted using the
    * Storage's ConversionModule to the given type. If there was no data associated
    * with the given key, or if conversion was unsuccessful null will be returned.
    */
   public <T> T get(String key, Class<T> clazz) {
      if (Collection.class.isAssignableFrom(clazz)) {
         return (T) getCollection(key, (Class<? extends Collection>) clazz, Object.class, false);
      } else if (Map.class.isAssignableFrom(clazz)) {
         return (T) getMap(key, (Class<? extends Map>) clazz, Object.class, Object.class, false);
      } else if (Storage.class.isAssignableFrom(clazz)) {
         return (T) getStorage(key, false);
      } else {
         Object obj = get(key);
         if (obj != null) {
            try {
               T t = getRoot().getConversionModule().convert(obj, clazz);
               set(key, t);
               return t;
            } catch (ConversionException ex) {
               return null;
            }
         }
      }
      return null;
   }

   /**
    * Set the given data value to the given key.
    *
    * @param key   The key.
    * @param value The value.
    *
    * @return This storage to allow for shorthand notation.
    */
   public Storage set(String key, Object value) {
      checkNotNull(key);
      String[] path = TextUtils.splitOnPeriods(key);
      Storage node = this;
      for (int i = 0; i < path.length; i++) {
         if (i != path.length - 1) {
            node = node.getStorage(path[i]);
         } else {
            if (value instanceof DataReferencable) {
               value = ((DataReferencable) value).getDataReference();
            }
            node._set(path[i], value);
         }
      }
      return this;
   }

   /**
    * Subclass implementation of the set(String, Object) method. The regular
    * method handles splitting the key into sub-keys if dot notation ("key.key.key.key")
    * is used and delegates the _set(String, Object) method to the correct
    * sub-storage to handle the actual setting.
    *
    * @param key   The key.
    * @param value The value.
    *
    */
   protected abstract void _set(String key, Object value);

   /**
    * Set the given data value to the given key as a WeakReference.
    *
    * @param key   The key.
    * @param value The value.
    *
    * @return This storage to allow for shorthand notation.
    */
   public Storage setWeak(String key, Object value) {
      checkNotNull(value);
      set(key, new WeakReference(value));
      return this;
   }

   /**
    * Clear any data associated with the given key.
    *
    * @param key The key.
    *
    * @return This storage to allow for shorthand notation.
    */
   public abstract Storage unset(String key);

   /**
    *
    * @param key The key.
    *
    * @return Whether or not any data is associated with the given key.
    * Returns the same as (get(key) != null).
    */
   public boolean isSet(String key) {
      checkNotNull(key);
      return get(key) != null;
   }

   /**
    *
    * @param key   The key.
    * @param clazz The value type.
    *
    * @return Whether or not any data is associated with the given key,
    * and if it is of the given type. Returns the same as (get(key, clazz) != null).
    */
   public boolean isSet(String key, Class<?> clazz) {
      checkNotNull(key);
      return get(key, clazz) != null;
   }

   public void assertSet(String key) throws InvalidInputException {
      checkNotNull(key);
      Object obj = get(key);
      if (obj == null) {
         throw new InvalidInputException("Missing value at: '" + getStoragePath() + "." + key + "'");
      }
   }

   /**
    * @return The key used to refer to this Storage in the parent Storage.
    */
   public abstract String getStorageKey();

   /**
    * @return The root Storage of this Storage.
    */
   public abstract Storage getRoot();

   /**
    * @return The parent Storage of this Storage.
    */
   public abstract Storage getParent();

   /**
    * @return All keys that have data associated in this Storage.
    */
   public abstract Set<String> getKeys();

   public List<Storage> getNodes() {
      List<Storage> nodes = new ArrayList();
      for (String key : getKeys()) {
         Storage storage = getStorage(key, false);
         if (storage != null) {
            nodes.add(storage);
         }
      }
      return nodes;
   }

   /**
    * @return All values that are referred to by keys in this Storage.
    */
   public abstract Collection<Object> getValues();

   /**
    * Set all key-value mappings. All keys are converted to String.
    *
    * @param map The mappings.
    */
   public void setAll(Map<?, ?> map) {
      for (Entry<?, ?> entry : map.entrySet()) {
         try {
            String key = getConversionModule().convert(entry.getKey(), String.class);
            set(key, entry.getValue());
         } catch (ConversionException ex) {
         }

      }
   }

   /**
    * Get the Storage at the given key.
    *
    * @param key            The key.
    * @param createIfAbsent Whether or not a new Storage should
    *                       be created if there was nothing there
    *                       before.
    *
    * @return The Storage, can be null.
    */
   public abstract Storage getStorage(String key, boolean createIfAbsent);

   /**
    * Identical to getStorage(key, true).
    */
   public Storage getStorage(String key) {
      return getStorage(key, true);
   }

   public Storage getAndAssertStorage(String key) throws InvalidInputException {
      return getAndAssert(key, Storage.class);
   }

   /**
    *
    * @param key The key.
    *
    * @return Whether the data at the given key is a Storage, or can be
    * converted to a Storage.
    */
   public boolean isStorage(String key) {
      Object obj = get(key);
      return (obj instanceof Storage || obj instanceof Map);
   }

   /**
    * Get the object at the given key, and assert that it is of or can be
    * converted to the correct type. If it cannot, an exception will be thrown.
    *
    * @param <T>   The value type.
    * @param key   The key.
    * @param clazz The value type class.
    *
    * @return The object at the given key, never null. An exception is thrown if
    * the object could not be converted or if there was no data found.
    * @throws InvalidInputException Thrown when there was no data found, or if the
    * data could not be converted to the given type.
    */
   public <T> T getAndAssert(String key, Class<T> clazz) throws InvalidInputException {
      T t = get(key, clazz);
      if (t != null) {
         return t;
      } else {
         assertSet(key);
         Object obj = get(key);
         throw new InvalidInputException(TextUtils.getTypeAndValueDescription(t) + " at: '" + getStoragePath()
                                         + "' could not be converted to type: " + TextUtils.getDescription(clazz));
      }
   }

   /**
    * Get a Collection at the given key with the given collection type and elements of the
    * given type.
    *
    * @param <C>            The Collection type.
    * @param <E>            The Collection element type.
    * @param key            The key.
    * @param collectionType The Collection type class.
    * @param elementType    The Collection element type class.
    * @param emptyIfNull    Whether or not an empty Collection of type C should be returned if
    *                       no Collection was found.
    *
    * @return The Collection. Can be null if emptyIfNull is false.
    */
   public <C extends Collection<E>, E> C getCollection(String key, Class<C> collectionType, Class<E> elementType, boolean emptyIfNull) {
      Object obj = get(key);

      //If the collection type and the cached element type are known and correct, return the obj.
      if (obj != null && collectionType.isAssignableFrom(obj.getClass())) {
         Class<?> cachedElementType = getConversionModule().getCachedElementType((Collection) obj);
         if (cachedElementType != null && cachedElementType.isAssignableFrom(elementType)) {
            return (C) obj;
         }
      }
      //Otherwise convert, override and return
      C c = getConversionModule().convertToCollection(obj, collectionType, elementType, emptyIfNull);
      set(key, c);
      return c;
   }

   /**
    * Identical to getCollection(String, Class<C>, Class<E>, false).
    */
   public <C extends Collection<E>, E> C getCollection(String key, Class<C> collectionType, Class<E> elementType) {
      return getCollection(key, collectionType, elementType, false);
   }

   /**
    * Get a collection at the given key with the given Collection type and elements of the
    * given type. If there was no data found at the given key, or if the data could not be
    * converted to the given Collection type with elements of the given type an exception
    * is thrown. A minimum size can also be defined.
    *
    * @param <C>            The Collection type.
    * @param <E>            The Collection element type.
    * @param key            The key.
    * @param collectionType The Collection type class.
    * @param elementType    The Collection element type class.
    * @param size           The minimum size of the Collection. If the Collection is below
    *                       this size, an exception is thrown.
    *
    * @return The Collection. Is never null.
    * @throws InvalidInputException Thrown if no data was found, if the data found could
    * not be converted correctly or if the collection was not of the required size.
    */
   public <C extends Collection<E>, E> C getAndAssertCollection(String key, Class<C> collectionType, Class<E> elementType, int size) throws InvalidInputException {
      C c = getCollection(key, collectionType, elementType, false);
      if (c != null) {
         if (c.size() >= size) {
            return c;
         } else {
            throw new InvalidInputException("Collection at: '" + getStoragePath() + "." + key + "' must have a minimum of "
                                            + size + " elements of type " + elementType.getSimpleName()
                                            + ". It has: " + c.size());
         }
      } else {
         assertSet(key);
         Object obj = get(key);
         throw new InvalidInputException(TextUtils.getTypeAndValueDescription(obj)
                                         + " at: '" + getStoragePath() + "." + key + "' could not be converted to a "
                                         + TextUtils.getDescription(collectionType) + " with elements of type: "
                                         + TextUtils.getDescription(elementType));
      }
   }

   /**
    * Get a Map at the given key with the given Map type and keys and values of the
    * given types.
    *
    * @param <M>         The Map type.
    * @param <K>         The Map key type.
    * @param <V>         The Map value type.
    * @param key         The key.
    * @param mapType     The Map type class.
    * @param keyType     The Map key type class.
    * @param valueType   The Map value type class.
    * @param emptyIfNull Whether or not an empty Map of type M should be returned if
    *                    no Map was found.
    *
    * @return The Map. Can be null if emptyIfNull is false.
    */
   public <K, V, M extends Map<K, V>> M getMap(String key, Class<M> mapType, Class<K> keyType, Class<V> valueType, boolean emptyIfNull) {
      Object obj = get(key);

      //If the collection type and the cached element type are known and correct, return the obj.
      if (obj != null && mapType.isAssignableFrom(obj.getClass())) {
         Pair<Class<?>, Class<?>> cachedKeyValueTypes = getConversionModule().getCachedKeyValueTypes((Map) obj);
         if (cachedKeyValueTypes != null && cachedKeyValueTypes.getKey().isAssignableFrom(keyType) && cachedKeyValueTypes.getValue().isAssignableFrom(valueType)) {
            return (M) obj;
         }
      }
      //Otherwise convert, override and return
      M m = getConversionModule().convertToMap(obj, mapType, keyType, valueType, emptyIfNull);
      set(key, m);
      return m;
   }

   /**
    * Identical to calling getMap(String, Class<M>, Class<K>, Class<V>, false).
    */
   public <K, V, M extends Map<K, V>> M getMap(String key, Class<M> mapType, Class<K> keyType, Class<V> valueType) {
      return getMap(key, mapType, keyType, valueType, false);
   }

   /**
    * Get a Map at the given key with the given Map type and keys and values of the
    * given types. If there was no data found at the given key, or if the data could not be
    * converted to the given Map type an exception is thrown. A minimum size can also be defined.
    *
    * @param <M>       The Map type.
    * @param <K>       The Map key type.
    * @param <V>       The Map value type.
    * @param key       The key.
    * @param mapType   The Map type class.
    * @param keyType   The Map key type class.
    * @param valueType The Map value type class.
    *
    * @return The Collection. Is never null.
    * @throws InvalidInputException Thrown if no data was found, if the data found could
    * not be converted correctly or if the Map was not of the required size.
    */
   public <K, V, M extends Map<K, V>> M getAndAssertMap(String key, Class<M> mapType, Class<K> keyType, Class<V> valueType, int size) throws InvalidInputException {
      M m = getMap(key, mapType, keyType, valueType, false);
      if (m != null) {
         if (m.size() >= size) {
            return m;
         } else {
            throw new InvalidInputException("Map at: '" + getStoragePath() + "." + key + "' must have a minimum of " + size
                                            + " entries with key type: " + keyType.getSimpleName()
                                            + " and value type: " + valueType.getSimpleName()
                                            + ". It has: " + m.size());
         }
      } else {
         assertSet(key);
         Object obj = get(key);
         throw new InvalidInputException(TextUtils.getTypeAndValueDescription(obj) + " at: '"
                                         + getStoragePath() + "." + key + "' could not be converted to a "
                                         + mapType.getSimpleName() + " with key type: " + keyType.getSimpleName()
                                         + " ,and value type: " + valueType.getSimpleName());
      }
   }

   public String getStoragePath() {
      List<String> path = new ArrayList();
      Storage currentStorage = this;
      while (currentStorage != null && !(currentStorage instanceof MemoryStorageRoot)) {
         path.add(currentStorage.getStorageKey());
      }
      return TextUtils.concatenateWith(Lists.reverse(path), ".");
   }

   @Override
   public Object convertToKey(String stringKey) {
      return stringKey;
   }

   @Override
   public String getDataValueDescription() {
      return "Value";
   }
}
