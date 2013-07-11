package fr.xgouchet.xmleditor.parser.xml;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import fr.xgouchet.xmleditor.data.xml.XmlNode;
import fr.xgouchet.xmleditor.data.xml.XmlValidator;

/**
 * 
 */
public abstract class XmlTreeParser {

	/**
	 */
	protected XmlTreeParser() {
		mStack = new Stack<XmlNode>();
		mNamespaceURIs = new HashMap<String, String>();
		mNamespacePrefixes = new HashMap<String, String>();
		mNewNamespaces = new LinkedList<String>();

		sLastNode = null;
	}

	/**
	 * Declares a new namespace
	 * 
	 * @param prefix
	 *            the namespace prefix
	 * @param uri
	 *            the namespace uri
	 */
	protected void declareNamespace(String prefix, String uri) {
		// TODO more checks if overwriting prefix or URI
		if (!mNamespacePrefixes.containsKey(prefix)) {
			mNamespaceURIs.put(uri, prefix);
			mNamespacePrefixes.put(prefix, uri);
			mNewNamespaces.add(uri);
		}
	}

	/**
	 * Creates the root document
	 */
	protected void createRootDocument() {
		XmlNode doc = XmlNode.createDocument();
		mStack.push(doc);
		mRoot = doc;
	}

	/**
	 * call when a new element is started
	 * 
	 * @param node
	 *            the node to push
	 */
	protected void onCreateElement(XmlNode node) {
		String prefix;
		for (String newUri : mNewNamespaces) {
			prefix = mNamespaceURIs.get(newUri);
			node.getContent().addAttribute(XmlValidator.XML_NS, prefix, newUri);
		}
		mNewNamespaces.clear();

		onCreateNode(node);

		mStack.push(node);

	}

	/**
	 * call when an element is closed
	 */
	protected void onCloseElement() {
		mStack.pop();
	}

	/**
	 * @param node
	 *            the create node (must node be an element node ! )
	 */
	protected void onCreateNode(XmlNode node) {
		mStack.peek().addChildNode(node);

		sLastNode = node.toString().trim();
	}

	/**
	 * @param uri
	 *            a namespace URI
	 * @return the prefix for the given uri or null if URI is unknown
	 */
	protected String getPrefixForUri(String uri) {
		String prefix = null;
		if (mNamespaceURIs.containsKey(uri)) {
			prefix = mNamespaceURIs.get(uri);
		}

		return prefix;
	}

	/**
	 * @return the root node
	 */
	public XmlNode getRoot() {
		if (mRoot != null) {
			mRoot.reorderDocumentChildren();
		}
		return mRoot;
	}

	private Stack<XmlNode> mStack;
	private XmlNode mRoot;
	private HashMap<String, String> mNamespaceURIs, mNamespacePrefixes;
	private List<String> mNewNamespaces;

	/** the content of the last node being parsed */
	protected static String sLastNode;
}
