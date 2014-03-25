package fr.xgouchet.xmleditor.ui.fragment;

import android.app.Fragment;
import android.net.Uri;
import fr.xgouchet.xmleditor.data.XmlEditor;
import fr.xgouchet.xmleditor.data.XmlEditorListener;
import fr.xgouchet.xmleditor.data.xml.XmlNode;

/**
 * Base class for a fragment editing an XML Node
 * 
 * @author Xavier Gouchet
 * 
 */
public abstract class ANodeEditorFragment extends Fragment implements XmlEditorListener {

    protected XmlNode mXmlNode;
    protected XmlEditor mXmlEditor;

    //////////////////////////////////////////////////////////////////////////////////////
    // FRAGMENT LIFECYCLE
    //////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onDestroy() {
        super.onDestroy();
        mXmlEditor.removeListener(this);
    }

    //////////////////////////////////////////////////////////////////////////////////////
    // GETTERS / SETTERS
    //////////////////////////////////////////////////////////////////////////////////////

    public void setXmlEditor(final XmlEditor xmlEditor) {
        mXmlEditor = xmlEditor;
        mXmlEditor.addListener(this);
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
    // XML EDITOR LISTENER IMPLEMENTATION
    //////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onXmlDocumentChanged(final XmlNode root, final String name, final Uri uri) {
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
}
