package nl.larsdenbakker.property.properties;

import nl.larsdenbakker.conversion.ConversionException;
import nl.larsdenbakker.conversion.ConversionModule;
import nl.larsdenbakker.property.PropertyHolder;
import nl.larsdenbakker.storage.Storage;

/**
 *
 * @author Lars den Bakker <larsdenbakker at gmail.com>
 */
public class SimpleProperty<V> extends StorageProperty<V> {

   public SimpleProperty(Storage storage, Class<V> propertyValueClass) {
      super(storage, propertyValueClass);
   }

   @Override
   public void removeFromValue(PropertyHolder ph, Object value) throws PropertyModificationException {
      throw new PropertyModificationException(this.getTypeAndValueDescription() + " does not support remove modifications.");
   }

   @Override
   public void addToValue(PropertyHolder ph, Object value) throws PropertyModificationException {
      throw new PropertyModificationException(this.getTypeAndValueDescription() + " does not support add modifications.");
   }

   @Override
   protected V convertToValueType(ConversionModule conversionModule, Object val) throws ConversionException {
      return conversionModule.convert(val, getPropertyValueClass());
   }

}
