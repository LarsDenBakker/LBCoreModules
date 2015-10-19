package nl.larsdenbakker.property.properties;

import nl.larsdenbakker.conversion.ConversionException;
import nl.larsdenbakker.property.PropertyHolder;
import nl.larsdenbakker.storage.Storage;

/**
 *
 * @author Lars den Bakker <larsdenbakker at gmail.com>
 */
public class StringProperty extends SimpleProperty<String> {

   public StringProperty(Storage storage) {
      super(storage, String.class);
   }

   @Override
   public void addToValue(PropertyHolder ph, Object value) throws PropertyModificationException {
      try {
         String convertedValue = convertToValueType(ph.getConversionModule(), value);
         String prevVal = getValue(ph);
         String newVal = prevVal + convertedValue;
         setValue(ph, newVal);
      } catch (ConversionException ex) {
         throw new PropertyModificationException(ex);
      }
   }

   @Override
   public String getTypeDescription() {
      return "String Property";
   }

}
