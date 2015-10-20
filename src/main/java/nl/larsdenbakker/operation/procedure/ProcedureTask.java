package nl.larsdenbakker.operation.procedure;

import nl.larsdenbakker.operation.OperationContext;
import nl.larsdenbakker.util.OperationResponse;

/**
 * A task that is to be executed by a Procedure.
 *
 * @author Lars den Bakker<larsdenbakker@gmail.com>
 */
public interface ProcedureTask {

   /**
    * Execute this Procedure Task
    *
    * @param context       The context of execution.
    * @param stopOnFailure Whether or not execution should stop if one of the sub-operations fails.
    *
    * @return The response of the operation.
    */
   public OperationResponse execute(OperationContext context, boolean stopOnFailure);

}
