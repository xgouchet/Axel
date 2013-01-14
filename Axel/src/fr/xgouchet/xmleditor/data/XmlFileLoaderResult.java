package fr.xgouchet.xmleditor.data;

import java.io.File;

import fr.xgouchet.xmleditor.data.xml.XmlNode;
import fr.xgouchet.xmleditor.data.xml.XmlTreeParserException;

public class XmlFileLoaderResult {

	public static final int FLAG_IGNORE_FILE = 0x01;
	public static final int FLAG_FORCE_READ_ONLY = 0x02;

	/**
	 * @return the document
	 */
	public XmlNode getDocument() {
		return mDocument;
	}

	/**
	 * @return the error
	 */
	public XmlTreeParserException.XmlError getError() {
		return mError;
	}

	/**
	 * @return the fileHash
	 */
	public String getFileHash() {
		return mFileHash;
	}

	/**
	 * @return the errorInfo
	 */
	public String getErrorInfo() {
		return mErrorInfo;
	}

	/**
	 * @return the encoding
	 */
	public String getEncoding() {
		return mEncoding;
	}

	/**
	 * @return the file
	 */
	public File getFile() {
		return mFile;
	}

	/**
	 * @return the flags
	 */
	public int getFlags() {
		return mFlags;
	}

	/**
	 * @return if the force read only flag is set
	 */
	public boolean hasForceReadOnly() {
		return ((mFlags & FLAG_FORCE_READ_ONLY) == FLAG_FORCE_READ_ONLY);
	}

	/**
	 * @return if the ignore file flag is set
	 */
	public boolean hasIgnoreFile() {
		return ((mFlags & FLAG_IGNORE_FILE) == FLAG_IGNORE_FILE);
	}

	/**
	 * @param document
	 *            the document to set
	 */
	public void setDocument(XmlNode document) {
		mDocument = document;
	}

	/**
	 * @param error
	 *            the error to set
	 */
	public void setError(XmlTreeParserException.XmlError error) {
		mError = error;
	}

	/**
	 * @param fileHash
	 *            the fileHash to set
	 */
	public void setFileHash(String fileHash) {
		mFileHash = fileHash;
	}

	/**
	 * @param errorInfo
	 *            the errorInfo to set
	 */
	public void setErrorInfo(String errorInfo) {
		mErrorInfo = errorInfo;
	}

	/**
	 * @param encoding
	 *            the encoding to set
	 */
	public void setEncoding(String encoding) {
		mEncoding = encoding;
	}

	/**
	 * @param file
	 *            the file to set
	 */
	public void setFile(File file) {
		mFile = file;
	}

	/**
	 * @param flags
	 *            the flags to set
	 */
	public void setFlags(int flags) {
		mFlags = flags;
	}

	private XmlNode mDocument;
	private XmlTreeParserException.XmlError mError;
	private String mFileHash;
	private String mErrorInfo;
	private String mEncoding;
	private File mFile;
	private int mFlags;

}
