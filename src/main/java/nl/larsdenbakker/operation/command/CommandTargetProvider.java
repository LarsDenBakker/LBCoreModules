package nl.larsdenbakker.operation.command;

import java.util.Map;

/**
 *
 * @author Lars den Bakker <larsdenbakker at gmail.com>
 */
public class CommandTargetProvider<T> implements CommandArgumentProvider {

   private final T t;

   public CommandTargetProvider(T t) {
      this.t = t;
   }

   @Override
   public void onCommand(Command parentCommand, Command command, String[] args, Map<String, Object> arguments) {
      arguments.put("target", t);
   }

}
