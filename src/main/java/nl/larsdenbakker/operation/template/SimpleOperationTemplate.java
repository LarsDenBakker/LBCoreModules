package nl.larsdenbakker.operation.template;

import nl.larsdenbakker.operation.operations.Operation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import nl.larsdenbakker.app.Module;
import nl.larsdenbakker.storage.Storage;
import nl.larsdenbakker.operation.OperationContext;
import nl.larsdenbakker.operation.OperationModule;
import nl.larsdenbakker.operation.variables.Variable;
import nl.larsdenbakker.app.InvalidInputException;

/**
 *
 * @author Lars den Bakker<larsdenbakker@gmail.com>
 */
public class SimpleOperationTemplate extends OperationTemplate {

   private final Class<? extends Operation> operationClass;

   public SimpleOperationTemplate(Module parentModule, OperationModule operationHandler, String name, Variable[] variables, Class<? extends Operation> operationClass) {
      super(parentModule, operationHandler, name, variables);
      this.operationClass = operationClass;
   }

   public Class<? extends Operation> getOperationClass() {
      return operationClass;
   }

   @Override
   public Operation createInstance(OperationContext context, Storage storage) throws InvalidInputException {
      try {
         Constructor<? extends Operation> constructor = operationClass.getConstructor(OperationContext.class, Storage.class);
         return constructor.newInstance(context, storage);
      } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException ex) {
         throw new IllegalArgumentException("Malformed Operation Class: " + operationClass, ex);
      } catch (InvocationTargetException ex) {
         if (ex.getCause() instanceof InvalidInputException) {
            throw (InvalidInputException) ex.getCause();
         }
         throw new IllegalArgumentException("Malformed Operation Class: " + operationClass, ex);
      }
   }

}
