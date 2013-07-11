package fr.xgouchet.xmleditor.tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
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
		void onXmlFileWritten();

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

	/** the listener for this writer's events */
	private final XmlFileWriterListener mListener;

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

		if (params.length == 0) {
			// TODO mResult.setError(XmlError.fileNotFound);
		} else {
			try {
				doSaveFile(params[0]);
			} catch (OutOfMemoryError e) {
				Log.e("Axel", "Error while saving", e);
				// TODO mResult.setError(XmlError.outOfMemory);
			} catch (Exception e) {
				Log.e("Axel", "Error while saving", e);
				// TODO mResult.setError(XmlError.unknown);
			}
		}

		return null;
	}

	/**
	 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
	 */
	@Override
	protected void onPostExecute(final Void result) {
		super.onPostExecute(result);

		mListener.onXmlFileWritten();
		mDialog.dismiss();
	}

	/**
	 * 
	 * @param path
	 */
	protected void doSaveFile(final String path) {

		StringBuilder builder;

		publishProgress(mContext.getString(R.string.ui_generating));
		builder = new StringBuilder();
		mRoot.buildXmlString(builder);

		publishProgress(mContext.getString(R.string.ui_writing));
		if (!TextFileUtils.writeTextFile(path + ".tmp", builder.toString(),
				mEncoding)) {
			// TODO mResult.setError(XmlError.write);
			return;
		}

		if (!FileUtils.deleteItem(path)) {
			// TODO mResult.setError(XmlError.delete);
			return;
		}

		if (!FileUtils.renameItem(path + ".tmp", path)) {
			// TODO mResult.setError(XmlError.rename);
			return;
		}

		// TODO mResult.setError(XmlError.noError);

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
