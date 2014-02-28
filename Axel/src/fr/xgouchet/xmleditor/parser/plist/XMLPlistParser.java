package fr.xgouchet.xmleditor.parser.plist;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.NotSerializableException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map.Entry;

import android.util.Log;
import fr.xgouchet.plist.PlistParser;
import fr.xgouchet.plist.data.PArray;
import fr.xgouchet.plist.data.PBoolean;
import fr.xgouchet.plist.data.PDate;
import fr.xgouchet.plist.data.PDict;
import fr.xgouchet.plist.data.PInt;
import fr.xgouchet.plist.data.PObject;
import fr.xgouchet.plist.data.PReal;
import fr.xgouchet.plist.data.PString;
import fr.xgouchet.xmleditor.data.xml.XmlNode;
import fr.xgouchet.xmleditor.parser.xml.XmlTreeParser;


public class XMLPlistParser extends XmlTreeParser {
    
    private static final String ISO8601_PATTERN = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    private static final DateFormat ISO8601 = new SimpleDateFormat(
            ISO8601_PATTERN, Locale.US);
    
    public static final XmlNode parseXmlTree(final InputStream input)
            throws NotSerializableException, EOFException,
            FileNotFoundException, IOException {
        
        XMLPlistParser parser = new XMLPlistParser();
        PObject root;
        
        root = new PlistParser().parse(input);
        
        parser.createRootDocument();
        parser.createDocDecl();
        parser.createRootNode();
        parser.convertPObject(root);
        
        return parser.getRoot();
    }
    
    /**
	 * 
	 */
    private void createDocDecl() {
        onCreateNode(XmlNode.createDocumentDeclaration("1.0", "UTF-8", false));
        onCreateNode(XmlNode
                .createDoctypeDeclaration("plist PUBLIC \"-//Apple//DTD PLIST 1.0//EN\" \"http://www.apple.com/DTDs/PropertyList-1.0.dtd\""));
    }
    
    /**
	 * 
	 */
    private void createRootNode() {
        XmlNode root = XmlNode.createElement("plist");
        root.getContent().addAttribute(null, "version", "1.0");
        
        onCreateElement(root);
    }
    
    /**
	 * 
	 */
    private void convertPObject(final PObject object) {
        PObject.Type type = object.getType();
        
        switch (type) {
            case BOOL:
                convertPBoolean((PBoolean) object);
                break;
            case INT:
                convertPInt((PInt) object);
                break;
            case REAL:
                convertPReal((PReal) object);
                break;
            case STRING:
                convertPString((PString) object);
                break;
            case ARRAY:
                convertPArray((PArray) object);
                break;
            case DICT:
                convertPDict((PDict) object);
                break;
            case DATE:
                convertPDate((PDate) object);
                break;
            default:
                Log.w("Axel", "Missing PList item " + type.name());
                break;
        }
    }
    
    /**
     * @param bool
     */
    private void convertPBoolean(final PBoolean bool) {
        if (bool.isTrue()) {
            onCreateElement(XmlNode.createElement("true"));
        } else {
            onCreateElement(XmlNode.createElement("false"));
        }
        onCloseElement();
    }
    
    /**
     * 
     * @param integer
     */
    private void convertPInt(final PInt integer) {
        onCreateElement(XmlNode.createElement("integer"));
        onCreateNode(XmlNode.createText(String.valueOf(integer.getValue())));
        onCloseElement();
    }
    
    /**
     * 
     * @param real
     */
    private void convertPReal(final PReal real) {
        onCreateElement(XmlNode.createElement("real"));
        onCreateNode(XmlNode.createText(String.valueOf(real.getValue())));
        onCloseElement();
    }
    
    /**
     * 
     * @param string
     */
    private void convertPString(final PString string) {
        onCreateElement(XmlNode.createElement("string"));
        onCreateNode(XmlNode.createText(string.getValue()));
        onCloseElement();
    }
    
    /**
     * @param array
     */
    private void convertPArray(final PArray array) {
        
        onCreateElement(XmlNode.createElement("array"));
        
        for (PObject child : array) {
            convertPObject(child);
        }
        
        onCloseElement();
    }
    
    private void convertPDict(final PDict dict) {
        onCreateElement(XmlNode.createElement("dict"));
        
        for (Entry<String, PObject> entry : dict) {
            onCreateElement(XmlNode.createElement("key"));
            onCreateNode(XmlNode.createText(entry.getKey()));
            onCloseElement();
            
            convertPObject(entry.getValue());
        }
        
        onCloseElement();
    }
    
    private void convertPDate(final PDate date) {
        onCreateElement(XmlNode.createElement("date"));
        
        // Log.i("Axel", "Converting date " + date.getValue());
        
        String value = ISO8601.format(new Date(date.getValue()));
        onCreateElement(XmlNode.createText(value));
        
        onCloseElement();
    }
}
