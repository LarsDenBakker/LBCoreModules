package nl.larsdenbakker.property.properties;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javafx.util.Pair;
import nl.larsdenbakker.conversion.ConversionException;
import nl.larsdenbakker.conversion.ConversionModule;
import nl.larsdenbakker.property.PropertyHolder;
import nl.larsdenbakker.property.PropertyValidationException;
import nl.larsdenbakker.storage.Storage;
import nl.larsdenbakker.operation.operations.TargetedOperation;
import nl.larsdenbakker.operation.template.OperationTemplate;
import nl.larsdenbakker.util.CollectionUtils;
import nl.larsdenbakker.util.MapUtils;
import nl.larsdenbakker.util.OperationResponse;
import nl.larsdenbakker.util.TextUtils;

/**
 *
 * @author Lars den Bakker <larsdenbakker at gmail.com>
 */
public class MapProperty<K, V> extends StorageProperty<Map<K, V>> {

   public static final String KEY_KEY_VALIDATION_OPERATIONS = "key-validation-operations";
   public static final String KEY_VALUE_VALIDATION_OPERATIONS = "value-validation-operations";
   public static final String KEY_ENTRY_VALIDATION_OPERATIONS = "entry-validation-operations";

   private final Class<K> keyType;
   private final Class<V> valueType;
   private final Class<? extends Map<K, V>> mapType;
   private final OperationTemplate[] keyValidationOperations;
   private final OperationTemplate[] valueValidationOperations;
   private final OperationTemplate[] entryValidationOperations;

   public MapProperty(Storage storage, Class<? extends Map<K, V>> mapType, Class<K> keyType, Class<V> valueType) {
      super(storage, (Class<Map<K, V>>) ((Class) Map.class));
      this.mapType = mapType;
      this.keyType = keyType;
      this.valueType = valueType;
      List<OperationTemplate> keyConstraintList = storage.getCollection(KEY_KEY_VALIDATION_OPERATIONS, List.class, OperationTemplate.class, false);
      this.keyValidationOperations = (keyConstraintList != null) ? CollectionUtils.asArrayOfType(OperationTemplate.class, keyConstraintList) : null;
      List<OperationTemplate> valueConstraintList = storage.getCollection(KEY_VALUE_VALIDATION_OPERATIONS, List.class, OperationTemplate.class, false);
      this.valueValidationOperations = (valueConstraintList != null) ? CollectionUtils.asArrayOfType(OperationTemplate.class, valueConstraintList) : null;
      List<OperationTemplate> entryConstraintList = storage.getCollection(KEY_ENTRY_VALIDATION_OPERATIONS, List.class, OperationTemplate.class, false);
      this.entryValidationOperations = (entryConstraintList != null) ? CollectionUtils.asArrayOfType(OperationTemplate.class, entryConstraintList) : null;
   }

   @Override
   protected Map<K, V> _getValue(PropertyHolder pdh) {
      Map<K, V> val = pdh.getStorage().getMap(getKey(), getPropertyValueClass(), getKeyType(), getValueType());
      return (val != null) ? val : getDefaultValue(pdh);
   }

   @Override
   protected Map<K, V> getDefaultValue(PropertyHolder dh) {
      Map<K, V> copy = MapUtils.of(mapType);
      setValidValue(dh, copy);
      return copy;
   }

   @Override
   public void validate(Map<K, V> val) throws PropertyValidationException {
      super.validate(val);
      if (keyValidationOperations != null || valueValidationOperations != null || entryValidationOperations != null) {
         for (Entry<K, V> entry : val.entrySet()) {
            if (entry.getValue() != null) {

               if (keyValidationOperations != null) {
                  for (OperationTemplate template : keyValidationOperations) {
                     OperationResponse response = template.execute(MapUtils.of(TargetedOperation.KEY_TARGET, entry.getKey()));
                     if (!response.hasSucceeded()) {
                        throw new PropertyValidationException("Error at key " + entry.getKey() + ": " + response.getMessage());
                     }
                  }
               }

               if (valueValidationOperations != null) {
                  for (OperationTemplate template : valueValidationOperations) {
                     OperationResponse response = template.execute(MapUtils.of(TargetedOperation.KEY_TARGET, entry.getValue()));
                     if (!response.hasSucceeded()) {
                        throw new PropertyValidationException("Error at value " + entry.getValue() + ": " + response.getMessage());
                     }
                  }
               }

               if (entryValidationOperations != null) {
                  for (OperationTemplate template : entryValidationOperations) {
                     OperationResponse response = template.execute(MapUtils.of(TargetedOperation.KEY_TARGET, new Pair(entry.getKey(), entry.getValue())));
                     if (!response.hasSucceeded()) {
                        throw new PropertyValidationException("Error at entry " + entry.getKey() + "=" + entry.getValue() + ": " + response.getMessage());
                     }
                  }
               }

            } else {
               throw new PropertyValidationException("Value at key: " + TextUtils.getDescription(entry.getKey()) + " cannot be null.");
            }
         }
      }
   }

   public Map<K, V> getCopy(PropertyHolder pdh) {
      Map<K, V> copy = MapUtils.of(getMapType());
      Map<K, V> val = getValue(pdh);
      copy.putAll(val);
      return copy;
   }

   @Override
   public void addToValue(PropertyHolder ph, Object value) throws PropertyModificationException {
      try {
         Map<K, V> convertedValue = convertToValueType(ph.getConversionModule(), value);
         Map<K, V> copy = getCopy(ph);
         copy.putAll(convertedValue);
         validate(copy);
         getValue(ph).putAll(copy);
      } catch (ConversionException | PropertyValidationException ex) {
         throw new PropertyModificationException(ex);
      }
   }

   @Override
   public void removeFromValue(PropertyHolder ph, Object value) throws PropertyModificationException {
      try {
         Map<K, V> convertedValue = convertToValueType(ph.getConversionModule(), value);
         Map<K, V> copy = getCopy(ph);
         for (Entry<K, V> entry : convertedValue.entrySet()) {
            copy.remove(entry.getKey());
         }
         validate(copy);
         Map<K, V> storedValue = getValue(ph);
         for (Entry<K, V> entry : convertedValue.entrySet()) {
            storedValue.remove(entry.getKey());
         }
      } catch (ConversionException | PropertyValidationException ex) {
         throw new PropertyModificationException(ex);
      }
   }

   public Map<K, V> convertToValueType(ConversionModule conversionHandler, Object obj) throws ConversionException {
      return conversionHandler.convertToMap(obj, mapType, keyType, valueType, true);
   }

   public Class<K> getKeyType() {
      return keyType;
   }

   public Class<V> getValueType() {
      return valueType;
   }

   public Class<? extends Map> getMapType() {
      return mapType;
   }

   @Override
   public void clearValue(PropertyHolder dh) {
      getValue(dh).clear();
   }

   @Override
   public String getTypeDescription() {
      return "Map Property";
   }

}
