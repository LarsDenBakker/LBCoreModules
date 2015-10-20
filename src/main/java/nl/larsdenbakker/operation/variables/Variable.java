package nl.larsdenbakker.operation.variables;

import nl.larsdenbakker.datapath.DataPathResolveException;
import nl.larsdenbakker.storage.Storage;
import nl.larsdenbakker.operation.OperationModule;
import nl.larsdenbakker.util.TextUtils;

/**
 * A key-value mapping with a method to map this variable from one storage
 * to another. The variable String key is non-null, the value is nullable.
 * Any value stored in the parent storage to the same key as this variable
 * overrides any value set for this variable. Data-paths are resolved, other
 * variables can be defined as a string value with a $ preceding the key of
 * the variable. These variables are then looked up in the parent storage, and
 * their value is set as this variable's value in the sub storage.
 *
 * @author Lars den Bakker <larsdenbakker at gmail.com>
 */
public class Variable {

   private final String name;
   private final Object value;
   private final OperationModule operationModule;

   public Variable(OperationModule operationModule, String name, Object value) {
      this.name = name.intern();
      this.value = value;
      this.operationModule = operationModule;
   }

   public String getName() {
      return name;
   }

   public Object getValue() {
      return value;
   }

   public void mapVariableToStorage(Storage parentStorage, Storage storage) {
      //If key was already set in parentStorage, it overrides anything we have
      if (parentStorage != null && parentStorage.isSet(name)) {
         storage.set(name, parentStorage.get(name));
      } else if (value instanceof String) {
         String stringValue = (String) value;
         //If it starts with a $ it references a variable in the parent
         if (stringValue.startsWith("$")) {
            String key = stringValue.substring(1);
            //Grab the variable from the local storage first
            Object obj = storage.get(key);
            if (obj == null) {
               //Then from the parent storage if it could not be found
               obj = (parentStorage != null) ? parentStorage.get(key) : null;
            }
            if (obj != null) {
               storage.set(name, obj);
            }
            //If it ends with a @ it is a full registry path
         } else if (stringValue.contains("$")) {

            String[] split = TextUtils.splitOnSpaces(stringValue);
            StringBuilder sb = new StringBuilder();
            for (String str : split) {
               if (str.startsWith("$")) {
                  String key = str.substring(1);
                  //Grab the variable from the local storage first
                  Object obj = storage.get(key);
                  if (obj == null) {
                     //Then from the parent storage if it could not be found
                     obj = (parentStorage != null) ? parentStorage.get(key) : null;
                  }
                  if (obj != null) {
                     sb.append(obj.toString()).append(' ');
                  } else {
                     sb.append("<variable not found>").append(' ');
                  }
               } else {
                  sb.append(str).append(' ');
               }
            }
            storage.set(name, sb.toString());
         } else if (stringValue.startsWith(".")) {
            try {
               Object obj = operationModule.getDataPathModule().resolveDataPath(operationModule.getRegistryModule().getRootRegistry(), stringValue);
               if (obj != null) {
                  storage.set(name, obj);
               }
            } catch (DataPathResolveException ex) {

            }
            //It's a property of a variable in the parent storage
         } else if (stringValue.contains(".$")) {
            String[] split = stringValue.split(".\\$");
            if (split.length == 2) {
               Object registry = (parentStorage != null) ? parentStorage.get(split[1]) : null;
               if (registry != null) {
                  try {
                     Object obj = operationModule.getDataPathModule().resolveDataPath(registry, split[0]);
                     if (obj != null) {
                        storage.set(name, obj);
                     }
                  } catch (DataPathResolveException ex) {

                  }
               }
            }
         } else {
            storage.set(name, value);
         }
      } else {
         storage.set(name, value);
      }
   }
}
