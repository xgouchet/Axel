package fr.xgouchet.xmleditor.common;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.xmlpull.v1.XmlPullParserException;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore.MediaColumns;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.EditText;
import fr.xgouchet.xmleditor.R;
import fr.xgouchet.xmleditor.data.xml.XmlNode;
import fr.xgouchet.xmleditor.parser.xml.XmlTreePullParser;
import fr.xgouchet.xmleditor.parser.xml.XmlTreePullParser.XmlPullParserInstantiationException;
import fr.xgouchet.xmleditor.parser.xml.XmlTreePullParser.XmlPullParserUnavailableFeatureException;

@SuppressWarnings("serial")
public class AxelUtils {

	public static final String[] XML_MIME_TYPE = new String[] {
	/** default */
	"text/xml", "application/xml",

	/** application/*+xml */
	"application/atom+xml", "application/rdf+xml", "application/rss+xml",
			"application/vnd.google-earth.kml+xml",
			"application/vnd.mozilla.xul+xml", "application/xhtml+xml",
			"application/xslt+xml", "application/xspf+xml",
			"application/x-webarchive-xml",

			/** application/? */
			"application/smil",

			/** image */
			"image/svg+xml",

			/** text */
			"text/html", "text/x-opml+xml", "text/vnd.wap.wml",

	};

	/** A list of known HTML extensions */
	public static final List<String> HTML_EXT = new LinkedList<String>() {

		{
			add("htm");
			add("html");
			add("xhtml");
		}
	};

	/** A list of known XML extensions */
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
		}
	};

	private static final boolean PROMPT_PREFIX = true;

	/**
	 * Converts a String content to an {@link XmlNode}
	 * 
	 * @param content
	 *            the content to convert
	 * @return the string content as an XML Node (or null
	 * @throws XmlPullParserUnavailableFeatureException
	 *             - when a feature is not supported by the current device
	 * @throws XmlPullParserInstantiationException
	 *             - when the factory can't create a new parser
	 * @throws XmlPullParserException
	 *             - when a parsing error occurs
	 * @throws IOException
	 * @throws StringIndexOutOfBoundsException
	 */
	public static XmlNode contentAsXml(final String content)
			throws StringIndexOutOfBoundsException,
			XmlPullParserUnavailableFeatureException,
			XmlPullParserInstantiationException, XmlPullParserException,
			IOException {
		XmlNode node = null, doc = null;
		InputStream input;

		if (content.indexOf('<') < 0) {
			node = XmlNode.createText(content);
		} else if (content
				.matches("^\\s*<\\?\\s*([a-zA-Z:_])([a-zA-Z0-9:_]*)\\s(.(?!>))*\\?>\\s*$")) {
			int start = content.indexOf("<?") + 2;
			int end = content.indexOf("?>");
			node = XmlNode.createProcessingInstruction(content.substring(start,
					end));
		} else if (content.matches("^\\s*<!\\[CDATA\\[([\\w\\W\\s]*)]]>\\s*$")) {
			int start = content.indexOf("<![CDATA[") + 9;
			int end = content.indexOf("]]>");
			node = XmlNode.createCDataSection(content.substring(start, end));
		} else {
			input = new ByteArrayInputStream(content.getBytes());
			doc = XmlTreePullParser.parseXmlTree(input, false, null);

			if ((doc != null) && (doc.getChildrenCount() > 0)) {
				node = (XmlNode) doc.getChildAtPos(0);
			}
		}

		return node;
	}

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

	/**
	 * Verifies if the documents file extension matches a known HTML format
	 * 
	 * @param uri
	 *            the document's uri
	 * @return true if it matches an HTML document
	 */
	public static boolean isHtmlDocument(final Uri uri) {
		// get uri extension
		String ext = "";
		String path = uri.getPath();
		int index = path.lastIndexOf('.');
		if (index != -1) {
			ext = path.substring(index + 1).toLowerCase();
		}

		// check if it matches HTML formats
		return HTML_EXT.contains(ext);
	}

	/**
	 * Verifies if the documents file extension matches a known XML format
	 * 
	 * @param uri
	 *            the document's uri
	 * @return true if it matches an XML document
	 */
	public static boolean isXmlDocument(final Uri uri) {
		// get uri extension
		String ext = getUriFileExtension(uri);

		// check if it matches XML formats
		return XML_EXT.contains(ext);
	}

	/**
	 * Checks if the given document can be displayed as a web content (page,
	 * svg, ... ?)
	 * 
	 * @param document
	 *            the document
	 * @param uri
	 *            the document's URI
	 * 
	 * @return if the current document can be displayed in a webview
	 */
	public static boolean canBePreviewed(final XmlNode document, final Uri uri) {

		// test on file name extension
		String ext = getUriFileExtension(uri);
		if (ext.equals("html") || ext.equals("htm") || ext.equals("php")
				|| (ext.equals("svg"))) {
			return true;
		}

		// test on root node
		XmlNode root = document.getRootChild();
		if (root != null) {
			String tag = root.getContent().getName();
			if ("html".equalsIgnoreCase(tag) || "svg".equalsIgnoreCase(tag)) {
				return true;
			}
		}

		// TODO test on root namespace / doctype

		return false;
	}

	/**
	 * 
	 * @param document
	 *            the document
	 * @param uri
	 *            the document's URI
	 * @return the mime type of the given document
	 */
	public static String getMimeType(final XmlNode document, final Uri uri) {
		String type = "text/xml";

		// test on file name extension
		String ext = getUriFileExtension(uri);
		if (ext.equals("html") || ext.equals("htm") || ext.equals("php")) {
			type = "text/html";
		} else if (ext.equals("svg")) {
			type = "text/html";
		} else {
			// test on root node
			XmlNode root = document.getRootChild();
			if (root != null) {
				String tag = root.getContent().getName().toLowerCase();
				if (tag.equals("html")) {
					type = "text/html";
				} else if (tag.equals("svg")) {
					type = "text/html";
				}
			}
		}

		return type;
	}

	/**
	 * TODO use the file name instead of uri to check the extension
	 * 
	 * Returns the extension of the given uri if any
	 * 
	 * @param uri
	 *            the uri
	 * @return the extension (only simple extensions are matched, eg .gz instead
	 *         of .tar.gz)
	 */
	public static String getUriFileExtension(final Uri uri) {
		String ext = "";

		if (uri == null) {
			return "";
		}

		String path = uri.getPath();
		int index = path.lastIndexOf('.');
		if (index != -1) {
			ext = path.substring(index + 1).toLowerCase();
		}

		return ext;
	}

	/**
	 * 
	 * @param uri
	 * @param context
	 * @return
	 */
	public static String getUriFileName(final Uri uri, final Context context) {
		String name = "";

		// Exctract name from provider
		int index;
		String[] proj = { MediaColumns.TITLE, MediaColumns.DISPLAY_NAME };
		Cursor cursor = context.getContentResolver().query(uri, proj, null,
				null, null);

		// only if a match is found
		if ((cursor != null) && (cursor.getCount() != 0)) {
			cursor.moveToFirst();

			if (TextUtils.isEmpty(name)) {
				index = cursor.getColumnIndexOrThrow(MediaColumns.TITLE);
				name = cursor.getString(index);
			}

			if (TextUtils.isEmpty(name)) {
				index = cursor.getColumnIndexOrThrow(MediaColumns.DISPLAY_NAME);
				name = cursor.getString(index);
			}
		}

		if (TextUtils.isEmpty(name)) {
			List<String> segments = uri.getPathSegments();
			name = segments.get(segments.size() - 1);
			index = name.lastIndexOf(File.separatorChar);
			if (index >= 0) {
				name = name.substring(index);
			}
		}

		//
		Log.i("Name", name);

		if (cursor != null) {
			cursor.close();
		}

		return name;
	}

	/**
	 * Create an xml node based on action ids
	 * 
	 * @param itemId
	 *            the action item id (R.id.action_add_XXX)
	 * @return the created node or null
	 */
	public static XmlNode createXmlNode(final int itemId) {
		XmlNode node;
		switch (itemId) {
		case R.id.action_add_child_element:
			node = XmlNode.createElement("element");
			break;
		case R.id.action_add_child_doctype:
			node = XmlNode
					.createDoctypeDeclaration("root SYSTEM \"DTD location\"");
			break;
		case R.id.action_add_child_pi:
			node = XmlNode.createProcessingInstruction("target", "instruction");
			break;
		case R.id.action_add_child_comment:
			node = XmlNode.createComment("comment");
			break;
		case R.id.action_add_child_text:
			node = XmlNode.createText("text");
			break;
		case R.id.action_add_child_cdata:
			node = XmlNode.createCDataSection("unparsed data");
			break;
		default:
			node = null;
			break;
		}

		return node;
	}

}
