package nl.larsdenbakker.operation;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import nl.larsdenbakker.app.ApplicationUser;
import nl.larsdenbakker.operation.command.Command;
import nl.larsdenbakker.operation.command.CommandArgument;
import nl.larsdenbakker.operation.command.CommandArgumentProvider;
import nl.larsdenbakker.operation.operations.Operation;
import nl.larsdenbakker.operation.template.OperationTemplate;
import nl.larsdenbakker.operation.template.SimpleOperationTemplate;
import nl.larsdenbakker.util.OperationResponse;
import nl.larsdenbakker.util.TextUtils;

/**
 * Utility methods to execute commands and operations.
 *
 * @author Lars den Bakker<larsdenbakker@gmail.com>
 */
public class OperationExecutor {

   private final OperationModule operationModule;

   public OperationExecutor(OperationModule operationModule) {
      this.operationModule = operationModule;
   }

   public OperationResponse executeCommand(ApplicationUser executor, String[] args) {
      if (args.length >= 1) {
         String mainCommand = args[0];
         if (mainCommand.equalsIgnoreCase("operation")) {
            if (args.length != 1) {
               if (executor.hasRootAccess()) {
                  return executeOperation(executor, Arrays.copyOfRange(args, 1, args.length));
               } else {
                  return OperationResponse.failed("You are not allowed to use this command.");
               }
            } else {
               return OperationResponse.failed("You must provide an operation to perform. Use: operation <operation> [key=value] [key=value] [key=value]...");
            }
         } else if (mainCommand.equalsIgnoreCase("help")) {
            if (args.length == 1) {
               return executeOperation(executor, "data-info", "target=.commands");
            } else {
               return executeOperation(executor, "data-info", "target=.commands,key-filters=" + args[1]);
            }
         } else {
            Command command = operationModule.getCommandRegistry().getByKey(mainCommand);
            if (command != null) {
               return executeCommand(null, command, executor, args, 1);
            } else {
               return OperationResponse.failed("Unknown command: " + mainCommand);
            }
         }
      } else {
         return OperationResponse.failed("Empty command given.");
      }
   }

   private OperationResponse executeCommand(Command parentCommand, Command command, ApplicationUser executor, String[] args, int index) {
      if (index < args.length) {
         String firstArgument = args[index];
         Command subCommand = command.getSubcommand(firstArgument);
         if (subCommand != null) {
            return executeCommand(command, subCommand, executor, args, index + 1);
         }
      }
      if (command.getOperation() != null) {
         if (args.length - index < command.getRequiredArgumentsCount()) {
            return OperationResponse.failed("Incorrect argument count. Use: " + command.listArguments());

         }
         Map<String, Object> arguments = new HashMap();
         List<CommandArgument> argumentsMappings = command.getArgumentsMappings();
         for (int i = index; i < args.length; i++) {
            int argumentIndex = i - index;
            if (argumentIndex < argumentsMappings.size()) {
               addCommandArgument(arguments, argumentsMappings.get(argumentIndex).getVariable(), args[i]);
            }
         }
         List<CommandArgumentProvider> argumentProviders = command.getArgumentProviders();
         if (argumentProviders != null) {
            for (CommandArgumentProvider argumentProvider : argumentProviders) {
               argumentProvider.onCommand(parentCommand, command, args, arguments);
            }
         }
         if (command.getVariables() != null) {
            arguments.putAll(command.getVariables());
         }
         return executeOperation(executor, command.getOperation(), arguments);
      } else {
         if (command.getSubCommands() != null && !command.getSubCommands().isEmpty()) {
            List<String> subCommands = new ArrayList();
            for (Command subCommand : command.getSubCommands().values()) {
               subCommands.add(subCommand.listArguments());
            }
            return OperationResponse.succeeded().addMessages(Lists.reverse(subCommands)).addMessages("Use: ");
         } else {
            return OperationResponse.failed("Misconfigured command.");
         }
      }
   }

   private void addCommandArgument(Map<String, Object> map, String key, Object val) {
      if (map.containsKey(key)) {
         Object presentObject = map.get(key);
         if (presentObject instanceof List) {
            List list = (List) presentObject;
            if (list.isEmpty()) {
               list.add(val);
            } else {
               Object firstObject = list.get(0);
               if (firstObject.getClass().isAssignableFrom(val.getClass())) {
                  list.add(val);
               } else {
                  throw new IllegalArgumentException("Trying to add argument at key " + key + " with value of type " + val.getClass().getSimpleName() + " to a list of type " + firstObject.getClass().getSimpleName());
               }
            }
         } else if (presentObject.getClass().isAssignableFrom(val.getClass())) {
            List list = new ArrayList();
            list.add(presentObject);
            list.add(val);
            map.put(key, list);
         } else {
            throw new IllegalArgumentException("Trying to add argument at key " + key + " with value of type " + val.getClass().getSimpleName() + " to a list of type " + presentObject.getClass().getSimpleName());
         }
      } else {
         map.put(key, val);
      }
   }

   public OperationResponse executeOperation(ApplicationUser executor, String rawCommand) {
      checkNotNull(executor);
      checkNotNull(rawCommand);
      checkArgument(!rawCommand.isEmpty());

      String[] splitCommand = TextUtils.splitOnSpacesExceptInQuotes(rawCommand);

      OperationResponse templateResponse = OperationResponse.succeeded();
      OperationTemplate template = getTemplateFromInput(templateResponse, splitCommand[0]);
      if (templateResponse.hasSucceeded()) {
         if (splitCommand.length != 1) {
            OperationResponse variablesResponse = OperationResponse.succeeded();
            Map<String, Object> mappedVariables = getVariablesFromInput(variablesResponse, splitCommand, 1);
            if (variablesResponse.hasSucceeded()) {
               return executeOperation(executor, template, mappedVariables);
            } else {
               return variablesResponse;
            }
         } else {
            return executeOperation(executor, template);
         }
      } else {
         return templateResponse;
      }
   }

   public OperationResponse executeOperation(ApplicationUser executor, String args[]) {
      checkNotNull(executor);
      checkNotNull(args);
      checkArgument(args.length != 0);

      OperationResponse templateResponse = OperationResponse.succeeded();
      OperationTemplate template = getTemplateFromInput(templateResponse, args[0]);
      if (templateResponse.hasSucceeded()) {
         if (args.length != 1) {
            OperationResponse variablesResponse = OperationResponse.succeeded();
            Map<String, Object> variables = getVariablesFromInput(variablesResponse, args, 1);
            if (variablesResponse.hasSucceeded()) {
               return executeOperation(executor, template, variables);
            } else {
               return variablesResponse;
            }
         } else {
            return executeOperation(executor, template);
         }
      } else {
         return templateResponse;
      }
   }

   public OperationResponse executeOperation(ApplicationUser executor, String operation, String variables) {
      checkNotNull(executor);
      checkNotNull(operation);
      checkNotNull(variables);
      checkArgument(!operation.isEmpty());
      checkArgument(!variables.isEmpty());

      OperationResponse templateResponse = OperationResponse.succeeded();
      OperationTemplate template = getTemplateFromInput(templateResponse, operation);
      if (templateResponse.hasSucceeded()) {
         OperationResponse variablesResponse = OperationResponse.succeeded();
         Map<String, Object> mappedVariables = getVariablesFromInput(variablesResponse, TextUtils.splitOnSpacesExceptInQuotes(variables), 0);
         if (variablesResponse.hasSucceeded()) {
            return executeOperation(executor, template, mappedVariables);
         } else {
            return variablesResponse;
         }
      } else {
         return templateResponse;
      }
   }

   public OperationResponse executeOperation(ApplicationUser executor, String operation, Map<String, Object> variables) {
      checkNotNull(executor);
      checkNotNull(operation);
      checkNotNull(variables);
      checkArgument(!operation.isEmpty());

      OperationResponse response = OperationResponse.succeeded();
      OperationTemplate template = getTemplateFromInput(response, operation);
      if (response.hasSucceeded()) {
         return executeOperation(executor, template, variables);
      } else {
         return response;
      }
   }

   public OperationResponse executeOperation(ApplicationUser executor, OperationTemplate operation, String variables) {
      checkNotNull(executor);
      checkNotNull(operation);
      checkNotNull(variables);
      checkArgument(!variables.isEmpty());

      OperationResponse response = OperationResponse.succeeded();
      Map<String, Object> mappedVariables = getVariablesFromInput(response, TextUtils.splitOnSpacesExceptInQuotes(variables), 0);
      if (response.hasSucceeded()) {
         return executeOperation(executor, operation, mappedVariables);
      } else {
         return response;
      }
   }

   public OperationResponse executeOperation(ApplicationUser executor, OperationTemplate operation, Map<String, Object> variables) {
      checkNotNull(executor);
      checkNotNull(operation);
      checkNotNull(variables);
      OperationContextRoot context = new OperationContextRoot(operationModule, executor, operation, variables);
      return context.execute();
   }

   public OperationResponse executeOperation(ApplicationUser executor, OperationTemplate operation) {
      checkNotNull(executor);
      checkNotNull(operation);

      OperationContextRoot context = new OperationContextRoot(operationModule, executor, operation, new HashMap());
      return context.execute();
   }

   public OperationResponse executeOperation(ApplicationUser executor, Class<? extends Operation> operation, Map<String, Object> variables) {
      checkNotNull(executor);
      checkNotNull(operation);

      for (OperationTemplate template : operationModule.getOperationRegistry().getAll()) {
         if (template instanceof SimpleOperationTemplate) {
            SimpleOperationTemplate baseTemplate = (SimpleOperationTemplate) template;
            if (baseTemplate.getOperationClass().equals(operation)) {
               OperationContextRoot context = new OperationContextRoot(operationModule, executor, template, variables);
               return context.execute();
            }
         }
      }

      throw new IllegalArgumentException("Unknown operation: " + operation);
   }

   public OperationResponse executeOperation(ApplicationUser executor, Class<? extends Operation> operation) {
      return executeOperation(executor, operation, new HashMap());
   }

   public OperationResponse executeOperation(OperationTemplate operation, Map<String, Object> variables) {
      return executeOperation(operationModule.getParentApplication().getConsole(), operation, variables);
   }

   public OperationResponse executeOperation(OperationTemplate operation) {
      return executeOperation(operationModule.getParentApplication().getConsole(), operation);

   }

   public OperationResponse executeOperation(Class<? extends Operation> operation, Map<String, Object> variables) {
      return executeOperation(operationModule.getParentApplication().getConsole(), operation, variables);
   }

   public OperationResponse executeOperation(Class<? extends Operation> operation) {
      return executeOperation(operationModule.getParentApplication().getConsole(), operation);
   }

   private OperationTemplate getTemplateFromInput(OperationResponse response, String input) {
      OperationTemplate template = operationModule.getOperationRegistry().getByKey(input);
      if (template != null) {
         return template;
      } else {
         response.setSucceeded(false).addMessages("Could not find operation: " + input);
         return null;
      }
   }

   private Map<String, Object> getVariablesFromInput(OperationResponse response, String[] input, int start) {
      Map<String, Object> variables = new HashMap();
      for (int i = start; i < input.length; i++) {
         String str = input[i];
         String[] pairArray = str.split("=");
         if (pairArray.length == 2) {
            variables.put(pairArray[0], pairArray[1]);
         } else {
            response.setSucceeded(false).addMessages("Incorrect variable mapping: " + str + ". Mapping should be: 'key=value'.");
            return null;
         }
      }
      return variables;
   }

}
