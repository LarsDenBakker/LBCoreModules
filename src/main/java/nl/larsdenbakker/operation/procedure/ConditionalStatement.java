package nl.larsdenbakker.operation.procedure;

import nl.larsdenbakker.operation.OperationContext;
import nl.larsdenbakker.util.OperationResponse;

/**
 * A type of ProcedureTask that consists of a series of ProcedureTasks that are executed logically.
 * First all if statements are executed, if these all match all then statements are executed - otherwise
 * all else statements are executed.
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
