package nl.larsdenbakker.datafile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

/**
 *
 * @author Lars den Bakker <larsdenbakker at gmail.com>
 */
public enum DataFormat {

   JSON(new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT).disable(SerializationFeature.WRITE_NULL_MAP_VALUES)),
   JSON_UNINDENTED(new ObjectMapper().disable(SerializationFeature.WRITE_NULL_MAP_VALUES)),
   YAML(new ObjectMapper(new YAMLFactory()).disable(SerializationFeature.WRITE_NULL_MAP_VALUES));

   private final ObjectMapper mapper;

   private DataFormat(ObjectMapper mapper) {
      this.mapper = mapper;
   }

   public ObjectMapper getObjectMapper() {
      return mapper;
   }
}
