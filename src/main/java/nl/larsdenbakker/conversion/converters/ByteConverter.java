package nl.larsdenbakker.conversion.converters;

import nl.larsdenbakker.conversion.ConversionException;

/**
 *
 * @author Lars den Bakker <larsdenbakker at gmail.com>
 */
public class ByteConverter extends DataConverter<Byte> {

   public ByteConverter() {
      super(Byte.class);
   }

   @Override
   protected Byte _convert(Object input) throws ConversionException {
      try {
         byte b = Byte.parseByte(input.toString());
         return b;
      } catch (NumberFormatException e) {
         throw new ConversionException(input + " is not a byte.");
      }
   }

}
