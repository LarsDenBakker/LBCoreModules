package nl.larsdenbakker.operation.operations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import nl.larsdenbakker.datapath.DataHolder;
import nl.larsdenbakker.property.PropertyHolder;
import nl.larsdenbakker.property.properties.Property;
import nl.larsdenbakker.storage.Storage;
import nl.larsdenbakker.operation.OperationContext;
import nl.larsdenbakker.app.InvalidInputException;
import nl.larsdenbakker.util.OperationResponse;
import nl.larsdenbakker.util.TextUtils;

/**
 * Operation to read and describe a DataHolder. All the DataHolder's
 * contents are queried and described.
 *
 * @author Lars den Bakker <larsdenbakker at gmail.com>
 */
public class DataHolderInfoOperation extends TargetedOperation<DataHolder> {

   public static final String KEY_KEY_FILTERS = "key-filters".intern();
   public static final String KEY_VALUE_FILTERS = "value-filters".intern();

   private final List<String> keyFilters;
   private final List<String> valueFilters;

   public DataHolderInfoOperation(OperationContext context, Storage storage) throws InvalidInputException {
      super(context, storage, DataHolder.class);
      keyFilters = storage.getCollection(KEY_KEY_FILTERS, ArrayList.class, String.class);
      valueFilters = storage.getCollection(KEY_VALUE_FILTERS, ArrayList.class, String.class);
   }

   @Override
   protected OperationResponse _execute() {
      DataHolder target = getTarget();
      Map<String, Object> contents;
      if (target instanceof PropertyHolder) {
         contents = new HashMap();
         PropertyHolder ph = (PropertyHolder) target;
         for (Property p : ph.getProperties().getAll()) {
            contents.put(p.getKey(), ph.getPropertyValue(p));
         }
      } else {
         contents = target.getContents();
      }
      List<String> responses = new ArrayList();

      for (Entry<String, Object> entry : contents.entrySet()) {
         boolean filtered = true;
         if (keyFilters != null || valueFilters != null) {
            if (keyFilters != null) {
               String key = entry.getKey().toLowerCase();
               for (String filter : keyFilters) {
                  if (!key.contains(filter.toLowerCase())) {
                     filtered = false;
                  }
               }
            }
            if (filtered) {
               if (valueFilters != null) {
                  String value = TextUtils.getDescription(entry.getValue()).toLowerCase();
                  for (String filter : valueFilters) {
                     if (!value.contains(filter.toLowerCase())) {
                        filtered = false;
                     }
                  }
               }
            }
         }
         if (filtered) {
            responses.add(TextUtils.getDescription(entry.getKey()) + ": " + TextUtils.getDescription(entry.getValue(), ""));
         }
      }
      return OperationResponse.succeeded(target.getTypeAndValueDescription()).addMessages(responses);
   }

}
