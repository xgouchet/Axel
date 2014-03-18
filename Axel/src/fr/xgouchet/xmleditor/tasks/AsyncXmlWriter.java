package fr.xgouchet.xmleditor.tasks;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.concurrent.CancellationException;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.TextUtils;
import fr.xgouchet.xmleditor.R;
import fr.xgouchet.xmleditor.common.FileUtils;
import fr.xgouchet.xmleditor.data.xml.XmlNode;

public class AsyncXmlWriter extends AsyncTask<Void, String, Void> {

	/** The current application context */
	protected final Context mContext;

	/** The loaded uri */
	protected final Uri mUri;
	/** the document's XML root */
	protected final XmlNode mRoot;

	/** The saved file's encoding */
	protected String mEncoding;
	protected final boolean mKeepUri;

	/** The Listener */
	private final XmlWriterListener mListener;
	/** Throwable thrown while writing */
	protected Throwable mThrowable;

	private ProgressDialog mDialog;

	private String mHash;

	/**
	 * 
	 * @param context
	 * @param uri
	 * @param root
	 * @param listener
	 * @param keepUri
	 */
	public AsyncXmlWriter(final Context context, final Uri uri,
			final XmlNode root, final XmlWriterListener listener,
			final String encoding, final boolean keepUri) {
		mContext = context;
		mUri = uri;
		mRoot = root;
		mEncoding = encoding;
		mKeepUri = keepUri;
		mListener = listener;
	}

	// ////////////////////////////////////////////////////////////////////////////////////
	// ASYNC TASK LIFECYCLE
	// ////////////////////////////////////////////////////////////////////////////////////

	@Override
	protected void onPreExecute() {
		// create the progress dialog
		if (mDialog == null) {
			mDialog = new ProgressDialog(mContext);
			mDialog.setTitle(R.string.ui_writing);
			mDialog.setMessage(mContext.getString(R.string.ui_wait));
		}

		// show the progress dialog
		mDialog.show();
		mDialog.setCancelable(true);
		mDialog.setOnCancelListener(new OnCancelListener() {

			@Override
			public void onCancel(final DialogInterface dialog) {
				cancel(true);
			}
		});
	}

	@Override
	protected Void doInBackground(final Void... params) {
		try {
			doWriteDocument();
		} catch (Exception e) {
			mThrowable = e;
		}

		return null;
	}

	@Override
	protected void onProgressUpdate(final String... values) {
		// update dialog title
		mDialog.setTitle(TextUtils.concat(values));

		super.onProgressUpdate(values);
	}

	@Override
	protected void onPostExecute(final Void result) {
		super.onPostExecute(result);

		// dismiss the dialog
		if (mDialog.isShowing()) {
			mDialog.dismiss();
		}
		mDialog = null;

		// callback uri
		Uri uri = (mKeepUri ? mUri : null);

		if (isCancelled()) {
			mListener.onXmlDocumentWriteError(uri, new CancellationException(),
					null);
		}

		if (mThrowable == null) {
			mListener.onXmlDocumentWritten(uri, mHash);
		} else {
			mListener.onXmlDocumentWriteError(uri, mThrowable, null);
		}
	}

	// ////////////////////////////////////////////////////////////////////////////////////
	// XML WRITING
	// ////////////////////////////////////////////////////////////////////////////////////

	private void doWriteDocument() {

		// generate the text/xml content
		publishProgress(mContext.getString(R.string.ui_generating));
		StringBuilder builder = new StringBuilder();
		mRoot.buildXmlString(builder);

		// Write to file
		writeToOutputStream(builder.toString());

		// Get file's original hash
		publishProgress(mContext.getString(R.string.ui_hashing));
		try {
			mHash = FileUtils.getHash(getInputStream());
		} catch (FileNotFoundException e) {
			// ignore
			mHash = "";
		}
	}

	/**
	 * 
	 * @param data
	 */
	private void writeToOutputStream(final String data) {
		OutputStreamWriter osw = null;
		BufferedWriter bw = null;

		try {
			osw = new OutputStreamWriter(getOutputStream(),
					TextUtils.isEmpty(mEncoding) ? "UTF-8" : mEncoding);
			bw = new BufferedWriter(osw);
			bw.write(data);
			bw.flush();
		} catch (IOException e) {
			mThrowable = e;
		} finally {

			if (bw != null) {
				try {
					bw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}

	}

	// ////////////////////////////////////////////////////////////////////////////////////
	// UTILS
	// ////////////////////////////////////////////////////////////////////////////////////

	/**
	 * 
	 * @return the input stream to load
	 * @throws FileNotFoundException
	 */
	protected InputStream getInputStream() throws FileNotFoundException {
		return mContext.getContentResolver().openInputStream(mUri);
	}

	/**
	 * 
	 * @return the output stream to load
	 * @throws FileNotFoundException
	 */
	protected OutputStream getOutputStream() throws FileNotFoundException {
		return mContext.getContentResolver().openOutputStream(mUri);
	}
}
