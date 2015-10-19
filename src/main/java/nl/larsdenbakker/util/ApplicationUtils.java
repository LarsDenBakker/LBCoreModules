package nl.larsdenbakker.util;

import nl.larsdenbakker.datafile.DataFileUtils;
import java.io.File;
import java.util.Map;
import nl.larsdenbakker.app.Module;
import nl.larsdenbakker.datafile.DataFileException;
import nl.larsdenbakker.datafile.DataFormat;

/**
 *
 * @author Lars den Bakker <larsdenbakker at gmail.com>
 */
public class ApplicationUtils {

   public static Map<String, Object> loadModuleConfiguration(Module module, String fileName) throws DataFileException {
      String filePath = module.getModuleConfigFolder() + File.separator + fileName;
      String resourcePath = "resources" + File.separator + module.getName() + File.separator + fileName;
      Map<String, Object> configuration = DataFileUtils.loadContents(DataFormat.YAML, filePath);
      Map<String, Object> resource = DataFileUtils.loadResource(DataFormat.YAML, resourcePath);
      configuration.putAll(resource);
      return configuration;
   }

   public static Map<String, Object> loadModuleData(Module module, String fileName) throws DataFileException {
      String filePath = module.getModuleDataFolder() + File.separator + fileName;
      Map<String, Object> configuration = DataFileUtils.loadContents(DataFormat.YAML, filePath);
      return configuration;
   }

}
