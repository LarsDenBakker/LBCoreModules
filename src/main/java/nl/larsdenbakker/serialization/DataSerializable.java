package nl.larsdenbakker.serialization;

/**
 * An Object that can be serialized.
 *
 * @author Lars den Bakker <larsdenbakker at gmail.com>
 */
public interface DataSerializable {

   /**
    * @return the serialized object. This Object's .toString() method
    * must return a String that can be de-serialized into the same Object,
    * or an Array, Collection or Map containing the same type of Objects.
    */
   public Object toSerializable();

}
