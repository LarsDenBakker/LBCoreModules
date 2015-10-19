package nl.larsdenbakker.property.properties;

import java.util.List;
import nl.larsdenbakker.conversion.reference.DataReferenceList;
import nl.larsdenbakker.storage.Storage;

/**
 *
 * @author Lars den Bakker<larsdenbakker@gmail.com>
 */
public class DataReferenceListProperty<E> extends CollectionProperty< E, List<E>> {

   public DataReferenceListProperty(Storage storage, Class<E> collectedElementClass) {
      super(storage, (Class) List.class, (Class) DataReferenceList.class, collectedElementClass);
   }

}
