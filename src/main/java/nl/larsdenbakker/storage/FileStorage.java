package nl.larsdenbakker.storage;

import java.io.IOException;
import nl.larsdenbakker.conversion.ConversionModule;
import nl.larsdenbakker.datafile.DataFile;
import nl.larsdenbakker.datafile.DataFileException;

/**
 * A type of Storage wrapped around a DataFile. Loading the DataFile will
 * override the Storage's contents with the contents of the file. Saving will
 * save the Storage's contents of the file.
 *
 * @author Lars den Bakker<larsdenbakker@gmail.com>
 */
public class FileStorage extends MemoryStorageRoot {

   private final DataFile dataFile;

   public FileStorage(ConversionModule conversionHandler, DataFile dataFile) {
      super(conversionHandler, "");
      this.dataFile = dataFile;
   }

   /**
    * Load the associated DataFile, overriding the Storage's contents.
    *
    * @throws DataFileException Thrown if anything goes wrong during loading.
    */
   public void load() throws DataFileException {
      overrideContents(dataFile.load());
   }

   /**
    * Saves the Storage's contents to the DataFile.
    *
    * @throws DataFileException Thrown if anything goes wrong during saving.
    */
   public void save() throws DataFileException {
      dataFile.save(getContents());
   }

   /**
    * Schedule this storage to be automatically saved to the DataFile during
    * regular intervals. See DataFile for more information on auto-saving.
    */
   public void scheduleAutoSave() {
      dataFile.scheduleAutoSave(this.getContents());
   }

   /**
    * Cancel any schedules auto-saving.
    */
   public void cancelAutoSave() {
      dataFile.cancelAutoSave();
   }

}
