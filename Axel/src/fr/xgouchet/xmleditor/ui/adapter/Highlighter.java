package fr.xgouchet.xmleditor.ui.adapter;

import fr.xgouchet.xmleditor.data.tree.TreeNode;

public interface Highlighter<T> {

	boolean shouldHighlight(final TreeNode<T> node);
}
