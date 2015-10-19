package nl.larsdenbakker.registry;

/**
 * Default implementation of Registrable.
 *
 * @author Lars den Bakker <larsdenbakker at gmail.com>
 */
public abstract class AbstractRegisterable implements Registrable {

   @Override
   public boolean equals(Object obj) {
      if (obj != null && obj.getClass().isAssignableFrom(getClass())) {
         return getKey().equals(((Registrable) obj).getKey());
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return getKey().hashCode();
   }

}
