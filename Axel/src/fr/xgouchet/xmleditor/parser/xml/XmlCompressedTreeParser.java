package fr.xgouchet.xmleditor.parser.xml;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import fr.xgouchet.axml.Attribute;
import fr.xgouchet.axml.CompressedXmlParser;
import fr.xgouchet.axml.CompressedXmlParserListener;
import fr.xgouchet.xmleditor.data.xml.XmlNode;


public class XmlCompressedTreeParser extends XmlTreeParser implements
        CompressedXmlParserListener {
    
    public static final XmlNode parseXmlTree(final InputStream input)
            throws FileNotFoundException, IOException {
        
        XmlCompressedTreeParser parser = new XmlCompressedTreeParser();
        
        new CompressedXmlParser().parse(input, parser);
        
        return parser.getRoot();
    }
    
    /**
     * @see fr.xgouchet.apkxmllib.CompressedXmlParserListener#startDocument()
     */
    @Override
    public void startDocument() {
        createRootDocument();
    }
    
    /**
     * @see fr.xgouchet.apkxmllib.CompressedXmlParserListener#endDocument()
     */
    @Override
    public void endDocument() {
        XmlNode decl = XmlNode.createDocumentDeclaration("1.0", "UTF-8", false);
        onCreateNode(decl);
    }
    
    /**
     * @see fr.xgouchet.apkxmllib.CompressedXmlParserListener#startPrefixMapping(java.lang.String,
     *      java.lang.String)
     */
    @Override
    public void startPrefixMapping(final String prefix, final String uri) {
        declareNamespace(prefix, uri);
    }
    
    @Override
    public void endPrefixMapping(final String prefix, final String uri) {
        
    }
    
    /**
     * @see fr.xgouchet.apkxmllib.CompressedXmlParserListener#startElement(java.lang.String,
     *      java.lang.String, java.lang.String, fr.xgouchet.apkxmllib.Attribute[])
     */
    @Override
    public void startElement(final String uri, final String localName,
            final String qName, final Attribute[] atts) {
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
    @Override
    public void endElement(final String uri, final String localName,
            final String qName) {
        onCloseElement();
    }
    
    /**
     * @see fr.xgouchet.apkxmllib.CompressedXmlParserListener#characterData(java.lang.String)
     */
    @Override
    public void characterData(final String data) {
        onCreateNode(XmlNode.createCDataSection(data));
    }
    
    /**
     * @see fr.xgouchet.apkxmllib.CompressedXmlParserListener#text(java.lang.String)
     */
    @Override
    public void text(final String data) {
        onCreateNode(XmlNode.createText(data));
    }
    
    /**
     * @see fr.xgouchet.apkxmllib.CompressedXmlParserListener#processingInstruction(java.lang.String,
     *      java.lang.String)
     */
    @Override
    public void processingInstruction(final String target, final String data) {
        XmlNode pi;
        
        pi = XmlNode.createProcessingInstruction(target, data);
        
        onCreateNode(pi);
    }
}
