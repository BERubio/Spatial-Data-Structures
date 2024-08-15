package spatial.knnutils;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;

import spatial.exceptions.UnimplementedMethodException;

/**
 * <p>
 * {@link BoundedPriorityQueue} is a priority queue whose number of elements is
 * bounded. Insertions are such that if the queue's provided capacity is
 * surpassed, its length is not expanded, but rather the maximum priority
 * element is ejected (which could be the element just attempted to be
 * enqueued).
 * </p>
 *
 * <p>
 * <b>YOU ***** MUST ***** IMPLEMENT THIS CLASS!</b>
 * </p>
 *
 * @author <a href = "https://github.com/jasonfillipou/">Jason Filippou</a>
 *
 * @see PriorityQueue
 * @see PriorityQueueNode
 */
public class BoundedPriorityQueue<T> implements PriorityQueue<T> {

	/* *********************************************************************** */
	/* ************* PLACE YOUR PRIVATE FIELDS AND METHODS HERE: ************ */
	/* *********************************************************************** */
	private ArrayList<PriorityQueueNode<T>> queue;
	private int size;
	private int insertOrder;
	public int concurrChangeCount = 0;

	/* *********************************************************************** */
	/* *************** IMPLEMENT THE FOLLOWING PUBLIC METHODS: ************ */
	/* *********************************************************************** */

	public void printBPQ() {
		for (PriorityQueueNode<T> node : this.queue) {
			System.out.println(node.getData() + " : " + node.getPriority());
		}
	}

	/**
	 * Constructor that specifies the size of our queue.
	 * 
	 * @param size The static size of the {@link BoundedPriorityQueue}. Has to be a
	 *             positive integer.
	 * @throws IllegalArgumentException if size is not a strictly positive integer.
	 */
	public BoundedPriorityQueue(int size) throws IllegalArgumentException {
		if (size > 0) {
			this.size = size;
			this.insertOrder = 0;
			this.queue = new ArrayList<>();
		} else {
			throw new IllegalArgumentException();
		}
	}

	public int getInsertOrder() {
		return insertOrder;
	}

	/**
	 * <p>
	 * Enqueueing elements for BoundedPriorityQueues works a little bit differently
	 * from general case PriorityQueues. If the queue is not at capacity, the
	 * element is inserted at its appropriate location in the sequence. On the other
	 * hand, if the object is at capacity, the element is inserted in its
	 * appropriate spot in the sequence (if such a spot exists, based on its
	 * priority) and the maximum priority element is ejected from the structure.
	 * </p>
	 * 
	 * @param element  The element to insert in the queue.
	 * @param priority The priority of the element to insert in the queue.
	 */
	@Override
	public void enqueue(T element, double priority) {
		// If the queue is full, compare the new element's priority with the lowest priority (highest value)
		PriorityQueueNode<T> insertNode = new PriorityQueueNode<>(element, priority, this.insertOrder);
		this.insertOrder++;
		boolean insert_check = false;
		
		for(int i = 0;i < this.queue.size(); i++){
			// target should be inserted into the previous slot. 
			if(this.queue.get(i).getPriority() > priority){
				this.concurrChangeCount++;
				
				this.queue.add(i, insertNode);
				insert_check = true;
				
				break;
			}
		}
		// if the proper position is not in between the existed nodes, then simply append it to the end of the queue.
		if (insert_check == false){
			this.concurrChangeCount ++;
			
			if(this.queue.isEmpty()){
				this.queue.add(0, insertNode);
			}else{
				this.queue.add(insertNode);
			}
		}
		// eject the last one if the size exceed the max bound.
		if (this.queue.size() > this.size){
			this.queue.remove(this.queue.size()-1);
		}

	}

	@Override
	public T dequeue() {
		if (this.queue.size() == 0) {
			return null;
		}
		this.concurrChangeCount++;
		T deleted_elem = this.queue.get(0).getData();
		this.queue.remove(0);
		return deleted_elem;
	}

	@Override
	public T first() {
		if (this.queue.size() == 0) {
			return null;
		}
		return this.queue.get(0).getData();
	}

	/**
	 * Returns the last element in the queue. Useful for cases where we want to
	 * compare the priorities of a given quantity with the maximum priority of our
	 * stored quantities. In a minheap-based implementation of any
	 * {@link PriorityQueue}, this operation would scan O(n) nodes and O(nlogn)
	 * links. In an array-based implementation, it takes constant time.
	 * 
	 * @return The maximum priority element in our queue, or null if the queue is
	 *         empty.
	 */
	public T last() {
		if (this.queue.size() == 0) {
			return null;
		}
		return this.queue.get(this.size() - 1).getData();
	}

	/**
	 * Inspects whether a given element is in the queue. O(N) complexity.
	 * 
	 * @param element The element to search for.
	 * @return {@code true} iff {@code element} is in {@code this}, {@code false}
	 *         otherwise.
	 */
	public boolean contains(T element) {
		for(int i = 0; i < this.queue.size(); i++) {
			if (this.queue.get(i).equals(element)){
				return true;
			}
		}
		return false;
	}

	@Override
	public int size() {
		return this.queue.size();
	}

	@Override
	public boolean isEmpty() {
		if(this.queue.size() == 0) {
			return true;
		}
		return false;
	}

	@Override
	public Iterator<T> iterator() {
		return new Iterator<>() {
			int index = 0;
			int changeCount = concurrChangeCount;

			@Override
			public boolean hasNext() {
				if (changeCount == concurrChangeCount) {
					boolean check = (index < queue.size());
					return check;
				} else {
					throw new ConcurrentModificationException();
				}
			}

			@Override
			public T next() {
				if (changeCount == concurrChangeCount) {
					index++;
					return queue.get(index - 1).getData();
				} else {
					throw new ConcurrentModificationException();
				}
			}
		};
	}
}
