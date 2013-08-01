package fr.xgouchet.xmleditor.ui.adapter;

import android.content.Context;
import android.graphics.Rect;
import android.view.GestureDetector;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.GestureDetector.OnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.TouchDelegate;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.BufferType;
import fr.xgouchet.xmleditor.R;
import fr.xgouchet.xmleditor.common.Constants;
import fr.xgouchet.xmleditor.common.Settings;
import fr.xgouchet.xmleditor.data.tree.TreeNode;

/**
 * Displays a {@link TreeNode} in a Tree form, using a ListView
 * 
 * @param <T>
 *            the content type of the {@link TreeNode}
 */
public class TreeAdapter<T> extends BaseAdapter {

	public interface TreeNodeEventListener<T> {
		/**
		 * Called when a node receives a single tap
		 * 
		 * @param node
		 *            the node
		 */
		public void onNodeTapped(TreeNode<T> node, View view, int position);

		/**
		 * Called when a node receives a double tap
		 * 
		 * @param node
		 *            the node
		 */
		public void onNodeDoubleTapped(TreeNode<T> node, View view, int position);

		/**
		 * Called when a node receives a long press
		 * 
		 * @param node
		 *            the node
		 */
		public void onNodeLongPressed(TreeNode<T> node, View view, int position);
	}

	public class TreeNodeHandle implements OnClickListener, OnTouchListener,
			OnGestureListener, OnDoubleTapListener {

		public TreeNodeHandle(final View view) {
			nodeView = view;

			textView = (TextView) nodeView.findViewById(R.id.textNode);
			textView.setOnTouchListener(this);

			decorator = (ImageView) nodeView.findViewById(R.id.imageDecorator);
			decorator.setOnClickListener(this);

			Rect bounds = new Rect();
			decorator.getHitRect(bounds);
			bounds.right += mPaddingPixelUnit * 2;
			bounds.left -= mPaddingPixelUnit * 2;
			bounds.top -= mPaddingPixelUnit * 2;
			bounds.bottom += mPaddingPixelUnit * 2;
			nodeView.setTouchDelegate(new TouchDelegate(bounds, decorator));

			scrollview = (HorizontalScrollView) nodeView
					.findViewById(R.id.textScrollView);

			mDetector = new GestureDetector(getContext(), this);
			mDetector.setOnDoubleTapListener(this);
		}

		/**
		 * @see android.view.View.OnClickListener#onClick(android.view.View)
		 */
		@Override
		public void onClick(final View view) {
			if (!node.isLeaf()) {
				node.switchExpanded();
				notifyDataSetChanged();
			}
		}

		/**
		 * @see android.view.View.OnTouchListener#onTouch(android.view.View,
		 *      android.view.MotionEvent)
		 */
		@Override
		public boolean onTouch(final View v, final MotionEvent event) {
			return mDetector.onTouchEvent(event);

		}

		/**
		 * @see android.view.GestureDetector.OnGestureListener#onDown(android.view.MotionEvent)
		 */
		@Override
		public boolean onDown(final MotionEvent e) {
			return true;
		}

		/**
		 * @see android.view.GestureDetector.OnGestureListener#onFling(android.view.MotionEvent,
		 *      android.view.MotionEvent, float, float)
		 */
		@Override
		public boolean onFling(final MotionEvent e1, final MotionEvent e2,
				final float velocityX, final float velocityY) {
			return false;
		}

		/**
		 * @see android.view.GestureDetector.OnGestureListener#onLongPress(android.view.MotionEvent)
		 */
		@Override
		public void onLongPress(final MotionEvent e) {
			if (mListener != null) {
				mListener.onNodeLongPressed(node, nodeView, position);
			}
		}

		/**
		 * @see android.view.GestureDetector.OnGestureListener#onScroll(android.view.MotionEvent,
		 *      android.view.MotionEvent, float, float)
		 */
		@Override
		public boolean onScroll(final MotionEvent e1, final MotionEvent e2,
				final float distanceX, final float distanceY) {
			return false;
		}

		/**
		 * @see android.view.GestureDetector.OnGestureListener#onShowPress(android.view.MotionEvent)
		 */
		@Override
		public void onShowPress(final MotionEvent e) {

		}

		/**
		 * @see android.view.GestureDetector.OnGestureListener#onSingleTapUp(android.view.MotionEvent)
		 */
		@Override
		public boolean onSingleTapUp(final MotionEvent e) {
			return true;
		}

		/**
		 * @see android.view.GestureDetector.OnDoubleTapListener#onDoubleTapEvent(android.view.MotionEvent)
		 */
		@Override
		public boolean onDoubleTapEvent(final MotionEvent e) {
			return true;
		}

		/**
		 * @see android.view.GestureDetector.OnDoubleTapListener#onDoubleTap(android.view.MotionEvent)
		 */
		@Override
		public boolean onDoubleTap(final MotionEvent e) {
			if (mListener != null) {
				mListener.onNodeDoubleTapped(node, nodeView, position);
			}
			return true;
		}

		/**
		 * @see android.view.GestureDetector.OnDoubleTapListener#onSingleTapConfirmed(android.view.MotionEvent)
		 */
		@Override
		public boolean onSingleTapConfirmed(final MotionEvent e) {
			if (mListener != null) {
				mListener.onNodeTapped(node, nodeView, position);
			}
			return true;
		}

		private final GestureDetector mDetector;

		public int position;
		public TreeNode<T> node;
		public View nodeView;
		public TextView textView;
		public ImageView decorator;
		public HorizontalScrollView scrollview;

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

		updatePadding();
	}

	public void updatePadding() {
		int indent_unit;
		if (Constants.INDENT_SMALL.equalsIgnoreCase(Settings.sIndentationSize)) {
			indent_unit = R.dimen.padding_unit_small;
		} else if (Constants.INDENT_LARGE
				.equalsIgnoreCase(Settings.sIndentationSize)) {
			indent_unit = R.dimen.padding_unit_large;
		} else {
			indent_unit = R.dimen.padding_unit;
		}

		mPaddingPixelUnit = mContext.getResources().getDimensionPixelSize(
				indent_unit);
	}

	/**
	 * @see android.widget.Adapter#getCount()
	 */
	@Override
	public int getCount() {
		return mTreeRoot.getViewCount();
	}

	/**
	 * @see android.widget.Adapter#getItem(int)
	 */
	@Override
	public TreeNode<T> getItem(final int position) {
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
	@Override
	public long getItemId(final int position) {
		return position;
	}

	/**
	 * @see android.widget.Adapter#getView(int, android.view.View,
	 *      android.view.ViewGroup)
	 */
	@Override
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
		handle.position = position;

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

		if ((mHighlighter != null) && mHighlighter.shouldHighlight(node)) {
			handle.nodeView.setBackgroundResource(R.drawable.item_selected);
		} else {
			handle.nodeView.setBackgroundResource(0);
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
	 * @param listener
	 *            the listener for node events
	 */
	public void setListener(final TreeNodeEventListener<T> listener) {
		mListener = listener;
	}

	/**
	 * @param highlighter
	 *            the highlighter for node search
	 */
	public void setHighlighter(final Highlighter<T> highlighter) {
		mHighlighter = highlighter;
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

	private TreeNodeEventListener<T> mListener;
	private Highlighter<T> mHighlighter;

	private final LayoutInflater mInflater;

	private int mPaddingPixelUnit;

}
