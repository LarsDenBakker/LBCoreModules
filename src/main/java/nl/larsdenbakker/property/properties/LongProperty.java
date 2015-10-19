package nl.larsdenbakker.property.properties;

import nl.larsdenbakker.conversion.ConversionException;
import nl.larsdenbakker.property.PropertyHolder;
import nl.larsdenbakker.storage.Storage;

/**
 *
 * @author Lars den Bakker<larsdenbakker@gmail.com>
 */
public class LongProperty extends SimpleProperty<Long> {

   public LongProperty(Storage storage) {
      super(storage, Long.class);
   }

   @Override
   public void addToValue(PropertyHolder ph, Object value) throws PropertyModificationException {
      try {
         Long convertedValue = convertToValueType(ph.getConversionModule(), value);
         Long currentVal = getValue(ph);
         Long newVal = Math.addExact(currentVal, convertedValue);
         setValue(ph, newVal);
      } catch (ConversionException ex) {
         throw new PropertyModificationException(ex);
      }
   }

   @Override
   public void removeFromValue(PropertyHolder ph, Object value) throws PropertyModificationException {
      try {
         Long convertedValue = convertToValueType(ph.getConversionModule(), value);
         Long currentVal = this.getValue(ph);
         Long newVal = Math.subtractExact(currentVal, convertedValue);
         setValue(ph, newVal);
      } catch (ConversionException ex) {
         throw new PropertyModificationException(ex);
      }
   }

   @Override
   public String getTypeDescription() {
      return "Long Property";
   }

}
