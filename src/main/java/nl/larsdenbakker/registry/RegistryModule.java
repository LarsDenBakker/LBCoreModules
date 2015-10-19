package nl.larsdenbakker.registry;

import nl.larsdenbakker.app.AbstractModule;
import nl.larsdenbakker.app.Application;
import nl.larsdenbakker.app.Module;
import nl.larsdenbakker.conversion.ConversionModule;
import nl.larsdenbakker.datapath.DataHolder;
import nl.larsdenbakker.datapath.DataPathModule;
import nl.larsdenbakker.datapath.DataPathRoot;
import nl.larsdenbakker.util.CollectionUtils;
import nl.larsdenbakker.util.MapUtils;
import nl.larsdenbakker.app.UserInputException;

/**
 * A module that handles Registries and the RootRegistry. Depends on the
 * DataPathModule and ConversionModule.
 *
 * @author Lars den Bakker <larsdenbakker at gmail.com>
 */
public class RegistryModule extends AbstractModule {

   private RootRegistry rootRegistry;

   public RegistryModule(Application app) {
      super(app);
   }

   @Override
   public String getName() {
      return "registry";
   }

   @Override
   public Class<? extends Module>[] getDependencies() {
      return CollectionUtils.asArray(ConversionModule.class);
   }

   @Override
   protected void _load() throws UserInputException {
      //Verify that the DataPathModule has not been loaded yet.
      if (getParentApplication().isLoaded(DataPathModule.class)) {
         throw new IllegalStateException("DataPathModule has been loaded before RegistryModule.");
      }

      //Set RootRegistry as DataPathRoot in the DataPathModule
      rootRegistry = new RootRegistry(this);
      DataPathRootRegistry dataPathRootRegistry = new DataPathRootRegistry();
      getParentApplication().loadModule(DataPathModule.class,
                                        MapUtils.of(DataPathRoot.class,
                                                    dataPathRootRegistry));

      getConversionModule().registerSuperTypeConverter(new RegistryConverter(rootRegistry));
   }

   @Override
   protected void _unload() {
      ConversionModule conversionModule = getConversionModule();
      conversionModule.unregisterConverter(Registry.class);
      for (Registry registry : rootRegistry.getAll()) {
         conversionModule.unregisterSuperConverter(registry.getValueType());
      }
   }

   @Override
   public void onUnloadOf(Module module) {
      rootRegistry.unregisterByModule(module);
   }

   public ConversionModule getConversionModule() {
      return getParentApplication().getModule(ConversionModule.class);
   }

   public DataPathModule getDataPathModule() {
      return getParentApplication().getModule(DataPathModule.class);
   }

   public RootRegistry getRootRegistry() {
      return rootRegistry;
   }

   private class DataPathRootRegistry extends DataPathRoot {

      @Override
      public ConversionModule getConversionModule() {
         return RegistryModule.this.getConversionModule();
      }

      @Override
      public DataHolder getDataValue() {
         return rootRegistry;
      }
   }

}
