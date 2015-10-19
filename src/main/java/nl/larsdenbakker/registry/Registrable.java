package nl.larsdenbakker.registry;

import nl.larsdenbakker.app.Application;
import nl.larsdenbakker.app.Console;
import nl.larsdenbakker.app.Module;
import nl.larsdenbakker.conversion.reference.DataReferencable;
import nl.larsdenbakker.conversion.reference.DataReference;
import nl.larsdenbakker.datapath.DataPathNode;
import nl.larsdenbakker.serialization.DataSerializable;
import nl.larsdenbakker.util.TextUtils;

/**
 * An interface that an Object is registered in a Registry. Automates several
 * registration processes.
 *
 * @param <K> The key type this type is identified by.
 *
 * @author Lars den Bakker<larsdenbakker@gmail.com>
 */
public interface Registrable<K> extends DataSerializable, DataReferencable {

   /**
    * @return The key for this Registrable.
    */
   public K getKey();

   /**
    * @return The Registry this Registrable is registered to. This can be null
    * if the Registrable is not registered to any. It is good practice to create
    * and register, and delete and unregister Registrables at the same time.
    */
   public Registry getParentRegistry();

   /**
    * Set which Registry this Registrable is registered to.
    *
    * @param parentRegistry
    */
   public void setParentRegistry(Registry parentRegistry);

   /**
    * @return Serialize this Registrable as it's data path.
    */
   @Override
   public default Object toSerializable() {
      return getDataPath().getFullPath();
   }

   /**
    * @return The Module this Registrable belongs to.
    */
   public Module getParentModule();

   public default Application getParentApplication() {
      return getParentModule().getParentApplication();
   }

   public default Console getConsole() {
      return getParentApplication().getConsole();
   }

   /**
    * @return The RootRegistry this Registrable belongs to.
    */
   public default RootRegistry getRootRegistry() {
      Registry parent = getParentRegistry();
      while (parent != null) {
         if (parent instanceof RootRegistry) {
            return (RootRegistry) parent;
         }
         parent = parent.getParentRegistry();
      }
      throw new IllegalStateException(TextUtils.getTypeAndValueDescription(this) + " does not lead to a RootRegistry.");
   }

   /**
    * @return The DataPath from this Registrable to the RootRegistry.
    */
   public default DataPathNode getDataPath() {
      if (getParentRegistry() != null) {
         return DataPathNode.of(getParentRegistry().getRegistryModule().getDataPathModule(), getParentRegistry(), this);
      } else {
         throw new IllegalStateException("Cannot retreive DataPath from unregistered Registrable");
      }
   }

   /**
    * @return The DataPath of this Registrable as a DataReference.
    */
   @Override
   public default DataReference getDataReference() {
      return getDataPath();
   }

}
