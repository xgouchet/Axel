package fr.xgouchet.xmleditor.network;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import android.os.AsyncTask;
import android.util.Log;

public class ValidateFileTask extends AsyncTask<File, Integer, Void> {

	private static final String VALIDATOR_URL = "http://validator.w3.org/check";

	private static final String END = "\r\n";
	private static final String HYPHENS = "--";
	private static final String BOUNDARY = "*****++++++************++++++++++++";
	private static final String SEP_LINE = HYPHENS + BOUNDARY + END;

	private static final int BUFFER_SIZE = 1024;
	private static final int MAX_PROGRESS = 10000;

	/**
	 * An interface to listen to events occuring while loading the XML file
	 */
	public static interface ValidationListener {

		void onValidationRequestProgress(int progress);
	}

	private ValidationListener mListener;

	public void setListener(final ValidationListener listener) {
		mListener = listener;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
	}

	@Override
	protected Void doInBackground(final File... params) {
		// TODO check input
		try {
			requestValidation(params[0]);
		} catch (Exception e) {
			Log.e("Validate", "Error", e);
		}
		return null;
	}

	@Override
	protected void onProgressUpdate(final Integer... values) {
		super.onProgressUpdate(values);

		mListener.onValidationRequestProgress(values[0]);
	}

	@Override
	protected void onCancelled() {
		// TODO Auto-generated method stub
		super.onCancelled();
	}

	@Override
	protected void onPostExecute(final Void result) {
		super.onPostExecute(result);
	}

	private void requestValidation(final File file) throws IOException {

		publishProgress(0);

		URL url = new URL(VALIDATOR_URL);
		HttpURLConnection connection = buildHttpConnection(url);

		DataOutputStream ds = new DataOutputStream(connection.getOutputStream());

		// Write Form Data
		ds.writeBytes(buildFormData(file.getName()));

		// Write file data
		FileInputStream fStream = new FileInputStream(file);
		int bufferSize = 1024;
		byte[] buffer = new byte[bufferSize];
		int length = -1;

		while ((length = fStream.read(buffer)) != -1) {
			ds.write(buffer, 0, length);
		}
		ds.writeBytes(END);
		ds.writeBytes(HYPHENS + BOUNDARY + HYPHENS + END);

		// close streams
		fStream.close();
		ds.flush();
		ds.close();

		// check HTTP status code
		if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
			Log.e("Validate",
					"Request HTTP status code is "
							+ connection.getResponseCode());
			return;
		} else {
			Log.i("Validate", "Yeah !");
		}

		// read response
		String result = readInputStream(connection);

		Log.i("Validate", result);
	}

	/**
	 * 
	 * @param input
	 * @return
	 * @throws IOException
	 */
	private String readInputStream(final URLConnection connection)
			throws IOException {
		int total = connection.getContentLength();
		int read = 0;

		InputStream input = connection.getInputStream();
		StringBuffer strBuffer = new StringBuffer();
		byte[] byteBuffer = new byte[BUFFER_SIZE];
		int length;

		do {
			length = input.read(byteBuffer);
			if (length > 0) {
				strBuffer.append(new String(byteBuffer, 0, length));
			}
			read += length;
			publishProgress((read * MAX_PROGRESS) / total);
		} while (length != -1);

		return strBuffer.toString();
	}

	/**
	 * TODO see why it returns HTML instead of SOAP
	 * Builds the request parameter
	 * 
	 * @param filename
	 *            the file name
	 * @return the request content
	 */
	private String buildFormData(final String filename) {
		StringBuilder request = new StringBuilder();

		request.append(SEP_LINE);
		request.append("Content-Disposition: form-data; name=\"output\"");
		request.append(END);
		request.append(END);
		request.append("soap12");
		request.append(END);

		request.append(SEP_LINE);
		request.append("Content-Disposition: form-data; name=\"debug\"");
		request.append(END);
		request.append(END);
		request.append("1");
		request.append(END);

		request.append(SEP_LINE);
		request.append("Content-Disposition: form-data; name=\"uploaded_file\";filename=\"");
		request.append(filename);
		request.append('"');
		request.append(END);

		return request.toString();
	}

	/**
	 * Build the connection
	 * 
	 * @param url
	 *            the url
	 * @return the connection
	 * @throws IOException
	 */
	private HttpURLConnection buildHttpConnection(final URL url)
			throws IOException {
		HttpURLConnection connection;

		connection = (HttpURLConnection) url.openConnection();

		connection.setDoInput(true);
		connection.setDoOutput(true);
		connection.setUseCaches(false);
		connection.setRequestMethod("POST");

		/* setRequestProperty */
		connection.setRequestProperty("Connection", "Keep-Alive");
		connection.setRequestProperty("Charset", "UTF-8");
		connection.setRequestProperty("Content-Type",
				"multipart/form-data;boundary=" + BOUNDARY);

		return connection;
	}
}
