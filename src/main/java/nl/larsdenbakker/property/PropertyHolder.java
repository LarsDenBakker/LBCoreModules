package nl.larsdenbakker.property;

import nl.larsdenbakker.util.InitializationException;
import nl.larsdenbakker.property.properties.Property;
import nl.larsdenbakker.property.properties.Properties;
import java.util.Map;
import nl.larsdenbakker.app.Module;
import nl.larsdenbakker.datapath.AbstractDataHolder;
import nl.larsdenbakker.conversion.ConversionModule;
import nl.larsdenbakker.property.properties.PropertyModificationException;
import nl.larsdenbakker.registry.Registrable;
import nl.larsdenbakker.registry.Registry;
import nl.larsdenbakker.util.Describable;
import nl.larsdenbakker.storage.Storage;
import nl.larsdenbakker.operation.OperationModule;
import nl.larsdenbakker.util.TextUtils;

/**
 * A type of DataHolder whose data is held in Properties. See the Property and
 * Properties classes for more information.
 *
 * @author Lars den Bakker <larsdenbakker at gmail.com>
 */
public abstract class PropertyHolder<K> extends AbstractDataHolder implements Describable, Registrable<K> {

   private final Module parentModule;
   private final ConversionModule conversionModule;
   private final Storage storage;

   private boolean initialized = false;

   private Registry parentRegistry;

   public PropertyHolder(Module parentModule, ConversionModule conversionModule, Storage storage) {
      this.parentModule = parentModule;
      this.conversionModule = conversionModule;
      this.storage = storage;
   }

   @Override
   public Module getParentModule() {
      return parentModule;
   }

   @Override
   public ConversionModule getConversionModule() {
      return conversionModule;
   }

   public Storage getStorage() {
      return storage;
   }

   /**
    * Initialize this PropertyHolder, loading and verifying it's properties.
    * A PropertyHolder that has not been initialized or whose initialization
    * failed will throw an exception when accessed. This method should be
    * called only once, typically by the PropertyHolderRegistry that
    * handles PropertyHolders of this type before registration.
    *
    * @throws InitializationException if the PropertyHolder could not be initialized.
    */
   public final void initialize() throws InitializationException {
      if (!isInitialized()) {
         try {
            verifyProperties();
            _initialize();
            initialized = true;
         } catch (PropertyValidationException ex) {
            throw new InitializationException(ex);
         }
      } else {
         throw new IllegalStateException("Called initialize() on " + TextUtils.getTypeDescription(this) + " while already initialized.");
      }
   }

   /**
    * Method to allow subclasses to safely initialize this object.
    */
   protected void _initialize() throws InitializationException {
   }

   protected final boolean isInitialized() {
      return initialized;
   }

   /**
    * Get the value of a Property registered to this PropertyHolder. Can be null.
    * PropertyHolder must have the given property. This can be checked with
    * PropertyHolder.hasProperty(Property).
    *
    * @param <T>      The Property value type.
    * @param property The Property.
    *
    * @return The Property value. Can be null if nothing is set.
    */
   public final <T> T getPropertyValue(Property<T> property) {
      return property.getValue(this);
   }

   /**
    * Set the value of a Property registered to this PropertyHolder.
    * PropertyHolder must have the given property. This can be checked with
    * PropertyHolder.hasProperty(Property).
    *
    * @param <T>      The Property value type.
    * @param property The Property.
    * @param value    The value to set.
    *
    * @throws PropertyModificationException if the value was invalid.
    */
   public <T> void setPropertyValue(Property<T> property, Object value) throws PropertyModificationException {
      property.setValue(this, value);
   }

   /**
    * Set a valid value of a Property registered to this PropertyHolder.
    * The value is still checked for validity internally, but a RuntimeException
    * is thrown instead. This is useful when values are hard coded and must be
    * garaunteed to be correct.
    *
    * @param <T>      The Property value type.
    * @param property The Property.
    * @param value    The value to set.
    *
    */
   public <T> void setValidPropertyValue(Property<T> property, T value) {
      property.setValidValue(this, value);
   }

   /**
    * Add given value to the value of a Property registered to this PropertyHolder.
    *
    * @param <T>      The Property value type.
    * @param property The Property.
    * @param value    The value to add.
    *
    * @throws PropertyModificationException if the value was invalid.
    */
   public <T> void addToPropertyValue(Property<T> property, Object value) throws PropertyModificationException {
      property.setValue(this, value);
   }

   /**
    * Remove given value from the value of a Property registered to this PropertyHolder.
    *
    * @param <T>      The Property value type.
    * @param property The Property.
    * @param value    The value to add.
    *
    * @throws PropertyModificationException if the value was invalid.
    */
   public <T> void removeFromPropertyValue(Property<T> property, Object value) throws PropertyModificationException {
      property.setValue(this, value);
   }

   /**
    * Clear the value of a Property registered to this PropertyHolder.
    *
    * @param <T>      The Property value type.
    * @param property The Property.
    *
    * @throws PropertyModificationException if the value could not be cleared.
    */
   public <T> void clearPropertyValue(Property<T> property) throws PropertyModificationException {
      property.clearValue(this);
   }

   /**
    *
    * @param prop The Property you want to check.
    *
    * @return Whether or not this type of PropertyHolder has the provided
    * Property.
    */
   public boolean hasProperty(Property<?> prop) {
      return getProperties().hasProperty(prop);
   }

   /**
    * @return The Properties object associated with this PropertyHolder type.
    */
   public abstract Properties getProperties();

   /**
    *
    * @param key The key of the Property.
    *
    * @return A property from this PropertyHolder's Properties references by
    * this key.
    */
   public final Property<?> getProperty(String key) {
      return getProperties().getProperty(key);
   }

   /**
    * Implementation of DataHolder data getter. The key references to a
    * property, and the property's value for this PropertyHolder is returned.
    *
    * @param key The key referencing to a Property.
    *
    * @return The property value if found, otherwise null.
    */
   @Override
   protected Object _getDataValue(Object key) {
      Property prop = getProperty(key.toString());
      if (prop != null) {
         return getPropertyValue(prop);
      } else {
         return null;
      }
   }

   private final void verifyProperties() throws PropertyValidationException {
      //Check if all properties that are not nullable are set properly
      for (Property prop : getProperties().getAll()) {
         prop.validate(this);
      }
   }

   @Override
   public Map<String, Object> getContents() {
      return storage.getContents();
   }

   @Override
   public String getDataValueDescription() {
      return "Property";
   }

   @Override
   public Registry getParentRegistry() {
      return parentRegistry;
   }

   @Override
   public void setParentRegistry(Registry parentRegistry) {
      this.parentRegistry = parentRegistry;
   }

   @Override
   public Object toSerializable() {
      return Registrable.super.toSerializable();
   }

   @Override
   public Object convertToKey(String stringKey) {
      return stringKey;
   }

   public PropertyModule getPropertyModule() {
      return getProperties().getPropertyModule();
   }

   public OperationModule getOperationModule() {
      return getPropertyModule().getOperationModule();
   }

   @Override
   public boolean equals(Object obj) {
      if (obj != null && obj.getClass().isAssignableFrom(getClass())) {
         return getKey().equals(((Registrable) obj).getKey());
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return getKey().hashCode();
   }

}
