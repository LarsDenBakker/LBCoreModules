package nl.larsdenbakker.registry;

/**
 * The root of a Registry path. In essence a registry of registries. All registries must be registered to this Registry,
 * or have one of it's parents lead to it.
 *
 * @author Lars den Bakker<larsdenbakker@gmail.com>
 */
public class RootRegistry extends RegisterableRegistry<String, Registry<?, ?>> {

   public RootRegistry(RegistryModule registryModule) {
      super(registryModule, registryModule, String.class, (Class<Registry<?, ?>>) ((Class) Registry.class));
   }

   /**
    * Register a value and create a register RegistryValueConverter for the Registry.
    * See the RegistryValueConverter class for more information.
    *
    * @param key
    * @param val
    *
    * @return
    */
   @Override
   public boolean register(String key, Registry<?, ?> val) {
      if (super.register(key, val)) {
         RegistryValueConverter converter = new RegistryValueConverter(val);
         getConversionModule().registerSuperTypeConverter(converter);
         return true;
      } else {
         return false;
      }
   }

   /**
    * See register(String, Registry).
    */
   @Override
   public boolean register(Registry<?, ?> val) {
      return register(val.getKey(), val);
   }

   /**
    * Unregister a Registry and it's RegistryValueConverter.
    *
    * @param key The key for the Registry.
    *
    * @return The Registry if it was found.
    */
   @Override
   public Registry<?, ?> unregister(String key) {
      Registry<?, ?> reg = super.unregister(key);
      if (reg != null) {
         getConversionModule().unregisterSuperConverter(reg.getValueType());
      }
      return reg;
   }

   /**
    * Unregister a Registry and it's RegistryValueConverter.
    *
    * @param val The Registry.
    *
    * @return Whether or not unregistration was successful.
    */
   @Override
   public boolean unregisterByValue(Registry<?, ?> val) {
      if (super.unregisterByValue(val)) {
         getConversionModule().unregisterSuperConverter(val.getValueType());
         return true;
      } else {
         return false;
      }
   }

   /**
    * Get a Registry registered to this RootRegistry matching the given class.
    *
    * @param <A>           The Registry type.
    * @param registryClass The Registry type class.
    *
    * @return The Registry if it was found, otherwise null.
    */
   public <A extends Registry> A getByRegistryClass(Class<A> registryClass) {
      for (Registry reg : this.getAll()) {
         if (registryClass.isAssignableFrom(reg.getClass())) {
            return (A) reg;
         }
      }
      return null;
   }

   /**
    * Get a Registry registered to this RootRegistry that registers values matching the given type.
    *
    *
    * @param registerableClass The registered value type.
    *
    * @return The Registry if it was found, otherwise null.
    */
   public Registry<?, ?> getByRegisterableClass(Class<?> registerableClass) {
      for (Registry reg : getAll()) {
         if (reg.getValueType().isAssignableFrom(registerableClass)) {
            return reg;
         }
      }
      return null;
   }

   @Override
   public String getKeyFor(Registry<?, ?> val) {
      return val.getKey();
   }

   @Override
   public String getPluralDataValueDescription() {
      return "Registries";
   }

   @Override
   public String getDataValueDescription() {
      return "Registry";
   }

   @Override
   public String getKey() {
      return "registries";
   }

}
