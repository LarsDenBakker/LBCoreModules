package nl.larsdenbakker.property;

import nl.larsdenbakker.util.InitializationException;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import nl.larsdenbakker.app.Module;
import nl.larsdenbakker.conversion.ConversionException;
import nl.larsdenbakker.conversion.ConversionModule;
import nl.larsdenbakker.datafile.DataFile;
import nl.larsdenbakker.datafile.DataFileException;
import nl.larsdenbakker.datafile.DataFileModule;
import nl.larsdenbakker.datafile.DataFormat;
import nl.larsdenbakker.property.properties.Properties;
import nl.larsdenbakker.property.properties.Property;
import nl.larsdenbakker.property.properties.PropertyModificationException;
import nl.larsdenbakker.registry.RegisterableRegistry;
import nl.larsdenbakker.registry.RegistryModule;
import nl.larsdenbakker.storage.MemoryStorage;
import nl.larsdenbakker.storage.Storage;
import nl.larsdenbakker.util.ClassUtils;
import static nl.larsdenbakker.util.Message.Type.INFO;
import nl.larsdenbakker.util.TextUtils;

/**
 * A Registry for PropertyHolders with a few utility methods for creating new
 * PropertyHolders.
 *
 * @author Lars den Bakker <larsdenbakker at gmail.com>
 */
public abstract class PropertyHolderRegistry<K, V extends PropertyHolder<K>> extends RegisterableRegistry<K, V> {

   public PropertyHolderRegistry(Module parentModule, RegistryModule registryHandler, Class<K> keyType, Class<V> valueType, DataFile dataFile) {
      super(parentModule, registryHandler, keyType, valueType, dataFile);
   }

   public PropertyHolderRegistry(Module parentModule, RegistryModule registryHandler, Class<K> keyType, Class<V> valueType) {
      super(parentModule, registryHandler, keyType, valueType);
   }

   public abstract Properties getProperties();

   /**
    * Create and register a new PropertyHolder of this PropertyHolderRegistry's
    * PropertyHolder type with the given arguments for the PropertyHolder's
    * constructor properties.
    *
    * @param arguments Arguments to pass to the constructor properties of the PropertyHolder.
    *
    * @return The newly created PropertyHolder. Never null.
    *
    * @throws PropertyHolderCreationException if anything goes wrong during creation.
    */
   public V createAndRegister(Object... arguments) throws PropertyHolderCreationException {
      try {
         ConversionModule conversionModule = getConversionModule();
         Properties properties = getProperties();

         //Create an instance of V
         V v = ClassUtils.getNewInstance(getValueType(), getParentModule(), getConversionModule(), MemoryStorage.create(conversionModule), properties);

         //Check if there are any constructor properties and if there are enough arguments given.
         Map<Property, Object> constructorParameters = new HashMap();
         List<Property<?>> constructorProperties = properties.getConstructorParameterProperties();
         if (arguments.length < constructorProperties.size()) {
            String error = "Incorrect constructor argument count. "
                           + constructorParameters.size()
                           + " arguments are required for Properties: "
                           + TextUtils.concatenateWith(constructorProperties, ", ");
            throw new PropertyHolderCreationException(error).addFailedAction("Creating a " + getDataValueDescription() + ".");
         }

         //Convert arguments to correct type for the constructor properties
         for (int i = 0; i < constructorProperties.size(); i++) {
            Property<?> property = constructorProperties.get(i);
            try {
               Object convertedArgument = conversionModule.convert(arguments[i], property.getPropertyValueClass());
               constructorParameters.put(property, convertedArgument);
            } catch (ConversionException ex) {
               throw new PropertyHolderCreationException(ex.getMessage()).addFailedAction("Creating a " + getDataValueDescription() + ".");
            }
         }

         //Set the constructor properties
         for (Entry<Property, Object> entry : constructorParameters.entrySet()) {
            try {
               v.setPropertyValue(entry.getKey(), entry.getValue());
            } catch (PropertyModificationException ex) {
               throw new PropertyHolderCreationException(ex.getMessage()).addFailedAction("Creating a " + getDataValueDescription() + ".");
            }
         }

         try {
            //Initialize and register if successful
            v.initialize();
         } catch (InitializationException ex) {
            throw new PropertyHolderCreationException(ex.getMessage()).addFailedAction("Creating a " + getDataValueDescription() + ".");
         }
         register(v);
         return v;
      } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
         throw new IllegalArgumentException("Could not create PropertyHolder of type " + getDataValueDescription() + ". Misconfigured PropertyHolder class?", ex);
      }
   }

   /**
    * Load the contents of the given DataFile, create PropertyHolders associated
    * with this Registry from the DataFile's contents and register them to this
    * Registry.
    *
    * @param dataFile The given DataFile.
    *
    * @throws PropertyHolderCreationException if anything goes wrong creating
    *                                         the PropertyHolders.
    * @throws DataFileException               if anything goes wrong loading the DataFile.
    */
   public void createAndRegisterContents(DataFile dataFile) throws PropertyHolderCreationException, DataFileException {
      Map<String, Object> map = dataFile.load();
      Storage storage = MemoryStorage.create(getConversionModule(), map);
      createAndRegisterContents(storage);
   }

   /**
    * Load the contents of the DataFile associated with this Registry, create
    * PropertyHolders associated with this Registry from the DataFile's contents
    * and register them to this Registry. Registry must have an associated
    * DataFile.
    *
    * @throws PropertyHolderCreationException if anything goes wrong creating
    *                                         the PropertyHolders.
    * @throws DataFileException               if anything goes wrong loading the DataFile.
    * @throws IllegalStateException           if Registry has no associated DataFile.
    */
   public void loadDataFile() throws PropertyHolderCreationException, DataFileException {
      DataFile dataFile = getDataFile();
      if (dataFile != null) {
         PropertyHolderRegistry.this.createAndRegisterContents(dataFile);
      } else {
         throw new IllegalStateException("Cannot load DataFile for a "
                                         + "Registry that has no associated DataFile.");
      }
   }

   protected void createAndRegisterContents(Storage mainStorage) throws PropertyHolderCreationException {
      boolean allSucceeded = true;
      int count = 0;
      for (Storage storage : mainStorage.getNodes()) {
         PropertyHolderRegistry.this.createAndRegister(storage);
      }
      getConsole().message(INFO, "Registered " + count + " " + getPluralDataValueDescription() + ".");
   }

   protected V createAndRegister(Storage storage) throws PropertyHolderCreationException {
      try {
         V v = ClassUtils.getNewInstance(getValueType(), getParentModule(), getConversionModule(), storage, getProperties());
         v.initialize();
         register(v);
         return v;
      } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
         throw new IllegalStateException("Unable to create instance of " + getDataValueDescription() + ".", ex);
      } catch (InitializationException ex) {
         throw new PropertyHolderCreationException(ex.getMessage()).addFailedAction("Creating a " + getDataValueDescription() + ".");
      }
   }

   @Override
   public Object toSerializable() {
      Map<K, Map<String, Object>> serializedPropertyHolders = new HashMap();
      for (PropertyHolder<K> val : getAll()) {
         serializedPropertyHolders.put(val.getKey(), val.getStorage().getContents());
      }
      return serializedPropertyHolders;
   }

   /**
    * Create an instance of the given PropertyHolderRegistry and register it to
    * the RootRegistry of the given RegistryModule. A DataFile is created and
    * loaded from the given fileName and the default data path for the given
    * module.
    *
    * @param <R>            The PropertyHolderRegistry type.
    * @param registryType   The PropertyHolderRegistry type class.
    * @param module         The module.
    * @param registryModule The RegistryModule.
    * @param dataFileModule The DataFileModule.
    * @param properties     The Properties for PropertyHolderRegistry R.
    * @param fileName       The file name of the PropertyHolderRegistry R's data.
    *
    * @return The non-null PropertyHolderRegistry R.
    *
    * @throws DataFileException               if there are issues loading the DataFile.
    * @throws PropertyHolderCreationException if there are issues creating
    *                                         PropertyHolders from the DataFile.
    */
   public static <R extends PropertyHolderRegistry> R createAndInitializeRegistry(Class<R> registryType, Module module, RegistryModule registryModule, DataFileModule dataFileModule, Properties properties, String fileName) throws DataFileException, PropertyHolderCreationException {
      try {
         ConversionModule conversionModule = registryModule.getConversionModule();
         DataFile dataFile = dataFileModule.createDataFile(new File(module.getModuleDataFolder() + File.separator + fileName), DataFormat.YAML);
         R registry = ClassUtils.getNewInstance(registryType, module, registryModule, properties, dataFile);
         registryModule.getRootRegistry().register(registry);
         registry.loadDataFile();
         return registry;
      } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
         throw new IllegalArgumentException(ex);
      }
   }

}
