package nl.larsdenbakker.conversion.converters;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import nl.larsdenbakker.conversion.ConversionException;

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
         A coll = subclass.getConstructor().newInstance();
         coll.add(input);
         return coll;
      } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
         throw new ConversionException("Unable to convert collection type " + subclass + ": " + ex.getMessage());
      }

   }

}
