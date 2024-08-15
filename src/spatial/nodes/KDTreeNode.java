package spatial.nodes;

import spatial.exceptions.UnimplementedMethodException;
import spatial.kdpoint.KDPoint;
import spatial.knnutils.BoundedPriorityQueue;
import spatial.knnutils.NNData;

import java.util.Collection;

/**
 * <p>
 * {@link KDTreeNode} is an abstraction over nodes of a KD-Tree. It is used
 * extensively by {@link spatial.trees.KDTree} to implement its functionality.
 * </p>
 *
 * <p>
 * <b>YOU ***** MUST ***** IMPLEMENT THIS CLASS!</b>
 * </p>
 *
 * @author ---- BRANDON RUBIO -----
 *
 * @see spatial.trees.KDTree
 */
public class KDTreeNode {

	/*
	 * ***************************************************************************
	 */
	/* ************* WE PROVIDE THESE FIELDS TO GET YOU STARTED. **************** */
	/* ************************************************************************** */
	private KDPoint p;
	private int height;
	private KDTreeNode left, right;

	/*
	 * *****************************************************************************
	 * **********
	 */
	/*
	 * ************* PLACE ANY OTHER PRIVATE FIELDS AND YOUR PRIVATE METHODS HERE:
	 * ************
	 */
	/*
	 * *****************************************************************************
	 * ********
	 */

	// private helper for calculating next dimension to use for Tree operations
	private int nextDim(int currentDim, int totalDims) {
		int insertDim = 0;
		if (currentDim + 1 == totalDims) {
			insertDim = 0;
		} else {
			insertDim = currentDim + 1;
		}
		return insertDim;
	}

	/* *********************************************************************** */
	/* *************** IMPLEMENT THE FOLLOWING PUBLIC METHODS: ************ */
	/* *********************************************************************** */

	/**
	 * 1-arg constructor. Stores the provided {@link KDPoint} inside the freshly
	 * created node.
	 * 
	 * @param p The {@link KDPoint} to store inside this. Just a reminder:
	 *          {@link KDPoint}s are <b>mutable!!!</b>.
	 */
	public KDTreeNode(KDPoint p) {
		this.p = new KDPoint(p);
		this.height = 0;
		this.left = null;
		this.right = null;
	}

	/**
	 * <p>
	 * Inserts the provided {@link KDPoint} in the tree rooted at this. To select
	 * which subtree to recurse to, the KD-Tree acts as a Binary Search Tree on
	 * currDim; it will examine the value of the provided {@link KDPoint} at currDim
	 * and determine whether it is larger than or equal to the contained
	 * {@link KDPoint}'s relevant dimension value. If so, we recurse right, like a
	 * regular BST, otherwise left.
	 * </p>
	 * 
	 * @param currDim The current dimension to consider
	 * @param dims    The total number of dimensions that the space considers.
	 * @param pIn     The {@link KDPoint} to insert into the node.
	 * @see #delete(KDPoint, int, int)
	 */
	public void insert(KDPoint pIn, int currDim, int dims) {
		if (pIn.coords[currDim] >= this.p.coords[currDim]) {
			// move to right subtree
			if (this.right == null) {
				this.right = new KDTreeNode(pIn);
				// if left child is null as well, increment height (inserted at unoccupied
				// level)
				if (this.left == null) {
					this.height++;
				}
			} else {
				this.right.insert(pIn, nextDim(currDim, dims), dims);
			}
		} else {
			// move to left subtree
			if (this.left == null) {
				this.left = new KDTreeNode(pIn);
				// if right child is null as well, increment height (inserted at unoccupied
				// level)
				if (this.right == null) {
					this.height++;
				}
			} else {
				this.left.insert(pIn, nextDim(currDim, dims), dims);
			}
		}
	}

	/**
	 * <p>
	 * Deletes the provided {@link KDPoint} from the tree rooted at this. To select
	 * which subtree to recurse to, the KD-Tree acts as a Binary Search Tree on
	 * currDim; it will examine the value of the provided {@link KDPoint} at currDim
	 * and determine whether it is larger than or equal to the contained
	 * {@link KDPoint}'s relevant dimension value. If so, we recurse right, like a
	 * regular BST, otherwise left. There exist two special cases of deletion,
	 * depending on whether we are deleting a {@link KDPoint} from a node who
	 * either:
	 * </p>
	 *
	 * <ul>
	 * <li>Has a NON-null subtree as a right child.</li>
	 * <li>Has a NULL subtree as a right child.</li>
	 * </ul>
	 *
	 * <p>
	 * You should consult the class slides, your notes, and the textbook about what
	 * you need to do in those two special cases.
	 * </p>
	 * 
	 * @param currDim The current dimension to consider.
	 * @param dims    The total number of dimensions that the space considers.
	 * @param pIn     The {@link KDPoint} to delete from the node.
	 * @see #insert(KDPoint, int, int)
	 * @return A reference to this after the deletion takes place.
	 */
	public KDTreeNode delete(KDPoint pIn, int currDim, int dims) {
		if (this.p.equals(pIn)) {
			if (this.left == null && this.right == null) {
				this.p = null;
				return null;
			} else if (this.right != null) {
				// Is there a non-null right subtree?
				// Find in-order successor for currDim and recursively delete successor

				KDTreeNode successor = this.right.findMin(currDim, nextDim(currDim, dims), dims);
				this.p = new KDPoint(successor.p);
				this.right = this.right.delete(new KDPoint(successor.p), nextDim(currDim, dims), dims);
				return this;
			} else {
				// this.left != null && this.right == null
				KDTreeNode successor = this.left.findMin(currDim, nextDim(currDim, dims), dims);
				this.p = new KDPoint(successor.p);
				this.right = this.left;
				this.left = null;
				this.right = this.right.delete(new KDPoint(successor.p), nextDim(currDim, dims), dims);
				return this;
			}
		} else if (pIn.coords[currDim] >= this.p.coords[currDim]) {
			// need to go to right subtree to find node for deletion
			this.right = this.right.delete(pIn, nextDim(currDim, dims), dims);
			return this;
		} else {
			// need to go to right subtree to find node for deletion
			this.left = this.left.delete(pIn, nextDim(currDim, dims), dims);
			return this;
		}
	}

	// private helper for finding the minimum of the current node based on currDim
	// in left subtree
	private KDTreeNode findMin(int targetDim, int currDim, int dims) {

		if (this.left == null && this.right == null) {
			return this;
		}
		if (targetDim == currDim) {
			if (this.left == null) { // no more left subtree, current will be the smallest
				return this;
			} else {
				return this.left.findMin(targetDim, (((currDim + 1) == dims) ? 0 : currDim + 1), dims);
			}
		}
		KDTreeNode leftNode = (this.left == null) ? null
				: this.left.findMin(targetDim, (((currDim + 1) == dims) ? 0 : currDim + 1), dims);
		KDTreeNode rightNode = (this.right == null) ? null
				: this.right.findMin(targetDim, (((currDim + 1) == dims) ? 0 : currDim + 1), dims);
		return findMin2(leftNode, rightNode, this, targetDim);
	}

	// find minimum of current node, its left child, and its right child
	private KDTreeNode findMin2(KDTreeNode leftMin, KDTreeNode rightMin, KDTreeNode curr, int targetDim) {
		if (leftMin != null && rightMin != null && curr != null) {
			KDTreeNode childSuccessor = leftMin;
			if (leftMin.p.coords[targetDim] >= rightMin.p.coords[targetDim]) {
				childSuccessor = rightMin;
			} else {
				childSuccessor = leftMin;
			}

			if (childSuccessor.p.coords[targetDim] >= this.p.coords[targetDim]) {
				return this;
			} else {
				return childSuccessor;
			}
		} else if (leftMin == null && rightMin != null && curr != null) {
			return (rightMin.p.coords[targetDim] >= this.p.coords[targetDim]) ? this : rightMin;
		} else if (leftMin != null && rightMin == null && curr != null) {
			return (leftMin.p.coords[targetDim] >= this.p.coords[targetDim]) ? this : leftMin;
		} else if (leftMin != null && rightMin != null && curr == null) {
			return (leftMin.p.coords[targetDim] >= rightMin.p.coords[targetDim]) ? rightMin : leftMin;
		} else if (leftMin == null && rightMin == null && curr != null) {
			return curr;
		} else if (leftMin != null && rightMin == null && curr == null) {
			return leftMin;
		} else {
			return rightMin;
		}
	}

	/**
	 * Searches the subtree rooted at the current node for the provided
	 * {@link KDPoint}.
	 * 
	 * @param pIn     The {@link KDPoint} to search for.
	 * @param currDim The current dimension considered.
	 * @param dims    The total number of dimensions considered.
	 * @return true iff pIn was found in the subtree rooted at this, false
	 *         otherwise.
	 */
	public boolean search(KDPoint pIn, int currDim, int dims) {
		// found the target node, return
		if (this.p.equals(pIn)) {
			return true;
		}

		if (pIn.coords[currDim] >= this.p.coords[currDim]) {
			// must go right to continue the search
			if (this.right == null) {
				return false;
			}
			return this.right.search(pIn, nextDim(currDim, dims), dims);
		} else {
			// pIn.coords[currDim] < this.p.coords[currDim]
			if (this.left == null) {
				return false;
			} else {
				return this.left.search(pIn, nextDim(currDim, dims), dims);
			}
		}
	}

	/**
	 * <p>
	 * Executes a range query in the given {@link KDTreeNode}. Given an
	 * &quot;anchor&quot; {@link KDPoint}, all {@link KDPoint}s that have a
	 * {@link KDPoint#euclideanDistance(KDPoint) euclideanDistance} of <b>at
	 * most</b> range <b>INCLUSIVE</b> from the anchor point <b>except</b> for the
	 * anchor itself should be inserted into the {@link Collection} that is passed.
	 * </p>
	 *
	 * <p>
	 * Remember: range queries behave <em>greedily</em> as we go down (approaching
	 * the anchor as &quot;fast&quot; as our currDim allows and <em>prune
	 * subtrees</em> that we <b>don't</b> have to visit as we backtrack. Consult all
	 * of our resources if you need a reminder of how these should work.
	 * </p>
	 *
	 * @param anchor  The centroid of the hypersphere that the range query
	 *                implicitly creates.
	 * @param results A {@link Collection} that accumulates all the {@link }
	 * @param currDim The current dimension examined by the {@link KDTreeNode}.
	 * @param dims    The total number of dimensions of our {@link KDPoint}s.
	 * @param range   The <b>INCLUSIVE</b> range from the &quot;anchor&quot;
	 *                {@link KDPoint}, within which all the {@link KDPoint}s that
	 *                satisfy our query will fall. The euclideanDistance metric
	 *                used} is defined by
	 *                {@link KDPoint#euclideanDistance(KDPoint)}.
	 */
	/*
	 * public void range(KDPoint anchor, Collection<KDPoint> results, double range,
	 * int currDim, int dims) { if (this.p.coords[currDim] >=
	 * anchor.coords[currDim]) { if (this.right == null) { if
	 * ((KDPoint.euclideanDistance(this.p, anchor) <= range) &&
	 * !anchor.equals(this.p)) { results.add(this.p); }
	 * 
	 * if (this.left != null) { // PRUNING: check if query on left subtree is
	 * necessary based on range valueS if (Math.abs(this.p.coords[currDim] -
	 * anchor.coords[currDim]) <= range) { // Can't prune left subtree, continue
	 * range query on left side this.left.range(anchor, results, range,
	 * nextDim(currDim, dims), dims); } } } else { // this.right != null // greedy
	 * descent: this.right.range(anchor, results, range, nextDim(currDim, dims),
	 * dims); // current node should be in range due to greedy descent if
	 * ((KDPoint.euclideanDistance(this.p, anchor) <= range) &&
	 * !anchor.equals(this.p)) { results.add(this.p); }
	 * 
	 * // check right subtree if (this.right != null) { // PRUNING: check if query
	 * on right subtree is necessary based on range values if
	 * (Math.abs(this.p.coords[currDim] - anchor.coords[currDim]) <= range) { //
	 * Can't prune left subtree, continue range query on left side
	 * this.right.range(anchor, results, range, nextDim(currDim, dims), dims); } } }
	 * } else { // go left, avoiding greedy descent until next level if (this.left
	 * == null) { if ((KDPoint.euclideanDistance(this.p, anchor) <= range) &&
	 * !anchor.equals(this.p)) { results.add(this.p); } // now do greedy descent,
	 * checking right subtree if (this.right != null) { // PRUNING: check if query
	 * on right subtree is necessary based on range values if
	 * (Math.abs(this.p.coords[currDim] - anchor.coords[currDim]) <= range) { //
	 * Can't prune, check right subtree this.right.range(anchor, results, range,
	 * nextDim(currDim, dims), dims); } } } else { // this.left != null
	 * 
	 * // go left, begin greedy descent at next level this.left.range(anchor,
	 * results, range, nextDim(currDim, dims), dims);
	 * 
	 * if ((KDPoint.euclideanDistance(this.p, anchor) <= range) &&
	 * !anchor.equals(this.p)) { results.add(this.p); }
	 * 
	 * // check right subtree if (this.right != null) { // PRUNING: check if query
	 * on right subtree is necessary based on range values if
	 * (Math.abs(this.p.coords[currDim] - anchor.coords[currDim]) <= range) { //
	 * Can't prune, check right subtree this.right.range(anchor, results, range,
	 * nextDim(currDim, dims), dims); } }
	 * 
	 * } } }
	 */
	public void range(KDPoint anchor, Collection<KDPoint> results, double range, int currDim, int dims) {
		range2(this, anchor, results, range, currDim, dims);
	}

	//private helper for range query
	private void range2(KDTreeNode curr, KDPoint anchor, Collection<KDPoint> results, double range, int currDim,
			int dims) {

		if (currDim == dims) {
			currDim = 0;
		}
		
		//check if the current KDPoint is within range of the anchor
		if (!curr.getPoint().equals(anchor)) {
			if (curr.getPoint().euclideanDistance(anchor) <= range) {
				//if within range, add it to the list

				results.add(curr.getPoint());
			}
		}
		
		

		if (curr.left != null || curr.right != null) {
			// if both subtrees are non-null
			if (anchor.coords[currDim] < curr.p.coords[currDim]) {
				// anchor is lesser in this dimension, try to go left
				if (curr.left == null && curr.right != null) {
					range2(curr.right, anchor, results, range, currDim + 1, dims);
				} else {
					range2(curr.left, anchor, results, range, currDim + 1, dims);
					//traverse right if within rangeS
					if (curr.right != null) {
						if (anchor.coords[currDim] - curr.p.coords[currDim] <= range) {
							range2(curr.right, anchor, results, range, currDim + 1, dims);
						}
					}
				}

			} else {
				//one of the subtrees is null
				if (curr.right == null && curr.left != null) {
					// traverse left and avoid dead-end
					range2(curr.left, anchor, results, range, currDim + 1, dims);
				} else {
					// traverse right to avoid dead-end
					range2(curr.right, anchor, results, range, currDim + 1, dims);
					// traverse left if within range
					if (curr.left != null) {
						if (anchor.coords[currDim] - curr.p.coords[currDim] <= range) {
							range2(curr.left, anchor, results, range, currDim + 1, dims);
						}
					}
				}
			}
		}
	}

	/**
	 * <p>
	 * Executes a nearest neighbor query, which returns the nearest neighbor, in
	 * terms of {@link KDPoint#euclideanDistance(KDPoint)}, from the
	 * &quot;anchor&quot; point.
	 * </p>
	 *
	 * <p>
	 * Recall that, in the descending phase, a NN query behaves <em>greedily</em>,
	 * approaching our &quot;anchor&quot; point as fast as currDim allows. While
	 * doing so, it implicitly <b>bounds</b> the acceptable solutions under the
	 * current <b>best solution</b>, which is passed as an argument. This approach
	 * is known in Computer Science as &quot;branch-and-bound&quot; and it helps us
	 * solve an otherwise exponential complexity problem (nearest neighbors)
	 * efficiently. Remember that when we want to determine if we need to recurse to
	 * a different subtree, it is <b>necessary</b> to compare the euclideanDistance
	 * reported by {@link KDPoint#euclideanDistance(KDPoint)} and coordinate
	 * differences! Those are comparable with each other because they are the same
	 * data type ({@link Double}).
	 * </p>
	 *
	 * @return An object of type {@link NNData}, which exposes the pair
	 *         (distance_of_NN_from_anchor, NN), where NN is the nearest
	 *         {@link KDPoint} to the anchor {@link KDPoint} that we found.
	 *
	 * @param anchor  The &quot;ancor&quot; {@link KDPoint}of the nearest neighbor
	 *                query.
	 * @param currDim The current dimension considered.
	 * @param dims    The total number of dimensions considered.
	 * @param n       An object of type {@link NNData}, which will define a nearest
	 *                neighbor as a pair (distance_of_NN_from_anchor, NN), * where
	 *                NN is the nearest neighbor found.
	 *
	 * @see NNData
	 * @see #kNearestNeighbors(int, KDPoint, BoundedPriorityQueue, int, int)
	 */
	public NNData<KDPoint> nearestNeighbor(KDPoint anchor, int currDim, NNData<KDPoint> n, int dims) {
		nearestNeighbor2(anchor, currDim, n, dims);
		return n;
	}

	// private helper method for NN
	private void nearestNeighbor2(KDPoint anchor, int currDim, NNData<KDPoint> n, int dims) {
		if (this.p.coords[currDim] < anchor.coords[currDim]) {
			if (this.right == null) {
				double currDist = this.p.euclideanDistance(anchor);
				// if current node is closer than current NN, update as the result
				if ((n.getBestDist() == -1.0 || currDist <= n.getBestDist()) && !anchor.equals(this.p)) {
					n.update(this.p, currDist);
				}

				// Non-null left Subtree
				if (this.left != null) {
					// Can the left subtree be Pruned? Check next left child distance.
					if (this.left.p.euclideanDistance(anchor) <= n.getBestDist()) {
						// left subtree has possible NN, check it
						this.left.nearestNeighbor2(anchor, nextDim(currDim, dims), n, dims);
					}
				}

			} else {
				// if current node is closer than current NN, update as the result
				double currDist = this.p.euclideanDistance(anchor);
				if ((n.getBestDist() == -1.0 || currDist <= n.getBestDist()) && !anchor.equals(this.p)) {
					n.update(this.p, currDist);
				}
				// visit right subtree
				this.right.nearestNeighbor2(anchor, nextDim(currDim, dims), n, dims);
				if (this.left != null) {
					// Can the left subtree be Pruned? Check next left child distance.
					if (this.left.p.euclideanDistance(anchor) <= n.getBestDist()) {
						// left subtree has possible NN, check it
						this.left.nearestNeighbor2(anchor, nextDim(currDim, dims), n, dims);
					}
				}
			}
		} else {
			if (this.left == null) {
				double currDist = this.p.euclideanDistance(anchor);
				if ((n.getBestDist() == -1.0 || currDist <= n.getBestDist()) && !anchor.equals(this.p)) {
					n.update(this.p, currDist);
				}

				if (this.right != null) {
					if (this.right != null) {
						// Can the right subtree be Pruned? Check next left child distance.
						if (this.right.p.euclideanDistance(anchor) <= n.getBestDist()) {
							// left right has possible NN, check it
							this.right.nearestNeighbor2(anchor, nextDim(currDim, dims), n, dims);
						}
					}
				}

			} else {
				// this.left != null
				double currDist = this.p.euclideanDistance(anchor);
				if ((n.getBestDist() == -1.0 || currDist <= n.getBestDist()) && !anchor.equals(this.p)) {
					n.update(this.p, currDist);
				}

				// visit left subtree
				this.left.nearestNeighbor2(anchor, nextDim(currDim, dims), n, dims);
				if (this.right != null) {
					// Can the left subtree be Pruned? Check next left child distance.
					if (this.right.p.euclideanDistance(anchor) <= n.getBestDist()) {
						// left subtree has possible NN, check it
						this.right.nearestNeighbor2(anchor, nextDim(currDim, dims), n, dims);
					}
				}
			}

		}
	}

	/**
	 * <p>
	 * Executes a nearest neighbor query, which returns the nearest neighbor, in
	 * terms of {@link KDPoint#euclideanDistance(KDPoint)}, from the
	 * &quot;anchor&quot; point.
	 * </p>
	 *
	 * <p>
	 * Recall that, in the descending phase, a NN query behaves <em>greedily</em>,
	 * approaching our &quot;anchor&quot; point as fast as currDim allows. While
	 * doing so, it implicitly <b>bounds</b> the acceptable solutions under the
	 * current <b>worst solution</b>, which is maintained as the last element of the
	 * provided {@link BoundedPriorityQueue}. This is another instance of
	 * &quot;branch-and-bound&quot; Remember that when we want to determine if we
	 * need to recurse to a different subtree, it is <b>necessary</b> to compare the
	 * euclideanDistance reported by* {@link KDPoint#euclideanDistance(KDPoint)} and
	 * coordinate differences! Those are comparable with each other because they are
	 * the same data type ({@link Double}).
	 * </p>
	 *
	 * <p>
	 * The main difference of the implementation of this method and the
	 * implementation of {@link #nearestNeighbor(KDPoint, int, NNData, int)} is the
	 * necessity of using the class {@link BoundedPriorityQueue} effectively.
	 * Consult your various resources to understand how you should be using this
	 * class.
	 * </p>
	 *
	 * @param k       The total number of neighbors to retrieve. It is better if
	 *                this quantity is an odd number, toa avoid ties in Binary
	 *                Classification tasks.
	 * @param anchor  The &quot;anchor&quot; {@link KDPoint} of the nearest neighbor
	 *                query.
	 * @param currDim The current dimension considered.
	 * @param dims    The total number of dimensions considered.
	 * @param queue   A {@link BoundedPriorityQueue} that will maintain at most k
	 *                nearest neighbors of the anchor point at all times, sorted by
	 *                euclideanDistance to the point.
	 *
	 * @see BoundedPriorityQueue
	 */
	public void kNearestNeighbors(int k, KDPoint anchor, BoundedPriorityQueue<KDPoint> queue, int currDim, int dims) {
		if (anchor.coords[currDim] < this.p.coords[currDim]) {
			// search left
			if (this.left == null) {
				// Reached end of greedy descent, check the point
				if (!this.p.equals(anchor)) {
					// if point is not anchor, add to queue
					queue.enqueue(this.p, this.p.euclideanDistance(anchor));
				}
				// Check other nodes for pruning
				if (this.right != null) {
					// right subtree
					if (queue.size() != k) {
						// check queue capacity. If not full, check right subtree
						this.right.kNearestNeighbors(k, anchor, queue, nextDim(currDim, dims), dims);
					} else {
						if (queue.last().euclideanDistance(anchor) >= this.right.p.euclideanDistance(anchor)) {
							// Queue is full, compare last elem. in queue to right child and check
							// if right child has shorter or equal distance
							this.right.kNearestNeighbors(k, anchor, queue, nextDim(currDim, dims), dims);
						}
					}
				}
			} else {
				// add current node and compare with current points in queue
				if (!this.p.equals(anchor)) {
					queue.enqueue(this.p, this.p.euclideanDistance(anchor));
				}
				// greedy descent
				this.left.kNearestNeighbors(k, anchor, queue, nextDim(currDim, dims), dims);

				// Pruning check on right subtree
				if (this.right != null) {
					if (queue.size() != k) {
						this.right.kNearestNeighbors(k, anchor, queue, nextDim(currDim, dims), dims);
					} else if (queue.last().euclideanDistance(anchor) >= this.right.p.euclideanDistance(anchor)) {
						// queue is full so do distance comparison between right child and last point in
						// the queue
						this.right.kNearestNeighbors(k, anchor, queue, nextDim(currDim, dims), dims);
					}
				}
			}

		} else {
			// search to the right
			if (this.right == null) {
				if (!this.p.equals(anchor)) {
					queue.enqueue(this.p, this.p.euclideanDistance(anchor));
				}

				// Pruning check on left side
				if (this.left != null) {
					if (queue.size() != k) {

						this.left.kNearestNeighbors(k, anchor, queue, nextDim(currDim, dims), dims);
					} else if (queue.last().euclideanDistance(anchor) >= this.left.p.euclideanDistance(anchor)) {
						this.left.kNearestNeighbors(k, anchor, queue, nextDim(currDim, dims), dims);
					}
				} else {
					if (!this.p.equals(anchor)) {
						queue.enqueue(this.p, this.p.euclideanDistance(anchor));
					}

					this.right.kNearestNeighbors(k, anchor, queue, nextDim(currDim, dims), dims);

					if (this.left != null) {
						if (queue.size() != k) {
							this.left.kNearestNeighbors(k, anchor, queue, nextDim(currDim, dims), dims);
						} else {
							if (queue.last().euclideanDistance(anchor) >= this.left.p.euclideanDistance(anchor)) {
								this.left.kNearestNeighbors(k, anchor, queue, nextDim(currDim, dims), dims);
							}
						}
					}
				}
			} else {
				if (!this.p.equals(anchor)) {
					queue.enqueue(anchor, this.p.euclideanDistance(anchor));
				}

				this.right.kNearestNeighbors(k, anchor, queue, nextDim(currDim, dims), dims);

				if (this.left != null) {
					if (queue.size() != k) {
						this.left.kNearestNeighbors(k, anchor, queue, nextDim(currDim, dims), dims);
					} else if (queue.last().euclideanDistance(anchor) >= this.left.p.euclideanDistance(anchor)) {
						this.left.kNearestNeighbors(k, anchor, queue, nextDim(currDim, dims), dims);
					}
				}
			}
		}
	}

	/**
	 * Returns the height of the subtree rooted at the current node. Recall our
	 * definition of height for binary trees:
	 * <ol>
	 * <li>A null tree has a height of -1.</li>
	 * <li>A non-null tree has a height equal to max(height(left_subtree),
	 * height(right_subtree))+1</li>
	 * </ol>
	 * 
	 * @return the height of the subtree rooted at the current node.
	 */
	public int height() {
		return height2(this);
	}

	private int height2(KDTreeNode curr) {
		// if empty tree or subtree, return -1
		if (curr == null) {
			return -1;
		}
		// If dead-end return 0
		if (curr.left == null && curr.right == null) {
			return 0;
		} else {
			// Otherwise, check heights of left and right subtrees
			// Take the greater height of the subtrees
			// Add 1 to account for current level
			return Math.max(height2(curr.left), height2(curr.right)) + 1;
		}
	}

	/**
	 * A simple getter for the {@link KDPoint} held by the current node. Remember:
	 * {@link KDPoint}s ARE MUTABLE, SO WE NEED TO DO DEEP COPIES!!!
	 * 
	 * @return The {@link KDPoint} held inside this.
	 */
	public KDPoint getPoint() {
		KDPoint point = new KDPoint(this.p);
		return point;
	}

	public KDTreeNode getLeft() {
		return this.left;
	}

	public KDTreeNode getRight() {
		return this.right;
	}
}
