package fr.xgouchet.xmleditor.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import fr.xgouchet.xmleditor.R;


public class TextNodeEditorFragment extends ANodeEditorFragment {
    
    private EditText mEditText;
    
    //////////////////////////////////////////////////////////////////////////////////////
    // FRAGMENT LIFECYCLE
    //////////////////////////////////////////////////////////////////////////////////////    
    
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
            final Bundle savedInstanceState) {
        
        View root = inflater.inflate(R.layout.fragment_node_text_editor, container, false);
        
        // Setup Text input
        mEditText = (EditText) root.findViewById(R.id.edit_text_content);
        mEditText.setText(mXmlNode.getContent().getText());
        
        // show the done / discard pattern
        showDoneDiscardButtons();
        
        
        return root;
    }
    
    @Override
    protected boolean onApply() {
        mXmlNode.getContent().setText(mEditText.getText().toString());
        return true;
    }
    
    @Override
    protected boolean onDiscard() {
        return true;
    }
    
}
