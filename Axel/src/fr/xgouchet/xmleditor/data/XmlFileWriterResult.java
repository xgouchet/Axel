package fr.xgouchet.xmleditor.data;

import fr.xgouchet.xmleditor.parser.xml.XmlTreeParserException.XmlError;

@Deprecated
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
	public void setPath(final String path) {
		mPath = path;
	}

	/**
	 * @param error
	 *            the error to set
	 */
	public void setError(final XmlError error) {
		mError = error;
	}

	private XmlError mError;
	private String mPath;

}
