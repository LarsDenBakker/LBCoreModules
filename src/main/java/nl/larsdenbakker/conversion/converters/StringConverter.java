package nl.larsdenbakker.conversion.converters;

import nl.larsdenbakker.conversion.ConversionException;

/**
 *
 * @author Lars den Bakker <larsdenbakker at gmail.com>
 */
public class StringConverter extends DataConverter<String> {

   public StringConverter() {
      super(String.class);
   }

   @Override
   protected String _convert(Object input) throws ConversionException {
      return input.toString();
   }

}
