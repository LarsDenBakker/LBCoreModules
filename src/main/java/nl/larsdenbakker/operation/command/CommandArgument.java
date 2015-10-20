package nl.larsdenbakker.operation.command;

/**
 * Argument for a Command. Arguments can be optional or non-optional
 * and are mapped to an operation variable.
 *
 * @author Lars den Bakker<larsdenbakker@gmail.com>
 */
public class CommandArgument {

   private final String variable;
   private final String description;
   private final boolean optional;

   public CommandArgument(String variable, String description, boolean optional) {
      this.description = description;
      this.variable = variable;
      this.optional = optional;
   }

   public String getDescription() {
      return description;
   }

   public String getVariable() {
      return variable;
   }

   public boolean isOptional() {
      return optional;
   }

}
