package nl.larsdenbakker.property.properties;

import java.util.UUID;
import nl.larsdenbakker.property.PropertyHolder;
import nl.larsdenbakker.storage.Storage;

/**
 *
 * @author Lars den Bakker<larsdenbakker@gmail.com>
 */
public class UUIDProperty extends SimpleProperty<UUID> {

   private final boolean autoGenerate;

   public UUIDProperty(Storage storage) {
      super(storage, UUID.class);
      this.autoGenerate = storage.get("auto-generate", Boolean.class, true);
   }

   @Override
   public UUID getDefaultValue(PropertyHolder dh) {
      if (autoGenerate) {
         UUID uuid = UUID.randomUUID();
         setValidValue(dh, uuid);
         return uuid;
      }
      return null;
   }

   @Override
   public String getTypeDescription() {
      return "UUID Property";
   }
}
