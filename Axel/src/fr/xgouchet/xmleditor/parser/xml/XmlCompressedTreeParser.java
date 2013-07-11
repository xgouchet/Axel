package fr.xgouchet.xmleditor.parser.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import fr.xgouchet.axml.Attribute;
import fr.xgouchet.axml.CompressedXmlParser;
import fr.xgouchet.axml.CompressedXmlParserListener;
import fr.xgouchet.xmleditor.data.xml.XmlNode;
import fr.xgouchet.xmleditor.parser.xml.XmlTreeParserException.XmlError;

public class XmlCompressedTreeParser extends XmlTreeParser implements
		CompressedXmlParserListener {

	public static final XmlNode parseXmlTree(File file)
			throws XmlTreeParserException {

		XmlCompressedTreeParser parser = new XmlCompressedTreeParser();

		try {
			new CompressedXmlParser().parse(new FileInputStream(file), parser);
		} catch (IllegalStateException e) {
			throw new XmlTreeParserException(XmlError.parseException, e);
		} catch (IOException e) {
			throw new XmlTreeParserException(XmlError.parseException, e);
		}

		return parser.getRoot();
	}

	/**
	 * @see fr.xgouchet.apkxmllib.CompressedXmlParserListener#startDocument()
	 */
	public void startDocument() {
		createRootDocument();
	}

	/**
	 * @see fr.xgouchet.apkxmllib.CompressedXmlParserListener#endDocument()
	 */
	public void endDocument() {
		XmlNode decl = XmlNode.createDocumentDeclaration("1.0", "UTF-8", false);
		onCreateNode(decl);
	}

	/**
	 * @see fr.xgouchet.apkxmllib.CompressedXmlParserListener#startPrefixMapping(java.lang.String,
	 *      java.lang.String)
	 */
	public void startPrefixMapping(String prefix, String uri) {
		declareNamespace(prefix, uri);
	}

	public void endPrefixMapping(String prefix, String uri) {

	}

	/**
	 * @see fr.xgouchet.apkxmllib.CompressedXmlParserListener#startElement(java.lang.String,
	 *      java.lang.String, java.lang.String,
	 *      fr.xgouchet.apkxmllib.Attribute[])
	 */
	public void startElement(String uri, String localName, String qName,
			Attribute[] atts) {
		String prefix = getPrefixForUri(uri);

		XmlNode tag = XmlNode.createElement(prefix, localName);

		for (Attribute attr : atts) {
			if (attr.getNamespace() == null) {
				prefix = null; // NOPMD
			} else {
				prefix = getPrefixForUri(attr.getNamespace());
			}

			tag.getContent().addAttribute(prefix, attr.getName(),
					attr.getValue());
		}

		onCreateElement(tag);
	}

	/**
	 * @see fr.xgouchet.apkxmllib.CompressedXmlParserListener#endElement(java.lang.String,
	 *      java.lang.String, java.lang.String)
	 */
	public void endElement(String uri, String localName, String qName) {
		onCloseElement();
	}

	/**
	 * @see fr.xgouchet.apkxmllib.CompressedXmlParserListener#characterData(java.lang.String)
	 */
	public void characterData(String data) {
		onCreateNode(XmlNode.createCDataSection(data));
	}

	/**
	 * @see fr.xgouchet.apkxmllib.CompressedXmlParserListener#text(java.lang.String)
	 */
	public void text(String data) {
		onCreateNode(XmlNode.createText(data));
	}

	/**
	 * @see fr.xgouchet.apkxmllib.CompressedXmlParserListener#processingInstruction(java.lang.String,
	 *      java.lang.String)
	 */
	public void processingInstruction(String target, String data) {
		XmlNode pi;

		pi = XmlNode.createProcessingInstruction(target, data);

		onCreateNode(pi);
	}
}
