package fr.xgouchet.xmleditor.data.xml;

import java.util.List;

import android.text.TextUtils;
import fr.xgouchet.xmleditor.common.Settings;
import fr.xgouchet.xmleditor.data.tree.TreeNode;

/**
 * A node containing {@link XmlData}
 */
public class XmlNode extends TreeNode<XmlData> {

	/** XML Schema Instance Namespace URI */
	public static final String XSI_URI = "http://www.w3.org/2001/XMLSchema-instance";

	/** The xml document property : Version */
	public static final String PROPERTY_XML_VERSION = "http://xmlpull.org/v1/doc/properties.html#xmldecl-version";
	/** The xml document property : Standalone */
	public static final String PROPERTY_XML_STANDALONE = "http://xmlpull.org/v1/doc/properties.html#xmldecl-standalone";

	/**
	 * @param data
	 *            any non-null {@link XmlData}
	 */
	private XmlNode(XmlData data) {
		super(null, data);
		if (data == null) {
			throw new IllegalArgumentException(new NullPointerException());
		}
	}

	/**
	 * Called when the content of this node changes
	 */
	public void onContentChanged() {
		for (TreeNode<XmlData> child : mChildren) {
			child.onParentChanged();
		}
	}

	/**
	 * @see fr.xgouchet.xmleditor.data.tree.TreeNode#onParentChanged()
	 */
	public void onParentChanged() {
		super.onParentChanged();
		if (mParent != null) {
			mContent.readParentData(mParent.getContent());
			onContentChanged();
		}
	}

	/**
	 * @see fr.xgouchet.xmleditor.data.tree.TreeNode#onChildListChanged()
	 */
	public void onChildListChanged() {
		if (mContent.isElement()) {
			if (mContent.hasFlag(XmlData.FLAG_EMPTY)) {
				if (mChildren.size() > 0) {
					mContent.setFlags((byte) 0);
				}
			}
		} else if (mContent.isDocument()) {
			int elements = 0;
			for (TreeNode<XmlData> child : mChildren) {
				if (child.getContent().isElement()) {
					elements++;
				}
			}
			if (elements > 1) {
				throw new IllegalStateException(
						"An XML Document can only have one element at its root");
			}
		}
	}

	/**
	 * @return an empty XML Document
	 */
	public static XmlNode createDocument() {
		return new XmlNode(new XmlData(XmlData.XML_DOCUMENT));
	}

	/**
	 * @param version
	 *            the version of the xml document
	 * @param encoding
	 *            the encoding charset
	 * @param standalone
	 *            is the file standalone
	 * @return an XML Document Declaration node
	 */
	public static XmlNode createDocumentDeclaration(String version,
			String encoding, Boolean standalone) {
		XmlNode node = new XmlNode(
				new XmlData(XmlData.XML_DOCUMENT_DECLARATION));

		node.mContent.setName("xml");

		if (version == null) {
			version = "1.0";
		}
		node.mContent.addAttribute(null, "version", version);

		if (encoding != null) {
			node.mContent.addAttribute(null, "encoding", encoding);
		}

		if (standalone != null) {
			String value = (standalone.booleanValue() ? "yes" : "no");
			node.mContent.addAttribute(null, "standalone", value);
		}

		node.forceLeaf();

		return node;
	}

	/**
	 * @param text
	 *            the text of the doctype
	 * @return an XML Doctype declaration node
	 */
	public static XmlNode createDoctypeDeclaration(String text) {
		XmlNode node = new XmlNode(new XmlData(XmlData.XML_DOCTYPE));

		node.mContent.setText(text);

		node.forceLeaf();

		return node;
	}

	/**
	 * @param target
	 *            the processing target name
	 * @param text
	 *            the processing information
	 * @return an XML Document processing node
	 */
	public static XmlNode createProcessingInstruction(String target, String text) {
		XmlNode node = new XmlNode(new XmlData(
				XmlData.XML_PROCESSING_INSTRUCTION));

		node.mContent.setName(target);
		node.mContent.setText(text);

		node.forceLeaf();

		return node;
	}

	/**
	 * @param name
	 *            the name of the element (eg : "foo" for a
	 *            &lt;foo&gt;&lt;/foo&gt; element)
	 * @return an xml element node
	 */
	public static XmlNode createElement(String name) {
		XmlNode node = new XmlNode(new XmlData(XmlData.XML_ELEMENT));

		node.mContent.setName(name);
		node.mContent.setFlags(XmlData.FLAG_EMPTY);

		return node;
	}

	/**
	 * @param prefix
	 *            the namespace prefix for this element
	 * @param name
	 *            the name of the element (eg : "foo" for a
	 *            &lt;foo&gt;&lt;/foo&gt; element)
	 * @return an xml element node
	 */
	public static XmlNode createElement(String prefix, String name) {
		XmlNode node = new XmlNode(new XmlData(XmlData.XML_ELEMENT));

		node.mContent.setPrefix(prefix);
		node.mContent.setName(name);
		node.mContent.setFlags(XmlData.FLAG_EMPTY);

		return node;
	}

	/**
	 * @param comment
	 *            the comment text content
	 * @return an xml comment node
	 */
	public static XmlNode createComment(String comment) {
		XmlNode node = new XmlNode(new XmlData(XmlData.XML_COMMENT));

		node.mContent.setText(comment);
		node.forceLeaf();

		return node;
	}

	/**
	 * @param text
	 *            the text content
	 * @return an xml Text node
	 */
	public static XmlNode createText(String text) {
		XmlNode node = new XmlNode(new XmlData(XmlData.XML_TEXT));

		if (Settings.sKeepTextExact) {
			node.mContent.setText(text);
		} else {
			node.mContent.setText(text.trim());
		}
		node.forceLeaf();

		return node;
	}

	/**
	 * @param text
	 *            the content of the CData section
	 * @return an xml CData node
	 */
	public static XmlNode createCDataSection(String text) {
		XmlNode node = new XmlNode(new XmlData(XmlData.XML_CDATA));

		node.mContent.setText(text);
		node.forceLeaf();

		return node;
	}

	/**
	 * @see fr.xgouchet.xmleditor.data.tree.TreeNode#toString()
	 */
	public String toString() {
		return mContent.toString();
	}

	/**
	 * @param builder
	 *            the {@link StringBuilder} to use for constructing the XML code
	 */
	public void buildXmlString(StringBuilder builder) {

		if (!(mContent.isText() && Settings.sKeepTextExact)) {
			buildXmlStringEndentation(builder);
		}

		switch (mContent.getType()) {
		case XmlData.XML_DOCUMENT:
			buildChildrenXmlString(builder);
			break;
		case XmlData.XML_DOCUMENT_DECLARATION:
			buildXmlDeclarationString(builder);
			break;
		case XmlData.XML_DOCTYPE:
			builder.append("<!DOCTYPE ");
			builder.append(mContent.getText());
			builder.append(">\n");
			break;
		case XmlData.XML_PROCESSING_INSTRUCTION:
			builder.append("<?");
			builder.append(mContent.getName());
			builder.append(" ");
			builder.append(mContent.getText());
			builder.append("?>\n");
			break;
		case XmlData.XML_ELEMENT:
			buildXmlElementString(builder);
			break;
		case XmlData.XML_TEXT:
			builder.append(mContent.getText());
			if (!Settings.sKeepTextExact) {
				builder.append("\n");
			}
			break;
		case XmlData.XML_CDATA:
			builder.append("<![CDATA[");
			builder.append(mContent.getText());
			builder.append("]]>\n");
			break;
		case XmlData.XML_COMMENT:
			builder.append("<!--");
			builder.append(mContent.getText());
			builder.append("-->\n");
			break;
		default:
			throw new IllegalArgumentException("Data type not supported");
		}
	}

	/**
	 * @param builder
	 *            the {@link StringBuilder} to use
	 */
	protected void buildXmlDeclarationString(StringBuilder builder) {
		builder.append("<?");
		builder.append("xml");

		List<XmlAttribute> attrs = mContent.getAttributes();
		for (XmlAttribute attr : attrs) {
			builder.append(" ");
			if (!TextUtils.isEmpty(attr.getPrefix())) {
				builder.append(attr.getPrefix());
				builder.append(":");
			}
			builder.append(attr.getName());
			builder.append("=\"");
			builder.append(attr.getValue());
			builder.append("\"");
		}

		builder.append("?>\n");
	}

	/**
	 * @param builder
	 *            the {@link StringBuilder} to use
	 */
	protected void buildXmlElementString(StringBuilder builder) {
		builder.append("<");

		String name = "";
		if (!TextUtils.isEmpty(mContent.getPrefix())) {
			name = mContent.getPrefix() + ":";
		}
		name = name + mContent.getName();
		builder.append(name);

		List<XmlAttribute> attrs = mContent.getAttributes();
		for (XmlAttribute attr : attrs) {
			builder.append(" ");
			if (!TextUtils.isEmpty(attr.getPrefix())) {
				builder.append(attr.getPrefix());
				builder.append(":");
			}
			builder.append(attr.getName());
			builder.append("=\"");
			builder.append(attr.getValue());
			builder.append("\"");
		}

		if (mContent.hasFlag(XmlData.FLAG_EMPTY) || !hasChildren()) {
			builder.append("/>\n");
		} else {
			builder.append(">");

			if (!(Settings.sKeepTextExact && hasTextOnly())) {
				builder.append("\n");
			}

			buildChildrenXmlString(builder);

			if (!(Settings.sKeepTextExact && hasTextOnly())) {
				buildXmlStringEndentation(builder);
			}
			builder.append("</");
			builder.append(name);
			builder.append(">\n");
		}
	}

	/**
	 * @param builder
	 *            the {@link StringBuilder} to use
	 */
	protected void buildXmlStringEndentation(StringBuilder builder) {
		for (int i = 0; i < mDepth - 1; ++i) {
			builder.append("  ");
		}
	}

	/**
	 * @param builder
	 *            the {@link StringBuilder} to use
	 */
	protected void buildChildrenXmlString(StringBuilder builder) {
		for (TreeNode<XmlData> child : mChildren) {
			((XmlNode) child).buildXmlString(builder);
		}
	}

	/**
	 * @return if the element only has {@link XmlData#XML_TEXT} elements. If the
	 *         type of this node is not {@link XmlData#XML_ELEMENT}, false is
	 *         returned
	 */
	public boolean hasTextOnly() {
		boolean result = false;
		if (mContent.isElement()) {
			result = true;
			for (TreeNode<XmlData> child : mChildren) {
				if (!child.getContent().isText()) {
					result = false;
					break;
				}
			}
		}
		return result;
	}

	/**
	 * @return if the document already has a Doctype child node or not. If the
	 *         type of this node is not {@link XmlData#XML_DOCUMENT}, false is
	 *         returned
	 */
	public boolean hasDoctype() {
		boolean result = false;

		if (mContent.isDocument()) {
			for (TreeNode<XmlData> child : mChildren) {
				if (child.getContent().isDoctype()) {
					result = true;
					break;
				}
			}
		}

		return result;
	}

	/**
	 * @return if the document already has a Doctype child node the full doctype
	 *         content (including <!DOCTYPE and >. If the type of this node is
	 *         not {@link XmlData#XML_DOCUMENT}, or it doesn't have doctype,
	 *         null is returned
	 */
	public String getDoctype() {
		String result = null;

		if (mContent.isDocument()) {
			for (TreeNode<XmlData> child : mChildren) {
				if (child.getContent().isDoctype()) {
					StringBuilder builder = new StringBuilder();
					((XmlNode) child).buildXmlString(builder);
					result = builder.toString();
					break;
				}
			}
		}

		return result;
	}

	/**
	 * @return the schema used by this document
	 */
	public String[] getSchemaDeclarations() {
		String result[] = null;

		XmlNode root = getRootChild();

		if (root != null) {
			String xsins = root.getContent().getAttributeValue("xmlns:xsi");
			boolean xsiPresent = XSI_URI.equals(xsins);

			if (xsiPresent) {
				String location = root.getContent().getAttributeValue(
						"xsi:schemaLocation");
				if (location != null) {
					String locs[] = location.split(" ");
					result = new String[locs.length / 2];
					for (int i = 0; i < locs.length; i += 2) {
						result[i / 2] = locs[i];
					}
				}
			} else {
				result = root.getContent().getNamespaces();
			}
		}

		return result;
	}

	/**
	 * @return if the document already has an Element child node or not. If the
	 *         type of this node is not {@link XmlData#XML_DOCUMENT}, false is
	 *         returned
	 */
	public boolean hasRootChild() {
		boolean result = false;

		if (mContent.isDocument()) {
			for (TreeNode<XmlData> child : mChildren) {
				if (child.getContent().isElement()) {
					result = true;
					break;
				}
			}
		}

		return result;
	}

	/**
	 * @return the root element if the document already has an Element child
	 *         node. If the type of this node is not
	 *         {@link XmlData#XML_DOCUMENT}, null is returned
	 */
	public XmlNode getRootChild() {
		XmlNode result = null;

		if (mContent.isDocument()) {
			for (TreeNode<XmlData> child : mChildren) {
				if (child.getContent().isElement()) {
					result = (XmlNode) child;
					break;
				}
			}
		}

		return result;
	}

	/**
	 * Reorder automatically children
	 */
	public void reorderDocumentChildren() {

		int doctype, docdecl, element;
		int count = mChildren.size();
		TreeNode<XmlData> child;

		doctype = docdecl = element = 0;
		for (int i = 0; i < count; ++i) {
			child = mChildren.get(i);
			if (child.getContent().isDocumentDeclaration()) {
				docdecl = i;
			} else if (child.getContent().isDoctype()) {
				doctype = i;
			} else if (child.getContent().isElement()) {
				element = i;
			}
		}

		if (docdecl != 0) {
			child = mChildren.get(docdecl);
			mChildren.remove(child);
			mChildren.add(0, child);

			if (doctype < docdecl) {
				doctype++;
			}
			if (element < docdecl) {
				element++;
			}
		}

		if (doctype > element) {
			child = mChildren.get(doctype);
			mChildren.remove(child);
			mChildren.add(1, child);
		}

	}

	/**
	 * @param node
	 *            a node to add as child to this one
	 * @return if this node can accept such a node
	 */
	public boolean canHasChild(XmlNode node) {
		boolean result = false;
		if (mContent.isElement()) {
			result = !node.getContent().isDoctype();
		} else if (mContent.isDocument()) {
			result = node.getContent().isDoctype();
			result |= node.getContent().isComment();
			result |= node.getContent().isProcessingInstruction();
			result |= (node.getContent().isElement() && !node.hasRootChild());
		}

		return result;
	}

}
