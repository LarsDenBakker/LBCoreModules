package nl.larsdenbakker.property;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import nl.larsdenbakker.property.PropertyHolder;
import nl.larsdenbakker.property.properties.Properties;
import nl.larsdenbakker.property.properties.Property;

/**
 *
 * @author Lars den Bakker <larsdenbakker at gmail.com>
 */
public class PropertyUtils {

   public static <T extends PropertyHolder> Properties getProperties(Class<T> clazz) {
      try {
         for (Field f : clazz.getFields()) {
            if (f.isAccessible() && Modifier.isStatic(f.getModifiers()) && Properties.class.isAssignableFrom(f.getType())) {
               return (Properties) f.get(null);
            }
         }
      } catch (IllegalArgumentException | IllegalAccessException ex) {
      }
      throw new IllegalArgumentException("Class " + clazz.getName() + " does not have a static instance of Properties.");
   }

   public static <T extends Property<?>> T getAssertedProperty(Properties properties, Class<T> assertedClass, String key) {
      Property<?> prop = properties.getProperty(key);
      if (prop == null) {
         throw new IllegalStateException("Property with key " + key + " was not defined in " + properties.getClass());
      }
      try {
         return (T) prop;
      } catch (ClassCastException e) {
         throw new IllegalStateException("Property with key " + key + " was not of type " + assertedClass + " in " + properties.getClass());
      }
   }

}
