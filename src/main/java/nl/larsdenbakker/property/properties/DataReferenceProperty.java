package nl.larsdenbakker.property.properties;

import nl.larsdenbakker.conversion.reference.DataReferencable;
import nl.larsdenbakker.storage.Storage;

/**
 *
 * @author Lars den Bakker <larsdenbakker at gmail.com>
 */
public class DataReferenceProperty<V extends DataReferencable> extends SimpleProperty<V> {

   public DataReferenceProperty(Storage storage, Class<V> propertyValueClass) {
      super(storage, propertyValueClass);
   }

}
