package nl.larsdenbakker.configuration;

import nl.larsdenbakker.registry.RegisterableRegistry;
import nl.larsdenbakker.registry.RegistryModule;

/**
 *
 * @author Lars den Bakker <larsdenbakker at gmail.com>
 */
public class ConfigurationTemplateRegistry extends RegisterableRegistry<String, ConfigurationTemplate> {

   public ConfigurationTemplateRegistry(ConfigurationModule configurationModule, RegistryModule registryHandler) {
      super(configurationModule, registryHandler, String.class, ConfigurationTemplate.class);
   }

   @Override
   public String getDataValueDescription() {
      return "Configuration Template";
   }

   @Override
   public String getPluralDataValueDescription() {
      return "Configuration Templates";
   }

   @Override
   public String getKey() {
      return "configuration-templates";
   }

}
