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
    // XML EDITOR EVENTS
    //////////////////////////////////////////////////////////////////////////////////////
    
    public void onXmlDocumentChanged(final XmlNode root) {
        mXmlRoot = root;
        
        displayXmlRoot();
    }
    
    public void onXmlContentChanged() {
        
    }
    
    /**
     * Callback called when the back button is pressed.
     * 
     * @return true if the action has been handled, false to let the default implementation handle
     *         this (usually finishes the activity)
     */
    public abstract boolean onBackPressed();
    
    protected abstract void displayXmlRoot();
    
    
    
}
