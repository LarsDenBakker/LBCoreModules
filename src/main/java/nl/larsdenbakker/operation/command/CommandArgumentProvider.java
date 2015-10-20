package nl.larsdenbakker.operation.command;

import java.util.Map;

/**
 * Utility to add default command arguments in-code.
 *
 * @author Lars den Bakker<larsdenbakker@gmail.com>
 */
public interface CommandArgumentProvider {

   public void onCommand(Command parentCommand, Command command, String[] args, Map<String, Object> arguments);

}
