package nl.larsdenbakker.storage;

import java.util.HashMap;
import java.util.Map;
import nl.larsdenbakker.conversion.ConversionModule;

/**
 *
 * @author Lars den Bakker <larsdenbakker at gmail.com>
 */
public class MemoryStorageRoot extends MemoryStorage {

   private final ConversionModule conversionHandler;

   protected MemoryStorageRoot(ConversionModule conversionHandler, String name, Map<String, Object> map) {
      super(name, map);
      this.conversionHandler = conversionHandler;
   }

   protected MemoryStorageRoot(ConversionModule conversionHandler, String name) {
      this(conversionHandler, name, new HashMap());
   }

   @Override
   public ConversionModule getConversionModule() {
      return conversionHandler;
   }

   @Override
   public Storage getRoot() {
      return this;
   }

   @Override
   public Storage getParent() {
      return null;
   }

}
