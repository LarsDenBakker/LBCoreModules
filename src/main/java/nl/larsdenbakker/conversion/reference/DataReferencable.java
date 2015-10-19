package nl.larsdenbakker.conversion.reference;

/**
 * An interface for types that must be turned into a DataRefernce to ensure
 * registry consistency. See the DataReference class for more information.
 *
 * @author Lars den Bakker <larsdenbakker at gmail.com>
 */
public interface DataReferencable {

   public DataReference getDataReference();

}
