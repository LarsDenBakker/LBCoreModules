package nl.larsdenbakker.datapath;

import java.lang.ref.Reference;
import nl.larsdenbakker.conversion.reference.DataReference;

/**
 * A helper class for implementations of DataHolder.
 *
 * @author Lars den Bakker <larsdenbakker at gmail.com>
 */
public abstract class AbstractDataHolder implements DataHolder {

   protected abstract Object _getDataValue(Object key);

   @Override
   public Object getDataValue(Object key) {
      Object obj = _getDataValue(key);
      if (obj != null) {
         if (obj instanceof Reference) {
            return ((Reference) obj).get();
         } else if (obj instanceof DataReference) {
            return ((DataReference) obj).getDataValue();
         }
      }
      return obj;
   }

}
