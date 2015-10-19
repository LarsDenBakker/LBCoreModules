package nl.larsdenbakker.conversion.converters;

import com.google.common.base.Preconditions;
import nl.larsdenbakker.conversion.ConversionException;

/**
 * A DataConverter to convert between different forms of data. Implementations
 * should be registered to the ConversionModule to be used during regular conversion.
 *
 * @author Lars den Bakker <larsdenbakker at gmail.com>
 */
public abstract class DataConverter<T> {

   private final Class<T> returnType;

   public DataConverter(Class<T> returnType) {
      this.returnType = returnType;
   }

   public final T convert(Object input) throws ConversionException {
      Preconditions.checkNotNull(input, "Input cannot be null");
      T t;
      if (!returnType.isAssignableFrom(input.getClass())) {
         t = _convert(input);
      } else {
         t = (T) input;
      }
      return t;
   }

   /**
    * Implementation of type conversion. Input is never null, but implementations
    * must do further type checking themselves.
    *
    * @param input The Object to be converted.
    *
    * @return the converted object. Never null.
    * @throws ConversionException thrown if the object could not be converted for any reason.
    */
   protected abstract T _convert(Object input) throws ConversionException;

   public Class<T> getReturnType() {
      return returnType;
   }

}
