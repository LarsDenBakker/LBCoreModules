package nl.larsdenbakker.operation.procedure;

import nl.larsdenbakker.app.Module;
import nl.larsdenbakker.storage.Storage;
import nl.larsdenbakker.operation.operations.Operation;
import nl.larsdenbakker.operation.OperationContext;
import nl.larsdenbakker.operation.OperationModule;
import nl.larsdenbakker.operation.template.OperationTemplate;
import nl.larsdenbakker.operation.variables.Variable;
import nl.larsdenbakker.app.InvalidInputException;

/**
 *
 * @author Lars den Bakker<larsdenbakker@gmail.com>
 */
public class ProcedureTemplate extends OperationTemplate {

   private final ProcedureTask[] procedures;

   public ProcedureTemplate(Module parentModule, OperationModule operationModule, String name, Variable[] variables, ProcedureTask... procedures) {
      super(parentModule, operationModule, name, variables);
      this.procedures = procedures;
   }

   public ProcedureTask[] getProcedures() {
      return procedures;
   }

   @Override
   public Operation createInstance(OperationContext context, Storage storage) throws InvalidInputException {
      return new Procedure(context, storage, procedures);
   }

}
