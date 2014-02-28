package fr.xgouchet.xmleditor.ui.adapter;

import android.widget.ImageView;
import android.widget.TextView;
import fr.xgouchet.xmleditor.data.tree.TreeNode;


/**
 * Holder for a view displaying a TreeNode
 * 
 * @param <T>
 *            the content type of the {@link TreeNode}
 * @author Xavier Gouchet
 */
public class NodeViewHolder<T> {
    
    public TextView content;
    public ImageView decorator;
    
    
}
