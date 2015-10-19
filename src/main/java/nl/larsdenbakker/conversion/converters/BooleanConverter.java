package nl.larsdenbakker.conversion.converters;

import nl.larsdenbakker.conversion.ConversionException;

/**
 *
 * @author Lars den Bakker <larsdenbakker at gmail.com>
 */
public class BooleanConverter extends DataConverter<Boolean> {

   public BooleanConverter() {
      super(Boolean.class);
   }

   @Override
   protected Boolean _convert(Object input) throws ConversionException {
      if (input instanceof String) {
         String str = (String) input;
         Boolean val = null;
         switch (str.toLowerCase()) {
            case "yes":
            case "true":
               val = true;
               break;
            case "no":
            case "false":
               val = false;
               break;
            default:
               throw new ConversionException("Input must be true, yes, false or no.");
         }
         return val;
      }
      throw new ConversionException("Invalid input");
   }

}
