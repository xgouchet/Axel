package fr.xgouchet.xmleditor.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.EditText;
import fr.xgouchet.androidlib.data.FileUtils;
import fr.xgouchet.xmleditor.data.xml.XmlNode;

@SuppressWarnings("serial")
public class AxelUtils {

	private static final boolean PROMPT_PREFIX = true;

	/**
	 * 
	 * @param prefix
	 */
	public static void setupPrefixEditText(final EditText prefix,
			final XmlNode node, final boolean allowXmlns) {
		if (PROMPT_PREFIX) {
			prefix.setInputType(InputType.TYPE_NULL);
			prefix.setOnFocusChangeListener(new OnFocusChangeListener() {
				@Override
				public void onFocusChange(final View v, final boolean hasFocus) {
					if (hasFocus) {
						displayPrefixDialog(prefix, node, hasFocus);
					}
				}
			});

			prefix.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(final View v) {
					displayPrefixDialog(prefix, node, allowXmlns);
				}
			});

		}
	}

	private static void displayPrefixDialog(final EditText editText,
			final XmlNode node, final boolean allowXmlns) {
		AlertDialog.Builder builder = new Builder(editText.getContext());

		final List<String> prefixes = node.getContent().getNamespacePrefixes();

		// add some options
		Collections.sort(prefixes);
		if (prefixes.contains("xmlns")) {
			prefixes.remove("xmlns");
		}
		if (allowXmlns) {
			prefixes.add(0, "xmlns");
		}
		prefixes.add(0, "");

		builder.setTitle("Prefix");
		builder.setItems(prefixes.toArray(new String[prefixes.size()]),
				new OnClickListener() {

					@Override
					public void onClick(final DialogInterface dialog,
							final int which) {
						String prefix = prefixes.get(which);
						editText.setText(prefix);
					}
				});

		builder.show();

	}

	/**
	 * Ellipsizes a text
	 * 
	 * @param text
	 *            the text to ellipsize
	 * @param length
	 *            the max length of the return string
	 * @return an ellipsized string which contains at most lengt characters
	 */
	public static String ellipsize(final String text, final int length) {
		String result;

		if (TextUtils.isEmpty(text)) {
			result = "";
		} else {
			result = text;
			if (result.length() > length) {
				result = result.substring(0, length - 6) + " [...]";
			}
		}

		return result;
	}

	public static final List<String> HTML_EXT = new LinkedList<String>() {
		{
			add("htm");
			add("html");
			add("xhtml");
		}
	};

	public static final List<String> XML_EXT = new LinkedList<String>() {
		{
			add("xml");
			add("xsd");
			add("xslt");
			add("rng");
			add("atom");
			add("rss");
			add("aiml");
			add("cml");
			add("ficml");
			add("gml");
			add("kml");
			add("opml");
			add("sbml");
			add("scxml");
			add("wml");
			add("xaml");
			add("dae");
			add("svg");
			add("x3d");
			add("xmp");
			add("dbk");
			add("ofx");
			add("rnd");
			add("smil");
			add("wsdl");
			add("xbrl");
			add("xpl");
			add("xrc");
			add("xspf");
			add("xul");
			add("xml");
			add("xml");
		}
	};

	public static boolean isHtmlDocument(final File file) {
		final String ext = FileUtils.getFileExtension(file).toLowerCase(
				Locale.getDefault());
		return HTML_EXT.contains(ext);
	}

	public static boolean isXmlDocument(final File file) {
		final String ext = FileUtils.getFileExtension(file).toLowerCase(
				Locale.getDefault());
		return XML_EXT.contains(ext);
	}

	public static boolean canOpenCompressedFiles() {
		return false;
	}

	public static boolean isValidXmlFile(final File file) {
		boolean result;

		try {
			final InputStream input = new FileInputStream(file.getPath());
			final byte[] header = new byte[5];
			input.read(header, 0, 5);

			result = true;
			result &= (header[0] == '<');
			result &= (header[1] == '?');
			result &= (header[2] == 'x');
			result &= (header[3] == 'm');
			result &= (header[4] == 'l');

			input.close();
		} catch (Exception e) {
			result = false;
		}

		return result;

	}
}
