package nl.larsdenbakker.conversion.converters;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import nl.larsdenbakker.conversion.ConversionException;
import nl.larsdenbakker.util.TimeUtils;

/**
 *
 * @author Lars den Bakker <larsdenbakker at gmail.com>
 */
public class LocalDateConverter extends DataConverter<LocalDate> {

   public LocalDateConverter() {
      super(LocalDate.class);
   }

   @Override
   protected LocalDate _convert(Object input) throws ConversionException {
      try {
         return LocalDate.parse(input.toString(), TimeUtils.DEFAULT_FORMAT);
      } catch (DateTimeParseException ex) {
         throw new ConversionException("Unable to parse " + input.toString() + ". Format: day-month-year");
      }
   }

}
