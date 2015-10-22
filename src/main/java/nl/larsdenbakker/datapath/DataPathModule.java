package nl.larsdenbakker.datapath;

import static com.google.common.base.Preconditions.checkArgument;
import nl.larsdenbakker.datapath.converters.DataPathConverter;
import static com.google.common.base.Preconditions.checkNotNull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.util.Pair;
import nl.larsdenbakker.app.AbstractModule;
import nl.larsdenbakker.app.Application;
import nl.larsdenbakker.app.Module;
import nl.larsdenbakker.conversion.ConversionException;
import nl.larsdenbakker.conversion.ConversionModule;
import nl.larsdenbakker.datapath.converters.DataReferenceConverter;
import nl.larsdenbakker.util.CollectionUtils;
import nl.larsdenbakker.util.TextUtils;

/**
 * A module that handles DataPaths. DataPaths start from a data object and contain
 * a chain of keys that are required to query the data object for the next Data Object,
 * eventually leading to a data value at the end of the path. Data objects can be Objects
 * implementing the DataHolder interface, Map or List.
 *
 * DataPaths can be represented either textually: ".some-registry.some-registerable.some-property"
 * or as an object. The DataPathValue object represents the end of the chain and it is backed by
 * parent DataPathNodes leading up to the DataPathRoot. See DataPathNode for more information.
 *
 * A DataPathRoot must be provided upon construction. This root is used as default starting
 * point from any path and as the root of any DataPath chain that is constructed.
 *
 * This module depends on the ConversionModule.
 *
 * @author Lars den Bakker <larsdenbakker at gmail.com>
 */
public class DataPathModule extends AbstractModule {
   
   private final Map<Class, DataPathResolver> pathResolvers = new HashMap();
   private final DataPathRoot dataPathRoot;
   
   private DataPathConverter dataPathConverter;
   private DataReferenceConverter dataReferenceConverter;
   
   public DataPathModule(Application app, DataPathRoot dataPathRoot) {
      super(app);
      this.dataPathRoot = dataPathRoot;
   }
   
   @Override
   public String getName() {
      return "data-path";
   }
   
   @Override
   public Class<? extends Module>[] getDependencies() {
      return CollectionUtils.asArray(ConversionModule.class);
   }
   
   @Override
   protected void _load() {
      dataPathConverter = new DataPathConverter(this);
      dataReferenceConverter = new DataReferenceConverter(this);
      
      ConversionModule module = getConversionModule();
      module.registerConversionOverride(dataPathConverter);
      module.registerConverter(dataReferenceConverter);
   }
   
   @Override
   public void _unload() {
      ConversionModule module = getConversionModule();
      module.unregisterConversionOverride(dataPathConverter);
   }
   
   public ConversionModule getConversionModule() {
      return getParentApplication().getModule(ConversionModule.class);
   }

   /**
    * Resolve a string representation of a DataPath and get the value
    * at the end of the path, starting at the default DataPathRoot.
    * If nothing is found an exception is thrown.
    *
    * @param path The path.
    *
    * @return The value at the end of the path, if any. This can be null if
    * the path supports null values.
    * @throws DataPathResolveException When there was a problem resolving anything along the path.
    */
   public Object resolveDataPath(String path) throws DataPathResolveException {
      return _resolveDataPath(dataPathRoot, path);
   }

   /**
    * Resolve a string representation of a DataPath and get the value
    * at the end of the path, starting at the specified start.
    * If nothing is found an exception is thrown.
    *
    * @param start The Object to start from. This must be either an instance of DataHolder,
    *              a List or a Map.
    * @param path  The path.
    *
    * @return The value at the end of the path, if any. This can be null if
    * the path supports null values.
    * @throws DataPathResolveException When there was a problem resolving anything along the path.
    */
   public Object resolveDataPath(Object start, String path) throws DataPathResolveException {
      return _resolveDataPath(start, path);
   }
   
   private String[] pathToKeys(String path) {
      checkNotNull(path);
      checkArgument(!path.isEmpty());
      if (path.startsWith(".")) {
         path = path.substring(1, path.length());
      }
      String[] pathArray = TextUtils.splitOnPeriods(path);
      return pathArray;
   }
   
   private Object _resolveDataPath(Object start, String path) throws DataPathResolveException {
      List<Object> history = _resolveDataPath(start, pathToKeys(path.replaceAll(" ", "_")));
      return history.get(history.size() - 1);
   }
   
   private List<Object> _resolveDataPath(Object start, String[] keys) throws DataPathResolveException {
      checkNotNull(start);
      checkNotNull(keys);
      checkArgument(keys.length != 0);
      
      Object dataObject = start;
      List<Object> history = new ArrayList();
      for (int i = 0; i < keys.length; i++) {
         history.add(dataObject);
         String key = keys[i];
         if (dataObject instanceof DataHolder) {
            DataHolder dataHolder = (DataHolder) dataObject;
            dataObject = dataHolder.getDataValue(key);
            if (dataObject == null) {
               throw new DataPathResolveException("Did not find anything at key " + key + " in " + dataHolder.getTypeAndValueDescription());
            }
         } else if (dataObject instanceof Map) {
            Map map = (Map) dataObject;
            Object objectKey = null;

            //Convert the string key to an object key if we know what type the key is
            Pair<Class<?>, Class<?>> keyValueTypes = getConversionModule().getCachedKeyValueTypes(map);
            if (keyValueTypes != null) {
               Class<?> keyClass = keyValueTypes.getKey();
               try {
                  Object temp = getConversionModule().convert(key, keyClass);
                  if (temp != null) {
                     objectKey = temp;
                  }
               } catch (ConversionException ex) {
                  //objectKey will stay null, handled below
               }
            }
            
            if (objectKey == null) {
               objectKey = key;
            }
            dataObject = map.get(objectKey);
            if (dataObject == null) {
               if (keyValueTypes != null) {
                  throw new DataPathResolveException("Did not find any " + TextUtils.getTypeDescription(keyValueTypes.getValue()) + " at key: " + key);
               } else {
                  throw new DataPathResolveException("Did not find anything at key: " + key);
               }
            }
         } else if (dataObject instanceof List) {
            try {
               Integer integerKey = getConversionModule().convert(key, Integer.class);
               List list = (List) dataObject;
               if (integerKey < list.size()) {
                  dataObject = list.get(integerKey);
               } else {
                  throw new DataPathResolveException("Index " + integerKey + " is outside the scope of this list. (max " + list.size() + ")");
               }
            } catch (ConversionException ex) {
            }
         } else {
            DataPathResolver pathResolver = getDataPathResolver(dataObject.getClass());
            if (pathResolver != null) {
               dataObject = pathResolver.resolvePath(dataObject, key);
               if (dataObject == null) {
                  throw new DataPathResolveException("Did not find any " + pathResolver.getResolvedTypeDescription() + " at key: " + key);
               }
            } else {
               throw new DataPathResolveException("Could not resolve path: " + Arrays.toString(keys) + ". No DataPathResolver registered for type: " + dataObject.getClass());
            }
         }
      }
      history.add(dataObject);
      return history;
   }
   
   public <T> DataPathResolver<? super T> getDataPathResolver(Class<T> type) {
      return pathResolvers.get(type);
   }
   
   public void registerDataResolver(DataPathResolver pathResolver) {
      pathResolvers.put(pathResolver.getResolvedType(), pathResolver);
   }
   
   public DataPathRoot getDataPathRoot() {
      return dataPathRoot;
   }
   
}
