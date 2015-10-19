package nl.larsdenbakker.conversion.converters;

import nl.larsdenbakker.conversion.ConversionException;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 *
 * @author Lars den Bakker <larsdenbakker at gmail.com>
 */
public class FloatConverter extends DataConverter<Float> {

   public FloatConverter() {
      super(Float.class);
   }

   @Override
   protected Float _convert(Object input) throws ConversionException {
      checkNotNull(input);
      try {
         float temp = Float.parseFloat(input.toString());
         return temp;
      } catch (NumberFormatException e) {
         throw new ConversionException(input + " is not a number.");
      }
   }

}
