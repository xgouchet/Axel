package fr.xgouchet.xmleditor.tasks;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import android.content.Context;
import android.util.Log;
import fr.xgouchet.androidlib.data.FileUtils;
import fr.xgouchet.androidlib.data.TextFileUtils;
import fr.xgouchet.xmleditor.R;
import fr.xgouchet.xmleditor.data.xml.XmlNode;
import fr.xgouchet.xmleditor.parser.html.HtmlCleanerParser;
import fr.xgouchet.xmleditor.parser.xml.XmlTreeParserException;

public class AsyncHtmlFileLoader extends AsyncXmlFileLoader {

	public AsyncHtmlFileLoader(final Context context,
			final XmlFileLoaderListener listener, final int flags) {
		super(context, listener, flags);
	}

	/**
	 * @see fr.xgouchet.xmleditor.tasks.AsyncXmlFileLoader#doReadFile(java.io.File)
	 */
	@Override
	protected void doReadFile(final File file) {
		mFile = file;
		mEncoding = TextFileUtils.getFileEncoding(file);

		publishProgress(mContext.getString(R.string.ui_hashing));
		mHash = FileUtils.getFileHash(file);

		try {
			doOpenFileAsHtmlSoup(file);

			mListener.onXmlFileLoaded(mRoot, mFile, mHash, mEncoding, false);
		} catch (FileNotFoundException e) {
			mListener.onXmlFileError(e, null);
		} catch (OutOfMemoryError e) {
			mListener.onXmlFileError(e, null);
		} catch (IOException e) {
			mListener.onXmlFileError(e, null);
		} catch (Exception e) {
			Log.e("Axel", "Unknown error", e);
			mListener.onXmlFileError(e, null);
		}
	}

	/**
	 * Reads the given file into an {@link XmlNode} document
	 * 
	 * @param file
	 *            the file to load
	 */
	private void doOpenFileAsHtmlSoup(final File file)
			throws FileNotFoundException, IOException, XmlTreeParserException {
		Reader input = null;

		input = new InputStreamReader(new FileInputStream(file));

		publishProgress(mContext.getString(R.string.ui_parsing));
		mRoot = HtmlCleanerParser.parseHtmlTree(input);

		if (input != null) {
			input.close();
		}
	}
}
