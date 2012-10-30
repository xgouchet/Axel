package fr.xgouchet.xmleditor.data.xml;

import java.io.IOException;
import java.io.Reader;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import fr.xgouchet.xmleditor.data.xml.XmlTreeParserException.XmlError;

/**
 * 
 */
public class XmlTreePullParser extends XmlTreeParser {

	/** The xml document property : Version */
	public static final String PROPERTY_XML_VERSION = "http://xmlpull.org/v1/doc/properties.html#xmldecl-version";
	/** The xml document property : Standalone */
	public static final String PROPERTY_XML_STANDALONE = "http://xmlpull.org/v1/doc/properties.html#xmldecl-standalone";

	/**
	 * @param input
	 *            the input character stream
	 * @param createDocDecl
	 *            create the document declaration ?
	 * @return an XML Node from the parser
	 * @throws XmlTreeParserException
	 *             if an error occurs during the parsing
	 */
	public static XmlNode parseXmlTree(Reader input, boolean createDocDecl)
			throws XmlTreeParserException {

		XmlPullParserFactory factory;
		XmlPullParser xpp;
		XmlTreePullParser parser;

		parser = new XmlTreePullParser();

		try {
			factory = XmlPullParserFactory.newInstance();
		} catch (XmlPullParserException e) {
			throw new XmlTreeParserException(XmlError.noParser, e);
		}

		try {
			factory.setNamespaceAware(true);
			factory.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
			xpp = factory.newPullParser();
		} catch (XmlPullParserException e) {
			throw new XmlTreeParserException(XmlError.featureUnavailable, e);
		}

		try {
			xpp.setInput(input);
			parser.parse(xpp);
			if (createDocDecl) {
				parser.pullDocumentDeclaration(xpp);
			}
		} catch (XmlPullParserException e) {
			XmlTreeParserException xtpe = new XmlTreeParserException(
					XmlError.parseException, e);
			xtpe.setXmlContext(sLastNode);
			throw xtpe;
		} catch (StringIndexOutOfBoundsException e) {
			XmlTreeParserException xtpe = new XmlTreeParserException(
					XmlError.parseException, e);
			xtpe.setXmlContext(sLastNode);
			throw xtpe;
		} catch (IOException e) {
			throw new XmlTreeParserException(XmlError.ioException, e);
		}

		return parser.getRoot();
	}

	/**
	 * @param xpp
	 *            the {@link XmlPullParser} to use
	 * @throws IOException
	 * @throws XmlPullParserException
	 * @throws StringIndexOutOfBoundsException
	 */
	protected void parse(XmlPullParser xpp) throws XmlPullParserException,
			IOException, StringIndexOutOfBoundsException {

		int eventType = xpp.getEventType();

		while (eventType != XmlPullParser.END_DOCUMENT) {
			switch (eventType) {
			case XmlPullParser.START_DOCUMENT:
				createRootDocument();
				break;
			case XmlPullParser.END_DOCUMENT:
				break;
			case XmlPullParser.START_TAG:
				pullElementNode(xpp);
				break;
			case XmlPullParser.END_TAG:
				onCloseElement();
				break;
			case XmlPullParser.TEXT:
				pullTextNode(xpp);
				break;
			case XmlPullParser.CDSECT:
				pullCDataNode(xpp);
				break;
			case XmlPullParser.ENTITY_REF:
				break;
			case XmlPullParser.IGNORABLE_WHITESPACE:
				break;
			case XmlPullParser.PROCESSING_INSTRUCTION:
				pullProcessingInstructionNode(xpp);
				break;
			case XmlPullParser.COMMENT:
				pullCommentNode(xpp);
				break;
			case XmlPullParser.DOCDECL:
				pullDoctypeNode(xpp);
				break;
			default:
				break;
			}
			eventType = xpp.nextToken();
		}

	}

	/**
	 * @param xpp
	 *            the parser
	 * @throws XmlPullParserException
	 */
	protected void pullElementNode(XmlPullParser xpp)
			throws XmlPullParserException {
		String name, prefix, uri;

		name = xpp.getName();
		prefix = xpp.getPrefix();
		uri = xpp.getNamespace(prefix);
		if ((uri != null) && (prefix != null)) {
			declareNamespace(prefix, uri);
		}

		XmlNode tag = XmlNode.createElement(prefix, name);
		if (!xpp.isEmptyElementTag()) {
			tag.getContent().modifyFlags((byte) 0, XmlData.FLAG_EMPTY);
		}

		String attUri, attName, attValue, attPrefix;
		int count = xpp.getAttributeCount();
		for (int i = 0; i < count; ++i) {
			attUri = xpp.getAttributeNamespace(i);
			attPrefix = xpp.getAttributePrefix(i);
			attName = xpp.getAttributeName(i);
			attValue = xpp.getAttributeValue(i);

			if ((attUri != null) && (attPrefix != null)) {
				declareNamespace(attPrefix, attUri);
			}

			tag.getContent().addAttribute(attPrefix, attName, attValue);
		}

		onCreateElement(tag);
	}

	/**
	 * @param xpp
	 *            the parser
	 * @throws XmlPullParserException
	 */
	protected void pullTextNode(XmlPullParser xpp)
			throws XmlPullParserException {
		if (!xpp.isWhitespace()) {
			onCreateNode(XmlNode.createText(xpp.getText()));
		}
	}

	/**
	 * @param xpp
	 *            the parser
	 * @throws XmlPullParserException
	 */
	protected void pullCDataNode(XmlPullParser xpp)
			throws XmlPullParserException {
		onCreateNode(XmlNode.createCDataSection(xpp.getText()));
	}

	/**
	 * @param xpp
	 *            the parser
	 * @throws XmlPullParserException
	 */
	protected void pullCommentNode(XmlPullParser xpp)
			throws XmlPullParserException {
		onCreateNode(XmlNode.createComment(xpp.getText()));
	}

	/**
	 * @param xpp
	 *            the parser
	 * @throws XmlPullParserException
	 */
	protected void pullDoctypeNode(XmlPullParser xpp)
			throws XmlPullParserException {
		onCreateNode(XmlNode.createDoctypeDeclaration(xpp.getText()));
	}

	/**
	 * @param xpp
	 *            the parser
	 * @throws XmlPullParserException
	 */
	protected void pullProcessingInstructionNode(XmlPullParser xpp)
			throws XmlPullParserException {

		String text = xpp.getText().trim();
		String[] data = text.split("\\s+", 2);

		XmlNode pi;

		if (data.length == 2) {
			pi = XmlNode.createProcessingInstruction(data[0], data[1]);
		} else {
			pi = XmlNode.createProcessingInstruction(text, "");
		}

		onCreateNode(pi);
	}

	/**
	 * @param xpp
	 *            the parser
	 */
	protected void pullDocumentDeclaration(XmlPullParser xpp) {
		String version, enc;
		Boolean standalone;
		XmlNode decl;

		version = (String) xpp.getProperty(PROPERTY_XML_VERSION);
		if (version == null) {
			version = "1.0";
		}

		enc = xpp.getInputEncoding();

		standalone = (Boolean) xpp.getProperty(PROPERTY_XML_STANDALONE);

		decl = XmlNode.createDocumentDeclaration(version, enc, standalone);

		onCreateNode(decl);
	}

}
