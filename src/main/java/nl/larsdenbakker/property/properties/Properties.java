package nl.larsdenbakker.property.properties;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import nl.larsdenbakker.app.Module;
import nl.larsdenbakker.conversion.ConversionModule;
import nl.larsdenbakker.datafile.DataFileException;
import nl.larsdenbakker.datafile.DataFormat;
import nl.larsdenbakker.property.PropertyModule;
import nl.larsdenbakker.storage.MemoryStorage;
import nl.larsdenbakker.storage.Storage;
import nl.larsdenbakker.datafile.DataFileUtils;

/**
 * A grouping of Properties belonging to a PropertyHolder type. Only one instance of
 * each of a Properties subtype is created and the same instance is registered
 * to all PropertyHolders of the associated type.
 *
 * @author Lars den Bakker <larsdenbakker at gmail.com>
 */
public abstract class Properties {

   public static final String KEY_PROPERTIES = "properties";

   private final PropertyModule propertyModule;
   private final Property<?>[] properties;

   /**
    * Construct an instance of Properties.
    *
    * @param propertyModule       The associated Property Module.
    * @param parent               The parent Properties object registered to the parent of the
    *                             associated PropertyHolder.
    * @param predefinedProperties The Properties for this Properties object.
    */
   public Properties(PropertyModule propertyModule, Properties parent, Property<?>... predefinedProperties) {
      this.propertyModule = propertyModule;
      List<Property<?>> list = new ArrayList();

      if (parent != null) {
         for (Property<?> p : parent.getAll()) {
            list.add(p);
         }
      }

      if (predefinedProperties != null) {
         for (Property<?> p : predefinedProperties) {
            list.add(p);
         }
      }
      this.properties = list.toArray(new Property<?>[list.size()]);
   }

   public Properties(PropertyModule propertyModule, Property<?>... predefinedProperties) {
      this(propertyModule, null, predefinedProperties);
   }

   public PropertyModule getPropertyModule() {
      return propertyModule;
   }

   public final Property<?>[] getAll() {
      return properties;
   }

   public final Property<?> getProperty(String key) {
      for (Property<?> prop : properties) {
         if (prop.getKey().equalsIgnoreCase(key)) {
            return prop;
         }
      }
      return null;
   }

   public boolean hasProperty(Property prop) {
      for (Property<?> p : properties) {
         if (p.equals(prop)) {
            return true;
         }
      }
      return false;
   }

   /**
    * @return All Properties that are required to construct a new PropertyHolder
    * of the associated type.
    */
   public List<Property<?>> getConstructorParameterProperties() {
      List<Property<?>> list = new ArrayList();
      for (Property<?> p : properties) {
         if (p.isConstructorParameter()) {
            list.add(p);
         }
      }
      return list;
   }

   public static Storage getPropertiesConfiguration(ConversionModule conversionModule, Module module, String fileName) throws DataFileException {
      String resourcePath = "resources" + File.separator + module.getName() + File.separator + fileName;
      String filePath = module.getModuleConfigFolder() + File.separator + fileName;

      Map<String, Object> configuration;
      configuration = DataFileUtils.loadContents(DataFormat.YAML, new File(filePath));

      Map<String, Object> defaults;
      defaults = DataFileUtils.loadResource(DataFormat.YAML, resourcePath);
      Storage configurationStorage = (defaults != null) ? MemoryStorage.create(conversionModule, defaults) : MemoryStorage.create(conversionModule);
      if (configuration != null) {
         configurationStorage.setAll(configuration);
      }
      return configurationStorage.getStorage(KEY_PROPERTIES, true);
   }
}
