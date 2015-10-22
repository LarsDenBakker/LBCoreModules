package nl.larsdenbakker.conversion.converters;

import com.google.common.base.Preconditions;
import nl.larsdenbakker.conversion.ConversionException;

/**
 *
 * @author Lars den Bakker <larsdenbakker at gmail.com>
 */
public abstract class SuperTypeDataConverter<T> {

   private final Class<T> superClass;

   public SuperTypeDataConverter(Class<T> superClass) {
      this.superClass = superClass;
   }

   public <A extends T> A convert(Object input, Class<A> subclass) throws ConversionException {
      Preconditions.checkNotNull(input, "Input cannot be null");
      A t;
      if (!superClass.isAssignableFrom(input.getClass())) {
         t = _convert(input, subclass);
      } else {
         t = (A) input;
      }
      return t;
   }

   protected abstract <A extends T> A _convert(Object input, Class<A> subclass) throws ConversionException;

   public Class<T> getSuperClass() {
      return superClass;
   }

}
