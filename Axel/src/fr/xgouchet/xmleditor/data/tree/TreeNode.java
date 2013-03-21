package fr.xgouchet.xmleditor.data.tree;

import java.util.LinkedList;
import java.util.List;

import fr.xgouchet.xmleditor.data.xml.XmlNode;

/**
 * A tree node, holding some user data
 * 
 * @param <T>
 *            the user data class
 */
public class TreeNode<T> {

	/**
	 * @param parent
	 *            the parent node
	 * @param content
	 *            the node content
	 */
	public TreeNode(final TreeNode<T> parent, final T content) {
		mChildren = new LinkedList<TreeNode<T>>();
		mContent = content;
		mParent = parent;

		mExpanded = true;
		mViewCount = 1;
		updateDepth();
	}

	/**
	 * Adds a child node
	 * 
	 * @param child
	 *            the child to add
	 * @return if the child was added
	 */
	public boolean addChildNode(final TreeNode<T> child) {
		final boolean result = mChildren.add(child);

		if (result) {
			child.setParent(this);
			onChildListChanged();
		}

		updateParentViewCount();

		return result;
	}

	/**
	 * Adds a child at the given position in the child list
	 * 
	 * @param child
	 *            the child to add
	 * @param position
	 *            the position to insert the child
	 */
	public void addChildNode(final TreeNode<T> child, final int position) {
		mChildren.add(position, child);
		child.setParent(this);
		onChildListChanged();

		updateParentViewCount();
	}

	/**
	 * @param child
	 *            the child to remove if it exists
	 * @return if the node was removed (false usually means that the given node
	 *         wasn't a child of this node)
	 */
	public boolean removeChildNode(final TreeNode<T> child) {
		final boolean result = mChildren.remove(child);

		if (result) {
			child.setParent(null);
			onChildListChanged();
		}

		updateParentViewCount();

		return result;
	}

	/**
	 * @return if this node has children
	 */
	public boolean hasChildren() {
		return !mChildren.isEmpty();
	}

	/**
	 * @param node
	 *            the node to search for
	 * @param recursive
	 *            search recursively
	 * @return if the given node is a child of this node
	 */
	public boolean hasChildNode(final TreeNode<T> node, final boolean recursive) {
		boolean result = false;
		if (node == null) {
			result = false;
		} else if (node.equals(this)) {
			result = true;
		} else {
			for (TreeNode<T> child : mChildren) {
				if (recursive) {
					if (child.hasChildNode(node, recursive)) {
						result = true;
						break;
					}
				} else {
					if (child.equals(node)) {
						result = true;
						break;
					}
				}
			}
		}

		return result;
	}

	/**
	 * Called whenever the parent changed
	 */
	public void onParentChanged() {
		updateDepth();
	}

	/**
	 * Called whenever the child list changed (child added / removed)
	 */
	public void onChildListChanged() {
		// To be overriden by subclasses
	}

	/**
	 * Updates the depth of this node
	 */
	public final void updateDepth() {
		if (mParent == null) {
			mDepth = 0;
		} else {
			mDepth = mParent.getDepth() + 1;
		}

		for (TreeNode<T> child : mChildren) {
			child.updateDepth();
		}
	}

	/**
	 * @param parent
	 *            the parent for this node
	 */
	public void setParent(final TreeNode<T> parent) {
		if (!hasChildNode(parent, true)) {
			mParent = parent;
			onParentChanged();
		} else {
			throw new IllegalArgumentException();
		}
	}

	/**
	 * Removes this node from its parent children
	 * 
	 * @return if the node was removed (false usually means that the node is a
	 *         root)
	 */
	public boolean removeFromParent() {
		boolean result = false;
		if (mParent != null) {
			result = mParent.removeChildNode(this);
		}
		return result;
	}

	/**
	 * @return if this node is expanded
	 */
	public boolean isExpanded() {
		return mExpanded;
	}

	/**
	 * @return if this node is forced to be a leaf
	 */
	public boolean isForcedLeaf() {
		return mForceLeaf;
	}

	/**
	 * @return if this node is a leaf
	 */
	public boolean isLeaf() {
		return mChildren.isEmpty();
	}

	/**
	 * @return the parent node
	 */
	public TreeNode<T> getParent() {
		return mParent;
	}

	/**
	 * @return the depth of this node in the tree
	 */
	public int getDepth() {
		return mDepth;
	}

	/**
	 * @return the number of child for this node
	 */
	public int getChildrenCount() {
		return mChildren.size();
	}

	/**
	 * @return the children of this node
	 */
	public List<TreeNode<T>> getChildren() {
		return mChildren;
	}

	/**
	 * @param child
	 * @return the index of the child
	 */
	public int getChildPosition(final XmlNode child) {
		int index = -1;

		if (mChildren.contains(child)) {
			index = mChildren.indexOf(child);
		}

		return index;
	}

	/**
	 * @return the content of this node
	 */
	public T getContent() {
		return mContent;
	}

	/**
	 * @param index
	 *            the index of the child
	 * @return the child at the given index
	 */
	public TreeNode<T> getChildAtPos(final int index) {
		return mChildren.get(index);
	}

	/**
	 * @return the number of views needed to display this node and all its
	 *         children, according to its expanded state
	 */
	public int getViewCount() {
		// updateViewCount();
		return mViewCount;
	}

	/**
	 * 
	 */
	public void updateParentViewCount() {
		updateChildViewCount(false);
		if (mParent != null) {
			mParent.updateParentViewCount();
		}
	}

	/**
	 * 
	 */
	public void updateChildViewCount(final boolean recursive) {
		int count = 1;

		if (mExpanded) {
			for (TreeNode<T> child : mChildren) {
				if (recursive) {
					child.updateChildViewCount(recursive);
				}
				count += child.getViewCount();
			}
		}

		mViewCount = count;
	}

	/**
	 * Called whenever the child view changed (expand / collapse)
	 */
	public void onChildExpandCollapse() {
		int count = 1;

		if (mExpanded) {
			for (TreeNode<T> child : mChildren) {
				count += child.getViewCount();
			}
		}

		mViewCount = count;

		if (mParent != null) {
			mParent.onChildExpandCollapse();
		}
	}

	/**
	 * @param position
	 *            the position
	 * @return the node being displayed at the given position. Collapsed
	 *         subtrees are ignored
	 */
	public TreeNode<T> getNode(final int position) {
		TreeNode<T> result = null;

		if (position == 0) {
			result = this;
		} else if (position < getViewCount()) {
			int childpos = position - 1;
			int count;
			for (TreeNode<T> child : mChildren) {
				count = child.getViewCount();
				if (childpos < count) {
					result = child.getNode(childpos);
					break;
				} else {
					childpos -= count;
				}
			}
		}

		// if (result == null) {
		// throw new IndexOutOfBoundsException();
		// }

		return result;
	}

	/**
	 * Switch the expanded state of this node
	 */
	public void switchExpanded() {
		setExpanded(mExpanded ^ true);
	}

	/**
	 * @param expanded
	 *            the expanded state of this node
	 */
	public void setExpanded(final boolean expanded) {
		mExpanded = expanded;
		updateChildViewCount(false);
		if (mParent != null) {
			mParent.onChildExpandCollapse();
		}
	}

	/**
	 * @param expanded
	 *            the expanded state of this node
	 * @param recursive
	 *            propagate the state to the children of this node
	 */
	public void setExpanded(final boolean expanded, final boolean recursive) {
		setExpanded(expanded);

		if (recursive) {
			for (TreeNode<T> child : mChildren) {
				child.setExpanded(expanded, recursive);
			}
		}
	}

	/**
	 * Forces this node to be a leaf, meaning no child node can be added to this
	 * node (or it will raise an exception)
	 */
	public void setLeaf() {
		mForceLeaf = true;
		mChildren.clear();
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Node[content="
				+ ((mContent == null) ? "null" : mContent.toString()) + ";"
				+ mChildren.size() + " children]";
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object that) {
		boolean equal;

		if (this == that) {
			equal = true;
		} else if (!(that instanceof TreeNode<?>)) {
			equal = false;
		} else {
			final TreeNode<?> thatNode = (TreeNode<?>) that;
			if ((mContent == null) || (thatNode.mContent == null)) {
				equal = (mContent == thatNode.mContent);
			} else {
				equal = mContent.equals(thatNode.mContent);
			}
		}

		return equal;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		int contentHash = 0;

		if (mContent != null) {
			contentHash = mContent.hashCode();
		}

		return (37 * contentHash) + 1987;
	}

	/** The content of this node */
	protected T mContent;
	/** the list of children for this node */
	protected List<TreeNode<T>> mChildren;
	/** the parent of this node */
	protected TreeNode<T> mParent;
	/** this node's depth in the tree */
	protected int mDepth, mViewCount;
	/** a flag to prevent adding children to this ndoe */
	protected boolean mForceLeaf;
	/** is this node expanded (used for display only) */
	protected boolean mExpanded;

}
