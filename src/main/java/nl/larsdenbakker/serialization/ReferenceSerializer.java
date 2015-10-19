package nl.larsdenbakker.serialization;

import java.lang.ref.Reference;
import nl.larsdenbakker.conversion.ConversionException;

/**
 *
 * @author Lars den Bakker <larsdenbakker at gmail.com>
 */
public class ReferenceSerializer extends DataSerializer<Reference> {

   public ReferenceSerializer() {
      super(Reference.class);
   }

   @Override
   protected Object _toSerializable(Reference input) throws ConversionException {
      return input.get();
   }

}
