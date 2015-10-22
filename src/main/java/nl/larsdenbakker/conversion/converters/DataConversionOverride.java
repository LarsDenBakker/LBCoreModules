package nl.larsdenbakker.conversion.converters;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 *
 * @author Lars den Bakker<larsdenbakker@gmail.com>
 */
public abstract class DataConversionOverride {

   public <T> T convert(Object input, Class<T> type) {
      checkNotNull(input, "Input cannot be null");
      return _convert(input, type);
   }

   protected abstract <T> T _convert(Object input, Class<T> type);

}
