package fr.xgouchet.xmleditor.tasks;

import android.net.Uri;

public interface XmlWriterListener {

	/**
	 * Called when the XML document has been written successfully
	 * 
	 * @param uri
	 *            the uri the document has been saved to
	 */
	void onXmlDocumentWritten(Uri uri, String hash);

	/**
	 * Called when an error occured while trying to write the document
	 * 
	 * @param uri
	 *            the uri it tried to save to
	 * @param throwable
	 *            the error
	 * @param message
	 *            the error message
	 */
	void onXmlDocumentWriteError(Uri uri, Throwable throwable, String message);
}
