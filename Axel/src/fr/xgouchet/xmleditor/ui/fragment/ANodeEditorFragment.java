package fr.xgouchet.xmleditor.ui.fragment;

import android.app.ActionBar;
import android.app.Fragment;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import fr.xgouchet.xmleditor.R;
import fr.xgouchet.xmleditor.data.XmlEditor;
import fr.xgouchet.xmleditor.data.xml.XmlNode;


/**
 * Base class for a fragment editing an XML Node
 * 
 * @author Xavier Gouchet
 * 
 */
public abstract class ANodeEditorFragment extends Fragment {
    
    
    protected XmlNode mXmlNode;
    protected XmlEditor mXmlEditor;
    
    
    
    //////////////////////////////////////////////////////////////////////////////////////
    // GETTERS / SETTERS
    //////////////////////////////////////////////////////////////////////////////////////
    
    
    public void setXmlEditor(final XmlEditor xmlEditor) {
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
    
    //////////////////////////////////////////////////////////////////////////////////////
    // UI
    //////////////////////////////////////////////////////////////////////////////////////
    
    protected void showDoneDiscardButtons() {
        LayoutInflater inflater;
        
        
        final ActionBar actionBar = getActivity().getActionBar();
        
        inflater = (LayoutInflater) getActivity().getActionBar().getThemedContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        
        
        final View custom = inflater.inflate(R.layout.ab_done_discard, null);
        
        custom.findViewById(R.id.buttonDiscard).setOnClickListener(mDoneDiscardClickListener);
        custom.findViewById(R.id.buttonDone).setOnClickListener(mDoneDiscardClickListener);
        
        
        // Show the custom action bar views 
        // hide the normal Home icon and title.
        
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM,
                ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME
                        | ActionBar.DISPLAY_SHOW_TITLE);
        actionBar.setCustomView(custom, new ActionBar.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        
    }
    
    protected void hideDoneDiscardButton() {
        getActivity().getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME
                | ActionBar.DISPLAY_SHOW_TITLE, ActionBar.DISPLAY_SHOW_CUSTOM
                | ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE);
    }
    
    protected void hideSoftKeyboard() {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
        }
    }
    
    private OnClickListener mDoneDiscardClickListener = new OnClickListener() {
        
        @Override
        public void onClick(final View v) {
            boolean close = false;
            
            switch (v.getId()) {
                case R.id.buttonDone:
                    close = onApply();
                    break;
                case R.id.buttonDiscard:
                    close = onDiscard();
                    break;
                default:
                    break;
            }
            
            if (close) {
                getFragmentManager().popBackStack();
                hideDoneDiscardButton();
                hideSoftKeyboard();
            }
        }
    };
    
    /**
     * Called when the user presses the "Done" button
     * 
     * @return true if the fragment should be closed, false to keep it open (eg : when an input is
     *         invalid for instance)
     */
    protected abstract boolean onApply();
    
    /**
     * Called when the user presses the "Discard" button
     * 
     * @return true if the fragment should be closed, false to keep it open (eg : when an input is
     *         invalid for instance)
     */
    protected abstract boolean onDiscard();
}
