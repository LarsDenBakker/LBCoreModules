package nl.larsdenbakker.operation.template;

import java.util.Map;
import nl.larsdenbakker.app.Module;
import nl.larsdenbakker.operation.operations.Operation;
import nl.larsdenbakker.registry.Registrable;
import nl.larsdenbakker.registry.Registry;
import nl.larsdenbakker.storage.Storage;
import nl.larsdenbakker.operation.OperationContext;
import nl.larsdenbakker.operation.OperationModule;
import nl.larsdenbakker.operation.variables.Variable;
import nl.larsdenbakker.app.ApplicationUser;
import nl.larsdenbakker.app.InvalidInputException;
import nl.larsdenbakker.util.OperationResponse;

/**
 *
 * @author Lars den Bakker <larsdenbakker at gmail.com>
 */
public abstract class OperationTemplate implements Registrable<String> {

   private final Module parentModule;
   private final String name;
   private final OperationModule operationModule;
   private final Variable[] variables;

   private Registry parentRegistry;

   public OperationTemplate(Module parentModule, OperationModule operationHandler, String name, Variable[] variables) {
      this.parentModule = parentModule;
      this.operationModule = operationHandler;
      this.name = name;
      this.variables = variables;
   }

   public Variable[] getVariables() {
      return variables;
   }

   public OperationModule getOperationHandler() {
      return operationModule;
   }

   public OperationResponse executeFor(ApplicationUser executor) {
      return operationModule.getOperationExecutor().executeOperation(executor, this);
   }

   public OperationResponse executeFor(ApplicationUser executor, Map<String, Object> arguments) {
      return operationModule.getOperationExecutor().executeOperation(executor, this, arguments);
   }

   public OperationResponse execute() {
      return operationModule.getOperationExecutor().executeOperation(operationModule.getParentApplication().getConsole(), this);
   }

   public OperationResponse execute(Map<String, Object> arguments) {
      return operationModule.getOperationExecutor().executeOperation(operationModule.getParentApplication().getConsole(), this, arguments);
   }

   public abstract Operation createInstance(OperationContext context, Storage storage) throws InvalidInputException;

   @Override
   public String getKey() {
      return name;
   }

   @Override
   public Registry getParentRegistry() {
      return parentRegistry;
   }

   @Override
   public void setParentRegistry(Registry parentRegistry) {
      this.parentRegistry = parentRegistry;
   }

   @Override
   public Module getParentModule() {
      return parentModule;
   }

}
