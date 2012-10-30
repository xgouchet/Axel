package fr.xgouchet.xmleditor.common;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

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
	public static boolean writeTextFile(String path, String text) {
		File file = new File(path);
		OutputStreamWriter writer;
		BufferedWriter out;
		String eol_text = text;
		try {
			writer = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
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

}
