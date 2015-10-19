package nl.larsdenbakker.datapath;

import java.util.List;
import java.util.Map;
import javafx.util.Pair;
import nl.larsdenbakker.conversion.ConversionException;
import nl.larsdenbakker.conversion.ConversionModule;

/**
 * A value within the DataPath chain. See DataPathNode for more information.
 *
 * @author Lars den Bakker<larsdenbakker@gmail.com>
 */
public class DataPathValue<V> extends DataPathNode<V> {

   private final DataPathNode<?> parentReference;
   private final String stringKey;
   private Object key;

   public DataPathValue(DataPathNode<?> parentReference, String stringKey) {
      this.parentReference = parentReference;
      this.stringKey = stringKey;
   }

   @Override
   public V getDataValue() {
      if (key == null) {
         Object convertedKey = toKey(stringKey);
         if (convertedKey != null) {
            this.key = convertedKey;
         } else {
            return null;
         }
      }
      Object parentData = parentReference.getDataValue();
      Object rawValue = null;
      if (parentData instanceof DataHolder) {
         rawValue = ((DataHolder) parentData).getDataValue(key);
      } else if (parentData instanceof Map) {
         rawValue = ((Map) parentData).get(key);
      } else if (parentData instanceof List) {
         if (key instanceof Integer) {
            int i = (int) key;
            List list = (List) parentData;
            if (i >= 0 && i < list.size()) {
               rawValue = list.get(i);
            }
         }
      }
      if (rawValue != null) {
         try {
            return (V) rawValue;
         } catch (ClassCastException ex) {
            return null;
         }
      } else {
         return null;
      }
   }

   public String getStringKey() {
      return stringKey;
   }

   @Override
   public String getFullPath() {
      return parentReference.getFullPath() + '.' + getStringKey();
   }

   @Override
   protected Object toKey(String stringKey) {
      Object data = parentReference.getDataValue();
      if (data == null) {
         return null;
      } else if (data instanceof DataHolder) {
         return ((DataHolder) data).convertToKey(stringKey);
      } else if (data instanceof Map) {
         Pair<Class<?>, Class<?>> pair = getConversionModule().getCachedKeyValueTypes((Map) data);
         if (pair != null) {
            Class<?> keyClass = pair.getKey();
            try {
               return getConversionModule().convert(stringKey, keyClass);
            } catch (ConversionException ex) {
               return null;
            }
         } else {
            return null;
         }
      } else if (data instanceof List) {
         try {
            return getConversionModule().convert(stringKey, Integer.class);
         } catch (ConversionException ex) {
            return null;
         }
      } else {
         return null;
      }
   }

   @Override
   public ConversionModule getConversionModule() {
      return parentReference.getConversionModule();
   }

}
