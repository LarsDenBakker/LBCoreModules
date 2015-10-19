package nl.larsdenbakker.operation.variables;

import nl.larsdenbakker.datapath.DataPathResolveException;
import nl.larsdenbakker.storage.Storage;
import nl.larsdenbakker.operation.OperationModule;
import nl.larsdenbakker.util.TextUtils;

/**
 *
 * @author Lars den Bakker <larsdenbakker at gmail.com>
 */
public class Variable {

   private final String name;
   private final Object value;
   private final OperationModule operationHandler;

   public Variable(OperationModule operationHandler, String name, Object value) {
      this.name = name.intern();
      this.value = value;
      this.operationHandler = operationHandler;
   }

   public String getName() {
      return name;
   }

   public Object getValue() {
      return value;
   }

   public void mapVariableToContext(Storage parentStorage, Storage storage) {
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
               Object obj = operationHandler.getDataPathModule().resolveDataPath(operationHandler.getRegistryModule().getRootRegistry(), stringValue);
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
                     Object obj = operationHandler.getDataPathModule().resolveDataPath(registry, split[0]);
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
