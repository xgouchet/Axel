package fr.xgouchet.xmleditor.data;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import fr.xgouchet.androidlib.data.FileUtils;
import fr.xgouchet.androidlib.data.TextFileUtils;
import fr.xgouchet.xmleditor.AxelActivity;
import fr.xgouchet.xmleditor.R;
import fr.xgouchet.xmleditor.data.xml.XmlNode;
import fr.xgouchet.xmleditor.data.xml.XmlTreeParserException.XmlError;

public class AsyncXmlFileWriter extends
		AsyncTask<String, Void, XmlFileWriterResult> {

	public AsyncXmlFileWriter(final AxelActivity activity, final XmlNode root,
			final String encoding) {
		mResult = new XmlFileWriterResult();
		mActivity = activity;
		mDocument = root;
		mEncoding = encoding;
	}

	/**
	 * @see android.os.AsyncTask#onPreExecute()
	 */
	protected void onPreExecute() {
		super.onPreExecute();

		if (mDialog == null) {
			mDialog = new ProgressDialog(mActivity);
			mDialog.setTitle(R.string.ui_saving);
			mDialog.setMessage(mActivity.getString(R.string.ui_wait));
		}

		mDialog.show();
		mDialog.setCancelable(false);
	}

	/**
	 * @see android.os.AsyncTask#doInBackground(Params[])
	 */
	protected XmlFileWriterResult doInBackground(String... params) {

		if (params.length == 0) {
			mResult.setError(XmlError.fileNotFound);
		} else {
			try {
				doSaveFile(params[0]);
			} catch (Exception e) {
				Log.e("Axel", "Error while saving", e);
				mResult.setError(XmlError.unknown);
			}
		}

		return mResult;
	}

	/**
	 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
	 */
	protected void onPostExecute(XmlFileWriterResult result) {
		super.onPostExecute(result);

		mActivity.onFileSaved(mResult);
		mDialog.hide();
	}

	protected void doSaveFile(String path) {

		mResult.setPath(path);

		StringBuilder builder;

		setDialogTitle(R.string.ui_generating);
		builder = new StringBuilder();
		mDocument.buildXmlString(builder);

		setDialogTitle(R.string.ui_writing);

		if (!TextFileUtils.writeTextFile(path + ".tmp", builder.toString(),
				mEncoding)) {
			mResult.setError(XmlError.write);
			return;
		}

		if (!FileUtils.deleteItem(path)) {
			mResult.setError(XmlError.delete);
			return;
		}

		if (!FileUtils.renameItem(path + ".tmp", path)) {
			mResult.setError(XmlError.rename);
			return;
		}

		mResult.setError(XmlError.noError);

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

	private XmlFileWriterResult mResult;
	private final AxelActivity mActivity;
	private ProgressDialog mDialog;
	private XmlNode mDocument;
	private String mEncoding;
}