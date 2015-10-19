package nl.larsdenbakker.conversion.converters;

import nl.larsdenbakker.conversion.ConversionException;
import nl.larsdenbakker.conversion.ConversionModule;

/**
 *
 * @author Lars den Bakker <larsdenbakker at gmail.com>
 */
public class ClassConverter extends DataConverter<Class> {

   private final ConversionModule conversionHandler;

   public ClassConverter(ConversionModule conversionHandler) {
      super(Class.class);
      this.conversionHandler = conversionHandler;
   }

   @Override
   protected Class _convert(Object input) throws ConversionException {
      String stringInput = input.toString();

      try {
         return Class.forName(stringInput);
      } catch (ClassNotFoundException ex) {
         Class type = conversionHandler.getTypeMapping(stringInput);
         if (type != null) {
            return type;
         } else {
            throw new ConversionException(stringInput + " does not correspond to any mapped type.");
         }
      }

   }

}
