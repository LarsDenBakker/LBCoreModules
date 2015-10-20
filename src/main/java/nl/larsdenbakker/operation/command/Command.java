package nl.larsdenbakker.operation.command;

import static com.google.common.base.Preconditions.checkNotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import nl.larsdenbakker.app.Module;
import nl.larsdenbakker.registry.Registrable;
import nl.larsdenbakker.registry.Registry;
import nl.larsdenbakker.app.ApplicationUser;

/**
 * Helper class to create shortcuts and simplify operation execution. Variables can be pre-defined
 * and optional and non-optional command arguments can be defined and mapped to variables. Review
 * the project documentation for an overview of the configuration syntax.
 *
 * This class is only used internally. Commands are created and registered through the CommandFactory
 * and executed through the CommandExecutor.
 *
 * @author Lars den Bakker <larsdenbakker at gmail.com>
 */
public class Command implements Registrable<String> {

   private final Module parentModule;
   private Command parentCommand;
   private final String key;
   private final boolean adminCommand;
   private final boolean rootCommand;
   private final String operation;
   private final List<CommandArgument> argumentsMappings;
   private final Map<String, Command> subCommands;
   private ArrayList<CommandArgumentProvider> argumentProviders;
   private final Map<String, Object> variables;

   private Registry parentRegistry;

   public Command(Module parentModule, String key, boolean adminCommand, boolean rootCommand, String operation, List<CommandArgument> argumentsMappings, Map<String, Command> subCommands, Map<String, Object> variables) {
      this.parentModule = parentModule;
      this.key = key;
      this.adminCommand = adminCommand;
      this.rootCommand = rootCommand;
      this.operation = operation;
      if (argumentsMappings == null || argumentsMappings.isEmpty()) {
         this.argumentsMappings = null;
      } else {
         this.argumentsMappings = argumentsMappings;
      }
      if (subCommands == null || subCommands.isEmpty()) {
         this.subCommands = null;
      } else {
         this.subCommands = subCommands;
         for (Command command : subCommands.values()) {
            command.setParentCommand(this);
         }
      }
      if (variables == null || variables.isEmpty()) {
         this.variables = null;
      } else {
         this.variables = variables;
      }
   }

   @Override
   public Module getParentModule() {
      return parentModule;
   }

   public Map<String, Object> getVariables() {
      return variables;
   }

   public Command getParentCommand() {
      return parentCommand;
   }

   protected void setParentCommand(Command parentCommand) {
      this.parentCommand = parentCommand;
   }

   public boolean isAdminCommand() {
      return adminCommand;
   }

   public boolean isRootCommand() {
      return rootCommand;
   }

   public List<CommandArgument> getArgumentsMappings() {
      return argumentsMappings;
   }

   public String getOperation() {
      return operation;
   }

   public Map<String, Command> getSubCommands() {
      return subCommands;
   }

   public Command getSubcommand(String command) {
      if (subCommands != null) {
         return subCommands.get(command);
      } else {
         return null;
      }
   }

   @Override
   public String getKey() {
      return key;
   }

   @Override
   public Registry getParentRegistry() {
      return parentRegistry;
   }

   @Override
   public void setParentRegistry(Registry parentRegistry) {
      this.parentRegistry = parentRegistry;
   }

   public boolean canExecute(ApplicationUser user) {
      if (rootCommand) {
         return user.hasRootAccess();
      } else if (adminCommand) {
         return user.hasAdminAccess();
      } else {
         return true;
      }
   }

   public String listArguments() {
      StringBuilder sb = new StringBuilder();
      if (parentCommand != null) {
         sb.append(parentCommand.listArguments());
      }
      sb.append(key).append(' ');
      if (argumentsMappings != null) {
         for (CommandArgument argument : argumentsMappings) {
            if (argument.isOptional()) {
               sb.append('[').append(argument.getDescription()).append("] ");
            } else {
               sb.append('<').append(argument.getDescription()).append("> ");
            }
         }
      }
      return sb.toString();
   }

   public int getRequiredArgumentsCount() {
      if (argumentsMappings != null) {
         for (int i = 0; i < argumentsMappings.size(); i++) {
            if (argumentsMappings.get(i).isOptional()) {
               return i;
            }
         }
         return argumentsMappings.size();
      }
      return 0;
   }

   public ArrayList<CommandArgumentProvider> getArgumentProviders() {
      return argumentProviders;
   }

   public void addArgumentProvider(CommandArgumentProvider argumentProvider) {
      checkNotNull(argumentProvider);
      if (argumentProviders == null) {
         argumentProviders = new ArrayList();
      }
      argumentProviders.add(argumentProvider);
      argumentProviders.trimToSize();
   }

}
