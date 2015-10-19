package nl.larsdenbakker.conversion.converters;

import nl.larsdenbakker.conversion.ConversionException;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 *
 * @author Lars den Bakker <larsdenbakker at gmail.com>
 */
public class DoubleConverter extends DataConverter<Double> {

   public DoubleConverter() {
      super(Double.class);
   }

   @Override
   protected Double _convert(Object input) throws ConversionException {
      checkNotNull(input);
      try {
         double temp = Double.parseDouble(input.toString());
         return temp;
      } catch (NumberFormatException e) {
         throw new ConversionException(input + " is not a number.");
      }
   }

}
