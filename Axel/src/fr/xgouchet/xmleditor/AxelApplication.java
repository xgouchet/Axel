package fr.xgouchet.xmleditor;

import android.annotation.SuppressLint;
import android.app.Application;
import fr.xgouchet.androidlib.data.FileUtils;
import fr.xgouchet.xmleditor.data.xml.XmlNode;

/**
 * 
 */

public class AxelApplication extends Application {

	public void documentContentChanged() {
	}

	public void setCurrentDocument(XmlNode doc, String name, String path) {
		mCurrentDocument = doc;
		mCurrentDocumentName = name;
		mCurrentDocumentPath = path;
	}

	public void setCurrentSelection(XmlNode currentSelection) {
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

	/**
	 * @return if the current document can be displayed in a webview
	 */
	@SuppressLint("DefaultLocale")
	public boolean canBePreviewed() {
		boolean res = false;

		// test on file name extension
		String ext = FileUtils.getFileExtension(mCurrentDocumentName)
				.toLowerCase();
		if (ext.equals("html") || ext.equals("htm") || ext.equals("php")
				|| (ext.equals("svg"))) {
			res = true;
		}

		// test on root node
		XmlNode root = mCurrentDocument.getRootChild();
		if (root != null) {
			String tag = root.getContent().getName().toLowerCase();
			if (tag.equals("html") || tag.equals("svg")) {
				res = true;
			}
		}

		// TODO test on root namespace / dopctype

		return res;
	}

	/**
	 * @return the mime type of the current document
	 */
	@SuppressLint("DefaultLocale")
	public String getMimeType() {
		String type = "text/xml";

		// test on file name extension
		String ext = FileUtils.getFileExtension(mCurrentDocumentName)
				.toLowerCase();
		if (ext.equals("html") || ext.equals("htm") || ext.equals("php")) {
			type = "text/html";
		} else if (ext.equals("svg")) {
			type = "text/html";
		} else {
			// test on root node
			XmlNode root = mCurrentDocument.getRootChild();
			if (root != null) {
				String tag = root.getContent().getName().toLowerCase();
				if (tag.equals("html")) {
					type = "text/html";
				} else if (tag.equals("svg")) {
					type = "text/html";
				}
			}
		}

		return type;
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
