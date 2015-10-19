package nl.larsdenbakker.datapath;

import java.util.Map;
import nl.larsdenbakker.conversion.ConversionModule;
import nl.larsdenbakker.serialization.DataSerializable;
import nl.larsdenbakker.util.Describable;

/**
 * Class used as abstraction between different classes that 'hold data'
 * that can be referenced to by keys. These keys must be able to be constructed
 * from String value.
 *
 * @author Lars den Bakker<larsdenbakker@gmail.com>
 */
public interface DataHolder extends DataSerializable, Describable {

   public ConversionModule getConversionModule();

   /**
    * Get the data from this DataHolder that is references to by the given key.
    *
    * @param key The key.
    *
    * @return The date, or null if it was not found.
    */
   public Object getDataValue(Object key);

   /**
    * Convert a String value to a key object for this DataHolder, if possible.
    *
    * @param stringKey The String key.
    *
    * @return The key value.
    */
   public Object convertToKey(String stringKey);

   /**
    * Get the contents of this DataHolder as String-Object mappings.
    *
    * @return
    */
   public Map<String, Object> getContents();

   /**
    * @return A user friendly description of the registered data.
    */
   public String getDataValueDescription();

   @Override
   public default Object toSerializable() {
      return getContents();
   }

}
