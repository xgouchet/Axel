package fr.xgouchet.xmleditor.tasks;

import java.io.IOException;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import fr.xgouchet.androidlib.data.FileUtils;
import fr.xgouchet.androidlib.data.TextFileUtils;
import fr.xgouchet.xmleditor.R;
import fr.xgouchet.xmleditor.data.xml.XmlNode;

public class AsyncXmlFileWriter extends AsyncTask<String, String, Void> {

	/**
	 * An interface to listen to events occuring while saving the XML file
	 */
	public static interface XmlFileWriterListener {
		/**
		 * Called when the XML document has been loaded successfully
		 */
		void onXmlFileWritten(String filePath);

		/**
		 * Called when an error occured while trying to write the document
		 */
		void onXmlFileError(Throwable throwable, String message);
	}

	/** The current application context */
	protected final Context mContext;
	/** The progress dialog */
	private ProgressDialog mDialog;

	/** The saved file's encoding */
	protected String mEncoding;
	/** the saved file's XML root */
	protected XmlNode mRoot;
	/** the file path */
	protected String mFilePath;

	/** the listener for this writer's events */
	private final XmlFileWriterListener mListener;

	/** Throwable thrown while loading */
	private Throwable mThrowable;

	/**
	 * 
	 * @param activity
	 * @param listener
	 * @param root
	 * @param encoding
	 */
	public AsyncXmlFileWriter(final Context activity,
			final XmlFileWriterListener listener, final XmlNode root,
			final String encoding) {
		mContext = activity;
		mRoot = root;
		mEncoding = encoding;
		mListener = listener;
	}

	/**
	 * @see android.os.AsyncTask#onCancelled(java.lang.Object)
	 */
	@Override
	protected void onCancelled(final Void v) {
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
			mDialog.setTitle(R.string.ui_saving);
			mDialog.setMessage(mContext.getString(R.string.ui_wait));
		}

		mDialog.show();
		mDialog.setCancelable(false);
	}

	/**
	 * @see android.os.AsyncTask#doInBackground(Params[])
	 */
	@Override
	protected Void doInBackground(final String... params) {

		if (params == null) {
			throw new IllegalArgumentException(new NullPointerException());
		}

		if (params.length != 1) {
			throw new IllegalArgumentException();
		}

		if (params[0] == null) {
			throw new IllegalArgumentException(new NullPointerException());
		}

		try {
			doSaveFile(params[0]);
		} catch (Exception e) {
			mThrowable = e;
		}

		return null;
	}

	/**
	 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
	 */
	@Override
	protected void onPostExecute(final Void result) {
		super.onPostExecute(result);

		if (mThrowable == null) {
			mListener.onXmlFileWritten(mFilePath);
		} else {
			mListener.onXmlFileError(mThrowable, mThrowable.getMessage());
		}

		mDialog.dismiss();
	}

	/**
	 * 
	 * @param path
	 *            the path to save to
	 * @throws IOException
	 *             if an error occurs while saving
	 */
	protected void doSaveFile(final String path) throws IOException {

		StringBuilder builder;

		publishProgress(mContext.getString(R.string.ui_generating));

		builder = new StringBuilder();
		mRoot.buildXmlString(builder);

		publishProgress(mContext.getString(R.string.ui_writing));
		if (!TextFileUtils.writeTextFile(path + ".tmp", builder.toString(),
				mEncoding)) {
			throw new IOException("Unable to write temp file");
		}

		if (!FileUtils.deleteItem(path)) {
			throw new IOException("Unable to write delete file");
		}

		if (!FileUtils.renameItem(path + ".tmp", path)) {
			throw new IOException("Unable to rename temp file");
		}

		mFilePath = path;
	}

	/**
	 * @see android.os.AsyncTask#onProgressUpdate(Progress[])
	 */
	@Override
	protected void onProgressUpdate(final String... values) {
		mDialog.setTitle(TextUtils.concat(values));
		super.onProgressUpdate(values);
	}

}
