package nl.larsdenbakker.operation.command;

import java.util.Map;

/**
 * Baisc implementation of CommandArgumentProvider.
 *
 * @author Lars den Bakker <larsdenbakker at gmail.com>
 */
public class SimpleCommandArgumentProvider<T> implements CommandArgumentProvider {

   private final T t;
   private final String key;

   public SimpleCommandArgumentProvider(String key, T t) {
      this.key = key;
      this.t = t;
   }

   @Override
   public void onCommand(Command parentCommand, Command command, String[] args, Map<String, Object> arguments) {
      if (!arguments.containsKey(key)) {
         arguments.put(key, t);
      }
   }

}
