package fr.xgouchet.xmleditor.data;

import android.net.Uri;
import fr.xgouchet.xmleditor.data.xml.XmlNode;


/**
 * Callback for events on the document being edited in an {@link XmlEditor}
 * 
 * @author Xavier Gouchet
 */
public interface XmlEditorListener {
    
    /**
     * Called when the document being edited changed (clear / load)
     * 
     * @param root
     *            the root element of the document
     * @param name
     *            the file name
     * @param path
     *            the file path
     */
    void onXmlDocumentChanged(XmlNode root, String name, String path);
    
    /**
     * Call when something in the xml tree changed
     */
    void onXmlContentChanged();
    
    /**
     * Called when the current document has been saved
     */
    void onXmlDocumentSaved();
    
    
    /**
     * @param message
     *            an error message to display
     */
    void onErrorNotification(String message);
    
    /**
     * @param message
     *            an confirmation message to display
     */
    void onConfirmNotification(String message);
    
    /**
     * @param message
     *            an information message to display
     */
    void onInfoNotification(String message);
    
    
    
    /**
     * Called when a parse error occured while reading a file
     * 
     * @param uri
     *            the source file uri
     * @param message
     *            the error message
     */
    void onXmlParseError(Uri uri, String message);
    
    /**
     * Called when a parse error occured on a file detected as html
     */
    void onHtmlParseError();
    
}
