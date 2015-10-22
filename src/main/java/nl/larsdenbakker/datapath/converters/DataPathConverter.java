package nl.larsdenbakker.datapath.converters;

import nl.larsdenbakker.conversion.converters.DataConversionOverride;
import nl.larsdenbakker.datapath.DataPathModule;
import nl.larsdenbakker.datapath.DataPathResolveException;

/**
 *
 * @author Lars den Bakker<larsdenbakker@gmail.com>
 */
public class DataPathConverter extends DataConversionOverride {

   private final DataPathModule dataPathHandler;

   public DataPathConverter(DataPathModule dataPathHandler) {
      this.dataPathHandler = dataPathHandler;
   }

   @Override
   protected <T> T _convert(Object input, Class<T> type) {
      if (input instanceof String) {
         String stringInput = (String) input;
         if (stringInput.startsWith(".")) {
            try {
               Object obj = dataPathHandler.resolveDataPath(stringInput);
               if (obj != null && type.isAssignableFrom(obj.getClass())) {
                  return (T) obj;
               }
            } catch (DataPathResolveException ex) {
            }
         }
      }
      return null;
   }

}
