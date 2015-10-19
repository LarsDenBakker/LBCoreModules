package nl.larsdenbakker.conversion.converters;

import nl.larsdenbakker.conversion.ConversionException;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 *
 * @author Lars den Bakker <larsdenbakker at gmail.com>
 */
public class ShortConverter extends DataConverter<Short> {

   public ShortConverter() {
      super(Short.class);
   }

   @Override
   protected Short _convert(Object input) throws ConversionException {
      checkNotNull(input);
      try {
         short temp = Short.parseShort(input.toString());
         return temp;
      } catch (NumberFormatException e) {
         throw new ConversionException(input + " is not a number.");
      }
   }

}
