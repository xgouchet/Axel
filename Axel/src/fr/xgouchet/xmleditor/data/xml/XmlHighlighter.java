package fr.xgouchet.xmleditor.data.xml;

import fr.xgouchet.xmleditor.data.tree.TreeNode;
import fr.xgouchet.xmleditor.ui.adapter.Highlighter;

public class XmlHighlighter implements Highlighter<XmlData> {

	public XmlHighlighter(final String query) {
		mQuery = query;
	}

	@Override
	public boolean shouldHighlight(final TreeNode<XmlData> node) {

		XmlData content = node.getContent();
		if (content.isElement()) {
			return content.getName().contains(mQuery);
		} else if (content.isText()) {
			return content.getText().contains(mQuery);
		}
		return false;
	}

	private String mQuery;
}
