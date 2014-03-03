package fr.xgouchet.xmleditor.ui.adapter;

import android.view.View;
import fr.xgouchet.xmleditor.data.tree.TreeNode;


public interface NodeViewListener<T> {
    
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
