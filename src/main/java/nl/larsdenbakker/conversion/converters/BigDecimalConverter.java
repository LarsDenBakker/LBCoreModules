package nl.larsdenbakker.conversion.converters;

import java.math.BigDecimal;
import java.math.RoundingMode;
import nl.larsdenbakker.conversion.ConversionException;

/**
 *
 * @author Lars den Bakker <larsdenbakker at gmail.com>
 */
public class BigDecimalConverter extends DataConverter<BigDecimal> {

   private final DoubleConverter doubleParser = new DoubleConverter();
   private final int scale;

   public BigDecimalConverter(int scale) {
      super(BigDecimal.class);
      this.scale = scale;
   }

   public BigDecimalConverter() {
      this(2);
   }

   @Override
   protected BigDecimal _convert(Object input) throws ConversionException {
      return BigDecimal.valueOf(doubleParser.convert(input)).setScale(2, RoundingMode.UP);
   }

}
