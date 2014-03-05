package fr.xgouchet.xmleditor.ui.fragment;

import android.app.Fragment;
import fr.xgouchet.xmleditor.data.xml.XmlNode;


/**
 * Base class for a fragment editing an XML Node
 * 
 * @author Xavier Gouchet
 * 
 */
public class ANodeEditorFragment extends Fragment {
    
    
    protected XmlNode mNode;
    
    
    public void setXmlNode(final XmlNode node) {
        mNode = node;
    }
}
