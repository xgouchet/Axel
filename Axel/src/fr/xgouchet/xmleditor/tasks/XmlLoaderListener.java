package fr.xgouchet.xmleditor.tasks;

import android.net.Uri;
import fr.xgouchet.xmleditor.data.xml.XmlNode;


public interface XmlLoaderListener {
    
    
    /**
     * Called when the XML document has been loaded successfully
     */
    void onXmlDocumentLoaded(XmlNode root, Uri uri, String hash,
            String encoding, boolean readOnly);
    
    /**
     * Called when an error occured while trying to read the document
     */
    void onXmlDocumentLoadError(Uri uri, Throwable throwable, String message);
    
}
