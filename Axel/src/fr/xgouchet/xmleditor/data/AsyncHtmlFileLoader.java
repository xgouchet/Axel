package fr.xgouchet.xmleditor.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import android.util.Log;
import fr.xgouchet.androidlib.data.FileUtils;
import fr.xgouchet.androidlib.data.TextFileUtils;
import fr.xgouchet.xmleditor.AxelActivity;
import fr.xgouchet.xmleditor.R;
import fr.xgouchet.xmleditor.data.html.HtmlCleanerParser;
import fr.xgouchet.xmleditor.data.xml.XmlNode;
import fr.xgouchet.xmleditor.data.xml.XmlTreeParserException;
import fr.xgouchet.xmleditor.data.xml.XmlTreeParserException.XmlError;

public class AsyncHtmlFileLoader extends AsyncXmlFileLoader {

	public AsyncHtmlFileLoader(final AxelActivity activity, final int flags) {
		super(activity, flags);
	}

	/**
	 * @see fr.xgouchet.xmleditor.data.AsyncXmlFileLoader#doReadFile(java.io.File)
	 */
	@Override
	protected void doReadFile(final File file) {
		mResult.setFile(file);
		mResult.setEncoding(TextFileUtils.getFileEncoding(file));
		setDialogTitle(R.string.ui_hashing);
		mResult.setFileHash(FileUtils.getFileHash(file));

		try {
			dOpenFileAsHtmlSoup(file);
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
	private void dOpenFileAsHtmlSoup(final File file)
			throws FileNotFoundException, IOException, XmlTreeParserException {
		Reader input = null;

		if (file != null) {

			XmlNode document;
			input = new InputStreamReader(new FileInputStream(file));

			setDialogTitle(R.string.ui_parsing);
			document = HtmlCleanerParser.parseHtmlTree(input);

			setDialogTitle(R.string.ui_generating);
			document.setExpanded(true, true);
			document.updateChildViewCount(true);

			mResult.setDocument(document);
			mResult.setError(XmlError.noError);

			if (input != null) {
				input.close();
			}

		}
	}
}
