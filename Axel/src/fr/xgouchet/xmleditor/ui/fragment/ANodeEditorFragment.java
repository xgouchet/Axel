package fr.xgouchet.xmleditor.ui.fragment;

import android.app.Fragment;
import fr.xgouchet.xmleditor.data.XmlEditor;
import fr.xgouchet.xmleditor.data.xml.XmlNode;


/**
 * Base class for a fragment editing an XML Node
 * 
 * @author Xavier Gouchet
 * 
 */
public class ANodeEditorFragment extends Fragment {
    
    
    protected XmlNode mXmlNode;
    protected XmlEditor mXmlEditor;
    
    
    //////////////////////////////////////////////////////////////////////////////////////
    // GETTERS / SETTERS
    //////////////////////////////////////////////////////////////////////////////////////
    
    
    public void setXmlEditor(XmlEditor xmlEditor) {
        mXmlEditor = xmlEditor;
    }
    
    public void setXmlNode(final XmlNode node) {
        mXmlNode = node;
    }
    
    
    public XmlEditor getXmlEditor() {
        return mXmlEditor;
    }
    
    public XmlNode getXmlNode() {
        return mXmlNode;
    }
}
