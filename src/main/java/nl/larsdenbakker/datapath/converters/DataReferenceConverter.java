package nl.larsdenbakker.datapath.converters;

import nl.larsdenbakker.conversion.ConversionException;
import nl.larsdenbakker.conversion.converters.DataConverter;
import nl.larsdenbakker.datapath.DataPathModule;
import nl.larsdenbakker.conversion.reference.DataReference;
import nl.larsdenbakker.datapath.DataPathNode;

/**
 *
 * @author Lars den Bakker <larsdenbakker at gmail.com>
 */
public class DataReferenceConverter extends DataConverter<DataReference> {

   private final DataPathModule module;

   public DataReferenceConverter(DataPathModule module) {
      super(DataReference.class);
      this.module = module;
   }

   @Override
   protected DataReference _convert(Object input) throws ConversionException {
      String path = input.toString();
      if (path.startsWith(".") && path.length() >= 2 && path.charAt(1) != ' ') {
         return DataPathNode.of(module, path);
      } else {
         throw new ConversionException("Cannot convert path: " + path + " to data path reference.");
      }
   }

}
