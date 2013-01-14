package fr.xgouchet.xmleditor.ui.adapter;

import android.content.Context;
import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.TouchDelegate;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.BufferType;
import fr.xgouchet.xmleditor.R;
import fr.xgouchet.xmleditor.data.tree.TreeNode;

/**
 * Displays a {@link TreeNode} in a Tree form, using a ListView
 * 
 * @param <T>
 *            the content type of the {@link TreeNode}
 */
public class TreeAdapter<T> extends BaseAdapter {

	public class TreeNodeHandle implements OnLongClickListener, OnClickListener {

		public TreeNodeHandle(final View view) {
			nodeView = view;

			textView = (TextView) nodeView.findViewById(R.id.textNode);
			textView.setOnLongClickListener(this);

			decorator = (ImageView) nodeView.findViewById(R.id.imageDecorator);
			decorator.setOnClickListener(this);

			Rect bounds = new Rect();
			decorator.getHitRect(bounds);
			bounds.right += mPaddingPixelUnit * 2;
			bounds.left -= mPaddingPixelUnit * 2;
			bounds.top -= mPaddingPixelUnit * 2;
			bounds.bottom += mPaddingPixelUnit * 2;
			nodeView.setTouchDelegate(new TouchDelegate(bounds, decorator));

		}

		public void onClick(final View view) {
			if (!node.isLeaf()) {
				node.switchExpanded();
				notifyDataSetChanged();
			}
		}

		public boolean onLongClick(final View view) {
			nodeView.getParent().showContextMenuForChild(nodeView);
			return true;
		}

		public TreeNode<T> node;
		public View nodeView;
		public TextView textView;
		public ImageView decorator;

	}

	/**
	 * @param context
	 *            the current application context
	 * @param treeRoot
	 *            the root of the tree to display
	 */
	public TreeAdapter(final Context context, final TreeNode<T> treeRoot) {
		super();
		mContext = context;
		mInflater = (LayoutInflater) getContext().getSystemService(
				Context.LAYOUT_INFLATER_SERVICE);
		mTreeRoot = treeRoot;

		mPaddingPixelUnit = context.getResources().getDimensionPixelSize(
				R.dimen.padding_unit);
	}

	/**
	 * @see android.widget.Adapter#getCount()
	 */
	public int getCount() {
		return mTreeRoot.getViewCount();
	}

	/**
	 * @see android.widget.Adapter#getItem(int)
	 */
	public Object getItem(final int position) {
		return getNode(position);
	}

	/**
	 * @param position
	 *            the position in the ListView
	 * @return the node at the given linear position
	 */
	public TreeNode<T> getNode(final int position) {
		TreeNode<T> result = null;
		if (mTreeRoot != null) {
			result = mTreeRoot.getNode(position);
		}

		return result;
	}

	/**
	 * @see android.widget.Adapter#getItemId(int)
	 */
	public long getItemId(final int position) {
		return position;
	}

	/**
	 * @see android.widget.Adapter#getView(int, android.view.View,
	 *      android.view.ViewGroup)
	 */
	@SuppressWarnings("unchecked")
	public View getView(final int position, final View convertView,
			final ViewGroup parent) {

		View view = convertView;
		TreeNodeHandle handle;

		if (view == null) {
			view = mInflater.inflate(R.layout.item_node, parent, false);
			view.setLongClickable(true);
			handle = new TreeNodeHandle(view);
			view.setTag(handle);
		} else {
			final Object tag = view.getTag();
			if (tag instanceof TreeAdapter.TreeNodeHandle) {
				handle = (TreeNodeHandle) tag;
			} else {
				handle = new TreeNodeHandle(view);
				view.setTag(handle);
			}
		}

		final TreeNode<T> node = getNode(position);
		handle.node = node;

		if (node != null) {
			handle.textView.setVisibility(View.VISIBLE);

			// text content
			if (mNodeStyler != null) {
				handle.textView.setText(mNodeStyler.getSpannableForContent(
						node.getContent(), getContext()), BufferType.SPANNABLE);
			} else {
				handle.textView.setText(node.toString());
			}

			int indent;
			indent = node.getDepth() * mPaddingPixelUnit;

			// Indentation
			LinearLayout.LayoutParams params;
			params = (LinearLayout.LayoutParams) handle.decorator
					.getLayoutParams();
			params.leftMargin = indent;
			handle.decorator.setLayoutParams(params);

			// text width
			handle.textView.setMinimumWidth(((ListView) parent).getWidth()
					- indent - (4 * mPaddingPixelUnit));

			// Decorator image
			if (node.isLeaf()) {
				handle.decorator.setImageResource(R.drawable.expander_ignore);
			} else if (node.isExpanded()) {
				handle.decorator.setImageResource(R.drawable.expander_close);
			} else {
				handle.decorator.setImageResource(R.drawable.expander_open);
			}
		} else {
			handle.textView.setVisibility(View.GONE);
		}

		return view;
	}

	/**
	 * @return the current context
	 */
	public Context getContext() {
		return mContext;
	}

	/**
	 * Expand/collapse the root, and if recursive is set, the whole tree
	 * 
	 * @param expand
	 *            the expanded set
	 * @param recursive
	 *            propagate the state to the whole tree
	 */
	public void setExpanded(final boolean expand, final boolean recursive) {
		if (mTreeRoot != null) {
			mTreeRoot.setExpanded(expand, recursive);
		}
	}

	/**
	 * @return if the root {@link TreeNode} is expanded
	 */
	public boolean isRootExpanded() {
		return mTreeRoot.isExpanded();
	}

	/**
	 * @param nodeStyler
	 *            the styler used to display the {@link TreeNode}
	 */
	public void setNodeStyler(final AbstractTreeNodeStyler<T> nodeStyler) {
		mNodeStyler = nodeStyler;
	}

	/**
	 * Expand / collapse the node at the given position
	 * 
	 * @param position
	 *            a position in the list view
	 */
	public void expandCollapse(final int position) {
		getNode(position).switchExpanded();
		notifyDataSetChanged();
	}

	private final Context mContext;
	private final TreeNode<T> mTreeRoot;
	private AbstractTreeNodeStyler<T> mNodeStyler;

	private final LayoutInflater mInflater;

	private final int mPaddingPixelUnit;

}
