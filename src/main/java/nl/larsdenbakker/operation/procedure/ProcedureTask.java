package nl.larsdenbakker.operation.procedure;

import nl.larsdenbakker.operation.OperationContext;
import nl.larsdenbakker.util.OperationResponse;

/**
 *
 * @author Lars den Bakker<larsdenbakker@gmail.com>
 */
public interface ProcedureTask {

   public OperationResponse execute(OperationContext context, boolean stopOnFailure);

}
