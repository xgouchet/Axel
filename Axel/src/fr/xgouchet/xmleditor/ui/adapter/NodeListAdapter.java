package fr.xgouchet.xmleditor.ui.adapter;

import java.util.List;

import android.content.Context;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView.BufferType;
import fr.xgouchet.xmleditor.R;
import fr.xgouchet.xmleditor.data.tree.TreeNode;


/**
 * Displays a {@link TreeNode} in a List form, using a ListView. Unlike the {@link NodeTreeAdapter}
 * it will only display the first depth of the tree
 * 
 * @param <T>
 *            the content type of the {@link TreeNode}
 * @author Xavier Gouchet
 */
public class NodeListAdapter<T> extends ArrayAdapter<TreeNode<T>> {
    
    private AbstractTreeNodeStyler<T> mNodeStyler;
    private final LayoutInflater mInflater;
    private final NodeViewListener<T> mListener;
    
    private final int mIndentSize;
    private boolean mHasRoot;
    
    /**
     * @param context
     *            the current application context
     * @param nodes
     *            the nodes to display
     */
    public NodeListAdapter(final Context context, final List<TreeNode<T>> nodes,
            final NodeViewListener<T> listener) {
        super(context, R.layout.item_node_sort, nodes);
        
        mInflater = LayoutInflater.from(context);
        mListener = listener;
        
        mIndentSize = context.getResources().getDimensionPixelOffset(R.dimen.padding_unit);
    }
    
    
    /**
     * @see android.widget.ArrayAdapter#getView(int, android.view.View, android.view.ViewGroup)
     */
    @SuppressWarnings("unchecked")
    @Override
    public View getView(final int position, final View convertView,
            final ViewGroup parent) {
        
        View view = convertView;
        NodeViewHolder<T> holder;
        
        if (view == null) {
            // inflate view
            view = mInflater.inflate(R.layout.item_node, parent, false);
            view.setLongClickable(true);
            
            // create Holder
            holder = new NodeViewHolder<T>(view, getContext(), mListener);
            
            view.setTag(holder);
        } else {
            holder = (NodeViewHolder<T>) view.getTag();
        }
        
        // get node to display
        TreeNode<T> node;
        node = getItem(position);
        
        holder.position = position;
        holder.node = node;
        
        if (node != null) {
            int indent;
            if (mHasRoot) {
                indent = ((position == 0) ? 0 : mIndentSize);
            } else {
                indent = 0;
            }
            displayNode(holder, node, mNodeStyler, getContext(), indent);
        }
        
        return view;
    }
    
    
    public void setNodeStyler(final AbstractTreeNodeStyler<T> nodeStyler) {
        mNodeStyler = nodeStyler;
    }
    
    
    public AbstractTreeNodeStyler<T> getNodeStyler() {
        return mNodeStyler;
    }
    
    
    private void displayNode(final NodeViewHolder<T> holder, final TreeNode<T> node,
            final AbstractTreeNodeStyler<T> styler, final Context context, final int indent) {
        
        // setup the content text
        holder.content.setVisibility(View.VISIBLE);
        holder.content.setHorizontallyScrolling(true);
        holder.content.setMovementMethod(new ScrollingMovementMethod());
        holder.content.scrollTo(0, 0);
        
        // set text with syntax highlight
        if (styler == null) {
            holder.content.setText(node.toString());
        } else {
            holder.content.setText(styler.getSpannableForContent(
                    node.getContent(), context), BufferType.SPANNABLE);
        }
        
        // set node indentation
        LinearLayout.LayoutParams params;
        params = (LinearLayout.LayoutParams) holder.decorator
                .getLayoutParams();
        params.leftMargin = indent;
        holder.decorator.setLayoutParams(params);
        
        // set node decorator icon ( + or . )
        if (node.getChildrenCount() == 0) {
            holder.decorator.setImageResource(R.drawable.expander_ignore);
        } else {
            if (holder.position == 0) {
                holder.decorator.setImageResource(R.drawable.expander_open);
            } else {
                holder.decorator.setImageResource(R.drawable.expander_haschild);
            }
        }
    }
    
    
    public void setHasRoot(boolean hasRoot) {
        mHasRoot = hasRoot;
    }
}
