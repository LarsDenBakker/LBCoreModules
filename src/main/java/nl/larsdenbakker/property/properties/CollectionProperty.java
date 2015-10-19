package nl.larsdenbakker.property.properties;

import java.util.Collection;
import nl.larsdenbakker.conversion.ConversionException;
import nl.larsdenbakker.conversion.ConversionModule;
import nl.larsdenbakker.property.PropertyHolder;
import nl.larsdenbakker.property.PropertyValidationException;
import nl.larsdenbakker.storage.Storage;
import nl.larsdenbakker.util.CollectionUtils;

/**
 *
 * @author Lars den Bakker <larsdenbakker at gmail.com>
 */
public class CollectionProperty<E, V extends Collection<E>> extends StorageProperty<V> {

   private final Class<E> collectionElementType;
   private final Class<? extends V> collectionType;

   public CollectionProperty(Storage storage, Class<V> collectionInterface, Class<? extends V> collectionType, Class<E> collectedElementType) {
      super(storage, collectionInterface);
      this.collectionElementType = collectedElementType;
      this.collectionType = collectionType;
   }

   @Override
   protected V _getValue(PropertyHolder pdh) {
      V val = pdh.getStorage().getCollection(getKey(), getCollectionType(), getCollectionElementType(), true);
      return (val != null) ? val : getDefaultValue(pdh);
   }

   @Override
   protected V getDefaultValue(PropertyHolder dh) {
      V copy = CollectionUtils.instanceOf(collectionType);
      setValidValue(dh, copy);
      return copy;
   }

   public V getCopy(PropertyHolder pdh) {
      V copy = CollectionUtils.instanceOf(collectionType);
      V val = getValue(pdh);
      copy.addAll(val);
      return copy;
   }

   @Override
   public void addToValue(PropertyHolder ph, Object value) throws PropertyModificationException {
      try {
         E e = convertInputToElementType(ph.getConversionModule(), value);
      } catch (ConversionException ex1) {
         try {
            V v = convertToValueType(ph.getConversionModule(), value);
            V copy = getCopy(ph);
            copy.addAll(v);
            validate(copy);
            getValue(ph).addAll(v);
         } catch (ConversionException | PropertyValidationException ex2) {
            throw new PropertyModificationException(ex2);
         }
      }
   }

   @Override
   public void removeFromValue(PropertyHolder ph, Object value) throws PropertyModificationException {
      try {
         E e = convertInputToElementType(ph.getConversionModule(), value);
      } catch (ConversionException ex1) {
         try {
            V v = convertToValueType(ph.getConversionModule(), value);
            V copy = getCopy(ph);
            copy.removeAll(v);
            validate(copy);
            getValue(ph).removeAll(v);
         } catch (ConversionException ex2) {
            throw new PropertyModificationException(ex2);
         } catch (PropertyValidationException ex) {
            throw new PropertyModificationException(ex);
         }
      }
   }

   @Override
   public void clearValue(PropertyHolder dh) {
      getValue(dh).clear();
   }

   @Override
   protected V convertToValueType(ConversionModule conversionModule, Object obj) throws ConversionException {
      return conversionModule.convertToCollection(obj, getPropertyValueClass(), collectionElementType, true);
   }

   protected E convertInputToElementType(ConversionModule conversionModule, Object obj) throws ConversionException {
      return conversionModule.convert(obj, collectionElementType);
   }

   public Class<? extends V> getCollectionType() {
      return collectionType;
   }

   public Class<E> getCollectionElementType() {
      return collectionElementType;
   }

}
