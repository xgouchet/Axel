package fr.xgouchet.xmleditor.ui.fragment;

import android.app.Fragment;
import fr.xgouchet.xmleditor.data.xml.XmlNode;


/**
 * An abstract fragment handling the lifecycle of the underlying editor
 * 
 * @author Xavier Gouchet
 * 
 */
public abstract class AEditorFragment extends Fragment {
    
    protected XmlNode mXmlRoot;
    
    //////////////////////////////////////////////////////////////////////////////////////
    // FRAGMENT LIFECYCLE
    //////////////////////////////////////////////////////////////////////////////////////
    
    
    
    //////////////////////////////////////////////////////////////////////////////////////
    // Xml Editor events
    //////////////////////////////////////////////////////////////////////////////////////
    
    public void onXmlDocumentChanged(final XmlNode root) {
        mXmlRoot = root;
        
        displayXmlRoot();
    }
    
    public void onXmlContentChanged() {
        
    }
    
    protected abstract void displayXmlRoot();
    
    
    
}
