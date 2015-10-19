package nl.larsdenbakker.operation.procedure;

import nl.larsdenbakker.operation.OperationContext;
import nl.larsdenbakker.util.OperationResponse;

/**
 *
 * @author Lars den Bakker<larsdenbakker@gmail.com>
 */
public class ConditionalStatement implements ProcedureTask {

   private final ProcedureTask ifStatement;
   private final ProcedureTask thenStatement;
   private final ProcedureTask elseStatement;

   public ConditionalStatement(ProcedureTask ifStatement, ProcedureTask thenStatement, ProcedureTask elseStatement) {
      this.ifStatement = ifStatement;
      this.thenStatement = thenStatement;
      this.elseStatement = elseStatement;
   }

   @Override
   public OperationResponse execute(OperationContext context, boolean stopOnFailure) {
      OperationResponse result = ifStatement.execute(context, true);
      result = (result.hasSucceeded()) ? thenStatement.execute(context, false) : elseStatement.execute(context, false);
      return result;
   }
}
