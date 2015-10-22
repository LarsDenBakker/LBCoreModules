package nl.larsdenbakker.property.properties;

import nl.larsdenbakker.property.PropertyHolder;
import nl.larsdenbakker.property.PropertyValidationException;
import nl.larsdenbakker.operation.operations.TargetedOperation;
import nl.larsdenbakker.operation.template.OperationTemplate;
import nl.larsdenbakker.util.Describable;
import nl.larsdenbakker.util.MapUtils;
import nl.larsdenbakker.util.OperationResponse;

/**
 * A Property marks a key-value mapping of a PropertyHolder. The Property class defines methods
 * to get and modify the property and protects the property value from illegal modification.
 *
 * A Property instance is registered to a Properties object. Only one Properties object exists per
 * PropertyHolder type. This means that a Property only defines the behavior of reading and writing
 * the value, but the actual value is stored elsewhere.
 *
 * @author Lars den Bakker <larsdenbakker at gmail.com>
 */
public interface Property<V> extends Describable {

   public static final String KEY_NAME = "name".intern();
   public static final String KEY_DESCRIPTION = "description".intern();
   public static final String KEY_DEFAULT_VALUE = "default-value";
   public static final String KEY_VALIDATION_OPERATIONS = "validation-operations";
   public static final String KEY_NULLABLE = "nullable".intern();
   public static final String KEY_CONSTRUCTOR_PARAMETERS = "constructor-parameters";

   public Class<V> getPropertyValueClass();

   /**
    * @return The key for this Property. This is how the Property is identified within the PropertyHolder
    *         and during serialization and deserialization.
    */
   public String getKey();

   /**
    *
    * @param ph The PropertyHolder.
    *
    * @return The value of this Property for the given PropertyHolder.
    */
   public V getValue(PropertyHolder ph);

   /**
    * Set the value of this Property for the given PropertyHolder. Value must be
    * convertible to the correct type.
    *
    * @param ph    The PropertyHolder.
    * @param value The value.
    *
    * @throws PropertyModificationException if Property could not be set to the
    *                                       given value.
    */
   public void setValue(PropertyHolder ph, Object value) throws PropertyModificationException;

   /**
    * Add to the value of this Property for the given PropertyHolder. Value must be
    * convertible to the correct type.
    *
    * @param ph    The PropertyHolder.
    * @param value The value.
    *
    * @throws PropertyModificationException if the given value could not be added
    *                                       to the Property.
    */
   public void addToValue(PropertyHolder ph, Object value) throws PropertyModificationException;

   /**
    * Remove from the value of this Property for the given PropertyHolder. Value must be
    * convertible to the correct type.
    *
    * @param ph    The PropertyHolder.
    * @param value The value.
    *
    * @throws PropertyModificationException if the given value could not be removed
    *                                       from the Property.
    */
   public void removeFromValue(PropertyHolder ph, Object value) throws PropertyModificationException;

   /**
    * Clear the value of this Property for the given PropertyHolder.
    *
    * @param ph The PropertyHolder.
    *
    * @throws PropertyModificationException if the Property could not be cleared
    */
   public void clearValue(PropertyHolder ph) throws PropertyModificationException;

   /**
    * Set the value of this Property for the given PropertyHolder. The value must be
    * valid, an IllegalArgumentException is thrown if it is not. This method is used
    * when values are hardcoded and must always be asserted to be correct.
    *
    * @param ph  The PropertyHolder.
    * @param val The value.
    */
   public void setValidValue(PropertyHolder ph, V val);

   /**
    * Validate the value of this Property for the given PropertyHolder.
    *
    * @param ph The PropertyHolder.
    *
    * @throws PropertyValidationException if the value is invalid.
    *
    */
   public default void validate(PropertyHolder ph) throws PropertyValidationException {
      validate(getValue(ph));
   }

   /**
    *
    * @param val The value.
    *
    * @return whether the given value is a valid value for this Property.
    */
   public default boolean isValid(V val) {
      try {
         validate(val);
         return true;
      } catch (PropertyValidationException ex) {
         return false;
      }
   }

   /**
    * Validate if the given value is valid for this Property.
    *
    * @param val The value.
    *
    * @throws PropertyValidationException if the value is invalid.
    */
   public default void validate(V val) throws PropertyValidationException {
      if (val != null) {
         if (getValidationOperations() != null) {
            for (OperationTemplate template : getValidationOperations()) {
               OperationResponse response = template.execute(MapUtils.of(TargetedOperation.KEY_TARGET, val));
               if (!response.hasSucceeded()) {
                  throw new PropertyValidationException(response.getMessage());
               }
            }
         }
      } else {
         if (!isNullable()) {
            throw new PropertyValidationException("Value for property " + this.getDescription() + " cannot be empty.");
         }
      }
   }

   /**
    * @return Whether or not this Property is required for construction of the associated PropertyHolder.
    */
   public boolean isConstructorParameter();

   /**
    * @return Whether or not the value for this Property can be null.
    */
   public boolean isNullable();

   /**
    * @return Operations that must be performed to validate this Property, or null if none.
    */
   public OperationTemplate[] getValidationOperations();

   @Override
   public default String getTypeDescription() {
      return "Property";
   }
}
