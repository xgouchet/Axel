package fr.xgouchet.xmleditor.common;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import fr.xgouchet.androidlib.data.FileUtils;

@SuppressWarnings("serial")
public class AxelUtils {

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
}
