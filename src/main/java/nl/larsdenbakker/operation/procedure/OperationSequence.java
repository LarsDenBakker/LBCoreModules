package nl.larsdenbakker.operation.procedure;

import nl.larsdenbakker.operation.OperationContext;
import nl.larsdenbakker.operation.template.OperationTemplate;
import nl.larsdenbakker.util.OperationResponse;

/**
 *
 * @author Lars den Bakker<larsdenbakker@gmail.com>
 */
public class OperationSequence implements ProcedureTask {
   
   private final OperationTemplate[] templates;
   
   public OperationSequence(OperationTemplate... templates) {
      this.templates = templates;
   }
   
   @Override
   public OperationResponse execute(OperationContext context, boolean stopOnFailure) {
      for (OperationTemplate template : templates) {
         OperationResponse result = new OperationContext(context, template).execute();
         if (stopOnFailure && !result.hasSucceeded()) {
            return result;
         }
      }
      return OperationResponse.succeeded();
   }
   
}
