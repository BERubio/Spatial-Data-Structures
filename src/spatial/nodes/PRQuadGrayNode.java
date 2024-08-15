package spatial.nodes;

import spatial.exceptions.UnimplementedMethodException;
import spatial.kdpoint.KDPoint;
import spatial.knnutils.BoundedPriorityQueue;
import spatial.knnutils.NNData;
import spatial.trees.CentroidAccuracyException;
import spatial.trees.PRQuadTree;

import java.util.ArrayList;
import java.util.Collection;

/**
 * <p>
 * A {@link PRQuadGrayNode} is a gray (&quot;mixed&quot;) {@link PRQuadNode}. It
 * maintains the following invariants:
 * </p>
 * <ul>
 * <li>Its children pointer buffer is non-null and has a length of 4.</li>
 * <li>If there is at least one black node child, the total number of
 * {@link KDPoint}s stored by <b>all</b> of the children is greater than the
 * bucketing parameter (because if it is equal to it or smaller, we can prune
 * the node.</li>
 * </ul>
 *
 * <p>
 * <b>YOU ***** MUST ***** IMPLEMENT THIS CLASS!</b>
 * </p>
 *
 * @author --- BRANDON RUBIO ---
 */
public class PRQuadGrayNode extends PRQuadNode {

	/* ******************************************************************** */
	/* ************* PLACE ANY PRIVATE FIELDS AND METHODS HERE: ************ */
	/* ********************************************************************** */
	
	/*
	 * private PRQuadNode[] children; private int currHeight; private int numNodes;
	 */

	private PRQuadNode NW, NE, SW, SE;
	/* *********************************************************************** */
	/* *************** IMPLEMENT THE FOLLOWING PUBLIC METHODS: ************ */
	/* *********************************************************************** */

	/**
	 * Creates a {@link PRQuadGrayNode} with the provided {@link KDPoint} as a
	 * centroid;
	 * 
	 * @param centroid       A {@link KDPoint} that will act as the centroid of the
	 *                       space spanned by the current node.
	 * @param k              The See {@link PRQuadTree#PRQuadTree(int, int)} for
	 *                       more information on how this parameter works.
	 * @param bucketingParam The bucketing parameter fed to this by
	 *                       {@link PRQuadTree}.
	 * @see PRQuadTree#PRQuadTree(int, int)
	 */
	public PRQuadGrayNode(KDPoint centroid, int k, int bucketingParam) {
		super(centroid, k, bucketingParam); // Call to the super class' protected constructor to properly initialize the
											// object!
		NW = null;
		NE = null;
		SW = null;
		SE = null;
	}

	/**
	 * <p>
	 * Insertion into a {@link PRQuadGrayNode} consists of navigating to the
	 * appropriate child and recursively inserting elements into it. If the child is
	 * a white node, memory should be allocated for a {@link PRQuadBlackNode} which
	 * will contain the provided {@link KDPoint} If it's a {@link PRQuadBlackNode},
	 * refer to {@link PRQuadBlackNode#insert(KDPoint, int)} for details on how the
	 * insertion is performed. If it's a {@link PRQuadGrayNode}, the current method
	 * would be called recursively. Polymorphism will allow for the appropriate
	 * insert to be called based on the child object's runtime object.
	 * </p>
	 * 
	 * @param p A {@link KDPoint} to insert into the subtree rooted at the current
	 *          {@link PRQuadGrayNode}.
	 * @param k The side length of the quadrant spanned by the <b>current</b>
	 *          {@link PRQuadGrayNode}. It will need to be updated per recursive
	 *          call to help guide the input {@link KDPoint} to the appropriate
	 *          subtree.
	 * @return The subtree rooted at the current node, potentially adjusted after
	 *         insertion.
	 * @see PRQuadBlackNode#insert(KDPoint, int)
	 */
	@Override
	public PRQuadNode insert(KDPoint p, int k) {
		if (k < 0) {
			throw new CentroidAccuracyException("K can't be negative");
		}

		int kboundary = (int) Math.pow(2, k - 2);
		int centX, centY;

		if (p.coords[0] >= centroid.coords[0]) {
			// point will go either in NorthEast or SouthEast quadrant since x-value is
			// greater than centroid's x-value
			// extract x-value of the centroid
			centX = centroid.coords[0] + kboundary;
			if (p.coords[1] >= centroid.coords[1]) {
				// insert point has to go to the NorthEast quadrant: y-value GTE centroid
				// y-value

				centY = centroid.coords[1] + kboundary;
				// if no child here, create black node
				if (NE == null) {
					NE = new PRQuadBlackNode(new KDPoint(centX, centY), k - 1, bucketingParam, p);
				} else {
					// child here, insert based on whatever color node it is
					NE = NE.insert(p, k);
				}
			} else {
				// insert point has to go to the SouthEast quadrant: y-value less than centroid
				// y-value

				centY = centroid.coords[1] - kboundary;
				if (SE == null) {
					// no child here, create new Black node
					SE = new PRQuadBlackNode(new KDPoint(centX, centY), k - 1, bucketingParam, p);
				} else {
					// child already here, insert based on whatever color node it is

					SE = SE.insert(p, k);
				}
			}
		} else {
			// point will go either in NorthWest or SouthWest quadrant since x-value is less
			// than centroid's x-value
			// extract x-value of the centroid

			centX = centroid.coords[0] - kboundary;
			if (p.coords[1] < centroid.coords[1]) {
				// insert point has to go to the SouthWest quadrant: y-value less than centroid
				// y-value
				centY = centroid.coords[1] - kboundary;
				if (SW == null) {
					// no child here, create new Black node

					SW = new PRQuadBlackNode(new KDPoint(centX, centY), k - 1, bucketingParam, p);
				} else {
					// child already here, insert based on whatever color node it is

					SW = SW.insert(p, k);
				}
			} else {
				// insert point has to go to the NorthWest quadrant: y-value greater than
				// centroid y-value

				centY = centroid.coords[1] + kboundary;
				if (NW == null) {
					// no child here, create new Black node

					NW = new PRQuadBlackNode(new KDPoint(centX, centY), k - 1, bucketingParam, p);
				} else {
					// child already here, insert based on whatever color node it is

					NW = NW.insert(p, k);
				}
			}
		}
		return this;
	}

	/**
	 * <p>
	 * Deleting a {@link KDPoint} from a {@link PRQuadGrayNode} consists of
	 * recursing to the appropriate {@link PRQuadBlackNode} child to find the
	 * provided {@link KDPoint}. If no such child exists, the search has
	 * <b>necessarily failed</b>; <b>no changes should then be made to the subtree
	 * rooted at the current node!</b>
	 * </p>
	 *
	 * <p>
	 * Polymorphism will allow for the recursive call to be made into the
	 * appropriate delete method. Importantly, after the recursive deletion call, it
	 * needs to be determined if the current {@link PRQuadGrayNode} needs to be
	 * collapsed into a {@link PRQuadBlackNode}. This can only happen if it has no
	 * gray children, and one of the following two conditions are satisfied:
	 * </p>
	 *
	 * <ol>
	 * <li>The deletion left it with a single black child. Then, there is no reason
	 * to further subdivide the quadrant, and we can replace this with a
	 * {@link PRQuadBlackNode} that contains the {@link KDPoint}s that the single
	 * black child contains.</li>
	 * <li>After the deletion, the <b>total</b> number of {@link KDPoint}s contained
	 * by <b>all</b> the black children is <b>equal to or smaller than</b> the
	 * bucketing parameter. We can then similarly replace this with a
	 * {@link PRQuadBlackNode} over the {@link KDPoint}s contained by the black
	 * children.</li>
	 * </ol>
	 *
	 * @param p A {@link KDPoint} to delete from the tree rooted at the current
	 *          node.
	 * @return The subtree rooted at the current node, potentially adjusted after
	 *         deletion.
	 */
	@Override
	public PRQuadNode delete(KDPoint p) {

		PRQuadBlackNode deleteNode = new PRQuadBlackNode(centroid, k, bucketingParam);
		deleteNode.points = new KDPoint[bucketingParam];
		ArrayList<KDPoint> points = new ArrayList<KDPoint>();

		int blackNum = 0;
		int greyNum = 0;

		for (PRQuadNode node : getChildren()) {
			// count how many black and grey node are children of current Node
			if (node != null && node.getClass().equals(PRQuadBlackNode.class)) {
				blackNum++;
			} else if (node != null && node.getClass().equals(PRQuadGrayNode.class)) {
				greyNum++;
				// extract all non-white-node children from each node
				for (KDPoint pts : ((PRQuadBlackNode) node).points) {

					if (pts != null) {
						points.add(pts);
					}
				}
			}
		}
		// perform intended deletion
		points.remove(p);

		if (p.coords[0] < centroid.coords[0] && p.coords[1] >= centroid.coords[1]) {
			// if point lies in the NW quadrant (-, +) perform grey or black node deletion

			// white node means nothing happens
			if (NW == null) {
				return this;
			}

			NW = NW.delete(p);
			// if only one black node child, current grey node becomes a black node

			if (blackNum == 1) {
				return NW;
			}

			return deleteHelper(greyNum, points, deleteNode);

		} else if (p.coords[0] >= centroid.coords[0] && p.coords[1] >= centroid.coords[0]) {
			// if point lies in the NE quadrant (+, +) perform grey or black node deletion

			// white node means nothing happens
			if (NE == null) {
				return this;
			}
			NE = NE.delete(p);
			// if only one black node child, current grey node becomes a black node
			if (blackNum == 1) {
				return NE;
			}

			return deleteHelper(greyNum, points, deleteNode);

		} else if (p.coords[0] < centroid.coords[0] && p.coords[1] < centroid.coords[1]) {
			// if point lies in the SW quadrant (-, -) perform grey or black node deletion

			// white node means nothing happens
			if (SW == null) {
				return this;
			}

			SW = SW.delete(p);
			// if only one black node child, current grey node becomes a black node
			if (blackNum == 1) {
				return SW;
			}

			return deleteHelper(greyNum, points, deleteNode);

		} else if ((p.coords[0] >= centroid.coords[0] && p.coords[1] < centroid.coords[1])) {
			// if point lies in the SE quadrant (-, +) perform grey or black node deletion

			// white node means nothing happens
			if (SE == null) {
				return this;
			}
			SE = SE.delete(p);

			// if only one black node child, current grey node becomes a black node

			if (blackNum == 1) {
				return SE;
			}

			return deleteHelper(greyNum, points, deleteNode);
		}
		return this;
	}

	// Helper for deletion:
	// performs the conversion of a grey node to a black if bucketing param. is
	// greater than current KDPoint count and there's only one grey node present at
	// this subtree
	private PRQuadNode deleteHelper(int greyNum, ArrayList<KDPoint> points, PRQuadBlackNode greyToBlack) {
		// check conditions for condensing grey node
		if (count() <= bucketingParam && greyNum < 1) {
			int temp = 0;
			// extract all points and place them in new Black node
			for (KDPoint pts : points) {
				greyToBlack.points[temp++] = pts;
				greyToBlack.count++;
			}
			// return the new Black node
			return greyToBlack;
		} else {
			// otherwise, this helper function does nothing
			return this;
		}
	}

	@Override
	public boolean search(KDPoint p) {
		if (k < 0) {
			throw new CentroidAccuracyException("K can't be negative");
		}
		if (p == null) {
			return false;
		}

		// define current dimension restriction
		int kBoundary = (int) Math.pow(2, k);

		if (p.coords[0] < centroid.coords[0] && p.coords[1] >= centroid.coords[1]) {
			// if the KD point has a less than x-value and a GTE y-value compared to the
			// centroid,
			// point should be in NW quadrant
			if (NW == null) {
				return false;
			}

			// recursive call to search on NE quadrant (greedy)
			return NW.search(p);

		} else if ((p.coords[0] >= centroid.coords[0] && p.coords[1] >= centroid.coords[1])
				|| (p.coords[0] == centroid.coords[0] && p.coords[1] > centroid.coords[1])) {
			// if the KD point has a GTE x-value and a GTE y-value, point should be in NE
			// quadrant
			// NE quadrant contains the inclusive boundary: any point on the NE x or y axis
			// is within NE
			if (NE == null) {
				return false;
			}
			// recursive call to search on NE quadrant (greedy)
			return NE.search(p);
		} else if (p.coords[0] < centroid.coords[0] && p.coords[1] < centroid.coords[1]) {
			// if the KD point has a lesser x-value and a lesser y-value, point should be in
			// SW quadrant
			if (SW == null) {
				return false;
			}
			// recursive call to search on NE quadrant (greedy)

			return SW.search(p);
		} else if ((p.coords[0] >= centroid.coords[0] && p.coords[1] < centroid.coords[1])
				|| (p.coords[0] == centroid.coords[0] && p.coords[1] < centroid.coords[1])) {
			// if the KD point has a GTE x-value and a less than y-value, point should be in
			// SE quadrant
			// SE quadrant contains an inclusive y-axis boundary: any point on the NE y axis
			// is within NE
			if (SE == null) {
				return false;
			}

			return SE.search(p);
		} else if (p.coords[0] > centroid.coords[0] + kBoundary || p.coords[0] < centroid.coords[0] - kBoundary
				|| p.coords[1] < centroid.coords[1] - kBoundary || p.coords[1] > centroid.coords[1] + kBoundary) {
			// if the KDPoint is being out of the bounds of the current dimensions,
			return false;
		} else {
			return false;
		}
	}

	@Override
	public int height() {
		int NWHeight = 0;
		int NEHeight = 0;
		int SWHeight = 0;
		int SEHeight = 0;

		// grab heights of each child
		if (NW != null) {
			NWHeight = 1 + NW.height();
		}
		if (NE != null) {
			NEHeight = 1 + NE.height();
		}
		if (SW != null) {
			SWHeight = 1 + SW.height();
		}
		if (SE != null) {
			SEHeight = 1 + SE.height();
		}

		// compute the maximum of all children heights
		return Math.max(Math.max(NWHeight, NEHeight), Math.max(SWHeight, SEHeight));
	}

	@Override
	public int count() {
		int totalCount = 0;

		// grab KDPoint counts of each child

		if (NW != null) {
			totalCount += NW.count();
		}
		if (NE != null) {
			totalCount += NE.count();
		}
		if (SW != null) {
			totalCount += SW.count();
		}
		if (SE != null) {
			totalCount += SE.count();
		}

		// return sum of KDPoint counts
		return totalCount;
	}

	/**
	 * Returns the children of the current node in the form of a Z-ordered 1-D
	 * array.
	 * 
	 * @return An array of references to the children of {@code this}. The order is
	 *         Z (Morton), like so:
	 *         <ol>
	 *         <li>0 is NW</li>
	 *         <li>1 is NE</li>
	 *         <li>2 is SW</li>
	 *         <li>3 is SE</li>
	 *         </ol>
	 */
	public PRQuadNode[] getChildren() {
		PRQuadNode[] children = new PRQuadNode[4];
		// following Z-order
		children[0] = NW;
		children[1] = NE;
		children[2] = SW;
		children[3] = SE;

		return children;
	}

	@Override
	public void range(KDPoint anchor, Collection<KDPoint> results, double range) {
		// Range process:
		// Check coordinates of anchor and determine which quadrant that is most likely to contain the point. 
		// Call range recursively on closest quadrant first
		// Then call range on other quadrants in Z-order
		if (anchor.coords[0] < centroid.coords[0] && anchor.coords[1] >= centroid.coords[1]) {
			// NW quadrant will be queried first
			if (NW != null) {
				NW.range(anchor, results, range);
			}

			// range calls in Z-order
			if (NE != null && NE.doesQuadIntersectAnchorRange(anchor, range)) {
				NE.range(anchor, results, range);
			}

			if (SW != null && SW.doesQuadIntersectAnchorRange(anchor, range)) {
				SW.range(anchor, results, range);
			}

			if (SE != null && SE.doesQuadIntersectAnchorRange(anchor, range)) {
				SE.range(anchor, results, range);
			}

		} else if (anchor.coords[0] >= centroid.coords[0] && anchor.coords[1] >= centroid.coords[1]) {
			// NE quadrant will be queried first
			if (NE != null) {
				NE.range(anchor, results, range);
			}

			// range calls in Z-order

			if (NW != null && NW.doesQuadIntersectAnchorRange(anchor, range)) {
				NW.range(anchor, results, range);
			}

			if (SW != null && SW.doesQuadIntersectAnchorRange(anchor, range)) {
				SW.range(anchor, results, range);
			}

			if (SE != null && SE.doesQuadIntersectAnchorRange(anchor, range)) {
				SE.range(anchor, results, range);
			}

		} else if (anchor.coords[0] < centroid.coords[0] && anchor.coords[1] < centroid.coords[1]) {
			// SW quadrant will be queried first

			if (SW != null) {
				SW.range(anchor, results, range);
			}

			// range calls in Z-order

			if (NW != null && NW.doesQuadIntersectAnchorRange(anchor, range)) {
				NW.range(anchor, results, range);
			}

			if (NE != null && NE.doesQuadIntersectAnchorRange(anchor, range)) {
				NE.range(anchor, results, range);
			}

			if (SE != null && SE.doesQuadIntersectAnchorRange(anchor, range)) {
				SE.range(anchor, results, range);
			}

		} else if (anchor.coords[0] >= centroid.coords[0] && anchor.coords[1] < centroid.coords[1]) {
			// SE quadrant will be queried first

			if (SE != null) {
				SE.range(anchor, results, range);
			}

			// range calls in Z-order

			if (NW != null && NW.doesQuadIntersectAnchorRange(anchor, range)) {
				NW.range(anchor, results, range);
			}

			if (NE != null && NE.doesQuadIntersectAnchorRange(anchor, range)) {
				NE.range(anchor, results, range);
			}

			if (SW != null && SW.doesQuadIntersectAnchorRange(anchor, range)) {
				SW.range(anchor, results, range);
			}

		} else {
			// query went out of bounds
			throw new CentroidAccuracyException("Range Query out of bounds based on k-dim: " + this.k);
		}
	}

	@Override
	public NNData<KDPoint> nearestNeighbor(KDPoint anchor, NNData<KDPoint> n) {
		// if best distance is set to Infinity/-1, then update it with a large enough INT
		// that will be replaced by a smaller INT on the first KDPoint visited
		if (n.getBestDist() == -1) {
			n.update(null, 10000);
		}
		// Nearest Neighbor process:
		// Check coordinates of anchor and determine which quadrant is most likely
		// to contain the anchor. 
		// Call NN recursively on that quadrant first if it intersects the range of the query
		// Then call NN on other quadrants in Z-order to find other potential nearest neighbors.
		if (anchor.coords[0] < centroid.coords[0] && anchor.coords[1] >= centroid.coords[1]) {
			
			if (NW != null && NW.doesQuadIntersectAnchorRange(anchor, n.getBestDist())) {
				NW.nearestNeighbor(anchor, n);
			}
			
			if (NE != null && NE.doesQuadIntersectAnchorRange(anchor, n.getBestDist())) {
				NE.nearestNeighbor(anchor, n);
			}
			
			if (SW != null && SW.doesQuadIntersectAnchorRange(anchor, n.getBestDist())) {
				SW.nearestNeighbor(anchor, n);
			}
			
			if (SE != null && SE.doesQuadIntersectAnchorRange(anchor, n.getBestDist())) {
				SE.nearestNeighbor(anchor, n);
			}
		}else if (anchor.coords[0] >= centroid.coords[0] && anchor.coords[1] >= centroid.coords[1]) {
			if (NE != null) {
				NE.nearestNeighbor(anchor, n);
			}
			
			if (NW != null && NW.doesQuadIntersectAnchorRange(anchor, n.getBestDist())) {
				NW.nearestNeighbor(anchor, n);
			}
			
			if (SW != null && SW.doesQuadIntersectAnchorRange(anchor, n.getBestDist())) {
				SW.nearestNeighbor(anchor, n);
			}
			
			if (SE != null && SE.doesQuadIntersectAnchorRange(anchor, n.getBestDist())) {
				SE.nearestNeighbor(anchor, n);
			}

		} else if (anchor.coords[0] < centroid.coords[0] && anchor.coords[1] < centroid.coords[1]) {
			
			if (SW != null && SW.doesQuadIntersectAnchorRange(anchor, n.getBestDist())) {
				SW.nearestNeighbor(anchor, n);
			}
			
			if (NW != null && NW.doesQuadIntersectAnchorRange(anchor, n.getBestDist())) {
				NW.nearestNeighbor(anchor, n);
			}
			
			if (NE != null && NE.doesQuadIntersectAnchorRange(anchor, n.getBestDist())) {
				NE.nearestNeighbor(anchor, n);
			}
			
			if (SE != null && SE.doesQuadIntersectAnchorRange(anchor, n.getBestDist())) {
				SE.nearestNeighbor(anchor, n);
			}
			
		} else if (anchor.coords[0] >= centroid.coords[0] && anchor.coords[1] < centroid.coords[1]){
			
			if (SE != null && SE.doesQuadIntersectAnchorRange(anchor, n.getBestDist())) {
				SE.nearestNeighbor(anchor, n);
			}
			
			if (NW != null && NW.doesQuadIntersectAnchorRange(anchor, n.getBestDist())) {
				NW.nearestNeighbor(anchor, n);
			}
			
			if (NE != null && NE.doesQuadIntersectAnchorRange(anchor, n.getBestDist())) {
				NE.nearestNeighbor(anchor, n);
			}
			
			if (SW != null && SW.doesQuadIntersectAnchorRange(anchor, n.getBestDist())) {
				SW.nearestNeighbor(anchor, n);
			}

		}else {
			throw new CentroidAccuracyException("NN query went out of bounds based on k-dim: " + this.k);
		}
		return n;
	}

	@Override
	public void kNearestNeighbors(int k, KDPoint anchor, BoundedPriorityQueue<KDPoint> queue) {
		//kNN process:
		// Find the quadrant that is most likely to contain the anchor or points like the anchor.
		// recursively call kNN on that quadrant/child.
		// Then check other nodes 
		// to add to BPQ, the node must be within euclidean dist. of the anchor.
		// The BPQ will decide whether a KDPoint is added based on priority/distance, if at capacity,
		// compared to the last element in the queue.
		if (anchor.coords[0] < centroid.coords[0] && anchor.coords[1] >= centroid.coords[1]) {

			if (NW != null) {
				NW.kNearestNeighbors(k, anchor, queue);
			}
				
			if (NE != null) {
				//Possible pruning occurs here
				if (NE.doesQuadIntersectAnchorRange(anchor, queue.last().euclideanDistance(anchor))) {
					NE.kNearestNeighbors(k, anchor, queue);
				}
			}
			if (SW != null) {
				if (SW.doesQuadIntersectAnchorRange(anchor, queue.last().euclideanDistance(anchor))) {
					SW.kNearestNeighbors(k, anchor, queue);
				}
			}
			if (SE != null) {
				if (SE.doesQuadIntersectAnchorRange(anchor, queue.last().euclideanDistance(anchor))) {
					SE.kNearestNeighbors(k, anchor, queue);
				}
			}

		} else if (anchor.coords[0] >= centroid.coords[0] && anchor.coords[1] >= centroid.coords[1]) {
			if (NE != null) {
				NE.kNearestNeighbors(k, anchor, queue);
			}
			
			if (NW != null) {
				if (NW.doesQuadIntersectAnchorRange(anchor, queue.last().euclideanDistance(anchor))) {
					NW.kNearestNeighbors(k, anchor, queue);
				}
			}
			if (SW != null) {
				if (SW.doesQuadIntersectAnchorRange(anchor, queue.last().euclideanDistance(anchor))) {
					SW.kNearestNeighbors(k, anchor, queue);
				}
			}
			if (SE != null) {
				if (SE.doesQuadIntersectAnchorRange(anchor, queue.last().euclideanDistance(anchor))) {
					SE.kNearestNeighbors(k, anchor, queue);
				}
			}

		} else if (anchor.coords[0] < centroid.coords[0] && anchor.coords[1] < centroid.coords[1]) {

			if (SW != null) {
				SW.kNearestNeighbors(k, anchor, queue);
			}

			if (NW != null) {
				if (NW.doesQuadIntersectAnchorRange(anchor, queue.last().euclideanDistance(anchor))) {
					NW.kNearestNeighbors(k, anchor, queue);
				}
			}
			if (NE != null) {
				if (NE.doesQuadIntersectAnchorRange(anchor, queue.last().euclideanDistance(anchor))) {
					NE.kNearestNeighbors(k, anchor, queue);
				}
			}
			if (SE != null) {
				if (SE.doesQuadIntersectAnchorRange(anchor, queue.last().euclideanDistance(anchor))) {
					SE.kNearestNeighbors(k, anchor, queue);
				}
			}

		} else if(anchor.coords[0] >= centroid.coords[0] && anchor.coords[1] < centroid.coords[1]){

			if (SE != null) {
				SE.kNearestNeighbors(k, anchor, queue);
			}

			if (NW != null) {
				if (NW.doesQuadIntersectAnchorRange(anchor, queue.last().euclideanDistance(anchor))) {
					NW.kNearestNeighbors(k, anchor, queue);
				}
			}
			if (NE != null) {
				if (NE.doesQuadIntersectAnchorRange(anchor, queue.last().euclideanDistance(anchor))) {
					NE.kNearestNeighbors(k, anchor, queue);
				}
			}
			if (SW != null) {
				if (SW.doesQuadIntersectAnchorRange(anchor, queue.last().euclideanDistance(anchor))) {
					SW.kNearestNeighbors(k, anchor, queue);
				}
			}
		}
	}
}
