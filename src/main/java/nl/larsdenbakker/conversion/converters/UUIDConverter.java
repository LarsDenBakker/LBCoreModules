package nl.larsdenbakker.conversion.converters;

import java.util.UUID;
import nl.larsdenbakker.conversion.ConversionException;

/**
 *
 * @author Lars den Bakker <larsdenbakker at gmail.com>
 */
public class UUIDConverter extends DataConverter<UUID> {

   public UUIDConverter() {
      super(UUID.class);
   }

   @Override
   protected UUID _convert(Object input) throws ConversionException {
      try {
         return UUID.fromString(input.toString());
      } catch (IllegalArgumentException e) {
         throw new ConversionException(input + " is not a UUID.");
      }
   }

}
