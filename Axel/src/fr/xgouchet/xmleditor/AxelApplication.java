package fr.xgouchet.xmleditor;

import android.app.Application;
import fr.xgouchet.xmleditor.data.xml.XmlNode;

/**
 * 
 */
public class AxelApplication extends Application {

	public void documentContentChanged() {
	}

	public void setCurrentDocument(final XmlNode doc, final String name, final String path) {
		mCurrentDocument = doc;
		mCurrentDocumentName = name;
		mCurrentDocumentPath = path;
	}

	public void setCurrentSelection(final XmlNode currentSelection) {
		mCurrentSelection = currentSelection;
	}

	public XmlNode getCurrentDocument() {
		return mCurrentDocument;
	}

	public String getCurrentDocumentName() {
		return mCurrentDocumentName;
	}

	public String getCurrentDocumentPath() {
		return mCurrentDocumentPath;
	}

	public XmlNode getCurrentSelection() {
		return mCurrentSelection;
	}

	



	/** */
	private String mCurrentDocumentName;
	/** */
	private String mCurrentDocumentPath;
	/** */
	private XmlNode mCurrentDocument;
	/** */
	private XmlNode mCurrentSelection;

}
