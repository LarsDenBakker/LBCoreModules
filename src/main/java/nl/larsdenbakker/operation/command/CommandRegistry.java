package nl.larsdenbakker.operation.command;

import nl.larsdenbakker.app.Module;
import nl.larsdenbakker.registry.RegisterableRegistry;
import nl.larsdenbakker.registry.RegistryModule;

/**
 *
 * @author Lars den Bakker <larsdenbakker at gmail.com>
 */
public class CommandRegistry extends RegisterableRegistry<String, Command> {

   public CommandRegistry(Module parentModule, RegistryModule registryHandler) {
      super(parentModule, registryHandler, String.class, Command.class);
   }

   @Override
   public String getKey() {
      return "commands";
   }

   @Override
   public String getDataValueDescription() {
      return "Command";
   }

   @Override
   public String getPluralDataValueDescription() {
      return "Commands";
   }

}
