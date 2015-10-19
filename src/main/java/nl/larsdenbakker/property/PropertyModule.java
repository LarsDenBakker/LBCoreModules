package nl.larsdenbakker.property;

import java.math.BigDecimal;
import java.util.UUID;
import nl.larsdenbakker.app.AbstractModule;
import nl.larsdenbakker.app.Application;
import nl.larsdenbakker.app.Module;
import nl.larsdenbakker.configuration.ConfigurationModule;
import nl.larsdenbakker.configuration.ConfigurationTemplateFactory;
import nl.larsdenbakker.conversion.ConversionModule;
import nl.larsdenbakker.property.operations.PropertyHolderCreationOperation;
import nl.larsdenbakker.property.operations.PropertyHolderDeletionOperation;
import nl.larsdenbakker.property.operations.PropertyModificationOperation;
import nl.larsdenbakker.property.properties.BigDecimalProperty;
import nl.larsdenbakker.property.properties.IntegerProperty;
import nl.larsdenbakker.property.properties.LongProperty;
import nl.larsdenbakker.property.properties.StringProperty;
import nl.larsdenbakker.property.properties.UUIDProperty;
import nl.larsdenbakker.registry.RegistryModule;
import nl.larsdenbakker.operation.OperationFactory;
import nl.larsdenbakker.operation.OperationModule;
import nl.larsdenbakker.util.CollectionUtils;
import nl.larsdenbakker.app.UserInputException;

/**
 * Module that deals with Property creation. See the Property class for a full
 * explanation of properties.
 *
 * This module depends on ConversionModule, RegistryModule, OperationModule and
 * ConfigurationModule.
 *
 * @author Lars den Bakker <larsdenbakker at gmail.com>
 */
public class PropertyModule extends AbstractModule {

   private PropertyFactory propertyFactory;

   public PropertyModule(Application app) {
      super(app);
   }

   @Override
   public String getName() {
      return "property";
   }

   @Override
   public Class<? extends Module>[] getDependencies() {
      return CollectionUtils.asArray(ConversionModule.class, RegistryModule.class, OperationModule.class, ConfigurationModule.class);
   }

   @Override
   protected void _load() throws UserInputException {
      ConfigurationTemplateFactory.loadAndRegisterTemplates(this, this.getConfigurationModule(), "command-templates.yml");

      propertyFactory = new PropertyFactory(this);
      propertyFactory.registerPropertyType(BigDecimal.class, BigDecimalProperty.class);
      propertyFactory.registerPropertyType(Integer.class, IntegerProperty.class);
      propertyFactory.registerPropertyType(Long.class, LongProperty.class);
      propertyFactory.registerPropertyType(String.class, StringProperty.class);
      propertyFactory.registerPropertyType(UUID.class, UUIDProperty.class);

      OperationModule operationModule = getOperationModule();
      OperationFactory.registerOperations(this, operationModule, "property-modification", PropertyModificationOperation.class);
      OperationFactory.registerOperations(this, operationModule, "property-holder-creation", PropertyHolderCreationOperation.class);
      OperationFactory.registerOperations(this, operationModule, "property-holder-deletion", PropertyHolderDeletionOperation.class);
   }

   @Override
   protected void _unload() {
      propertyFactory = null;
   }

   public ConversionModule getConversionModule() {
      return getParentApplication().getModule(ConversionModule.class);
   }

   public ConfigurationModule getConfigurationModule() {
      return getParentApplication().getModule(ConfigurationModule.class);
   }

   public RegistryModule getRegistryModule() {
      return getParentApplication().getModule(RegistryModule.class);
   }

   public OperationModule getOperationModule() {
      return getParentApplication().getModule(OperationModule.class);
   }

   public PropertyFactory getPropertyFactory() {
      return propertyFactory;
   }

}
