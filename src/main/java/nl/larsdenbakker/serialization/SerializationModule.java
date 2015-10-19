package nl.larsdenbakker.serialization;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.larsdenbakker.app.AbstractModule;
import nl.larsdenbakker.app.Application;
import nl.larsdenbakker.app.Module;
import nl.larsdenbakker.conversion.ConversionModule;
import nl.larsdenbakker.conversion.ConversionException;
import nl.larsdenbakker.util.CollectionUtils;
import nl.larsdenbakker.app.UserInputException;

/**
 * A module that handles serialization and de-serialization of objects.
 *
 * @author Lars den Bakker<larsdenbakker@gmail.com>
 */
public class SerializationModule extends AbstractModule {

   private final Map<Class<?>, DataSerializer> serializers = new HashMap<>();

   public SerializationModule(Application app) {
      super(app);
   }

   @Override
   public String getName() {
      return "serialization";
   }

   @Override
   public Class<? extends Module>[] getDependencies() {
      return CollectionUtils.asArray(ConversionModule.class);
   }

   @Override
   protected void _load() throws UserInputException {
      registerSerializer(new ReferenceSerializer());
   }

   public ConversionModule getConversionModule() {
      return getParentApplication().getModule(ConversionModule.class);
   }

   public void registerSerializer(DataSerializer serializer) {
      serializers.put(serializer.getSerializedType(), serializer);
   }

   public <T> DataSerializer<? super T> getSerializer(Class<T> clazz) {
      return CollectionUtils.<DataSerializer>getMappedValueFromSuperType(serializers, clazz);
   }

   public Object toSerializable(Object obj) {
      if (obj == null) {
         return null;
      } else {
         return _toSerializable(obj, obj.getClass());
      }
   }

   public <T> Object _toSerializable(Object obj, Class<T> objectClass) {
      if (obj instanceof DataSerializable) {
         DataSerializable temp = (DataSerializable) obj;
         return toSerializable(((DataSerializable) obj).toSerializable());
      } else if (getSerializer(objectClass) != null) {
         DataSerializer<? super T> serializer = getSerializer(objectClass);
         try {
            return toSerializable(serializer.toSerializable((T) obj)); //Safe cast ensured because toSerializable(Object) calls this with the object's class
         } catch (ConversionException ex) {
            Logger.getLogger(SerializationModule.class.getName()).log(Level.SEVERE, null, ex);
         }
      } else if (obj instanceof Map) {
         return toSerializable((Map) obj);
      } else if (obj instanceof Collection) {
         return toSerializable((Collection) obj);
      } else if (obj instanceof Object[]) {
         return toSerializable((Object[]) obj);
      }
      return obj.toString();
   }

   public Map<Object, Object> toSerializable(Map<?, ?> map) {
      Map<Object, Object> serializedMap = new HashMap();

      for (Entry<?, ?> entry : map.entrySet()) {
         serializedMap.put(toSerializable(entry.getKey()), toSerializable(entry.getValue()));
      }

      return serializedMap;
   }

   public List<Object> toSerializable(Collection coll) {
      List<Object> list = new ArrayList();
      coll.stream().forEach(e -> {
         Object newElement = toSerializable(e);
         list.add(newElement);
      });
      return list;
   }

   public Object toSerializable(Object[] array) {
      Object[] serializedArray = new Object[array.length];
      for (int i = 0; i < array.length; i++) {
         serializedArray[i] = toSerializable(array[i]);
      }
      return serializedArray;
   }

}
