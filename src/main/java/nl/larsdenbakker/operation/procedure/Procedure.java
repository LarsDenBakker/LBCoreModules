package nl.larsdenbakker.operation.procedure;

import nl.larsdenbakker.storage.Storage;
import nl.larsdenbakker.operation.operations.Operation;
import nl.larsdenbakker.operation.OperationContext;
import nl.larsdenbakker.app.InvalidInputException;
import nl.larsdenbakker.util.OperationResponse;

/**
 *
 * @author Lars den Bakker<larsdenbakker@gmail.com>
 */
public class Procedure extends Operation {
   
   private final ProcedureTask[] procedureTasks;
   
   public Procedure(OperationContext context, Storage storage, ProcedureTask[] procedureTasks) throws InvalidInputException {
      super(context, storage);
      this.procedureTasks = procedureTasks;
   }
   
   @Override
   protected OperationResponse _execute() {      
      for (ProcedureTask procedureTask : procedureTasks) {
         procedureTask.execute(getContext(), false);
      }
      return OperationResponse.succeeded();
   }
   
}
