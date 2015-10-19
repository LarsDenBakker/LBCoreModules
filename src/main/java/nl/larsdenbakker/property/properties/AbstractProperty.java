package nl.larsdenbakker.property.properties;

import java.util.List;
import nl.larsdenbakker.property.PropertyHolder;
import nl.larsdenbakker.storage.Storage;
import nl.larsdenbakker.operation.template.OperationTemplate;
import nl.larsdenbakker.util.CollectionUtils;

/**
 *
 * @author Lars den Bakker <larsdenbakker at gmail.com>
 */
public abstract class AbstractProperty<V> implements Property<V> {

   private final Class<V> propertyValueClass;
   private final String description;
   private final String name;
   private final V defaultValue;
   private final boolean nullable;
   private final boolean constructorParameter;
   private final OperationTemplate[] validationOperations;

   public AbstractProperty(Storage config, Class<V> propertyValueClass) {
      this.propertyValueClass = propertyValueClass;
      this.name = config.get("name", config.getStorageKey());
      this.description = config.get("description", name);
      this.defaultValue = config.get("default-value", propertyValueClass);
      List<OperationTemplate> validationOperationsList = config.getCollection("validation-operations", List.class, OperationTemplate.class, false);
      this.validationOperations = (validationOperationsList != null) ? CollectionUtils.asArrayOfType(OperationTemplate.class, validationOperationsList) : null;
      this.nullable = config.get("nullable", true);
      this.constructorParameter = config.get("constructor-parameter", false);
   }

   @Override
   public Class<V> getPropertyValueClass() {
      return propertyValueClass;
   }

   @Override
   public String getDescription() {
      return description;
   }

   @Override
   public String getKey() {
      return name;
   }

   @Override
   public boolean isNullable() {
      return nullable;
   }

   protected V getDefaultValue(PropertyHolder pdh) {
      return defaultValue;
   }

   @Override
   public boolean isConstructorParameter() {
      return constructorParameter;
   }

   @Override
   public OperationTemplate[] getValidationOperations() {
      return validationOperations;
   }

   @Override
   public V getValue(PropertyHolder ph) {
      verifyHasProperty(ph);
      return _getValue(ph);

   }

   protected abstract V _getValue(PropertyHolder ph);

   protected void verifyHasProperty(PropertyHolder ph) {
      if (!ph.hasProperty(this)) {
         throw new IllegalArgumentException(ph.getTypeAndValueDescription() + " does not have property " + getTypeAndValueDescription());
      }
   }

}
