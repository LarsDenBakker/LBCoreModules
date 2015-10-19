package nl.larsdenbakker.configuration;

import nl.larsdenbakker.app.AbstractModule;
import nl.larsdenbakker.app.Application;
import nl.larsdenbakker.app.Module;
import nl.larsdenbakker.registry.RegistryModule;
import nl.larsdenbakker.util.CollectionUtils;
import nl.larsdenbakker.app.UserInputException;

/**
 * An Application Module that handles ConfigurationTemplates.
 * Depends on RegistryModule.
 *
 * @author Lars den Bakker <larsdenbakker at gmail.com>
 */
public class ConfigurationModule extends AbstractModule {

   private ConfigurationTemplateRegistry templateRegistry;

   public ConfigurationModule(Application app) {
      super(app);
   }

   @Override
   public Class<? extends Module>[] getDependencies() {
      return CollectionUtils.asArray(RegistryModule.class);
   }

   public RegistryModule getRegistryModule() {
      return getParentApplication().getModule(RegistryModule.class);
   }

   @Override
   protected void _load() throws UserInputException {
      RegistryModule registryModule = getRegistryModule();
      templateRegistry = new ConfigurationTemplateRegistry(this, registryModule);
      registryModule.getRootRegistry().register(templateRegistry);
   }

   @Override
   protected void _unload() {
      templateRegistry = null;
   }

   @Override
   public void onUnloadOf(Module module) {
      templateRegistry.unregisterByModule(module);
   }

   public ConfigurationTemplateRegistry getTemplateRegistry() {
      return templateRegistry;
   }

   @Override
   public String getName() {
      return "configuration";
   }

}
