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
	public NodeAdapter(Context context, List<TreeNode<T>> nodes) {
		super(context, R.layout.item_node_sort, nodes);

		mInflater = LayoutInflater.from(context);
	}

	/**
	 * @see android.widget.ArrayAdapter#getView(int, android.view.View,
	 *      android.view.ViewGroup)
	 */
	public View getView(int position, View convertView, ViewGroup parent) {

		View v = convertView;

		if (v == null) {
			v = mInflater.inflate(R.layout.item_node_sort, parent, false);
		}

		TextView text = (TextView) v.findViewById(R.id.textNode);
		text.setHorizontallyScrolling(true);
		text.setMovementMethod(new ScrollingMovementMethod());
		text.scrollTo(0, 0);

		ImageView decorator = (ImageView) v.findViewById(R.id.imageDecorator);

		TreeNode<T> node = getItem(position);

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

		return v;
	}

	/**
	 * @param nodeStyler
	 *            the node styler to use
	 */
	public void setNodeStyler(TreeNodeStyler<T> nodeStyler) {
		mNodeStyler = nodeStyler;
	}

	private TreeNodeStyler<T> mNodeStyler;

	private LayoutInflater mInflater;
}
