package nl.larsdenbakker.configuration;

import java.util.Map;
import java.util.Map.Entry;
import nl.larsdenbakker.app.Module;
import nl.larsdenbakker.datafile.DataFileException;
import nl.larsdenbakker.util.ApplicationUtils;
import static nl.larsdenbakker.util.Message.Type.INFO;

/**
 *
 * @author Lars den Bakker <larsdenbakker at gmail.com>
 */
public class ConfigurationTemplateFactory {

   /**
    * Utility method to load and register command templates for a given Module. File paths
    * are generated automatically based on the module's default configuration location.
    * Both the jar's resources and the file system are queried for configuration.
    *
    * @param module              The module.
    * @param configurationModule The associated ConfigurationModule.
    * @param fileName            The name of the commands template file.
    *
    * @throws DataFileException when there is a problem reading a resource or file.
    */
   public static void loadAndRegisterTemplates(Module module, ConfigurationModule configurationModule, String fileName) throws DataFileException {
      Map<String, Object> configurationTemplates = ApplicationUtils.loadModuleConfiguration(module, fileName);

      int count = 0;
      for (Entry<String, Object> entry : configurationTemplates.entrySet()) {
         Object obj = entry.getValue();
         if (obj instanceof Map) {
            Map<String, Object> node = (Map) obj;
            Object defaultsObject = node.get("defaults");
            Object templateObject = node.get("template");
            if (templateObject instanceof Map) {
               ConfigurationTemplate template = new ConfigurationTemplate(module, entry.getKey(), (defaultsObject instanceof Map) ? (Map) defaultsObject : null, (Map) templateObject);
               configurationModule.getTemplateRegistry().register(template);
               count++;
            }
         }
      }
      module.getParentApplication().getConsole().message(INFO, "Registered " + count + " ConfigurationTemplates.");
   }

}
