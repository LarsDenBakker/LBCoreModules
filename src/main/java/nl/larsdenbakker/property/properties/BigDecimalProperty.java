package nl.larsdenbakker.property.properties;

import java.math.BigDecimal;
import nl.larsdenbakker.conversion.ConversionException;
import nl.larsdenbakker.property.PropertyHolder;
import nl.larsdenbakker.storage.Storage;

/**
 *
 * @author Lars den Bakker<larsdenbakker@gmail.com>
 */
public class BigDecimalProperty extends SimpleProperty<BigDecimal> {

   private final int scale;

   public BigDecimalProperty(Storage storage) {
      super(storage, BigDecimal.class);
      this.scale = storage.get("scale", Integer.class, 2);
   }

   @Override
   public void addToValue(PropertyHolder ph, Object value) throws PropertyModificationException {
      try {
         BigDecimal convertedValue = this.convertToValueType(ph.getConversionModule(), value);
         BigDecimal currentVal = getValue(ph);
         BigDecimal newVal = currentVal.add(convertedValue);
         setValue(ph, newVal);
      } catch (ConversionException ex) {
         throw new PropertyModificationException(ex);
      }
   }

   @Override
   public void removeFromValue(PropertyHolder ph, Object value) throws PropertyModificationException {
      try {
         BigDecimal convertedValue = this.convertToValueType(ph.getConversionModule(), value);
         BigDecimal currentVal = this.getValue(ph);
         BigDecimal newVal = currentVal.subtract(convertedValue);
         setValue(ph, newVal);
      } catch (ConversionException ex) {
         throw new PropertyModificationException(ex);
      }
   }

   @Override
   public String getTypeDescription() {
      return "Decimal Property";
   }

   @Override
   protected void _setToStorage(PropertyHolder ph, BigDecimal value) {
      super._setToStorage(ph, value.setScale(scale));
   }

}
