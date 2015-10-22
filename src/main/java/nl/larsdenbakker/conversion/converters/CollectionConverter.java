package nl.larsdenbakker.conversion.converters;

import java.util.Collection;
import nl.larsdenbakker.conversion.ConversionException;
import nl.larsdenbakker.util.CollectionUtils;

/**
 *
 * @author Lars den Bakker<larsdenbakker@gmail.com>
 */
public class CollectionConverter extends SuperTypeDataConverter<Collection> {

   public CollectionConverter() {
      super(Collection.class);
   }

   @Override
   protected <A extends Collection> A _convert(Object input, Class<A> subclass) throws ConversionException {
      try {
         return (A) CollectionUtils.instanceOf(subclass, input);
      } catch (IllegalArgumentException ex) {
         throw new ConversionException("Unable to convert collection type " + subclass + ": " + ex.getMessage());
      }
   }

}
