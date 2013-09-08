package fr.xgouchet.xmleditor.parser.validator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ValidatorResult {

	/** the detected doctype */
	private String mDocType;

	/** the detected charset */
	private String mCharset;

	private final List<ValidatorEntry> mEntries = new ArrayList<ValidatorEntry>();

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

	public void addEntry(final ValidatorEntry entry) {
		mEntries.add(entry);
	}

	public List<ValidatorEntry> getEntries() {
		return Collections.unmodifiableList(mEntries);
	}
}
