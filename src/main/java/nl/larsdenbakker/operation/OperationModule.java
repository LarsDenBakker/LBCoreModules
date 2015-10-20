package nl.larsdenbakker.operation;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import nl.larsdenbakker.app.AbstractModule;
import nl.larsdenbakker.app.Application;
import nl.larsdenbakker.app.Module;
import nl.larsdenbakker.configuration.ConfigurationModule;
import nl.larsdenbakker.conversion.ConversionModule;
import nl.larsdenbakker.datapath.DataPathModule;
import nl.larsdenbakker.registry.RegistryModule;
import nl.larsdenbakker.operation.command.Command;
import nl.larsdenbakker.operation.command.CommandArgumentProvider;
import nl.larsdenbakker.operation.command.CommandRegistry;
import nl.larsdenbakker.operation.operations.constraints.CollectionSizeConstraint;
import nl.larsdenbakker.operation.operations.ComparisonOperation;
import nl.larsdenbakker.operation.operations.DataHolderInfoOperation;
import nl.larsdenbakker.operation.operations.InfoOperation;
import nl.larsdenbakker.operation.operations.ElementOfOperation;
import nl.larsdenbakker.operation.operations.constraints.FilterConstraint;
import nl.larsdenbakker.operation.operations.constraints.BigDecimalSizeConstraint;
import nl.larsdenbakker.operation.operations.constraints.DoubleSizeConstraint;
import nl.larsdenbakker.operation.operations.constraints.IntegerSizeConstraint;
import nl.larsdenbakker.operation.operations.constraints.LongSizeConstraint;
import nl.larsdenbakker.operation.operations.constraints.StringLengthConstraint;
import nl.larsdenbakker.operation.operations.constraints.StringOnlyASCIIConstraint;
import nl.larsdenbakker.operation.operations.constraints.StringOnlyLettersConstraint;
import nl.larsdenbakker.util.CollectionUtils;
import nl.larsdenbakker.app.UserInputException;

/**
 * Module that deals with management and execution of Operations and Commands.
 *
 * @author Lars den Bakker <larsdenbakker at gmail.com>
 */
public class OperationModule extends AbstractModule {

   private final OperationExecutor operationExecutor = new OperationExecutor(this);
   private OperationRegistry operationRegistry;
   private CommandRegistry commandRegistry;

   public OperationModule(Application app) {
      super(app);
   }

   @Override
   public String getName() {
      return "operation";
   }

   @Override
   public Class<? extends Module>[] getDependencies() {
      return CollectionUtils.asArray(ConversionModule.class, DataPathModule.class, RegistryModule.class, ConfigurationModule.class);
   }

   @Override
   protected void _load() throws UserInputException {
      RegistryModule registryModule = getRegistryModule();
      operationRegistry = new OperationRegistry(this, registryModule);
      commandRegistry = new CommandRegistry(this, registryModule);
      registryModule.getRootRegistry().register(operationRegistry);
      registryModule.getRootRegistry().register(commandRegistry);

      OperationFactory.registerOperations(this, this, "comparison", ComparisonOperation.class);
      OperationFactory.registerOperations(this, this, "info", InfoOperation.class);
      OperationFactory.registerOperations(this, this, "data-info", DataHolderInfoOperation.class);

      OperationFactory.registerOperations(this, this, "element-of", ElementOfOperation.class);
      OperationFactory.registerOperations(this, this, "filter", FilterConstraint.class);
      OperationFactory.registerOperations(this, this, "string-length", StringLengthConstraint.class);
      OperationFactory.registerOperations(this, this, "collection-size", CollectionSizeConstraint.class);
      OperationFactory.registerOperations(this, this, "int-size", IntegerSizeConstraint.class);
      OperationFactory.registerOperations(this, this, "long-size", LongSizeConstraint.class);
      OperationFactory.registerOperations(this, this, "double-size", DoubleSizeConstraint.class);
      OperationFactory.registerOperations(this, this, "bigdecimal-size", BigDecimalSizeConstraint.class);

      OperationFactory.registerOperations(this, this, "string-only-ascii", StringOnlyASCIIConstraint.class);
      OperationFactory.registerOperations(this, this, "string-only-letters", StringOnlyLettersConstraint.class);
   }

   @Override
   protected void _unload() {
      operationRegistry = null;
      commandRegistry = null;
   }

   @Override
   public void onUnloadOf(Module module) {
      if (operationRegistry != null) {
         operationRegistry.unregisterByModule(module);
      }
      if (commandRegistry != null) {
         commandRegistry.unregisterByModule(module);
      }
   }

   public ConversionModule getConversionModule() {
      return getParentApplication().getModule(ConversionModule.class);
   }

   public RegistryModule getRegistryModule() {
      return getParentApplication().getModule(RegistryModule.class);
   }

   public DataPathModule getDataPathModule() {
      return getParentApplication().getModule(DataPathModule.class);
   }

   public ConfigurationModule getConfigurationModule() {
      return getParentApplication().getModule(ConfigurationModule.class);
   }

   public OperationRegistry getOperationRegistry() {
      return operationRegistry;
   }

   public CommandRegistry getCommandRegistry() {
      return commandRegistry;
   }

   public OperationExecutor getOperationExecutor() {
      return operationExecutor;
   }

   public boolean registerCommandArgumentProvider(CommandArgumentProvider argumentProvider, String... commands) {
      checkNotNull(argumentProvider);
      checkNotNull(commands);
      checkArgument(commands.length != 0);

      //Grab the first command from registry
      Command command = commandRegistry.getByKey(commands[0]);
      if (command != null) {
         //If not null, loop command array to find the command we're looking for
         for (int i = 1; i < commands.length; i++) {
            command = command.getSubcommand(commands[i]);
            if (command == null) {
               break;
            }
         }
      }

      //If the command we're looking for exists
      if (command != null) {
         command.addArgumentProvider(argumentProvider);
         return true;
      } else {
         return false;
      }
   }

}
