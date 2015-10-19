package nl.larsdenbakker.property;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import nl.larsdenbakker.conversion.reference.DataReferencable;
import nl.larsdenbakker.conversion.reference.DataReferenceList;
import nl.larsdenbakker.property.properties.CollectionProperty;
import nl.larsdenbakker.property.properties.MapProperty;
import nl.larsdenbakker.property.properties.Property;
import nl.larsdenbakker.property.properties.SimpleProperty;
import nl.larsdenbakker.storage.Storage;

/**
 * This factory is called when PropertyHolder Properties are created. See the
 * Property class for more information on properties. Properties created for a
 * certain value type use an implementation of Property registered to this value
 * type, or a default implementation.
 *
 * @author Lars den Bakker <larsdenbakker at gmail.com>
 */
public class PropertyFactory {

   private final PropertyModule propertyModule;
   private final Map<Class<?>, Class<? extends Property>> propertyTypes = new HashMap();
   private final Map<Class<?>, Class<? extends CollectionProperty>> collectionPropertyTypes = new HashMap();
   private final Map<Class<?>, Class<? extends MapProperty>> mapPropertyTypes = new HashMap();

   public PropertyFactory(PropertyModule propertyModule) {
      this.propertyModule = propertyModule;
   }

   /**
    * Register a Property type to be used for a specific value type.
    *
    * @param valueType    The value type.
    * @param propertyType The property type.
    */
   public void registerPropertyType(Class<?> valueType, Class<? extends Property> propertyType) {
      propertyTypes.put(valueType, propertyType);
   }

   /**
    * Register a Collection Property type to be used for a specific collection
    * type.
    *
    * @param collectionType The Collection type.
    * @param propertyType   The property type.
    */
   public void registerCollectionPropertyType(Class<? extends Collection> collectionType, Class<? extends CollectionProperty> propertyType) {
      collectionPropertyTypes.put(collectionType, propertyType);
   }

   /**
    * Register a Property type to be used for a specific map type.
    *
    * @param mapType      The Map type.
    * @param propertyType The property type.
    */
   public void registerMapPropertyType(Class<? extends Map> mapType, Class<? extends MapProperty> propertyType) {
      mapPropertyTypes.put(mapType, propertyType);
   }

   /**
    * Create a property from a configuration.
    *
    * @param <T>           The Property value type.
    * @param configuration The configuration of the property.
    * @param valueType     The property value type.
    *
    * @return An implementation of Property to be used for the specified value
    * type.
    */
   public <T> Property<T> createProperty(Storage configuration, Class<T> valueType) {
      Class<? extends Property> propertyType = propertyTypes.get(valueType);

      if (propertyType != null) {
         try {
            Constructor<? extends Property> constructor = propertyType.getConstructor(Storage.class);
            return constructor.newInstance(configuration);
         } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | InvocationTargetException ex) {
            throw new RuntimeException("Malformed Property class.", ex);
         }
      } else {
         return new SimpleProperty(configuration, valueType);
      }
   }

   /**
    * Create a collection property from a configuration.
    *
    * @param <E>            The Collection element type.
    * @param <C>            The Collection type.
    * @param configuration  The configuration of the property.
    * @param collectionType The Collection type.
    * @param elementType    The element type.
    *
    * @return An implementation of Property to be used for the specified value
    * type.
    */
   public <E, C extends Collection<E>> CollectionProperty<E, C> createCollectionProperty(Storage configuration, Class<? extends C> collectionType, Class<E> elementType) {
      if (DataReferencable.class.isAssignableFrom(elementType)) {
         if (List.class.equals(collectionType)) {
            return new CollectionProperty(configuration, List.class, DataReferenceList.class, elementType);
         } else {
            throw new IllegalArgumentException("Cannot create CollectionProperty for non DataReferenceCollection with DataReferencable element type: " + elementType);
         }
      }

      Class<? extends CollectionProperty> propertyType = collectionPropertyTypes.get(collectionType);

      if (propertyType != null) {
         try {
            Constructor<? extends CollectionProperty> constructor = propertyType.getConstructor(Storage.class, Class.class);
            return constructor.newInstance(configuration, elementType);
         } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | InvocationTargetException ex) {
            throw new RuntimeException("Malformed Property class.", ex);
         }
      } else {
         Class<C> collectionInterface;
         //If the passed type is a collection interface

         if (List.class.equals(collectionType)) {
            collectionInterface = (Class<C>) collectionType;
            collectionType = (Class<? extends C>) ((Class) ArrayList.class);
         } else if (Set.class.equals(collectionType)) {
            collectionInterface = (Class<C>) collectionType;
            collectionType = (Class<? extends C>) ((Class) HashSet.class);
            //Else if the passed type is a subclass of a collecton interface
         } else if (List.class.isAssignableFrom(collectionType)) {
            collectionInterface = (Class<C>) ((Class) List.class);
         } else if (Set.class.isAssignableFrom(collectionType)) {
            collectionInterface = (Class<C>) ((Class) Set.class);
         } else {
            throw new IllegalArgumentException("Cannot determine Collection Interface for Collection Type: " + collectionType);
         }
         return new CollectionProperty(configuration, collectionInterface, collectionType, elementType);
      }
   }

   /**
    * Create a collection property from a configuration.
    *
    * @param <K>           The key type.
    * @param <V>           The value type.
    * @param <M>           The Map type.
    * @param configuration The configuration of the property.
    * @param mapType       The Map type.
    * @param valueType     The value type.
    * @param keyType       The key type.
    *
    * @return An implementation of Property to be used for the specified value
    * type.
    */
   public <K, V, M extends Map<K, V>> MapProperty<K, V> createMapProperty(Storage configuration, Class<? extends M> mapType, Class<K> keyType, Class<V> valueType) {
      Class<? extends MapProperty> propertyType = mapPropertyTypes.get(mapType);

      if (propertyType != null) {
         try {
            Constructor<? extends MapProperty> constructor = propertyType.getConstructor(Storage.class, Class.class, Class.class, Class.class
            );
            return constructor.newInstance(configuration, mapType, keyType, valueType);
         } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | InvocationTargetException ex) {
            throw new RuntimeException("Malformed Property class.", ex);
         }
      } else {
         if (Map.class.equals(mapType)) {
            mapType = (Class<M>) ((Class) HashMap.class);
         }
         return new MapProperty(configuration, mapType, keyType, valueType);
      }
   }

}
