package fr.xgouchet.xmleditor.ui.fragment;

import android.app.Fragment;
import android.net.Uri;
import fr.xgouchet.xmleditor.data.XmlEditor;
import fr.xgouchet.xmleditor.data.XmlEditorListener;
import fr.xgouchet.xmleditor.data.xml.XmlNode;

/**
 * An abstract fragment handling the lifecycle of the underlying editor
 * 
 * @author Xavier Gouchet
 * 
 */
public abstract class ADocumentEditorFragment extends Fragment implements XmlEditorListener {

    protected XmlNode mXmlRoot;
    protected XmlEditor mXmlEditor;

    public void setXmlEditor(final XmlEditor xmlEditor) {
        mXmlEditor = xmlEditor;
        mXmlEditor.addListener(this);
    }

    //////////////////////////////////////////////////////////////////////////////////////
    // FRAGMENT LIFECYCLE
    //////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onDestroy() {
        super.onDestroy();
        mXmlEditor.removeListener(this);
    }

    //////////////////////////////////////////////////////////////////////////////////////
    // XML EDITOR LISTENER IMPLEMENTATION
    //////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onXmlDocumentChanged(final XmlNode root, final String name, final Uri uri) {
        mXmlRoot = root;

        displayXmlRoot();
    }

    @Override
    public void onXmlContentChanged() {

    }

    @Override
    public void onXmlDocumentSaved() {

    }

    @Override
    public void onXmlParseError(final Uri uri, final String message) {
    }

    @Override
    public void onHtmlParseError(final Uri uri, final String message) {
    }

    @Override
    public void onErrorNotification(final String message) {
    }

    @Override
    public void onInfoNotification(final String message) {
    }

    @Override
    public void onConfirmNotification(final String message, final Runnable undo) {
    }

    //////////////////////////////////////////////////////////////////////////////////////
    // ABSTRACT METHODS
    //////////////////////////////////////////////////////////////////////////////////////

    protected abstract void displayXmlRoot();

}
