package nl.larsdenbakker.conversion.reference;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import nl.larsdenbakker.serialization.DataSerializable;

/**
 * A DataReference List Collection. In essence it is a wrapper around
 * a regular ArrayList. See DataReferenceCollection for more info.
 *
 * @author Lars den Bakker <larsdenbakker at gmail.com>
 */
public class DataReferenceList<E extends DataReferencable> extends AbstractList<E> implements DataReferenceCollection<E>, DataSerializable {

   protected final ArrayList<DataReference<E>> items;

   public DataReferenceList() {
      items = new ArrayList();
   }

   public DataReferenceList(Collection c) {
      items = new ArrayList(c.size());
      addAll(0, c);
   }

   @Override
   public Iterator<E> iterator() {
      return new ReferenceListIterator();
   }

   @Override
   public int size() {
      return items.size();
   }

   @Override
   public E get(int index) {
      return items.get(index).getDataValue();
   }

   @Override
   public String toString() {
      return items.toString();
   }

   @Override
   public boolean remove(Object o) {
      Iterator<DataReference<E>> it = items.iterator();
      while (it.hasNext()) {
         DataReference<E> next = it.next();
         if (next.getDataValue().equals(o)) {
            it.remove();
            return true;
         }
      }
      return false;
   }

   @Override
   public boolean add(E e) {
      return items.add(e.getDataReference());
   }

   @Override
   public void add(int index, E element) {
      items.add(index, element.getDataReference());
   }

   @Override
   public E remove(int index) {
      DataReference<E> ref = items.remove(index);
      if (ref != null) {
         return ref.getDataValue();
      } else {
         return null;
      }
   }

   @Override
   public boolean removeAll(Collection<?> c) {
      return items.removeAll(c);
   }

   @Override
   public E set(int index, E element) {
      DataReference<E> prev = items.set(index, element.getDataReference());
      if (prev != null) {
         return prev.getDataValue();
      } else {
         return null;
      }
   }

   public ArrayList<DataReference<E>> getItems() {
      return items;
   }

   @Override
   public void clear() {
      items.clear();
   }

   @Override
   public Object toSerializable() {
      return items;
   }

   private class ReferenceListIterator implements Iterator<E> {

      private int n;
      private int i;

      public ReferenceListIterator() {
         n = size();
         i = 0;
      }

      @Override
      public boolean hasNext() {
         return i < n;
      }

      @Override
      public E next() {
         return get(i++);
      }

      @Override
      public void remove() {
         throw new UnsupportedOperationException();
      }

   }

}
