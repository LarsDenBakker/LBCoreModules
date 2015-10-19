package nl.larsdenbakker.datafile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Lars den Bakker<larsdenbakker@gmail.com>
 */
public class DataFileUtils {

   /**
    * Load the contents of a File with the given DataFormat.
    * Content is returned as String-Object mappings. If no
    * file could be found, an empty Map is returned.
    *
    * @param format The DataFormat.
    * @param file   The File.
    *
    * @return The non-null contents of the file.
    * @throws DataFileException if the file could not be read.
    */
   public static Map<String, Object> loadContents(DataFormat format, File file) throws DataFileException {
      try {
         return format.getObjectMapper().readValue(file, Map.class);
      } catch (FileNotFoundException ex) {
         return new HashMap();
      } catch (IOException ex) {
         throw new DataFileException("Could not read file.", ex).addFailedAction("Reading file " + file.getPath());
      }
   }

   /**
    * Load the contents of a file at the given location with the given DataFormat.
    * Content is returned as String-Object mappings. If no
    * file could be found, an empty Map is returned.
    *
    * @param format   The DataFormat.
    * @param filePath The file location.
    *
    * @return The non-null contents of the file.
    * @throws DataFileException if the file could not be read.
    */
   public static Map<String, Object> loadContents(DataFormat format, String filePath) throws DataFileException {
      return loadContents(format, new File(filePath));
   }

   /**
    * Load the contents of an InputStream with the given DataFormat.
    * Content is returned as String-Object mappings. If no
    * file could be found, an empty Map is returned.
    *
    * @param format The DataFormat.
    * @param input  The input stream.
    *
    * @return The non-null contents of the InputStream.
    * @throws IOException if the InputStream could not be read.
    */
   public static Map<String, Object> loadContents(DataFormat format, InputStream input) throws IOException {
      return format.getObjectMapper().readValue(input, Map.class);
   }

   /**
    * Load the contents of a URL with the given DataFormat.
    * Content is returned as String-Object mappings. If no
    * file could be found, an empty Map is returned.
    *
    * @param format The DataFormat.
    * @param url    The URL.
    *
    * @return The non-null contents of the URL.
    * @throws IOException if the URL could not be read.
    */
   public static Map<String, Object> loadContents(DataFormat format, URL url) throws IOException {
      return format.getObjectMapper().readValue(url, Map.class);
   }

   /**
    * Load the contents of a resource (a file inside the jar) with the given DataFormat.
    * Content is returned as String-Object mappings. If no
    * file could be found, an empty Map is returned.
    *
    * @param format The DataFormat.
    * @param path   The resource path.
    *
    * @return The non-null contents of the URL.
    * @throws DataFileException if the file could not be read.
    */
   public static Map<String, Object> loadResource(DataFormat format, String path) throws DataFileException {
      InputStream stream = DataFileUtils.class.getClassLoader().getResourceAsStream(path);
      if (stream != null) {
         try {
            return loadContents(format, stream);
         } catch (IOException ex) {
            throw new DataFileException("Could not read resource.", ex).addFailedAction("Reading resource " + path);
         }
      } else {
         throw new DataFileException("Could not find resource at " + path);
      }
   }

}
