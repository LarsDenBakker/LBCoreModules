package nl.larsdenbakker.configuration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import nl.larsdenbakker.app.Module;
import nl.larsdenbakker.registry.Registrable;
import nl.larsdenbakker.registry.Registry;

/**
 * ConfigurationTemplates are a wrapper around a map with YML/JSON style 
 * String-Object mappings. Typically this class is used to replace marked locations
 * within another configuration, removing the need to re-type the same
 * configuration repeatedly. Optionally variables can be configured within the
 * template. These are defined by the configuration that 'calls' this template.
 * Default variables can be defined as well.
 *
 * @author Lars den Bakker <larsdenbakker at gmail.com>
 */
public class ConfigurationTemplate implements Registrable<String> {

   private Module parentModule;
   private final String key;
   private final Map<String, Object> defaultVariables;
   private final Map<String, Object> template;
   private Registry registry;

   protected ConfigurationTemplate(Module parentModule, String key, Map<String, Object> defaultVariables, Map<String, Object> template) {
      this.key = key;
      this.defaultVariables = defaultVariables;
      this.template = template;
   }

   @Override
   public String getKey() {
      return key;
   }

   @Override
   public Registry getParentRegistry() {
      return registry;
   }

   @Override
   public void setParentRegistry(Registry registry) {
      this.registry = registry;
   }

   @Override
   public Module getParentModule() {
      return parentModule;
   }

   /**
    * Get this template with the defined variables.
    *
    * @param variables The variables.
    *
    * @return The template.
    */
   public Map<String, Object> toTemplate(Map<String, Object> variables) {
      return convertMap(variables, template);
   }

   /**
    * Get this template with all default variables.
    *
    * @return The template.
    */
   public Map<String, Object> toTemplate() {
      return toTemplate(null);
   }

   private Object convertObject(Map<String, Object> variables, Object obj) {
      if (obj instanceof Map) {
         return convertMap(variables, (Map) obj);
      } else if (obj instanceof Collection) {
         return convertCollection(variables, (Collection) obj);
      } else if (obj instanceof String) {
         return convertString(variables, (String) obj);
      } else {
         return obj;
      }
   }

   private Map<String, Object> convertMap(Map<String, Object> variables, Map<String, Object> map) {
      Map<String, Object> newMap = new LinkedHashMap();
      for (Entry<String, Object> entry : map.entrySet()) {
         if (entry.getKey() != null && entry.getValue() != null) {
            newMap.put(convertString(variables, entry.getKey()).toString(), convertObject(variables, entry.getValue()));
         }
      }
      return newMap;
   }

   private List<Object> convertCollection(Map<String, Object> variables, Collection<Object> coll) {
      List<Object> newList = new ArrayList();
      for (Object obj : coll) {
         if (obj != null) {
            newList.add(convertObject(variables, obj));
         }
      }
      return newList;
   }

   private Object convertString(Map<String, Object> variables, String str) {
      if (str.startsWith("$")) {
         String key = str.substring(1);
         if (variables.containsKey(key)) {
            return variables.get(key);
         } else if (defaultVariables.containsKey(key)) {
            return defaultVariables.get(key);
         }
      }
      return str;
   }

}
