package fr.xgouchet.xmleditor.data.html;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.CommentNode;
import org.htmlcleaner.ContentNode;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.HtmlNode;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.TagNodeVisitor;

import android.text.TextUtils;
import fr.xgouchet.xmleditor.data.xml.XmlNode;
import fr.xgouchet.xmleditor.data.xml.XmlTreeParser;

public class HtmlCleanerParser extends XmlTreeParser implements TagNodeVisitor {

	/**
	 * @param input
	 *            the input character stream
	 * @param createDocDecl
	 *            create the document declaration ?
	 * @return an XML Node from the parser
	 * @throws XmlTreeParserException
	 *             if an error occurs during the parsing
	 */
	public static XmlNode parseHtmlTree(final Reader input) throws IOException {

		HtmlCleanerParser parser;
		HtmlCleaner cleaner;

		parser = new HtmlCleanerParser();
		cleaner = new HtmlCleaner();
		setupHtmlCleanerProperties(cleaner);

		TagNode node;
		node = cleaner.clean(input);

		parser.createRootDocument();
		parser.getRoot().addChildNode(
				XmlNode.createDocumentDeclaration("1.O", null, null));

		// parser.readElement(node);
		node.traverse(parser);

		return parser.getRoot();
	}

	/**
	 * @param cleaner
	 *            the cleaner object to setup
	 */
	protected static void setupHtmlCleanerProperties(final HtmlCleaner cleaner) {
		CleanerProperties props;
		props = cleaner.getProperties();

		// TODO check the cleaner properties
		props.setUseEmptyElementTags(true);
	}

	/**
	 * @see org.htmlcleaner.TagNodeVisitor#visit(org.htmlcleaner.TagNode,
	 *      org.htmlcleaner.HtmlNode)
	 */
	public boolean visit(final TagNode parentNode, final HtmlNode htmlNode) {
		Class<?> nodeClass;
		XmlNode parent;

		parent = getMap().get(parentNode);
		if (parent == null) {
			parent = getRoot();
		}

		nodeClass = htmlNode.getClass();
		if (CommentNode.class.isAssignableFrom(nodeClass)) {
			visitComment(parent, (CommentNode) htmlNode);
		} else if (ContentNode.class.isAssignableFrom(nodeClass)) {
			visitText(parent, (ContentNode) htmlNode);
		} else {
			visitTag(parent, (TagNode) htmlNode);
		}

		return true;
	}

	protected void visitTag(final XmlNode parent, final TagNode tag) {
		XmlNode elt;
		Map<String, String> attrs;
		Set<String> keys;

		elt = XmlNode.createElement(tag.getName());
		attrs = tag.getAttributes();
		keys = attrs.keySet();

		for (String key : keys) {
			elt.getContent().addAttribute(null, key, attrs.get(key));
		}

		parent.addChildNode(elt);
		getMap().put(tag, elt);

	}

	protected void visitText(final XmlNode parent, final ContentNode content) {
		String textContent = content.toString();
		if (!TextUtils.isEmpty(textContent)) {
			textContent = textContent.trim();
			if (!TextUtils.isEmpty(textContent)) {
				parent.addChildNode(XmlNode.createText(textContent));
			}
		}
	}

	protected void visitComment(final XmlNode parent, final CommentNode com) {
		parent.addChildNode(XmlNode.createComment(com.getContent().toString()));
	}

	protected Map<TagNode, XmlNode> getMap() {
		if (mStackMap == null) {
			mStackMap = new HashMap<TagNode, XmlNode>();
		}
		return mStackMap;
	}

	protected Map<TagNode, XmlNode> mStackMap;
}
