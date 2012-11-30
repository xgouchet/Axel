package fr.xgouchet.xmleditor.common;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import org.mozilla.universalchardet.UniversalDetector;

import android.text.TextUtils;
import android.util.Log;

/**
 * Misc file utilities
 */
public class TextFileUtils {

	private static final String TAG = "Axel";

	/**
	 * @param path
	 *            the absolute path to the file to save
	 * @param text
	 *            the text to write
	 * @return if the file was saved successfully
	 */
	public static boolean writeTextFile(String path, String text,
			String encoding) {
		File file = new File(path);
		OutputStreamWriter writer;
		BufferedWriter out;
		String eol_text = text;
		String enc = encoding;
		if (TextUtils.isEmpty(enc)) {
			enc = "UTF-8";
		}
		try {
			writer = new OutputStreamWriter(new FileOutputStream(file), enc);
			out = new BufferedWriter(writer);
			out.write(eol_text);
			out.close();
		} catch (OutOfMemoryError e) {
			Log.w(TAG, "Out of memory error", e);
			return false;
		} catch (IOException e) {
			Log.w(TAG, "Can't write to file " + path, e);
			return false;
		}
		return true;
	}

	/**
	 * @param file
	 *            a text file
	 * @return the encoding used by the file
	 */
	public static String getFileEncoding(File file) {
		String encoding = null;
		UniversalDetector detector = new UniversalDetector(null);
		byte[] buf = new byte[1024];

		try {
			FileInputStream input = new FileInputStream(file);

			int nread;
			while ((nread = input.read(buf)) > 0 && !detector.isDone()) {
				detector.handleData(buf, 0, nread);
			}
			detector.dataEnd();

			encoding = detector.getDetectedCharset();

			detector.reset();
			input.close();
		} catch (IOException e) {
			encoding = null;
		}

		return encoding;
	}
}
