package fr.xgouchet.xmleditor.data;

import fr.xgouchet.xmleditor.data.xml.XmlTreeParserException.XmlError;

public class XmlFileWriterResult {

	/**
	 * @return the path
	 */
	public String getPath() {
		return mPath;
	}

	/**
	 * @return the error
	 */
	public XmlError getError() {
		return mError;
	}

	/**
	 * @param path
	 *            the path to set
	 */
	public void setPath(String path) {
		mPath = path;
	}

	/**
	 * @param error
	 *            the error to set
	 */
	public void setError(XmlError error) {
		mError = error;
	}

	private XmlError mError;
	private String mPath;

}
