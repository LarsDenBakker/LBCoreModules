package nl.larsdenbakker.conversion;

import nl.larsdenbakker.conversion.converters.DoubleConverter;
import nl.larsdenbakker.conversion.converters.StringConverter;
import nl.larsdenbakker.conversion.converters.IntConverter;
import nl.larsdenbakker.conversion.converters.ByteConverter;
import nl.larsdenbakker.conversion.converters.EnumConverter;
import nl.larsdenbakker.conversion.converters.ClassConverter;
import nl.larsdenbakker.conversion.converters.BigDecimalConverter;
import nl.larsdenbakker.conversion.converters.LongConverter;
import nl.larsdenbakker.conversion.converters.BooleanConverter;
import nl.larsdenbakker.conversion.converters.ShortConverter;
import nl.larsdenbakker.conversion.converters.DataConversionOverride;
import nl.larsdenbakker.conversion.converters.SuperTypeDataConverter;
import nl.larsdenbakker.conversion.converters.DataConverter;
import nl.larsdenbakker.conversion.converters.UUIDConverter;
import nl.larsdenbakker.conversion.converters.FloatConverter;
import static com.google.common.base.Preconditions.checkNotNull;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import javafx.util.Pair;
import nl.larsdenbakker.app.AbstractModule;
import nl.larsdenbakker.app.Application;
import nl.larsdenbakker.conversion.converters.LocalDateConverter;
import nl.larsdenbakker.conversion.reference.DataReferencable;
import nl.larsdenbakker.conversion.reference.DataReference;
import nl.larsdenbakker.conversion.reference.DataReferenceList;
import nl.larsdenbakker.registry.Registry;
import nl.larsdenbakker.operation.operations.Operation;
import nl.larsdenbakker.util.CollectionUtils;
import nl.larsdenbakker.util.MapUtils;
import nl.larsdenbakker.util.TextUtils;

/**
 * A module that handles conversion between object data types and helps ensuring
 * type safety within collections and maps. Different types of custom converters
 * can be created and registered to trigger and different points of the
 * conversion process. This module has no dependencies.
 *
 * @author Lars den Bakker <larsdenbakker at gmail.com>
 */
public class ConversionModule extends AbstractModule {

   private final Map<Class<?>, DataConverter> converters = new HashMap<>();
   private final Map<Class<?>, SuperTypeDataConverter> superClassConverters = new HashMap<>();
   private final List<DataConversionOverride> weakConverters = new ArrayList<>();

   private final Map<Collection, Class<?>> collectionElementTypeCache = new WeakHashMap<>();
   private final Map<Map, Pair<Class<?>, Class<?>>> mapKeyValueTypesCache = new WeakHashMap<>();
   private final Map<String, Class<?>> typeMappings = new HashMap();

   public ConversionModule(Application app) {
      super(app);
   }

   @Override
   public String getName() {
      return "conversion";
   }

   @Override
   protected void _load() {
      registerConverter(new BigDecimalConverter());
      registerConverter(new BooleanConverter());
      registerConverter(new ByteConverter());
      registerConverter(new DoubleConverter());
      registerConverter(new FloatConverter());
      registerConverter(new IntConverter());
      registerConverter(new ShortConverter());
      registerConverter(new LongConverter());
      registerConverter(new StringConverter());
      registerConverter(new UUIDConverter());
      registerConverter(new ClassConverter(this));
      registerConverter(new ClassConverter(this));
      registerConverter(new LocalDateConverter());
      registerSuperTypeConverter(new EnumConverter());

      addTypeMapping("short", Short.class);
      addTypeMapping("int", Integer.class);
      addTypeMapping("integer", Integer.class);
      addTypeMapping("long", Long.class);
      addTypeMapping("float", Float.class);
      addTypeMapping("double", Double.class);
      addTypeMapping("decimal", BigDecimal.class);
      addTypeMapping("bigdecimal", BigDecimal.class);

      addTypeMapping("byte", Byte.class);
      addTypeMapping("boolean", Boolean.class);
      addTypeMapping("string", String.class);
      addTypeMapping("class", Class.class);
      addTypeMapping("uuid", UUID.class);
      addTypeMapping("registry", Registry.class);
      addTypeMapping("operation", Operation.class);

      addTypeMapping("map", Map.class);
      addTypeMapping("hashmap", HashMap.class);
      addTypeMapping("weakhashmap", WeakHashMap.class);
      addTypeMapping("enummap", WeakHashMap.class);
      addTypeMapping("linkedhashmap", WeakHashMap.class);

      addTypeMapping("collection", Collection.class);
      addTypeMapping("list", List.class);
      addTypeMapping("arraylist", ArrayList.class);
      addTypeMapping("linkedlist", LinkedList.class);
      addTypeMapping("set", Set.class);
      addTypeMapping("hashset", HashSet.class);
      addTypeMapping("linkedhashset", LinkedHashSet.class);
      addTypeMapping("enumset", EnumSet.class);
   }

   /**
    * Convert an object to the desired type. Throws an exception if conversion
    * could not take place for any reason.
    *
    * @param <T>        The desired return type.
    * @param obj        The object that is to be converted.
    * @param returnType The desired return type.
    *
    * @return The converted object.
    * @throws ConversionException
    */
   public <T> T convert(Object obj, Class<T> returnType) throws ConversionException {
      checkNotNull(obj);
      checkNotNull(returnType);
      //Look for a weak converter first, return if succesful.
      for (DataConversionOverride converter : getWeakConverters()) {
         T returnObject = converter.convert(obj, returnType);
         if (returnObject != null) {
            return returnObject;
         }
      }
      //Return is object can be cast to desired type.
      if (returnType.isAssignableFrom(obj.getClass())) {
         return (T) obj; //Safe cast ensured
      } else {
         //Look for a regular DataConverter and convert if found.
         DataConverter<T> dataConverter = getConverter(returnType);
         if (dataConverter != null) {
            return dataConverter.convert(obj);
         } else {
            //Look if a converter is registered for any of the return type's
            //super types, otherwise throw an exception.
            SuperTypeDataConverter<? super T> superTypeConverter = getSuperConverter(returnType);
            if (superTypeConverter != null) {
               return superTypeConverter.convert(obj, returnType);
            } else {
               throw new ConversionException("Did not find any Converter for class: " + returnType.getName());
            }
         }
      }
   }

   /**
    * Convert an Object to a Collection with elements of the desired type. If
    * it is a Collection and it is of the same collection and element type it is
    * returned as is, otherwise a new Collection is created with all elements
    * converted to the desired type. If it is a String representation of a
    * Collection it is converted to a Collection.
    *
    * @param <E>            The element type.
    * @param <C>            The Collection type.
    * @param obj            The Object to be converted.
    * @param collectionType The desired Collection type.
    * @param elementType    The desired element type.
    * @param emptyIfNull    Whether or not an empty Collection should be returned
    *                       if the Collection could not be converted.
    *
    * @return The converted Collection.
    */
   public <E, C extends Collection<E>> C convertToCollection(Object obj, Class<C> collectionType, Class<E> elementType, boolean emptyIfNull) {
      //Grab defaults for types if interfaces or non-datareference collections are provided
      Class<? extends C> constructionCollectionType;
      Class<? extends E> constructionElementType;
      if (collectionType.equals(List.class)) {
         if (DataReferencable.class.isAssignableFrom(elementType)) {
            constructionCollectionType = (Class) DataReferenceList.class;
            constructionElementType = (Class) DataReference.class;
         } else {
            constructionCollectionType = (Class) ArrayList.class;
         }
      } else if (collectionType.equals(Set.class)) {
         if (DataReferencable.class.isAssignableFrom(elementType)) {
            constructionElementType = (Class) DataReference.class;
            throw new IllegalArgumentException("Set class is not supported for DataReferencable elements");
         }
         constructionCollectionType = (Class) HashSet.class;
      } else {
         constructionCollectionType = collectionType;
      }

      if (obj != null) {
         if (obj instanceof Collection) {
            //If it's already a collection
            Collection collection = (Collection) obj;
            Class cachedElementType = getCachedElementType(collection);
            //If we have cached it's element type return if same
            if (cachedElementType != null && elementType.isAssignableFrom(cachedElementType) && collection.getClass().equals(constructionCollectionType)) {
               setCachedElementType(collection, elementType);
               return (C) collection;
            } else {
               return _convertContents(collection, constructionCollectionType, elementType);
            }
         } else {
            try {
               //If it's a single value convertable to E
               E e = convert(obj, elementType);
               C c = CollectionUtils.instanceOf(constructionCollectionType);
               c.add(e);
               return c;
            } catch (ConversionException ex) {
            }
            try {
               //If it's a string representation of one or many E's
               String string = convert(obj, String.class);
               String[] split = TextUtils.splitOnSpacesAndCommas(string);
               C c = CollectionUtils.instanceOf(constructionCollectionType);
               for (String str : split) {
                  try {
                     E e = convert(obj, elementType);
                     c.add(e);
                  } catch (ConversionException ex) {

                  }
               }
               if (!c.isEmpty()) {
                  return c;
               }
            } catch (ConversionException ex) {

            }
         }
      }

      //Fall through to here if a collection could not be converted for any reason
      if (emptyIfNull) {
         return CollectionUtils.instanceOf(constructionCollectionType);
      } else {
         return null;
      }
   }

   private <C extends Collection<E>, E> C _convertContents(Collection collection, Class<C> collectionClass, Class<E> elementClass) {
      C coll = (C) CollectionUtils.instanceOf(collectionClass);
      for (Object obj : collection) {
         try {
            E e = convert(obj, elementClass);
            coll.add(e);
         } catch (ConversionException ex) {
         }
      }
      setCachedElementType(coll, elementClass);
      return coll;
   }

   private <C extends Collection<E>, E> C _convertContents(C collection, Class<E> elementClass) {
      return _convertContents(collection, (Class<C>) ((Class) collection.getClass()), elementClass);
   }

   private <K, V, M extends Map<K, V>> M _convertContents(M map, Class<K> keyClass, Class<V> valueClass) {
      return _convertContents(map, (Class<M>) ((Class) map.getClass()), keyClass, valueClass);
   }

   private <K, V, M extends Map<K, V>> M _convertContents(Map<?, ?> map, Class<M> mapClass, Class<K> keyClass, Class<V> valueClass) {
      M newMap = (M) MapUtils.of(mapClass);
      for (Entry entry : map.entrySet()) {
         try {
            K k = convert(entry.getKey(), keyClass);
            V v = convert(entry.getValue(), valueClass);
            newMap.put(k, v);
         } catch (ConversionException ex) {
         }
      }
      setCacheKeyValueTypes(map, keyClass, valueClass);
      return newMap;
   }

   private <V, M extends Map<Object, V>> M _convertValues(Map<?, ?> map, Class<M> mapClass, Class<V> valueClass) {
      M newMap = (M) MapUtils.of(mapClass);
      for (Entry entry : map.entrySet()) {
         try {
            V v = convert(entry.getValue(), valueClass);
            newMap.put(entry.getKey(), v);
         } catch (ConversionException ex) {
         }
      }
      return newMap;
   }

   private <E> E[] _convertContents(Object[] array, Class<E> elementType) {
      List<E> list = new ArrayList();
      for (Object obj : array) {
         try {
            E e = convert(obj, elementType);
            list.add(e);
         } catch (ConversionException ex) {
         }
      }
      return CollectionUtils.asArrayOfType(elementType, list);
   }

   /**
    * Convert a Map to a Map with keys and values of the desired types. If it is a Map and it is of the same Map, Key and Value types it is
    * returned as is, otherwise a new Map is created with all keys and values converted to the desired type. If it is a String
    * representation of a Map it is converted to a Map.
    *
    * @param <K>         The key type.
    * @param <V>         The value type.
    * @param <M>         The Map type.
    * @param obj         The Object to be converted.
    * @param mapType     The desired Map type.
    * @param keyType     The desired key type.
    * @param valueType   The desired value type.
    * @param emptyIfNull Whether or not an empty Map should be returned
    *                    if the Map could not be converted.
    *
    * @return The converted Collection.
    */
   public <K, V, M extends Map<K, V>> M convertToMap(Object obj, Class<M> mapType, Class<K> keyType, Class<V> valueType, boolean emptyIfNull) {
      if (obj != null) {
         if (obj instanceof Map) {
            //If it's already a collection
            Map map = (Map) obj;
            Pair<Class<?>, Class<?>> cachedType = getCachedKeyValueTypes(map);
            //Look up the cached types if available, if they can be downcast to the
            //requested types do so and update cache
            if (cachedType != null && keyType.isAssignableFrom(cachedType.getKey())
                && valueType.isAssignableFrom(cachedType.getValue())) {
               setCacheKeyValueTypes(map, keyType, valueType);
               return (M) map;
            } else {
               return _convertContents(map, mapType, keyType, valueType);
            }
         } else {
            try {
               //If it's a string representation of key-value pairs
               M m = MapUtils.of(mapType);
               String string = convert(obj, String.class);
               if (string.startsWith("{") && string.endsWith("}")) {
                  string = string.substring(1, string.length() - 1);
               }
               String[] entries = TextUtils.splitOnSpacesAndCommas(string);
               for (String str : entries) {
                  String[] split = str.split("=");
                  if (split.length == 2) {
                     try {
                        K k = convert(split[0], keyType);
                        V v = convert(split[1], valueType);
                        m.put(k, v);
                     } catch (ConversionException ex) {
                     }
                  }
               }
               if (!m.isEmpty()) {
                  return m;
               }
            } catch (ConversionException ex) {
            }
         }
      }
      if (emptyIfNull) {
         return MapUtils.of(mapType);
      }
      return null;
   }

   public <T> boolean registerConverter(DataConverter<T> converter) {
      Class<T> clazz = converter.getReturnType();
      if (!converters.containsKey(clazz)) {
         converters.put(clazz, converter);
         return true;
      } else {
         return false;
      }
   }

   public <T> boolean registerSuperTypeConverter(SuperTypeDataConverter<T> converter) {
      if (!superClassConverters.containsKey(converter)) {
         superClassConverters.put(converter.getSuperClass(), converter);
         return true;
      } else {
         return false;
      }
   }

   public boolean unregisterConverter(Class clazz) {
      return converters.remove(clazz) != null;
   }

   public boolean unregisterSuperConverter(Class superClazz) {
      return superClassConverters.remove(superClazz) != null;
   }

   public <T> DataConverter<T> getConverter(Class<T> clazz) {
      DataConverter<T> dataConverter = converters.get(clazz); //register() ensures that DataConverter registered to Class<T> is of type T.
      if (dataConverter != null) {
         return dataConverter;
      } else {
         return null;
      }
   }

   public <T> SuperTypeDataConverter<? super T> getSuperConverter(Class<T> clazz) {
      return CollectionUtils.<SuperTypeDataConverter>getMappedValueFromSuperType(superClassConverters, clazz);
   }

   /**
    * @return The cached element type for this Collection or null if it is unknown.
    */
   public Class<?> getCachedElementType(Collection c) {
      for (Entry<Collection, Class<?>> entry : collectionElementTypeCache.entrySet()) {
         Collection k = entry.getKey();
         if (k == c || k.equals(c)) {
            return entry.getValue();
         }
      }
      return null;
   }

   /**
    * @return The cached key-value types for this map or null if it is unknown.
    */
   public Pair<Class<?>, Class<?>> getCachedKeyValueTypes(Map map) {
      return mapKeyValueTypesCache.get(map);
   }

   /**
    * Cache the known element type for a Collection.
    */
   public void setCachedElementType(Collection c, Class type) {
      checkNotNull(c);
      checkNotNull(type);
      collectionElementTypeCache.put(c, type);
   }

   public void setCacheKeyValueTypes(Map map, Class key, Class value) {
      checkNotNull(map);
      checkNotNull(key);
      checkNotNull(value);
      mapKeyValueTypesCache.put(map, new Pair(key, value));
   }

   public void addTypeMapping(String key, Class<?> type) {
      checkNotNull(key);
      checkNotNull(type);
      key = key.toLowerCase();
      if (!typeMappings.containsKey(key)) {
         typeMappings.put(key, type);
      }
   }

   public void addTypeMappings(Map<String, Class<?>> mappings) {
      checkNotNull(mappings);
      for (Entry<String, Class<?>> entry : mappings.entrySet()) {
         addTypeMapping(entry.getKey(), entry.getValue());
      }
   }

   public Class<?> getTypeMapping(String key) {
      return typeMappings.get(key.toLowerCase());
   }

   public void registerConversionOverride(DataConversionOverride converter) {
      weakConverters.add(converter);
   }

   public boolean unregisterConversionOverride(DataConversionOverride converter) {
      return weakConverters.remove(converter);
   }

   public List<DataConversionOverride> getWeakConverters() {
      return weakConverters;
   }

}
