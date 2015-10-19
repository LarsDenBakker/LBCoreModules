package nl.larsdenbakker.registry;

import nl.larsdenbakker.conversion.ConversionException;
import nl.larsdenbakker.conversion.converters.SuperTypeDataConverter;
import static com.google.common.base.Preconditions.checkNotNull;
import nl.larsdenbakker.datapath.DataPathResolveException;

/**
 * A SuperTypeDataConverter that handles types registered to a Registry.
 *
 * @author Lars den Bakker <larsdenbakker at gmail.com>
 */
public class RegistryConverter<T> extends SuperTypeDataConverter<T> {

   private final Registry registry;

   public RegistryConverter(Registry registry) {
      super(registry.getValueType());
      this.registry = registry;
   }

   @Override
   protected <A extends T> A _convert(Object input, Class<A> subClass) throws ConversionException {
      checkNotNull(input, subClass);
      //If we are already at the registry we need to be
      if (registry.getValueType().equals(getSuperClass())) {
         Object val = registry.getDataValue(input);
         if (val != null) {
            if (subClass.isAssignableFrom(val.getClass())) {
               return (A) val;
            }
         }
      }

      //Else convert it to a string and getDataValue it by path
      String path = registry.getConversionModule().convert(input, String.class);
      Object obj;
      if (path.startsWith(".")) {
         try {
            obj = registry.getRegistryModule().getDataPathModule().resolveDataPath(path);
         } catch (DataPathResolveException ex) {
            throw new ConversionException(ex.getMessage());
         }
      } else {
         try {
            obj = registry.getRegistryModule().getDataPathModule().resolveDataPath(registry, path);
         } catch (DataPathResolveException ex) {
            throw new ConversionException(ex.getMessage());
         }
      }
      if (subClass.isAssignableFrom(obj.getClass())) {
         return (A) obj;
      } else {
         throw new ConversionException("Unable to convert type " + input.getClass() + " to " + subClass + " in SuperConverter of super type " + getSuperClass());
      }

   }
}
