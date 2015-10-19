package nl.larsdenbakker.datafile;

import static com.google.common.base.Preconditions.checkNotNull;
import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.larsdenbakker.app.AbstractModule;
import nl.larsdenbakker.app.Application;
import nl.larsdenbakker.app.Module;
import nl.larsdenbakker.serialization.SerializationModule;
import nl.larsdenbakker.util.CollectionUtils;
import nl.larsdenbakker.util.TimeUtils;
import nl.larsdenbakker.app.UserInputException;

/**
 * A module that handles loading and saving of files. Depends on the
 * SerializationModule. This modules ensures data safety of all scheduled
 * DataFiles during a regular uninterrupted life-cycle of this module, so long
 * as it is properly shut down.
 *
 * When this module's saveAll() method is called (this normally happens when the
 * application shuts down or reloads), all it's scheduled auto-save files are
 * saved as well.
 *
 * @author Lars den Bakker <larsdenbakker at gmail.com>
 */
public class DataFileModule extends AbstractModule {

   private long autoSaveDelay = TimeUtils.MINUTE_MS * 30;
   private DataFormat defaultDataFormat = DataFormat.JSON;

   private final List<WeakReference<DataFile>> scheduled = new ArrayList<>();
   private final List<WeakReference<DataFile>> dataFiles = new ArrayList<>();
   private boolean autoSave;
   private Thread autoSaveThread = null;
   private AutoSaveTask autoSaveTask = null;

   public DataFileModule(Application parentApplication, boolean autoSave) {
      super(parentApplication);
      this.autoSave = autoSave;
   }

   public DataFileModule(Application parentApplication) {
      super(parentApplication);
      this.autoSave = true;
   }

   @Override
   public String getName() {
      return "data-files";
   }

   @Override
   public Class<? extends Module>[] getDependencies() {
      return CollectionUtils.asArray(SerializationModule.class);
   }

   @Override
   protected void _load() {
      if (autoSave) {
         enableAutosaving();
      }
   }

   @Override
   protected void _unload() {
      for (WeakReference ref : scheduled) {
         ref.clear();
      }
      scheduled.clear();
      for (WeakReference ref : dataFiles) {
         ref.clear();
      }
      dataFiles.clear();
      autoSave = true;
   }

   @Override
   protected void _saveToDisk() throws UserInputException {
      saveAll();
   }

   public SerializationModule getSerializationModule() {
      return getParentApplication().getModule(SerializationModule.class);
   }

   /**
    * Create a new DataFile of the provided File and DataFormat.
    *
    * @param file   The File.
    * @param format The DataFormat.
    *
    * @return A newly created DataFile.
    */
   public DataFile createDataFile(File file, DataFormat format) {
      DataFile df = new DataFile(this, file, format);
      dataFiles.add(new WeakReference(df));
      return df;
   }

   /**
    * Create a new DataFile of the provided File and the DataFormat from
    * getDefaultDataFormat().
    *
    * @param file The File.
    *
    * @return A newly created DataFile.
    */
   public DataFile createDataFile(File file) {
      return createDataFile(file, getDefaultDataFormat());
   }

   public void setDefaultDataFormat(DataFormat defaultDataFormat) {
      this.defaultDataFormat = defaultDataFormat;
   }

   public DataFormat getDefaultDataFormat() {
      return defaultDataFormat;
   }

   /**
    * Enable auto-saving of scheduled DataFiles. Note that this is called by
    * default during loading. This should only be used to start up auto-saving
    * again after you have disabled it.
    */
   public void enableAutosaving() {
      disableAutosaving();
      autoSaveTask = new AutoSaveTask();
      autoSaveThread = new Thread(autoSaveTask);
      autoSaveThread.start();
   }

   /**
    * Enable auto-saving of scheduled DataFiles at the specified interval. Note
    * that this is called by default during loading. This should only be used to
    * start up auto-saving again after you have disabled it.
    *
    * @param autoSaveDelay The auto-save delay in milliseconds.
    */
   public void enableAutosaving(long autoSaveDelay) {
      disableAutosaving();
      autoSaveTask = new AutoSaveTask();
      autoSaveThread = new Thread(autoSaveTask);
      this.autoSaveDelay = autoSaveDelay;
      this.autoSaveThread = new Thread(autoSaveThread);
      autoSaveThread.start();
   }

   /**
    * Cancel any active auto-save.
    */
   public void disableAutosaving() {
      if (autoSaveTask != null) {
         autoSaveTask.setCanceled(true);
      }
      autoSaveTask = null;
      autoSaveThread = null;
      autoSave = false;
   }

   protected void scheduleAutoSave(DataFile df) {
      checkNotNull(df);
      WeakReference<DataFile> ref = new WeakReference(df);
      scheduled.add(ref); //No danger in adding to list that is accessed async because we are using .iterator() elsewhere
   }

   protected void unscheduleAutoSaving(DataFile df) {
      Iterator<WeakReference<DataFile>> it = scheduled.iterator();
      while (it.hasNext()) {
         WeakReference<DataFile> ref = it.next();
         if (ref != null) {
            DataFile rdf = ref.get();
            if (rdf != null) {
               if (rdf.equals(df)) {
                  it.remove();
               }
            } else {
               it.remove();
            }
         } else {
            it.remove();
         }
      }
   }

   /**
    * Save all scheduled files synchronously.
    */
   public void saveAll() throws DataFileException {
      save(dataFiles);
   }

   /**
    * Save all scheduled files asynchronously.
    */
   public void saveAllAsync() {
      new Thread(new DataFileSaveTask()).start();
   }

   private void save(List<WeakReference<DataFile>> toSave) throws DataFileException {
      Iterator<WeakReference<DataFile>> it = toSave.iterator();
      while (it.hasNext()) {
         WeakReference<DataFile> ref = it.next();
         if (ref != null) {
            DataFile df = ref.get();
            if (df != null) {
               df.save();
            } else {
               it.remove();
            }
         } else {
            it.remove();
         }
      }
   }

   private void autosave() throws DataFileException {
      save(scheduled);
   }

   public class AutoSaveTask implements Runnable {

      private boolean canceled = false;

      @Override
      public void run() {
         while (autoSave) {
            try {
               Thread.sleep(autoSaveDelay);
            } catch (InterruptedException ex) {
               break;
            }
            if (autoSave && !canceled) {
               try {
                  autosave();
               } catch (DataFileException ex) {
                  Logger.getLogger(DataFileModule.class.getName()).log(Level.SEVERE, null, ex);
               }
            }
         }
      }

      public void setCanceled(boolean canceled) {
         this.canceled = canceled;
      }

   }

   public class DataFileSaveTask implements Runnable {

      @Override
      public void run() {
         try {
            saveAll();
         } catch (DataFileException ex) {
            Logger.getLogger(DataFileModule.class.getName()).log(Level.SEVERE, null, ex);
         }
      }

   }

}
