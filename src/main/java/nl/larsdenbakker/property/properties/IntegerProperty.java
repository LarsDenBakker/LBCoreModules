package nl.larsdenbakker.property.properties;

import nl.larsdenbakker.conversion.ConversionException;
import nl.larsdenbakker.property.PropertyHolder;
import nl.larsdenbakker.storage.Storage;

/**
 *
 * @author Lars den Bakker<larsdenbakker@gmail.com>
 */
public class IntegerProperty extends SimpleProperty<Integer> {

   public IntegerProperty(Storage storage) {
      super(storage, Integer.class);
   }

   @Override
   public void addToValue(PropertyHolder ph, Object value) throws PropertyModificationException {
      try {
         Integer convertedValue = convertToValueType(ph.getConversionModule(), value);
         Integer currentVal = getValue(ph);
         Integer newVal = Math.addExact(currentVal, convertedValue);
         setValue(ph, newVal);
      } catch (ConversionException ex) {
         throw new PropertyModificationException(ex);
      }
   }

   @Override
   public void removeFromValue(PropertyHolder ph, Object value) throws PropertyModificationException {
      try {
         Integer convertedValue = convertToValueType(ph.getConversionModule(), value);
         Integer currentVal = this.getValue(ph);
         Integer newVal = Math.subtractExact(currentVal, convertedValue);
         setValue(ph, newVal);
      } catch (ConversionException ex) {
         throw new PropertyModificationException(ex);
      }
   }

   @Override
   public String getTypeDescription() {
      return "Integer Property";
   }

}
