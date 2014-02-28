package fr.xgouchet.xmleditor.data.xml;

import java.util.LinkedList;
import java.util.List;

import android.text.TextUtils;
import fr.xgouchet.xmleditor.common.AxelUtils;


/**
 * The xml data model
 */
public class XmlData {
    
    /** */
    public static final byte XML_DOCUMENT = 1;
    /** */
    public static final byte XML_DOCTYPE = 2;
    /** */
    public static final byte XML_PROCESSING_INSTRUCTION = 3;
    /** */
    public static final byte XML_ELEMENT = 4;
    /** */
    public static final byte XML_CDATA = 5;
    /** */
    public static final byte XML_TEXT = 6;
    /** */
    public static final byte XML_COMMENT = 7;
    /** */
    public static final byte XML_DOCUMENT_DECLARATION = 8;
    
    /** Names of the different data types */
    protected static final String[] TYPES = new String[] {
            "INVALID",
            "Document", "Doctype", "Processing Instruction", "Element",
            "CData", "Text", "Comment", "Header"
    };
    
    /** Flag for an empty element (eg : &lt;foo /&gt;) */
    public static final byte FLAG_EMPTY = 1;
    
    /**
     * @param dataType
     *            the typde of the xml data
     */
    public XmlData(final byte dataType) {
        
        if ((dataType < XML_DOCUMENT) || (dataType > XML_DOCUMENT_DECLARATION)) {
            throw new IllegalArgumentException();
        }
        
        mDataType = dataType;
        
        if ((mDataType == XML_ELEMENT)
                || (mDataType == XML_DOCUMENT_DECLARATION)) {
            mAttributes = new LinkedList<XmlAttribute>();
            mLocalNamespaces = new LinkedList<XmlAttribute>();
        } else {
            mAttributes = null;
            mLocalNamespaces = null;
        }
    }
    
    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        String result;
        
        switch (mDataType) {
            case XML_DOCUMENT:
                result = "XML DOCUMENT";
                break;
            case XML_TEXT:
                result = AxelUtils.ellipsize(mText, 32);
                break;
            case XML_COMMENT:
                result = "<!-- " + AxelUtils.ellipsize(mText, 23) + " -->";
                break;
            case XML_ELEMENT:
                String open = ((TextUtils.isEmpty(mPrefix)) ? "" : (mPrefix + ":"))
                        + mName;
                
                if (hasFlag(FLAG_EMPTY)) {
                    result = "<" + AxelUtils.ellipsize(open, 29) + "/>";
                } else {
                    result = "<" + AxelUtils.ellipsize(open, 30) + ">";
                }
                break;
            case XML_CDATA:
                result = "<![CDATA[" + AxelUtils.ellipsize(mText, 20) + "]]>";
                break;
            case XML_DOCTYPE:
                result = "<!DOCTYPE " + AxelUtils.ellipsize(mText, 21) + ">";
                break;
            case XML_PROCESSING_INSTRUCTION:
                result = "<?" + AxelUtils.ellipsize(mText, 28) + "?>";
                break;
            case XML_DOCUMENT_DECLARATION:
                result = "<?xml?>";
                break;
            default:
                result = "***";
                break;
        }
        
        return result;
    }
    
    /**
     * Adds an attribute to an Xml Node element. The {@link XmlData} must have
     * its {@link DataType} set to {@link #XML_ELEMENT} or {@link #XML_DOCTYPE}
     * 
     * @param prefix
     *            the namespace prefix of the attribute
     * @param name
     *            the name of the attribute
     * @param value
     *            the value of the attribute
     */
    public void addAttribute(final XmlAttribute attr) {
        if (mAttributes != null) {
            mAttributes.add(attr);
            
            if ("xmlns".equals(attr.getPrefix())) {
                mLocalNamespaces.add(attr);
            }
        }
    }
    
    /**
     * Adds an attribute to an Xml Node element. The {@link XmlData} must have
     * its {@link DataType} set to {@link #XML_ELEMENT} or {@link #XML_DOCTYPE}
     * 
     * @param prefix
     *            the namespace prefix of the attribute
     * @param name
     *            the name of the attribute
     * @param value
     *            the value of the attribute
     */
    public void addAttribute(final String prefix, final String name,
            final String value) {
        if (mAttributes != null) {
            XmlAttribute attr = new XmlAttribute(prefix, name, value);
            mAttributes.add(attr);
            
            if ("xmlns".equals(prefix)) {
                mLocalNamespaces.add(attr);
            }
        }
    }
    
    public void attributeChanged(final XmlAttribute attr) {
        if (mAttributes != null) {
            if ("xmlns".equals(attr.getPrefix())) {
                mLocalNamespaces.add(attr);
            }
        }
    }
    
    /**
     * Sets an attribute to an Xml Node element. The {@link XmlData} must have
     * its {@link DataType} set to {@link #XML_ELEMENT} or {@link #XML_DOCTYPE}
     * 
     * @param name
     *            the name of the attribute
     * @param value
     *            the value of the attribute
     */
    public void setAttribute(final String name, final String value) {
        setAttribute("", name, value);
    }
    
    /**
     * Adds an attribute to an Xml Node element. The {@link XmlData} must have
     * its {@link DataType} set to {@link #XML_ELEMENT} or {@link #XML_DOCTYPE}
     * 
     * @param prefix
     *            the namespace prefix of the attribute
     * @param name
     *            the name of the attribute
     * @param value
     *            the value of the attribute
     */
    public void setAttribute(final String prefix, final String name,
            final String value) {
        String fullName = "";
        if (!TextUtils.isEmpty(prefix)) {
            fullName = prefix + ";";
        }
        fullName += name;
        
        XmlAttribute attr = getAttribute(fullName);
        if (attr != null) {
            attr.setValue(value);
        } else {
            addAttribute(prefix, name, value);
        }
    }
    
    /**
     * @param name
     *            the name attached to this node (for {@link #XML_ELEMENT},
     *            {@link #XML_PROCESSING_INSTRUCTION}, {@link #XML_DOCUMENT_DECLARATION})
     */
    public void setName(final String name) {
        mName = name;
    }
    
    /**
     * @param prefix
     *            the prefix (ie : namespace)
     */
    public void setPrefix(final String prefix) {
        mPrefix = (prefix == null) ? "" : prefix;
    }
    
    /**
     * Change the content text for {@link #XML_COMMENT}, {@link #XML_TEXT}, {@link #XML_CDATA},
     * {@link #XML_PROCESSING_INSTRUCTION}, {@link #XML_DOCTYPE}
     * 
     * @param text
     *            the text to set
     */
    public void setText(final String text) {
        mText = text;
    }
    
    /**
     * @param parent
     *            the data of the parent node
     */
    public void setParent(final XmlData parent) {
        mParent = parent;
    }
    
    /**
     * @param flags
     *            the node flags
     */
    public void setFlags(final byte flags) {
        mFlags = flags;
    }
    
    /**
     * @param add
     *            the flags to add
     * @param remove
     *            the flags to remove
     */
    public void modifyFlags(final byte add, final byte remove) {
        mFlags |= add;
        mFlags &= (0xFF ^ remove);
    }
    
    /**
     * @return the node's tyoe
     */
    public byte getType() {
        return mDataType;
    }
    
    /**
     * @return displayable name of the data type
     */
    public String getTypeName() {
        return TYPES[mDataType];
    }
    
    /**
     * @return the node's name (for {@link #XML_ELEMENT}, {@link #XML_PROCESSING_INSTRUCTION},
     *         {@link #XML_DOCUMENT_DECLARATION})
     */
    public String getName() {
        return mName;
    }
    
    /**
     * @return a short representation of this node
     */
    public String getShortName() {
        String result;
        
        switch (mDataType) {
            case XML_DOCUMENT:
                result = " * ";
                break;
            case XML_TEXT:
                result = AxelUtils.ellipsize(mText, 16);
                break;
            case XML_COMMENT:
                result = "<!-- " + AxelUtils.ellipsize(mText, 8) + " -->";
                break;
            case XML_ELEMENT:
                if (hasFlag(FLAG_EMPTY)) {
                    result = "<" + mName + "/>";
                } else {
                    result = "<" + mName + ">";
                }
                break;
            case XML_CDATA:
                result = "<![CDATA[" + AxelUtils.ellipsize(mText, 8) + "]]>";
                break;
            case XML_DOCTYPE:
                result = "<!DOCTYPE " + AxelUtils.ellipsize(mText, 8) + ">";
                break;
            case XML_PROCESSING_INSTRUCTION:
                result = "<?" + mName + "?>";
                break;
            case XML_DOCUMENT_DECLARATION:
                result = "<?xml?>";
                break;
            default:
                result = "***";
                break;
        }
        
        return result;
    }
    
    /**
     * @return the text content
     */
    public String getText() {
        return mText;
    }
    
    /**
     * @return the node's prefix (ie : namespace)
     */
    public String getPrefix() {
        return mPrefix;
    }
    
    /**
     * @param flag
     *            the flag to test
     * @return if the flag is set
     */
    public boolean hasFlag(final byte flag) {
        return ((mFlags & flag) == flag);
    }
    
    /**
     * @return the node's attribute or null if the node is neither {@link #XML_ELEMENT} nor
     *         {@link #XML_DOCUMENT_DECLARATION}
     */
    public List<XmlAttribute> getAttributes() {
        return mAttributes;
    }
    
    /**
     * @param key
     *            the name of the attribute to get
     * @return the node's attribute or null if the node doesn't have the
     *         attribute declared, or is neither {@link #XML_ELEMENT} nor
     *         {@link #XML_DOCUMENT_DECLARATION}
     */
    public XmlAttribute getAttribute(final String key) {
        XmlAttribute res = null;
        
        if (mAttributes != null) {
            for (XmlAttribute attr : mAttributes) {
                if (attr.getName().equalsIgnoreCase(key)
                        || attr.getFullName().equalsIgnoreCase(key)) {
                    res = attr;
                    break;
                }
            }
        }
        
        return res;
    }
    
    /**
     * @param key
     *            the name of the attribute to get
     * @return the node's attribute value or "" if the node doesn't have the
     *         attribute declared, or is neither {@link #XML_ELEMENT} nor
     *         {@link #XML_DOCUMENT_DECLARATION}
     */
    public String getAttributeValue(final String key) {
        XmlAttribute attr;
        String res = "";
        
        attr = getAttribute(key);
        if (attr != null) {
            res = attr.getValue();
        }
        
        return res;
    }
    
    /**
     * @return the namespace attributes
     */
    public List<XmlAttribute> getNamespaceAttributes() {
        List<XmlAttribute> res = new LinkedList<XmlAttribute>();
        
        if (mLocalNamespaces != null) {
            res.addAll(mLocalNamespaces);
        }
        
        if (mParent != null) {
            res.addAll(mParent.getNamespaceAttributes());
        }
        
        if (getDefaultNamespace() != null) {
            res.add(getDefaultNamespace());
        }
        
        return res;
    }
    
    /**
     * @return the default namespace on this node
     */
    public XmlAttribute getDefaultNamespace() {
        XmlAttribute res = mDefaultNamespace;
        
        if ((mDefaultNamespace == null) && (mParent != null)) {
            res = mParent.getDefaultNamespace();
        }
        
        return res;
    }
    
    /**
     * @return the list of namespaces uri known at this node
     */
    public String[] getNamespaces() {
        List<XmlAttribute> ns = getNamespaceAttributes();
        
        String[] res = new String[ns.size()];
        for (int i = 0; i < res.length; ++i) {
            res[i] = ns.get(i).mValue;
        }
        return res;
    }
    
    public List<String> getNamespacePrefixes() {
        String prefix;
        List<XmlAttribute> ns = getNamespaceAttributes();
        List<String> res = new LinkedList<String>();
        
        int count = ns.size();
        for (int i = 0; i < count; ++i) {
            prefix = ns.get(i).mName;
            if (!res.contains(prefix)) {
                res.add(prefix);
            }
        }
        
        return res;
    }
    
    /**
     * @return if the node is a {@link #XML_ELEMENT}
     */
    public boolean isElement() {
        return (mDataType == XML_ELEMENT);
    }
    
    /**
     * @return if the node is a {@link #XML_TEXT}
     */
    public boolean isText() {
        return (mDataType == XML_TEXT);
    }
    
    /**
     * @return if the node is a {@link #XML_CDATA}
     */
    public boolean isCData() {
        return (mDataType == XML_CDATA);
    }
    
    /**
     * @return if the node is a {@link #XML_COMMENT}
     */
    public boolean isComment() {
        return (mDataType == XML_COMMENT);
    }
    
    /**
     * @return if the node is a {@link #XML_DOCTYPE}
     */
    public boolean isDoctype() {
        return (mDataType == XML_DOCTYPE);
    }
    
    /**
     * @return if the node is a {@link #XML_PROCESSING_INSTRUCTION}
     */
    public boolean isProcessingInstruction() {
        return (mDataType == XML_PROCESSING_INSTRUCTION);
    }
    
    /**
     * @return if the node is a {@link #XML_DOCUMENT_DECLARATION}
     */
    public boolean isDocumentDeclaration() {
        return (mDataType == XML_DOCUMENT_DECLARATION);
    }
    
    /**
     * @return if the node is a {@link #XML_DOCUMENT}
     */
    public boolean isDocument() {
        return (mDataType == XML_DOCUMENT);
    }
    
    /** the node's flags */
    protected byte mFlags;
    
    /** the node's type */
    protected byte mDataType;
    
    /** for {@link #XML_ELEMENT} only, the namespace prefix */
    protected String mPrefix;
    
    /**
     * for {@link #XML_ELEMENT}, {@link #XML_PROCESSING_INSTRUCTION},
     * {@link #XML_DOCUMENT_DECLARATION}
     */
    protected String mName;
    
    /**
     * for {@link #XML_COMMENT}, {@link #XML_TEXT}, {@link #XML_CDATA},
     * {@link #XML_PROCESSING_INSTRUCTION}
     */
    protected String mText;
    
    /**
     * for {@link #XML_ELEMENT} to find parent namespaces
     */
    protected XmlData mParent;
    
    /**
     * for {@link #XML_ELEMENT}, {@link #XML_DOCUMENT_DECLARATION}
     */
    protected final List<XmlAttribute> mAttributes;
    
    /** for {@link #XML_ELEMENT} */
    protected XmlAttribute mDefaultNamespace;
    /** for {@link #XML_ELEMENT} */
    protected final List<XmlAttribute> mLocalNamespaces;
    
}
