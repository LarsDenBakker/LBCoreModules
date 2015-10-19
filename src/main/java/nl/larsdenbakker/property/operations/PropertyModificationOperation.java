package nl.larsdenbakker.property.operations;

import nl.larsdenbakker.property.PropertyHolder;
import nl.larsdenbakker.property.properties.Property;
import nl.larsdenbakker.property.properties.PropertyModificationException;
import nl.larsdenbakker.storage.Storage;
import nl.larsdenbakker.operation.OperationContext;
import nl.larsdenbakker.operation.operations.TargetedOperation;
import nl.larsdenbakker.util.Describable;
import nl.larsdenbakker.app.InvalidInputException;
import nl.larsdenbakker.util.OperationResponse;
import nl.larsdenbakker.util.TextUtils;

/**
 * An operation to handle modify a PropertyHolder Property. A
 * PropertyModificationOperation is a TargetedOperation, the target is a
 * PropertyHolder. The Operation further requires the Property to be modified,
 * the ModificationOperator of how to the Property should be modified an
 * argument to pass as modification.
 *
 * Keys can be found in the constants: KEY_PROPERTY, KEY_OPERATOR, KEY_ARGUMENT
 *
 * @author Lars den Bakker <larsdenbakker at gmail.com>
 */
public class PropertyModificationOperation extends TargetedOperation<PropertyHolder> {

   public static enum ModificationOperator implements Describable {

      SET("Set"),
      CLEAR("Clear"),
      ADD("Add"),
      REMOVE("Remove");

      private final String description;

      private ModificationOperator(String description) {
         this.description = description;
      }

      @Override
      public String getTypeDescription() {
         return "Operator";
      }

      @Override
      public String getDescription() {
         return description;
      }
   }

   public static final String KEY_PROPERTY = "property".intern();
   public static final String KEY_OPERATOR = "operator".intern();
   public static final String KEY_ARGUMENT = "argument".intern();

   private final String propertyName;
   private final ModificationOperator operator;
   private final Object argument;

   public PropertyModificationOperation(OperationContext context, Storage storage) throws InvalidInputException {
      super(context, storage, PropertyHolder.class);
      this.propertyName = storage.getAndAssert(KEY_PROPERTY, String.class);
      this.operator = storage.getAndAssert(KEY_OPERATOR, ModificationOperator.class);
      switch (operator) {
         case CLEAR:
            argument = null;
            break;
         default:
            argument = storage.getAndAssert(KEY_ARGUMENT, Object.class);
      }
   }

   public ModificationOperator getOperator() {
      return operator;
   }

   public Object getArgument() {
      return argument;
   }

   @Override
   protected OperationResponse _execute() {
      PropertyHolder target = getTarget();
      Property property = target.getProperty(propertyName);
      if (property != null) {
         try {
            switch (operator) {
               case SET:
                  target.setPropertyValue(property, argument);
                  break;
               case CLEAR:
                  target.clearPropertyValue(property);
                  break;
               case ADD:
                  target.addToPropertyValue(property, argument);
                  break;
               case REMOVE:
                  target.removeFromPropertyValue(property, argument);
                  break;
            }
            return getSucceededResponse();
         } catch (PropertyModificationException ex) {
            return OperationResponse.failed(ex.getMessage());
         }
      } else {
         return OperationResponse.failed(target.getTypeAndValueDescription() + " does not have Property " + propertyName);
      }
   }

   public String getProperty() {
      return propertyName;
   }

   private OperationResponse getSucceededResponse() {
      return getSuccessResult(argument);
   }

   private OperationResponse getSuccessResult(Object argument) {
      String valueString = TextUtils.getDescription(argument);
      switch (operator) {
         case SET:
            return OperationResponse.succeeded("You have set " + propertyName + " to: " + valueString);
         case ADD:
            return OperationResponse.succeeded("You have added " + valueString + " to " + propertyName);
         case REMOVE:
            return OperationResponse.succeeded("You have removed " + valueString + " from " + propertyName);
         case CLEAR:
            return OperationResponse.succeeded("You have cleared " + propertyName);
         default:
            return OperationResponse.succeeded("Successfully performed operation: " + operator);
      }
   }

}
