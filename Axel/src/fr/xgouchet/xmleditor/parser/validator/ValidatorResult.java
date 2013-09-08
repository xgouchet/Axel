package fr.xgouchet.xmleditor.parser.validator;

public class ValidatorResult {

	/** the detected doctype */
	private String mDocType;

	/** the detected charset */
	private String mCharset;

	public void setCharset(final String charset) {
		mCharset = charset;
	}

	public void setDocType(final String docType) {
		mDocType = docType;
	}

	public String getCharset() {
		return mCharset;
	}

	public String getDocType() {
		return mDocType;
	}
}
