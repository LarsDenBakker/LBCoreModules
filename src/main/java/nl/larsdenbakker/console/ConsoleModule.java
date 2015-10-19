package nl.larsdenbakker.console;

import nl.larsdenbakker.operation.command.CommandTargetProvider;
import java.util.Scanner;
import nl.larsdenbakker.app.AbstractModule;
import nl.larsdenbakker.app.Application;
import nl.larsdenbakker.app.Console;
import nl.larsdenbakker.app.Module;
import nl.larsdenbakker.console.operations.ApplicationSaveOperation;
import nl.larsdenbakker.console.operations.ApplicationShutdownOperation;
import nl.larsdenbakker.console.operations.ModuleLoadOperation;
import nl.larsdenbakker.conversion.ConversionModule;
import nl.larsdenbakker.operation.OperationFactory;
import nl.larsdenbakker.operation.OperationModule;
import nl.larsdenbakker.operation.command.CommandFactory;
import nl.larsdenbakker.util.CollectionUtils;
import static nl.larsdenbakker.util.Message.Type.ERROR;
import static nl.larsdenbakker.util.Message.Type.INFO;
import static nl.larsdenbakker.util.Message.Type.RAW;
import nl.larsdenbakker.util.OperationResponse;
import nl.larsdenbakker.util.TextUtils;
import nl.larsdenbakker.app.UserInputException;
import nl.larsdenbakker.operation.OperationExecutor;

/**
 * Module to provide a command console for users to send commands
 * to an OperationModule to view and manipulate the Application's
 * data.
 *
 * @author Lars den Bakker<larsdenbakker@gmail.com>
 */
public class ConsoleModule extends AbstractModule {

   private final Scanner scanner = new Scanner(System.in);
   private final Console console;

   private boolean active = false;

   public ConsoleModule(Application app, Console console) {
      super(app);
      this.console = console;
   }

   public ConsoleModule(Application app) {
      super(app);
      this.console = getParentApplication().getConsole();
   }

   @Override
   public String getName() {
      return "console";
   }

   @Override
   public Class<? extends Module>[] getDependencies() {
      return CollectionUtils.asArray(OperationModule.class, ConversionModule.class);
   }

   public OperationModule getOperationModule() {
      return getParentApplication().getModule(OperationModule.class);
   }

   public ConversionModule getConversionModule() {
      return getParentApplication().getModule(ConversionModule.class);
   }

   @Override
   protected void _shutdown() {
      deactivate();
   }

   @Override
   protected void _load() throws UserInputException {
      OperationModule operationModule = getOperationModule();

      OperationFactory.registerOperations(this, operationModule, "shutdown-application", ApplicationShutdownOperation.class);
      OperationFactory.registerOperations(this, operationModule, "save-application", ApplicationSaveOperation.class);
      OperationFactory.registerOperations(this, operationModule, "load-module", ModuleLoadOperation.class);

      operationModule.registerCommandArgumentProvider(new CommandTargetProvider(this), "quit");
      operationModule.registerCommandArgumentProvider(new CommandTargetProvider(getParentApplication()), "load");

      CommandFactory.registerCommands(this, operationModule, "commands");
   }

   @Override
   protected void _unload() {
      deactivate();
   }

   /**
    * Activate the console. Any commands sent through the command line will
    * be processed, sent to the OperationModule and the response returned.
    */
   public void activate() {
      console.message(INFO, "Console activated.");
      console.message(RAW, "");
      active = true;
      while (active) {
         String command = scanner.nextLine();
         if (command != null && !command.isEmpty()) {
            console.message(INFO, "Executing command: " + command + "...");
            long start = System.currentTimeMillis();
            OperationResponse result = getOperationModule().getOperationExecutor().executeCommand(console, TextUtils.splitOnSpacesExceptInQuotes(command));
            if (result.hasSucceeded()) {
               console.message(INFO, result.getMessages());
            } else {
               console.message(ERROR, result.getMessages());
            }
            console.message(INFO, "Finished in " + (System.currentTimeMillis() - start) + " ms.");
            System.out.println();
         }
      }
   }

   /**
    * Deactivate the console.
    */
   public void deactivate() {
      active = false;
      console.message(INFO, "Console deactivated.");
   }

}
