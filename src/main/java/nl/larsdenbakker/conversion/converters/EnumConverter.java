package nl.larsdenbakker.conversion.converters;

import nl.larsdenbakker.conversion.ConversionException;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 *
 * @author Lars den Bakker <larsdenbakker at gmail.com>
 */
public class EnumConverter extends SuperTypeDataConverter<Enum> {

   public EnumConverter() {
      super(Enum.class);
   }

   @Override
   protected <A extends Enum> A _convert(Object input, Class<A> subClass) throws ConversionException {
      checkNotNull(input);
      String str = input.toString().toUpperCase().replaceAll(" ", "_");
      try {
         A a = (A) Enum.valueOf(subClass, str);
         return a;
      } catch (IllegalArgumentException | ClassCastException e) {
         throw new ConversionException("Invalid input: " + input);
      }
   }

}
