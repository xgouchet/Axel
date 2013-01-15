package fr.xgouchet.xmleditor.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import fr.xgouchet.androidlib.data.FileUtils;
import fr.xgouchet.androidlib.data.TextFileUtils;
import fr.xgouchet.axml.CompressedXmlUtils;
import fr.xgouchet.plist.PlistUtils;
import fr.xgouchet.xmleditor.AxelActivity;
import fr.xgouchet.xmleditor.R;
import fr.xgouchet.xmleditor.data.plist.XMLPlistParser;
import fr.xgouchet.xmleditor.data.xml.XmlCompressedTreeParser;
import fr.xgouchet.xmleditor.data.xml.XmlNode;
import fr.xgouchet.xmleditor.data.xml.XmlTreeParserException;
import fr.xgouchet.xmleditor.data.xml.XmlTreeParserException.XmlError;
import fr.xgouchet.xmleditor.data.xml.XmlTreePullParser;

public class AsyncXmlFileLoader extends
		AsyncTask<File, Void, XmlFileLoaderResult> {

	public AsyncXmlFileLoader(final AxelActivity activity, final int flags) {
		mActivity = activity;
		mResult = new XmlFileLoaderResult();
		mResult.setFlags(flags);
	}

	/**
	 * @see android.os.AsyncTask#onPreExecute()
	 */
	protected void onPreExecute() {
		super.onPreExecute();

		if (mDialog == null) {
			mDialog = new ProgressDialog(mActivity);
			mDialog.setTitle(R.string.ui_loading);
			mDialog.setMessage(mActivity.getString(R.string.ui_wait));
		}
		mDialog.show();
		mDialog.setCancelable(false);
	}

	/**
	 * @see android.os.AsyncTask#doInBackground(Params[])
	 */
	protected XmlFileLoaderResult doInBackground(final File... params) {

		if (params.length == 0) {
			mResult.setError(XmlError.ioException);
		} else {
			if (params.length > 1) {
				Log.w("Axel", "Only the first file will be parsed");
			}

			doReadFile(params[0]);
		}

		return mResult;
	}

	/**
	 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
	 */
	protected void onPostExecute(XmlFileLoaderResult result) {
		super.onPostExecute(result);
		mActivity.onFileOpened(mResult);
		mDialog.hide();
	}

	/**
	 * Starts reading the file with the correct parser
	 * 
	 * @param file
	 */
	private void doReadFile(final File file) {
		mResult.setFile(file);
		mResult.setEncoding(TextFileUtils.getFileEncoding(file));
		setDialogTitle(R.string.ui_hashing);
		mResult.setFileHash(FileUtils.getFileHash(file));

		try {
			if (CompressedXmlUtils.isCompressedXml(file)) {
				doOpenFileAsCompressedXml(file);
			} else if (PlistUtils.isBinaryPlist(file)) {
				doOpenFileAsBinaryPlist(file);
			} else {
				dOpenFileAsXml(file);
			}
		} catch (FileNotFoundException e) {
			mResult.setError(XmlError.fileNotFound);
		} catch (OutOfMemoryError e) {
			mResult.setError(XmlError.outOfMemory);
		} catch (XmlTreeParserException e) {
			mResult.setError(e.getError());
		} catch (Exception e) {
			Log.e("Axel", "Unknown error", e);
			mResult.setError(XmlError.unknown);
		}
	}

	/**
	 * Reads the given file into an {@link XmlNode} document
	 * 
	 * @param file
	 *            the file to load
	 */
	private void dOpenFileAsXml(File file) throws FileNotFoundException,
			IOException, XmlTreeParserException {
		InputStream input = null;

		if (file != null) {

			XmlNode document;
			final String encoding = TextFileUtils.getFileEncoding(file);

			input = new FileInputStream(file);
			setDialogTitle(R.string.ui_parsing);
			document = XmlTreePullParser.parseXmlTree(input, true, encoding);

			setDialogTitle(R.string.ui_generating);
			document.setExpanded(true, true);
			document.updateViewCount(true);

			mResult.setDocument(document);
			mResult.setError(XmlError.noError);

			if (input != null) {
				input.close();
			}

		}
	}

	/**
	 * Reads the given file into an {@link XmlNode} document
	 * 
	 * @param file
	 *            the file to load
	 */
	private void doOpenFileAsCompressedXml(File file)
			throws FileNotFoundException, IOException, XmlTreeParserException {
		InputStream input = null;

		if (file != null) {
			XmlNode document;
			input = new FileInputStream(file);
			setDialogTitle(R.string.ui_parsing);
			document = XmlCompressedTreeParser.parseXmlTree(file);

			setDialogTitle(R.string.ui_generating);
			document.setExpanded(true, true);
			document.updateViewCount(true);

			mResult.setDocument(document);
			mResult.setError(XmlError.noError);
			mResult.setFlags(mResult.getFlags()
					| XmlFileLoaderResult.FLAG_FORCE_READ_ONLY);

			if (input != null) {
				input.close();
			}
		}
	}

	/**
	 * Reads the given file into an {@link XmlNode} document
	 * 
	 * @param file
	 *            the file to load
	 */
	private void doOpenFileAsBinaryPlist(File file)
			throws FileNotFoundException, IOException, XmlTreeParserException {
		InputStream input = null;

		if (file != null) {
			XmlNode document;
			input = new FileInputStream(file);
			setDialogTitle(R.string.ui_parsing);
			document = XMLPlistParser.parseXmlTree(file);

			setDialogTitle(R.string.ui_generating);
			document.setExpanded(true, true);
			document.updateViewCount(true);

			mResult.setDocument(document);
			mResult.setError(XmlError.noError);
			mResult.setFlags(mResult.getFlags()
					| XmlFileLoaderResult.FLAG_FORCE_READ_ONLY);

			if (input != null) {
				input.close();
			}
		}
	}

	/**
	 * @param string
	 */
	private void setDialogTitle(final int string) {
		mActivity.runOnUiThread(new Runnable() {
			public void run() {
				mDialog.setTitle(string);
			}
		});
	}

	private final AxelActivity mActivity;
	private final XmlFileLoaderResult mResult;
	private ProgressDialog mDialog;
}
