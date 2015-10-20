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

   /**
    * Load configuration with the given file name for the given module. The default configuration resource (jar file)
    * location for the module is searched, as well as the default configuration file location. Both files
    * are read as String-Object mappings. The contents of the regular are mapped onto the contents of the resource file,
    * overriding it. This way the resource configuration can be treated as the default configuration allowing the user
    * to overwrite it or add to it. If either file could not be found, an empty map is created instead.
    *
    * @param module   The module.
    * @param fileName The name of the file.
    *
    * @return The contents of the files added together.
    *
    * @throws DataFileException if there was a problem loading either of the two files.
    */
   public static Map<String, Object> loadModuleConfiguration(Module module, String fileName) throws DataFileException {
      String filePath = module.getModuleConfigFolder() + File.separator + fileName;
      String resourcePath = "resources" + File.separator + module.getName() + File.separator + fileName;
      Map<String, Object> configuration = DataFileUtils.loadContents(DataFormat.YAML, filePath);
      Map<String, Object> resource = DataFileUtils.loadResource(DataFormat.YAML, resourcePath);
      configuration.putAll(resource);
      return configuration;
   }

}
