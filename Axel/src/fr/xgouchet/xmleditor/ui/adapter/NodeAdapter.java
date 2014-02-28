package fr.xgouchet.xmleditor.ui.adapter;

import java.util.List;

import android.content.Context;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TextView.BufferType;
import fr.xgouchet.xmleditor.R;
import fr.xgouchet.xmleditor.data.tree.TreeNode;

/**
 * @param <T>
 *            the type of node content
 * 
 */
public class NodeAdapter<T> extends ArrayAdapter<TreeNode<T>> {

	/**
	 * @param context
	 *            the current application context
	 * @param nodes
	 *            the nodes to display
	 */
	public NodeAdapter(final Context context, final List<TreeNode<T>> nodes) {
		super(context, R.layout.item_node_sort, nodes);

		mInflater = LayoutInflater.from(context);
	}

	/**
	 * @see android.widget.ArrayAdapter#getView(int, android.view.View,
	 *      android.view.ViewGroup)
	 */
	@Override
    public View getView(final int position, final View convertView,
			final ViewGroup parent) {

		View view = convertView;

		if (view == null) {
			view = mInflater.inflate(R.layout.item_node_sort, parent, false);
		}

		TextView text;
		text = (TextView) view.findViewById(R.id.textNode);
		text.setHorizontallyScrolling(true);
		text.setMovementMethod(new ScrollingMovementMethod());
		text.scrollTo(0, 0);

		ImageView decorator;
		decorator = (ImageView) view.findViewById(R.id.imageDecorator);

		TreeNode<T> node;
		node = getItem(position);

		if (node != null) {
			text.setVisibility(View.VISIBLE);
			if (mNodeStyler != null) {
				text.setText(mNodeStyler.getSpannableForContent(
						node.getContent(), getContext()), BufferType.SPANNABLE);
			} else {
				text.setText(node.toString());
			}

			if (node.getChildrenCount() == 0) {
				decorator.setImageResource(R.drawable.expander_ignore);
			} else {
				decorator.setImageResource(R.drawable.expander_haschild);
			}
		}

		return view;
	}

	/**
	 * @param nodeStyler
	 *            the node styler to use
	 */
	public void setNodeStyler(final AbstractTreeNodeStyler<T> nodeStyler) {
		mNodeStyler = nodeStyler;
	}

    private AbstractTreeNodeStyler<T> mNodeStyler;
	private final LayoutInflater mInflater;
}
