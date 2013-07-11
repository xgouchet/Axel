package fr.xgouchet.xmleditor.parser.xml;

import java.io.IOException;
import java.io.Reader;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import android.util.Log;
import fr.xgouchet.xmleditor.data.xml.XmlNode;
import fr.xgouchet.xmleditor.parser.xml.XmlTreeParserException.XmlError;

/**
 * 
 */
public class XmlTreeSaxParser extends XmlTreeParser implements ContentHandler {

	/** Process namespaces */
	public static final String FEATURE_NAMESPACES = "http://xml.org/sax/features/namespaces";

	/** Validate document if a grammar is specified */
	public static final String FEATURE_VALIDATE_DYNAMIC = "http://apache.org/xml/features/validation/dynamic";

	/** validate id reference */
	public static final String FEATURE_VALIDATE_REFERENCE = "http://apache.org/xml/features/validation/id-idref-checking";

	/** validate URI syntax */
	public static final String FEATURE_VALIDATE_URI = "http://apache.org/xml/features/standard-uri-conformant";

	/** continue parsing after Fatal Error */
	public static final String FEATURE_IGNORE_ERROR = "http://apache.org/xml/features/continue-after-fatal-error";

	/** report namespace prefix and attributes */
	public static final String FEATURE_REPORT_NAMESPACE = "http://xml.org/sax/features/namespace-prefixes";

	/**
	 * @param input
	 *            the input character stream
	 * @return an XML Node from the parser
	 * @throws XmlTreeParserException
	 *             if an error occurs during the parsing
	 */
	public static XmlNode parseXmlTree(Reader input)
			throws XmlTreeParserException {
		XmlTreeSaxParser parser = new XmlTreeSaxParser();

		XMLReader reader;

		// set the driver
		System.setProperty("org.xml.sax.driver", "org.xmlpull.v1.sax2.Driver");

		try {
			reader = XMLReaderFactory.createXMLReader();
		} catch (SAXException e) {
			throw new XmlTreeParserException(XmlError.noParser, e);
		}

		try {
			reader.setContentHandler(parser);

			reader.setFeature(FEATURE_NAMESPACES, true);
			reader.setFeature(FEATURE_REPORT_NAMESPACE, true);
			reader.setFeature(FEATURE_VALIDATE_URI, true);
			reader.setFeature(FEATURE_IGNORE_ERROR, true);

		} catch (SAXNotSupportedException e) {
			throw new XmlTreeParserException(XmlError.featureUnavailable, e);
		} catch (SAXNotRecognizedException e) {
			throw new XmlTreeParserException(XmlError.featureUnavailable, e);
		}

		try {
			reader.parse(new InputSource(input));
		} catch (IOException e) {
			throw new XmlTreeParserException(XmlError.ioException, e);
		} catch (SAXException e) {
			throw new XmlTreeParserException(XmlError.parseException, e);
		}

		return parser.getRoot();
	}

	/**
	 * @see org.xml.sax.ContentHandler#setDocumentLocator(org.xml.sax.Locator)
	 */
	public void setDocumentLocator(Locator locator) {
	}

	/**
	 * @see org.xml.sax.ContentHandler#startDocument()
	 */
	public void startDocument() throws SAXException {
		createRootDocument();
	}

	/**
	 * @see org.xml.sax.ContentHandler#endDocument()
	 */
	public void endDocument() throws SAXException {
	}

	/**
	 * @see org.xml.sax.ContentHandler#startPrefixMapping(java.lang.String,
	 *      java.lang.String)
	 */
	public void startPrefixMapping(String prefix, String uri)
			throws SAXException {
		declareNamespace(prefix, uri);
	}

	/**
	 * @see org.xml.sax.ContentHandler#endPrefixMapping(java.lang.String)
	 */
	public void endPrefixMapping(String prefix) throws SAXException {

	}

	/**
	 * @see org.xml.sax.ContentHandler#startElement(java.lang.String,
	 *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	public void startElement(String uri, String localName, String qName,
			Attributes atts) throws SAXException {
		String prefix;

		// Handle Tag name
		prefix = getPrefixForUri(uri);
		XmlNode tag = XmlNode.createElement(prefix, localName);

		// Handle attributes
		String attUri, attName, attValue, attPrefix;
		int count = atts.getLength();
		for (int i = 0; i < count; ++i) {
			attUri = atts.getURI(i);
			attPrefix = getPrefixForUri(attUri);

			attName = atts.getLocalName(i);
			attValue = atts.getValue(i);

			tag.getContent().addAttribute(attPrefix, attName, attValue);
		}

		onCreateElement(tag);
	}

	/**
	 * @see org.xml.sax.ContentHandler#endElement(java.lang.String,
	 *      java.lang.String, java.lang.String)
	 */
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		onCloseElement();
	}

	/**
	 * @see org.xml.sax.ContentHandler#characters(char[], int, int)
	 */
	public void characters(char[] ch, int start, int length)
			throws SAXException {

	}

	/**
	 * @see org.xml.sax.ContentHandler#ignorableWhitespace(char[], int, int)
	 */
	public void ignorableWhitespace(char[] ch, int start, int length)
			throws SAXException {
	}

	/**
	 * @see org.xml.sax.ContentHandler#processingInstruction(java.lang.String,
	 *      java.lang.String)
	 */
	public void processingInstruction(String target, String data)
			throws SAXException {
		// XmlNode pi = XmlNode.createProcessingInstruction(target, data);

	}

	/**
	 * @see org.xml.sax.ContentHandler#skippedEntity(java.lang.String)
	 */
	public void skippedEntity(String name) throws SAXException {
		Log.i("IGNORED", name);
	}

}
