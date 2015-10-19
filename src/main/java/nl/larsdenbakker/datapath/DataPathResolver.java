package nl.larsdenbakker.datapath;

/**
 *
 * @author Lars den Bakker<larsdenbakker@gmail.com>
 */
public abstract class DataPathResolver<T> {

   private final Class<T> resolvedType;

   public DataPathResolver(Class<T> resolvedType) {
      this.resolvedType = resolvedType;
   }

   public abstract Object resolvePath(T t, String path) throws DataPathResolveException;

   public Class<T> getResolvedType() {
      return resolvedType;
   }

   public abstract String getResolvedTypeDescription();

}
