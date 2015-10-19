package nl.larsdenbakker.operation;

import nl.larsdenbakker.app.Module;
import nl.larsdenbakker.operation.template.OperationTemplate;
import nl.larsdenbakker.registry.RegisterableRegistry;
import nl.larsdenbakker.registry.RegistryModule;

/**
 * A Registry of Operations. See Operation for more information.
 *
 * @author Lars den Bakker <larsdenbakker at gmail.com>
 */
public class OperationRegistry extends RegisterableRegistry<String, OperationTemplate> {

   public OperationRegistry(Module parentModule, RegistryModule registryHandler) {
      super(parentModule, registryHandler, String.class, OperationTemplate.class);
   }

   @Override
   public String getPluralDataValueDescription() {
      return "Operations";
   }

   @Override
   public String getDataValueDescription() {
      return "Operation";
   }

   @Override
   public String getKey() {
      return "operations";
   }

}
