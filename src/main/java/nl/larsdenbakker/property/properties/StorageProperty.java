package nl.larsdenbakker.property.properties;

import static com.google.common.base.Preconditions.checkNotNull;
import nl.larsdenbakker.conversion.ConversionException;
import nl.larsdenbakker.conversion.ConversionModule;
import nl.larsdenbakker.property.PropertyHolder;
import nl.larsdenbakker.property.PropertyValidationException;
import nl.larsdenbakker.storage.Storage;

/**
 * A type of Property whose values are stored in a Storage.
 *
 * @author Lars den Bakker <larsdenbakker at gmail.com>
 */
public abstract class StorageProperty<V> extends AbstractProperty<V> {

   public StorageProperty(Storage storage, Class<V> propertyValueClass) {
      super(storage, propertyValueClass);
   }

   @Override
   protected V _getValue(PropertyHolder pdh) {
      V val = pdh.getStorage().get(getKey(), getPropertyValueClass());
      return (val != null) ? val : getDefaultValue(pdh);
   }

   @Override
   public void setValue(PropertyHolder ph, Object value) throws PropertyModificationException {
      try {
         V v = convertToValueType(ph.getConversionModule(), value);
         validate(v);
         _setToStorage(ph, v);
      } catch (ConversionException | PropertyValidationException ex) {
         throw new PropertyModificationException(ex);
      }
   }

   @Override
   public void setValidValue(PropertyHolder ph, V value) {
      checkNotNull(value);
      verifyHasProperty(ph);
      try {
         validate(value);
         _setToStorage(ph, value);
      } catch (PropertyValidationException ex) {
         throw new IllegalArgumentException("Asserted valid value was not valid", ex);
      }
   }

   protected void _setToStorage(PropertyHolder ph, V value) {
      ph.getStorage().set(getKey(), value);
   }

   @Override
   public void clearValue(PropertyHolder ph) throws PropertyModificationException {
      verifyHasProperty(ph);
      if (isNullable()) {
         ph.getStorage().unset(getKey());
      } else {
         throw new PropertyModificationException(getTypeAndValueDescription() + " cannot have no value.");
      }
   }

   protected abstract V convertToValueType(ConversionModule conversionModule, Object val) throws ConversionException;

}
