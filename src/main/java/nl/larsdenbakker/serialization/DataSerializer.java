package nl.larsdenbakker.serialization;

import com.google.common.base.Preconditions;
import nl.larsdenbakker.conversion.ConversionException;

/**
 * Object that handles serialization of an object type. It is best
 * practice for Objects to implement DataSerializable instead. This
 * class is used for serialization of types from external projects.
 *
 * @author Lars den Bakker <larsdenbakker at gmail.com>
 */
public abstract class DataSerializer<T> {

   private final Class<T> serializedType;

   public DataSerializer(Class<T> serializedType) {
      this.serializedType = serializedType;
   }

   public Object toSerializable(T input) throws ConversionException {
      Preconditions.checkNotNull(input, "Input cannot be null");
      return _toSerializable(input);
   }

   public Class<T> getSerializedType() {
      return serializedType;
   }

   protected abstract Object _toSerializable(T input) throws ConversionException;

}
