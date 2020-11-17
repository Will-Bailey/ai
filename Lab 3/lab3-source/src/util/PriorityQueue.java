package util;

import java.util.*;

/**
 * A class implementing a priority queue with explicit double priority values.  
 * Larger doubles are higher priorities.  Backed by a binary heap.
 *
 * @author Dan Klein
 * @author Christopher Manning
 *         For each entry, uses ~ 24 (entry) + 16? (Map.Entry) + 4 (List entry) = 44 bytes?
 */
public class PriorityQueue<E> extends AbstractSet<E> implements Iterator<E> {
    
    /**
     * An <code>Entry</code> stores an object in the queue along with
     * its current location (array position) and priority.
     * uses ~ 8 (self) + 4 (key ptr) + 4 (index) + 8 (priority) = 24 bytes?
     */
    private static final class Entry {
        public Object key;
        public int index;
        public double priority;
        
        public String toString() {
            return key + " at " + index + " (" + priority + ")";
        }
    }
    
    public boolean hasNext() {
        return size() > 0;
    }
    
    public E next() {
        return removeFirst();
    }
    
    public void remove() {
        throw new UnsupportedOperationException();
    }
    
    /**
     * <code>indexToEntry</code> maps linear array locations (not
     * priorities) to heap entries.
     */
    private List indexToEntry;
    /**
     * <code>keyToEntry</code> maps heap objects to their heap
     * entries.
     */
    private Map keyToEntry;
    
    private Entry parent(Entry entry) {
        int index = entry.index;
        return (index > 0 ? getEntry((index - 1) / 2) : null);
    }
    
    private Entry leftChild(Entry entry) {
        int leftIndex = entry.index * 2 + 1;
        return (leftIndex < size() ? getEntry(leftIndex) : null);
    }
    
    private Entry rightChild(Entry entry) {
        int index = entry.index;
        int rightIndex = index * 2 + 2;
        return (rightIndex < size() ? getEntry(rightIndex) : null);
    }
    
    private int compare(Entry entryA, Entry entryB) {
        return compare(entryA.priority, entryB.priority);
    }
    
    private int compare(double a, double b) {
        double diff = a - b;
        if (diff > 0.0) {
            return 1;
        }
        if (diff < 0.0) {
            return -1;
        }
        return 0;
    }
    
    /**
     * Structural swap of two entries.
     *
     * @param entryA
     * @param entryB
     */
    private void swap(Entry entryA, Entry entryB) {
        int indexA = entryA.index;
        int indexB = entryB.index;
        entryA.index = indexB;
        entryB.index = indexA;
        indexToEntry.set(indexA, entryB);
        indexToEntry.set(indexB, entryA);
    }
    
    /**
     * Remove the last element of the heap (last in the index array).
     */
    private void removeLastEntry() {
        Entry entry = (Entry) indexToEntry.remove(size() - 1);
        keyToEntry.remove(entry.key);
    }
    
    /**
     * Get the entry by key (null if none).
     */
    private Entry getEntry(Object key) {
        Entry entry = (Entry) keyToEntry.get(key);
        return entry;
    }
    
    /**
     * Get entry by index, exception if none.
     */
    private Entry getEntry(int index) {
        Entry entry = (Entry) indexToEntry.get(index);
        return entry;
    }
    
    private Entry makeEntry(Object key) {
        Entry entry = new Entry();
        entry.index = size();
        entry.key = key;
        entry.priority = Double.NEGATIVE_INFINITY;
        indexToEntry.add(entry);
        keyToEntry.put(key, entry);
        return entry;
    }
    
    /**
     * iterative heapify up: move item o at index up until correctly placed
     */
    private void heapifyUp(Entry entry) {
        while (true) {
            if (entry.index == 0) {
                break;
            }
            Entry parentEntry = parent(entry);
            if (compare(entry, parentEntry) <= 0) {
                break;
            }
            swap(entry, parentEntry);
        }
    }
    
    /**
     * On the assumption that
     * leftChild(entry) and rightChild(entry) satisfy the heap property,
     * make sure that the heap at entry satisfies this property by possibly
     * percolating the element o downwards.  I've replaced the obvious
     * recursive formulation with an iterative one to gain (marginal) speed
     */
    private void heapifyDown(Entry entry) {
        Entry currentEntry = entry;
        Entry bestEntry = null;
        
        do {
            bestEntry = currentEntry;
            
            Entry leftEntry = leftChild(currentEntry);
            if (leftEntry != null) {
                if (compare(bestEntry, leftEntry) < 0) {
                    bestEntry = leftEntry;
                }
            }
            
            Entry rightEntry = rightChild(currentEntry);
            if (rightEntry != null) {
                if (compare(bestEntry, rightEntry) < 0) {
                    bestEntry = rightEntry;
                }
            }
            
            if (bestEntry != currentEntry) {
                // Swap min and current
                swap(bestEntry, currentEntry);
                // at start of next loop, we set currentIndex to largestIndex
                // this indexation now holds current, so it is unchanged
            }
        } while (bestEntry != currentEntry);
        // System.err.println("Done with heapify down");
        // verify();
    }
    
    private void heapify(Entry entry) {
        heapifyUp(entry);
        heapifyDown(entry);
    }
    
    
    /**
     * Finds the object with the highest priority, removes it,
     * and returns it.
     *
     * @return the object with highest priority
     */
    public E removeFirst() {
        E first = getFirst();
        remove(first);
        return first;
    }
    
    /**
     * Finds the object with the highest priority and returns it, without
     * modifying the queue.
     *
     * @return the object with minimum key
     */
    public E getFirst() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        return (E) getEntry(0).key;
    }
    
    /**
     * Searches for the object in the queue and returns it.  May be useful if
     * you can create a new object that is .equals() to an object in the queue
     * but is not actually identical, or if you want to modify an object that is
     * in the queue.
     * @return null if the object is not in the queue, otherwise returns the
     * object.
     */
    public Object getObject(Object key) {
        if (!contains(key)) return null;
        Entry e = getEntry(key);
        return e.key;
    }
    
    /**
     * Get the priority of a key -- if the key is not in the queue, Double.NEGATIVE_INFINITY is returned.
     *
     * @param key
     * @return double
     */
    public double getPriority(Object key) {
        Entry entry = getEntry(key);
        if (entry == null) {
            return Double.NEGATIVE_INFINITY;
        }
        return entry.priority;
    }
    
    /**
     * Adds an object to the queue with the minimum priority (Double.NEGATIVE_INFINITY).  If the object is already in the queue
     * with worse priority, this does nothing.  If the object is
     * already present, with better priority, it will NOT cause an
     * a decreasePriority.
     *
     * @param key an <code>Object</code> value
     * @return whether the key was present before
     */
    public boolean add(E key) {
        if (contains(key)) {
            return false;
        }
        makeEntry(key);
        return true;
    }
    
    /**
     * Convenience method for if you want to pretend relaxPriority doesn't exist, or if you really want add's return conditions.
     */
    public boolean add(E key, double priority) {
        //    System.err.println("Adding " + key + " with priority " + priority);
        if (add(key)) {
            relaxPriority(key, priority);
            return true;
        }
        return false;
    }
    
    
    public boolean remove(Object key) {
        Entry entry = getEntry(key);
        if (entry == null) {
            return false;
        }
        removeEntry(entry);
        return true;
    }
    
    private void removeEntry(Entry entry) {
        Entry lastEntry = getLastEntry();
        if (entry != lastEntry) {
            swap(entry, lastEntry);
            removeLastEntry();
            heapify(lastEntry);
        } else {
            removeLastEntry();
        }
        return;
    }
    
    private Entry getLastEntry() {
        return getEntry(size() - 1);
    }
    
    /**
     * Promotes a key in the queue, adding it if it wasn't there already.  If the specified priority is worse than the current priority, nothing happens.  Faster than add if you don't care about whether the key is new.
     *
     * @param key an <code>Object</code> value
     * @return whether the priority actually improved.
     */
    public boolean relaxPriority(E key, double priority) {
        Entry entry = getEntry(key);
        if (entry == null) {
            entry = makeEntry(key);
        }
        if (compare(priority, entry.priority) <= 0) {
            return false;
        }
        entry.priority = priority;
        heapifyUp(entry);
        return true;
    }
    
    /**
     * Demotes a key in the queue, adding it if it wasn't there already.  If the specified priority is better than the current priority, nothing happens.  If you decrease the priority on a non-present key, it will get added, but at it's old implicit priority of Double.NEGATIVE_INFINITY.
     *
     * @param key an <code>Object</code> value
     * @return whether the priority actually improved.
     */
    public boolean decreasePriority(E key, double priority) {
        Entry entry = getEntry(key);
        if (entry == null) {
            entry = makeEntry(key);
        }
        if (compare(priority, entry.priority) >= 0) {
            return false;
        }
        entry.priority = priority;
        heapifyDown(entry);
        return true;
    }
    
    /**
     * Changes a priority, either up or down, adding the key it if it wasn't there already.
     *
     * @param key an <code>Object</code> value
     * @return whether the priority actually changed.
     */
    public boolean changePriority(E key, double priority) {
        Entry entry = getEntry(key);
        if (entry == null) {
            entry = makeEntry(key);
        }
        if (compare(priority, entry.priority) == 0) {
            return false;
        }
        entry.priority = priority;
        heapify(entry);
        return true;
    }
    
    /**
     * Checks if the queue is empty.
     *
     * @return a <code>boolean</code> value
     */
    public boolean isEmpty() {
        return indexToEntry.isEmpty();
    }
    
    /**
     * Get the number of elements in the queue.
     *
     * @return queue size
     */
    public int size() {
        return indexToEntry.size();
    }
    
    /**
     * Returns whether the queue contains the given key.
     */
    public boolean contains(Object key) {
        return keyToEntry.containsKey(key);
    }
    
    public List<E> toSortedList() {
        List<E> sortedList = new ArrayList<E>(size());
        PriorityQueue<E> queue = this.deepCopy();
        while (!queue.isEmpty()) {
            sortedList.add(queue.removeFirst());
        }
        return sortedList;
    }
    
    public PriorityQueue<E> deepCopy() {
        PriorityQueue queue = new PriorityQueue();
        for (Iterator i = keyToEntry.values().iterator(); i.hasNext();) {
            Entry entry = (Entry) i.next();
            queue.relaxPriority(entry.key, entry.priority);
    }
        return queue;
    }
    
    public Iterator iterator() {
        return Collections.unmodifiableCollection(toSortedList()).iterator();
    }
    
    /**
     * Clears the queue.
     */
    public void clear() {
        indexToEntry.clear();
        keyToEntry.clear();
    }
    
    //  private void verify() {
    //    for (int i = 0; i < indexToEntry.size(); i++) {
    //      if (i != 0) {
    //        // check ordering
    //        if (compare(getEntry(i), parent(getEntry(i))) < 0) {
    //          System.err.println("Error in the ordering of the heap! ("+i+")");
    //          System.exit(0);
    //        }
    //      }
    //      // check placement
    //      if (i != ((Entry)indexToEntry.get(i)).index)
    //        System.err.println("Error in placement in the heap!");
    //    }
    //  }

    public String toString() {
        return toString(0);
    }

    public String toString(int maxKeysToPrint) {
        if (maxKeysToPrint <= 0) maxKeysToPrint = Integer.MAX_VALUE;
        List<E> sortedKeys = toSortedList();
        StringBuffer sb = new StringBuffer("[");
        for (int i = 0; i < maxKeysToPrint && i < sortedKeys.size(); i++) {
            Object key = sortedKeys.get(i);
            sb.append(key).append("=").append(getPriority(key));
            if (i < maxKeysToPrint - 1 && i < sortedKeys.size() - 1) {
                sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString();
    }
    
    public String toVerticalString() {
        List sortedKeys = toSortedList();
        StringBuffer sb = new StringBuffer();
        for (Iterator keyI = sortedKeys.iterator(); keyI.hasNext();) {
            Object key = keyI.next();
            sb.append(key);
            sb.append("\t");
            sb.append(getPriority(key));
            if (keyI.hasNext()) {
                sb.append("\n");
            }
        }
        return sb.toString();
    }
    
    
    public PriorityQueue() {
        indexToEntry = new ArrayList();
        keyToEntry = new HashMap();
    }
    
    public static void main(String[] args) {
        PriorityQueue queue = new PriorityQueue();
        queue.add("a", 1.0);
        System.out.println("Added a:1 " + queue);
        queue.add("b", 2.0);
        System.out.println("Added b:2 " + queue);
        queue.add("c", 1.5);
        System.out.println("Added c:1.5 " + queue);
        queue.relaxPriority("a", 3.0);
        System.out.println("Increased a to 3 " + queue);
        queue.decreasePriority("b", 0.0);
        System.out.println("Decreased b to 0 " + queue);
        System.out.println("removeFirst()=" + queue.removeFirst());
        System.out.println("queue=" + queue);
        System.out.println("removeFirst()=" + queue.removeFirst());
        System.out.println("queue=" + queue);
        System.out.println("removeFirst()=" + queue.removeFirst());
        System.out.println("queue=" + queue);
    }
    
}
