package nl.larsdenbakker.conversion.converters;

import nl.larsdenbakker.conversion.ConversionException;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 *
 * @author Lars den Bakker <larsdenbakker at gmail.com>
 */
public class LongConverter extends DataConverter<Long> {

   public LongConverter() {
      super(Long.class);
   }

   @Override
   protected Long _convert(Object input) throws ConversionException {
      checkNotNull(input);
      try {
         long temp = Long.parseLong(input.toString());
         return temp;
      } catch (NumberFormatException e) {
         throw new ConversionException(input + " is not a number.");
      }
   }
}
