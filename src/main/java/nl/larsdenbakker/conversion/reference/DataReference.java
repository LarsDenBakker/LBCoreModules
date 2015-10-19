package nl.larsdenbakker.conversion.reference;

import nl.larsdenbakker.util.Describable;
import nl.larsdenbakker.util.TextUtils;

/**
 * Wrapper class to maintain a reference to data that may or may not exist at
 * a given moment. Typically the actually data is not stored inside the wrapper
 * but the wrapper knows where to find it, giving the ability for data to be
 * deleted safely without rogue references staying around throughout the code.
 *
 * @author Lars den Bakker <larsdenbakker at gmail.com>
 */
public interface DataReference<V> extends Describable {

   /**
    * Get the DataValue this reference is referencing to.
    * Returns null if the value does not exist.
    */
   public V getDataValue();

   @Override
   public default String getTypeDescription() {
      return TextUtils.getTypeDescription(getDataValue());
   }

   @Override
   public default String getDescription() {
      return TextUtils.getDescription(getDataValue());
   }

}
