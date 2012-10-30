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

		public TreeNodeHandle(View v) {
			view = v;

			textView = (TextView) v.findViewById(R.id.textNode);
			textView.setOnLongClickListener(this);

			decorator = (ImageView) v.findViewById(R.id.imageDecorator);
			decorator.setOnClickListener(this);

			Rect bounds = new Rect();
			decorator.getHitRect(bounds);
			bounds.right += mPaddingPixelUnit * 2;
			bounds.left -= mPaddingPixelUnit * 2;
			bounds.top -= mPaddingPixelUnit * 2;
			bounds.bottom += mPaddingPixelUnit * 2;
			v.setTouchDelegate(new TouchDelegate(bounds, decorator));

		}

		public void onClick(View v) {
			if (!node.isLeaf()) {
				node.switchExpanded();
				notifyDataSetChanged();
			}
		}

		public boolean onLongClick(View v) {
			view.getParent().showContextMenuForChild(view);
			return true;
		}

		public TreeNode<T> node;
		public View view;
		public TextView textView;
		public ImageView decorator;

	}

	/**
	 * @param context
	 *            the current application context
	 * @param treeRoot
	 *            the root of the tree to display
	 */
	public TreeAdapter(Context context, TreeNode<T> treeRoot) {
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
	public Object getItem(int position) {
		return getNode(position);
	}

	/**
	 * @param position
	 *            the position in the ListView
	 * @return the node at the given linear position
	 */
	public TreeNode<T> getNode(int position) {
		TreeNode<T> result = null;
		if (mTreeRoot != null) {
			result = mTreeRoot.getNode(position);
		}

		return result;
	}

	/**
	 * @see android.widget.Adapter#getItemId(int)
	 */
	public long getItemId(int position) {
		return position;
	}

	/**
	 * @see android.widget.Adapter#getView(int, android.view.View,
	 *      android.view.ViewGroup)
	 */
	@SuppressWarnings("unchecked")
	public View getView(int position, View convertView, ViewGroup parent) {

		View v = convertView;
		TreeNodeHandle handle;

		if (v == null) {
			v = mInflater.inflate(R.layout.item_node, parent, false);
			v.setLongClickable(true);
			handle = new TreeNodeHandle(v);
			v.setTag(handle);
		} else {
			Object tag = v.getTag();
			if (tag instanceof TreeAdapter.TreeNodeHandle) {
				handle = (TreeNodeHandle) tag;
			} else {
				handle = new TreeNodeHandle(v);
				v.setTag(handle);
			}
		}

		TreeNode<T> node = getNode(position);
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

			int indent = node.getDepth() * mPaddingPixelUnit;

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

		return v;
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
	public void setExpanded(boolean expand, boolean recursive) {
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
	public void setNodeStyler(TreeNodeStyler<T> nodeStyler) {
		mNodeStyler = nodeStyler;
	}

	/**
	 * Expand / collapse the node at the given position
	 * 
	 * @param position
	 *            a position in the list view
	 */
	public void expandCollapse(int position) {
		getNode(position).switchExpanded();
		notifyDataSetChanged();
	}

	private Context mContext;
	private TreeNode<T> mTreeRoot;
	private TreeNodeStyler<T> mNodeStyler;

	private LayoutInflater mInflater;

	private int mPaddingPixelUnit;

}
