package nl.larsdenbakker.datafile;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import nl.larsdenbakker.serialization.SerializationModule;
import nl.larsdenbakker.util.FileUtils;

/**
 * A wrapper class around a java.io.File instance and serialization and
 * deserialization information. An Object can be bound to a DataFile
 * and scheduled to be serialized and saved at regular intervals.
 *
 * @author Lars den Bakker <larsdenbakker at gmail.com>
 */
public class DataFile {

   private final File file;
   private final DataFormat format;
   private final DataFileModule dataFileModule;

   private Thread saveThread;
   private WeakReference<Object> toSave;

   protected DataFile(DataFileModule dataFileModule, File file, DataFormat format) {
      this.dataFileModule = dataFileModule;
      this.file = file;
      this.format = format;
   }

   public DataFileModule getDataFileManager() {
      return dataFileModule;
   }

   public SerializationModule getSerializationModule() {
      return dataFileModule.getSerializationModule();
   }

   public DataFormat getFormat() {
      return format;
   }

   public File getFile() {
      return file;
   }

   /**
    * Load the contents of this file using this DataFile's DataFormat.
    *
    * @return A map with the contents of the file as read by the DataFormat.
    * @throws DataFileException If there were I/O problems during loading.
    */
   public Map<String, Object> load() throws DataFileException {
      try {
         FileUtils.createAndTestReadWrite(file);
      } catch (IOException ex) {
         throw new DataFileException(ex.getMessage()).addFailedAction("Loading " + file);
      }
      try {
         return format.getObjectMapper().readValue(file, Map.class);
      } catch (IOException e) {
         return new HashMap();
      }
   }

   /**
    * Serialize the provided Object and save it to this DataFile's file
    * location.
    *
    * @param obj The Object that is to be serialized.
    * @throws nl.larsdenbakker.datafile.DataFileException
    */
   public void save(Object obj) throws DataFileException {
      try {
         FileUtils.createAndTestReadWrite(file);
         format.getObjectMapper().writeValue(file, getSerializationModule().toSerializable(obj));
      } catch (IOException ex) {
         throw new DataFileException(ex.getMessage()).addFailedAction("Saving " + file);
      }
   }

   protected void save() throws DataFileException {
      if (toSave != null) {
         Object obj = toSave.get();
         if (obj != null) {
            save(obj);
         }
      }
   }

   /**
    * Schedule the provided Object to be serialized and saved to this DataFile's
    * file location at a regular interval until it is canceled or this
    * DataFile's module is unloaded. The auto-save delay can be set at this
    * DataFile's DataFileModule. Scheduled DataFiles are automatically saved
    * when the module is asked to perform a saveAll().
    *
    * @param toSave The Object to be saved.
    */
   public void scheduleAutoSave(Object toSave) {
      this.toSave = new WeakReference(toSave);
      dataFileModule.scheduleAutoSave(this);
   }

   /**
    * Cancel the scheduled autosave, if any.
    */
   public void cancelAutoSave() {
      dataFileModule.unscheduleAutoSaving(this);
   }

}
