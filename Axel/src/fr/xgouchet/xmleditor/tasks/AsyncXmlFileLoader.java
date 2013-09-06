package fr.xgouchet.xmleditor.tasks;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.NotSerializableException;

import org.xmlpull.v1.XmlPullParserException;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import fr.xgouchet.androidlib.data.FileUtils;
import fr.xgouchet.androidlib.data.TextFileUtils;
import fr.xgouchet.axml.CompressedXmlUtils;
import fr.xgouchet.plist.PlistUtils;
import fr.xgouchet.xmleditor.R;
import fr.xgouchet.xmleditor.common.AxelUtils;
import fr.xgouchet.xmleditor.data.xml.UnknownFileFormatException;
import fr.xgouchet.xmleditor.data.xml.XmlNode;
import fr.xgouchet.xmleditor.parser.plist.XMLPlistParser;
import fr.xgouchet.xmleditor.parser.xml.XmlCompressedTreeParser;
import fr.xgouchet.xmleditor.parser.xml.XmlTreePullParser;
import fr.xgouchet.xmleditor.parser.xml.XmlTreePullParser.XmlPullParserInstantiationException;
import fr.xgouchet.xmleditor.parser.xml.XmlTreePullParser.XmlPullParserUnavailableFeatureException;

/**
 * An {@link AsyncTask} used to load an XML document from a file
 */
public class AsyncXmlFileLoader extends AsyncTask<File, String, Void> {

	/** Asks the loader to ignore the original file */
	public static final int FLAG_IGNORE_FILE = 0x01;
	/** forces the result to be read only */
	public static final int FLAG_FORCE_READ_ONLY = 0x02;
	/** treat the document as HTML soup */
	public static final int FLAG_HTML_SOUP = 0x04;

	/**
	 * An interface to listen to events occuring while loading the XML file
	 */
	public static interface XmlFileLoaderListener {

		/**
		 * Called when the XML document has been loaded successfully
		 */
		void onXmlFileLoaded(XmlNode root, File file, String hash,
				String encoding, boolean readOnly);

		/**
		 * Called when an error occured while trying to read the document
		 */
		void onXmlFileLoadError(File file, Throwable throwable, String message);
	}

	/** The current application context */
	protected final Context mContext;
	/** The progress dialog */
	private ProgressDialog mDialog;

	/** The loaded file */
	protected File mFile;
	/** The loaded file's Hash */
	protected String mHash;
	/** The loaded file's encoding */
	protected String mEncoding;
	/** the loaded file's XML root */
	protected XmlNode mRoot;

	/** Ignores the source file */
	private boolean mIgnoreFile;
	/** Force file as read only ? */
	private boolean mForceReadOnly;

	/** the listener for this loader's events */
	protected final XmlFileLoaderListener mListener;

	/** Throwable thrown while loading */
	protected Throwable mThrowable;

	/**
	 * 
	 * @param context
	 * @param listener
	 * @param flags
	 */
	public AsyncXmlFileLoader(final Context context,
			final XmlFileLoaderListener listener, final int flags) {
		mContext = context;
		mListener = listener;
		mForceReadOnly = ((flags & FLAG_FORCE_READ_ONLY) == FLAG_FORCE_READ_ONLY);
		mIgnoreFile = ((flags & FLAG_IGNORE_FILE) == FLAG_IGNORE_FILE);
	}

	/**
	 * @see android.os.AsyncTask#onCancelled(java.lang.Object)
	 */
	@Override
	protected void onCancelled(final Void result) {

		if (mDialog != null) {
			mDialog.dismiss();
		}
	}

	/**
	 * @see android.os.AsyncTask#onPreExecute()
	 */
	@Override
	protected void onPreExecute() {
		super.onPreExecute();

		if (mDialog == null) {
			mDialog = new ProgressDialog(mContext);
			mDialog.setTitle(R.string.ui_loading);
			mDialog.setMessage(mContext.getString(R.string.ui_wait));
		}
		mDialog.show();
		mDialog.setCancelable(false);
	}

	/**
	 * @see android.os.AsyncTask#doInBackground(Params[])
	 */
	@Override
	protected Void doInBackground(final File... params) {
		if (params == null) {
			throw new IllegalArgumentException(new NullPointerException());
		}

		if (params.length != 1) {
			throw new IllegalArgumentException();
		}

		if (params[0] == null) {
			throw new IllegalArgumentException(new NullPointerException());
		}

		doReadFile(params[0]);

		return null;
	}

	/**
	 * @see android.os.AsyncTask#onProgressUpdate(Progress[])
	 */
	@Override
	protected void onProgressUpdate(final String... values) {
		mDialog.setTitle(TextUtils.concat(values));
		super.onProgressUpdate(values);
	}

	/**
	 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
	 */
	@Override
	protected void onPostExecute(final Void result) {
		super.onPostExecute(result);
		File file = (mIgnoreFile ? null : mFile);
		if (mThrowable == null) {
			mListener.onXmlFileLoaded(mRoot, file, mHash, mEncoding,
					mForceReadOnly);
		} else {
			mListener.onXmlFileLoadError(file, mThrowable, null);
		}

		mDialog.dismiss();
		mDialog = null;
	}

	/**
	 * Starts reading the file with the correct parser
	 * 
	 * @param file
	 */
	protected void doReadFile(final File file) {

		if (file == null) {
			return;
		}

		try {
			doParseFile(file);
		} catch (Exception e) {
			mThrowable = e;
		}

	}

	/**
	 * Parses the file
	 * 
	 * @param file
	 *            the file to parse
	 */
	private void doParseFile(final File file) throws FileNotFoundException,
			OutOfMemoryError, IOException, StringIndexOutOfBoundsException,
			XmlPullParserUnavailableFeatureException,
			XmlPullParserInstantiationException, XmlPullParserException {
		mFile = file;
		mEncoding = null;

		// Get file's original hash
		publishProgress(mContext.getString(R.string.ui_hashing));
		mHash = FileUtils.getFileHash(file);

		// Check if file is compresed
		if (CompressedXmlUtils.isCompressedXml(file)) {
			doOpenFileAsCompressedXml(file);
		} else if (PlistUtils.isBinaryPlist(file)) {
			doOpenFileAsBinaryPlist(file);
		} else if (AxelUtils.isValidXmlFile(file)) {
			mEncoding = TextFileUtils.getFileEncoding(file);
			dOpenFileAsXml(file);
		} else {
			mThrowable = new UnknownFileFormatException();
		}

	}

	/**
	 * Reads the given file into an {@link XmlNode} document
	 * 
	 * @param file
	 *            the file to load
	 */
	private void dOpenFileAsXml(final File file) throws FileNotFoundException,
			IOException, StringIndexOutOfBoundsException,
			XmlPullParserUnavailableFeatureException,
			XmlPullParserInstantiationException, XmlPullParserException {
		InputStream input = null;

		final String encoding = TextFileUtils.getFileEncoding(file);

		input = new FileInputStream(file);
		publishProgress(mContext.getString(R.string.ui_parsing));
		mRoot = XmlTreePullParser.parseXmlTree(input, true, encoding);
		mForceReadOnly = false;

		if (input != null) {
			input.close();
		}
	}

	/**
	 * Reads the given file into an {@link XmlNode} document
	 * 
	 * @param file
	 *            the file to load
	 */
	private void doOpenFileAsCompressedXml(final File file)
			throws FileNotFoundException, IOException {
		InputStream input = null;

		input = new FileInputStream(file);
		publishProgress(mContext.getString(R.string.ui_parsing));
		mRoot = XmlCompressedTreeParser.parseXmlTree(file);
		mForceReadOnly = true;

		if (input != null) {
			input.close();
		}
	}

	/**
	 * Reads the given file into an {@link XmlNode} document
	 * 
	 * @param file
	 *            the file to load
	 */
	private void doOpenFileAsBinaryPlist(final File file)
			throws FileNotFoundException, IOException, EOFException,
			NotSerializableException {
		InputStream input = null;

		input = new FileInputStream(file);
		publishProgress(mContext.getString(R.string.ui_parsing));
		mRoot = XMLPlistParser.parseXmlTree(file);
		mForceReadOnly = true;

		if (input != null) {
			input.close();
		}
	}

}
