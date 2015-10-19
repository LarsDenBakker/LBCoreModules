package nl.larsdenbakker.datapath;

/**
 * The root of a DataPath.
 *
 * @author Lars den Bakker <larsdenbakker at gmail.com>
 */
public abstract class DataPathRoot extends DataPathNode<DataHolder> {

   @Override
   public String getFullPath() {
      return "";
   }

   @Override
   protected Object toKey(String stringKey) {
      return getDataValue().convertToKey(stringKey);
   }



}
