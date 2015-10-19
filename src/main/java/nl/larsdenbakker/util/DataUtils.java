package nl.larsdenbakker.util;

import nl.larsdenbakker.operation.operations.ComparisonOperator;
import java.util.Collection;
import nl.larsdenbakker.datapath.DataHolder;
import static nl.larsdenbakker.operation.operations.ComparisonOperator.EQUAL;

/**
 *
 * @author Lars den Bakker<larsdenbakker@gmail.com>
 */
public class DataUtils {

   public static boolean compare(ComparisonOperator operator, Object arg1, Object arg2) {
      if (operator == null || arg1 == null || arg2 == null) {
         return false;
      }
      if (arg1 instanceof Comparable) {
         if (arg2.getClass().isAssignableFrom(arg1.getClass())) {
            return DataUtils._compare(operator, (Comparable) arg1, (Comparable) arg2);
         }
      } else if (arg1 instanceof Collection) {
         if (arg2 instanceof Collection) {
            return _compare(operator, (Collection) arg1, (Collection) arg2);
         } else {
            return DataUtils._compare(operator, (Collection) arg1, arg2);
         }
      } else if (arg2 instanceof Collection) {
         return DataUtils._compare(operator, arg1, (Collection) arg2);
      }
      return DataUtils._compare(operator, arg1, arg2);
   }

   private static boolean _compare(ComparisonOperator operator, Object arg1, Object arg2) {
      switch (operator) {
         case NOT_EQUAL:
            return !arg1.equals(arg2);
         case EQUAL:
            return arg1.equals(arg2);
         case GREATER:
            return false;
         case GREATER_OR_EQUAL:
            return false;
         case LESS:
            return false;
         case LESS_OR_EQUAL:
            return false;
         default:
            throw new UnsupportedOperationException();
      }
   }

   private static <T extends Comparable> boolean _compare(ComparisonOperator operator, T arg1, T arg2) {
      int order = arg2.compareTo(arg1);
      switch (operator) {
         case NOT_EQUAL:
            return order != 0;
         case EQUAL:
            return order == 0;
         case GREATER:
            return order > 0;
         case GREATER_OR_EQUAL:
            return order >= 0;
         case LESS:
            return order < 0;
         case LESS_OR_EQUAL:
            return order <= 0;
         default:
            throw new UnsupportedOperationException();
      }
   }

   private static boolean _compare(ComparisonOperator operator, Object arg1, Collection<Object> arg2) {
      boolean returnValue = true;
      for (Object o : arg2) {
         returnValue = compare(operator, arg1, o);
         if (!returnValue) {
            break;
         }
      }
      return returnValue;
   }

   private static boolean _compare(ComparisonOperator operator, Collection<Object> arg1, Object arg2) {
      boolean returnValue = true;
      for (Object o : arg1) {
         returnValue = compare(operator, o, arg2);
         if (!returnValue) {
            break;
         }
      }
      return returnValue;
   }

   private static boolean _compare(ComparisonOperator operator, Collection<Object> arg1, Collection<Object> arg2) {
      boolean returnValue = true;
      outer:
      for (Object o1 : arg1) {
         inner:
         for (Object o2 : arg2) {
            returnValue = compare(operator, o1, o2);
            if (!returnValue) {
               break outer;
            }
         }
      }
      return returnValue;
   }

   public static boolean elementOf(ComparisonOperator operator, Object arg1, Object arg2) {
      if (operator == null || arg1 == null || arg2 == null) {
         return false;
      }

      if (arg1 instanceof Collection) {
         if (arg2 instanceof Collection) {
            return _elementOf(operator, (Collection) arg1, (Collection) arg2);
         } else if (arg2 instanceof DataHolder) {
            return _elementOf(operator, (Collection) arg1, (DataHolder) arg2);
         } else {
            return _elementOf(operator, (Collection) arg1, arg2);
         }
      } else if (arg1 instanceof DataHolder) {
         if (arg2 instanceof Collection) {
            return _elementOf(operator, (DataHolder) arg1, (Collection) arg2);
         } else if (arg2 instanceof DataHolder) {
            return _elementOf(operator, (DataHolder) arg1, (DataHolder) arg2);
         } else {
            return _elementOf(operator, (DataHolder) arg1, arg2);
         }
      } else {
         if (arg2 instanceof Collection) {
            return _elementOf(operator, arg1, (Collection) arg2);
         } else if (arg2 instanceof DataHolder) {
            return _elementOf(operator, arg1, (DataHolder) arg2);
         } else {
            return _elementOf(operator, arg1, arg2);
         }
      }
   }

   private static boolean _elementOf(ComparisonOperator operator, Object arg1, Object arg2) {
      switch (operator) {
         case NOT_EQUAL:
            return !arg1.equals(arg2);
         case EQUAL:
            return arg1.equals(arg2);
         case GREATER: //Not possible
            return false;
         case GREATER_OR_EQUAL: //Greater not possible
            return arg1.equals(arg2);
         case LESS: //Not possible
            return false;
         case LESS_OR_EQUAL: //Less not possible
            return arg1.equals(arg2);
         default:
            throw new UnsupportedOperationException();
      }
   }

   private static boolean _elementOf(ComparisonOperator operator, Object arg1, Collection<Object> arg2) {
      switch (operator) {
         case NOT_EQUAL:
            return arg2.size() != 1 || !arg2.contains(arg1);
         case EQUAL:
            return arg2.size() == 1 && arg2.contains(arg1);
         case GREATER: //Not possible
            return false;
         case GREATER_OR_EQUAL: //Greater not possible
            return _elementOf(EQUAL, arg1, arg2);
         case LESS:
            return arg2.size() > 1 && arg2.contains(arg1);
         case LESS_OR_EQUAL:
            return arg2.size() >= 1 && arg2.contains(arg1);
         default:
            throw new UnsupportedOperationException();
      }
   }

   private static boolean _elementOf(ComparisonOperator operator, Object arg1, DataHolder arg2) {
      switch (operator) {
         case NOT_EQUAL:
            return arg2.getContents().size() != 1 || arg2.getDataValue(arg1) != null;
         case EQUAL:
            return arg2.getContents().size() == 1 && arg2.getDataValue(arg1) == null;
         case GREATER: //Not possible
            return false;
         case GREATER_OR_EQUAL: //Greater not possible
            return _elementOf(EQUAL, arg1, arg2);
         case LESS:
            return arg2.getContents().size() > 1 && arg2.getDataValue(arg1) != null;
         case LESS_OR_EQUAL:
            return arg2.getContents().size() >= 1 && arg2.getDataValue(arg1) != null;
         default:
            throw new UnsupportedOperationException();
      }
   }

   private static boolean _elementOf(ComparisonOperator operator, Collection<Object> arg1, Object arg2) {
      switch (operator) {
         case NOT_EQUAL:
            return arg1.size() != 1 || !arg1.contains(arg2);
         case EQUAL:
            return arg1.size() == 1 && arg1.contains(arg2);
         case GREATER: //Not possible
            return arg1.size() > 1 && arg1.contains(arg2);
         case GREATER_OR_EQUAL: //Greater not possible
            return arg1.size() >= 1 && arg1.contains(arg2);
         case LESS: //Not possible
            return false;
         case LESS_OR_EQUAL:
            return _elementOf(EQUAL, arg1, arg2);
         default:
            throw new UnsupportedOperationException();
      }
   }

   private static boolean _elementOf(ComparisonOperator operator, Collection<Object> arg1, Collection<Object> arg2) {
      switch (operator) {
         case NOT_EQUAL:
            return arg1.size() != arg2.size() || !arg1.containsAll(arg2);
         case EQUAL:
            return arg1.size() == arg2.size() && arg1.containsAll(arg2);
         case GREATER:
            return arg1.size() > arg2.size() && arg1.containsAll(arg2);
         case GREATER_OR_EQUAL:
            return arg1.size() >= arg2.size() && arg1.containsAll(arg2);
         case LESS:
            return arg1.size() < arg2.size() && arg2.containsAll(arg1);
         case LESS_OR_EQUAL:
            return arg1.size() < arg2.size() && arg2.containsAll(arg1);
         default:
            throw new UnsupportedOperationException();
      }
   }

   private static boolean _elementOf(ComparisonOperator operator, Collection<Object> arg1, DataHolder arg2) {
      switch (operator) {
         case NOT_EQUAL:
            return arg2.getContents().size() != arg1.size() || arg2.getDataValue(arg1) != null;
         case EQUAL:
            return arg2.getContents().size() == 1 && arg2.getDataValue(arg1) == null;
         case GREATER: //Not possible
            return false;
         case GREATER_OR_EQUAL: //Greater not possible
            return _elementOf(EQUAL, arg1, arg2);
         case LESS:
            return arg2.getContents().size() > 1 && arg2.getDataValue(arg1) != null;
         case LESS_OR_EQUAL:
            return arg2.getContents().size() >= 1 && arg2.getDataValue(arg1) != null;
         default:
            throw new UnsupportedOperationException();
      }
   }

   private static boolean _constainsAll(Collection<Object> arg1, DataHolder arg2) {
      for (Object obj : arg1) {
         if (arg2.getDataValue(obj) == null) {
            return false;
         }
      }
      return true;
   }

}
