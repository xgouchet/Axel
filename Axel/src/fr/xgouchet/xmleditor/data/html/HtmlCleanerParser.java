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
import fr.xgouchet.xmleditor.data.xml.XmlTreeParserException;
import fr.xgouchet.xmleditor.data.xml.XmlTreeParserException.XmlError;

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
	public static XmlNode parseHtmlTree(Reader input)
			throws XmlTreeParserException {

		HtmlCleanerParser parser = new HtmlCleanerParser();

		HtmlCleaner cleaner = new HtmlCleaner();
		setupHtmlCleanerProperties(cleaner);

		TagNode node;
		try {
			node = cleaner.clean(input);
		} catch (IOException e) {
			throw new XmlTreeParserException(XmlError.ioException, e);
		}

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
	protected static void setupHtmlCleanerProperties(HtmlCleaner cleaner) {
		CleanerProperties props = cleaner.getProperties();

		// TODO check the cleaner properties
		props.setUseEmptyElementTags(true);
	}

	/**
	 * @see org.htmlcleaner.TagNodeVisitor#visit(org.htmlcleaner.TagNode,
	 *      org.htmlcleaner.HtmlNode)
	 */
	public boolean visit(TagNode parentNode, HtmlNode htmlNode) {

		Class<?> nodeClass = htmlNode.getClass();
		XmlNode parent = getMap().get(parentNode);
		if (parent == null) {
			parent = getRoot();
		}

		if (CommentNode.class.isAssignableFrom(nodeClass)) {
			visitComment(parent, (CommentNode) htmlNode);
		} else if (ContentNode.class.isAssignableFrom(nodeClass)) {
			visitText(parent, (ContentNode) htmlNode);
		} else {
			visitTag(parent, (TagNode) htmlNode);
		}

		return true;
	}

	protected void visitTag(XmlNode parent, TagNode tag) {
		XmlNode elt = XmlNode.createElement(tag.getName());

		Map<String, String> attrs = tag.getAttributes();
		Set<String> keys = attrs.keySet();
		for (String key : keys) {
			elt.getContent().addAttribute(null, key, attrs.get(key));
		}

		parent.addChildNode(elt);
		getMap().put(tag, elt);

	}

	protected void visitText(XmlNode parent, ContentNode content) {
		String textContent = content.toString();
		if (!TextUtils.isEmpty(textContent)) {
			textContent = textContent.trim();
			if (!TextUtils.isEmpty(textContent)) {
				XmlNode text = XmlNode.createText(textContent);
				parent.addChildNode(text);
			}
		}
	}

	protected void visitComment(XmlNode parent, CommentNode com) {
		XmlNode comment = XmlNode.createComment(com.getContent().toString());
		parent.addChildNode(comment);
	}

	protected Map<TagNode, XmlNode> getMap() {
		if (mStackMap == null) {
			mStackMap = new HashMap<TagNode, XmlNode>();
		}
		return mStackMap;
	}

	protected Map<TagNode, XmlNode> mStackMap;
}
