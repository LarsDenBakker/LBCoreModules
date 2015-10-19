package nl.larsdenbakker.storage;

import static com.google.common.base.Preconditions.checkNotNull;
import java.lang.ref.Reference;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import nl.larsdenbakker.conversion.ConversionModule;
import nl.larsdenbakker.conversion.reference.DataReference;
import nl.larsdenbakker.util.TextUtils;

/**
 * A type of Storage that uses a HashMap for storing data.
 *
 * @author Lars den Bakker <larsdenbakker at gmail.com>
 */
public class MemoryStorage extends Storage {

   private Map<String, Object> map;
   private final String name;
   private Storage parent;

   protected MemoryStorage(String name, Map<String, Object> map, Storage parent) {
      this.name = name;
      this.map = map;
      this.parent = parent;
   }

   protected MemoryStorage(String name, Map<String, Object> map) {
      this(name, map, null);
   }

   public static MemoryStorage create(ConversionModule conversionHandler, String name) {
      return new MemoryStorageRoot(conversionHandler, name);
   }

   public static MemoryStorage create(ConversionModule conversionHandler, String name, Map<String, Object> map) {
      return new MemoryStorageRoot(conversionHandler, name, map);
   }

   public static MemoryStorage create(ConversionModule conversionHandler) {
      return new MemoryStorageRoot(conversionHandler, "");
   }

   public static MemoryStorage create(ConversionModule conversionHandler, Map<String, Object> map) {
      return new MemoryStorageRoot(conversionHandler, "", map);
   }

   @Override
   public String getStorageKey() {
      return name;
   }

   @Override
   protected void _set(String key, Object value) {
      map.put(key, value);
   }

   @Override
   public Storage unset(String key) {
      checkNotNull(key);
      Object obj = map.remove(key);
      return this;
   }

   @Override
   public Object get(String key) {
      checkNotNull(key);
      Object obj = map.get(key);
      if (obj instanceof Reference) {
         return ((Reference) obj).get();
      } else if (obj instanceof DataReference) {
         return ((DataReference) obj).getDataValue();
      }
      return obj;
   }

   @Override
   public MemoryStorage getStorage(String key, boolean createIfNull) {
      Object obj = get(key);
      if (obj != null) {
         if (obj instanceof MemoryStorage) {
            return (MemoryStorage) obj;
         } else if (obj instanceof Map) {
            Map requestedMap = (Map) obj;
            MemoryStorage storage = new MemoryStorage(key, requestedMap, this);
            set(key, storage);
            return storage;
         }
      }
      if (createIfNull) {
         MemoryStorage storage = new MemoryStorage(key, new HashMap(), this);
         set(key, storage);
         return storage;
      } else {
         return null;
      }
   }

   @Override
   public Set<String> getKeys() {
      return map.keySet();
   }

   @Override
   public Collection<Object> getValues() {
      return map.values();
   }

   @Override
   public Storage getRoot() {
      return parent.getRoot();
   }

   @Override
   public Storage getParent() {
      return parent;
   }

   protected void overrideContents(Map<String, Object> map) {
      this.map = map;
   }

   @Override
   public ConversionModule getConversionModule() {
      return getRoot().getConversionModule();
   }

   @Override
   public Object toSerializable() {
      return map;
   }

   @Override
   public Map<String, Object> getContents() {
      return map;
   }

   @Override
   public String getDescription() {
      return getDescription(getContents(), 0);
   }

   private String getDescription(Map<String, Object> map, int offset) {
      StringBuilder sb = new StringBuilder();
      for (Map.Entry<String, Object> entry : map.entrySet()) {
         sb.append(getSpacesForOffset(offset)).append(entry.getKey()).append(": ");
         offset++;
         Object val = entry.getValue();
         if (val instanceof Map) {
            sb.append('\n');
            sb.append(getDescription((Map) val, offset));
         } else if (val instanceof Storage) {
            sb.append('\n');
            sb.append(getDescription(((Storage) val).getContents(), offset));
         } else {
            sb.append(TextUtils.getDescription(val));
         }
         sb.append('\n');
      }
      return sb.toString();
   }

   private String getSpacesForOffset(int offset) {
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < offset; i++) {
         sb.append("   ");
      }
      return sb.toString();
   }

   @Override
   public String getTypeDescription() {
      return "Memory Storage";
   }

}
