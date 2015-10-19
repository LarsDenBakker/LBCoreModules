package nl.larsdenbakker.conversion.reference;

import java.util.Collection;

/**
 * Marker interface to indicate that a Collection holds DataReferences.
 * Implementations should wrap any values that are set in a DataReference
 * and unwrap DataReferences during get and iteration operations.
 *
 * @author Lars den Bakker<larsdenbakker@gmail.com>
 */
public interface DataReferenceCollection<E extends DataReferencable> extends Collection<E> {

}
