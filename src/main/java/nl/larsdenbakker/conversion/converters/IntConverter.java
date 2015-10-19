package nl.larsdenbakker.conversion.converters;

import nl.larsdenbakker.conversion.ConversionException;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 *
 * @author Lars den Bakker <larsdenbakker at gmail.com>
 */
public class IntConverter extends DataConverter<Integer> {

   public IntConverter() {
      super(Integer.class);
   }

   @Override
   protected Integer _convert(Object input) throws ConversionException {
      checkNotNull(input);
      try {
         int temp = Integer.parseInt(input.toString());
         return temp;
      } catch (NumberFormatException e) {
         throw new ConversionException(input + " is not a number.");
      }
   }

}
