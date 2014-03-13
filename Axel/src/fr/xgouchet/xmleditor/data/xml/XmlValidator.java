package fr.xgouchet.xmleditor.data.xml;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.text.TextUtils;

/**
 * A utility class to check the validity of xml content according to w3c
 */
public class XmlValidator {

	/** Regexp to match a valid Name entity {@value} */
	public static final String NAME = "^[a-zA-Z_][a-zA-Z_0-9\\-\\.]*$";
	/** Regexp Pattern */
	public static final Pattern NAME_PATTERN = Pattern.compile(NAME);

	/** Regexp to match a valid version number {@value} */
	public static final String VERSION = "^1\\.[0-9]+$";
	/** Regexp Pattern */
	public static final Pattern VERSION_PATTERN = Pattern.compile(VERSION);

	/** Regexp to match a valid version number {@value} */
	public static final String ENCODING = "^[a-zA-Z][a-zA-Z0-9\\._-]*$";
	/** Regexp Pattern */
	public static final Pattern ENCODING_PATTERN = Pattern.compile(ENCODING);

	/** Regexp to find unescaped ampersand */
	public static final String AMPERSAND = "(&([\\w]*)[\\W&&[^;#]])|(&#(\\d+)[\\W&&[^;]])";
	/** Regexp Pattern */
	public static final Pattern AMPERSAND_PATTERN = Pattern.compile(AMPERSAND);

	/** XML namespace */
	public static final String XML_NS = "xmlns";

	/**
	 * Describes an invalid region in some text
	 */
	public static class InvalidRegion {
		public final int start, end;

		public InvalidRegion(final int start, final int end) {
			this.start = start;
			this.end = end;
		}
	}

	/**
	 * Check for invalid content in the input for a Comment Node
	 * 
	 * @param input
	 *            the input text
	 * @return the region of the first invalid input found or null if the input
	 *         is valid
	 */
	public static InvalidRegion getCommentInvalidRegion(final String input) {

		if (input.endsWith("-")) {
			return new InvalidRegion(input.length() - 1, input.length());
		}

		// less-than check
		return getSimpleMatchInvalidRegion(input, "--");
	}

	/**
	 * Check for invalid content in the input for a Text Node
	 * 
	 * @param input
	 *            the input text
	 * @return the region of the first invalid input found or null if the input
	 *         is valid
	 */
	public static InvalidRegion getTextInvalidRegion(final String input) {

		// Ampersand check
		Matcher ampMatcher = AMPERSAND_PATTERN.matcher(input);
		if (ampMatcher.find()) {
			return new InvalidRegion(ampMatcher.start(), ampMatcher.start() + 1);
		}

		// less-than check
		return getSimpleMatchInvalidRegion(input, "<");
	}

	/**
	 * Check for invalid content in the input for a CData Node
	 * 
	 * @param input
	 *            the input text
	 * @return the region of the first invalid input found or null if the input
	 *         is valid
	 */
	public static InvalidRegion getCDataInvalidRegion(final String input) {

		return getSimpleMatchInvalidRegion(input, "]]>");
	}

	public static InvalidRegion getPITargetInvalidRegion(final String input) {

		// Target cannot be exactly xml
		if (input.equalsIgnoreCase("xml")) {
			return new InvalidRegion(0, input.length());
		}

		// target must match the name pattern
		if (!NAME_PATTERN.matcher(input).matches()) {
			return new InvalidRegion(0, input.length());
		}

		return null;
	}

	public static InvalidRegion getPIContentInvalidRegion(final String input) {
		return getSimpleMatchInvalidRegion(input, "?>");
	}

	/**
	 * Search for a simple string match in the input and return the
	 * corresponding region
	 * 
	 * @param input
	 *            the input string
	 * @param match
	 *            the match to find
	 * @return the invalid region or null
	 */
	private static InvalidRegion getSimpleMatchInvalidRegion(
			final String input, final String match) {

		int index = input.indexOf(match);
		if (index >= 0) {
			return new InvalidRegion(index, index + match.length());
		}

		return null;
	}

	/**
	 * Escapes any illegal character in the input
	 * 
	 * @param input
	 *            the input string
	 * @return the valid escaped output
	 */
	public static String escapeAttributeValue(final String input) {
		String output;

		output = escapeAmpersand(input);
		output = output.replaceAll("<", "&lt;");
		output = output.replaceAll("\"", "&quot;");

		return output;
	}

	/**
	 * Escapes any illegal character in the input
	 * 
	 * @param input
	 *            the input string
	 * @return the valid escaped output
	 */
	public static String escapeTextContent(final String input) {
		String output;

		output = escapeAmpersand(input);
		output = output.replaceAll("<", "&lt;");
		// output.replaceAll(">", "&gt;"); not mandatory

		return output;
	}

	/**
	 * Escapes any unescaped and lonely ampersand found in the given document
	 * 
	 * @param input
	 *            the input string
	 * @return the escaped output
	 */
	public static String escapeAmpersand(final String input) {
		final Matcher matcher = AMPERSAND_PATTERN.matcher(input);
		final StringBuilder builder = new StringBuilder(input.length());

		boolean escape;
		int index, next;
		index = 0;
		do {

			escape = matcher.find(index);
			if (escape) {
				next = matcher.start();
				builder.append(input.substring(index, next));
				builder.append("&amp;");

				index = next + 1;
			} else if (index < input.length()) {
				builder.append(input.substring(index));
			}

		} while (escape && (index < input.length()));

		return builder.toString();
	}

	/**
	 * @param version
	 *            the version num in the xml document declaration
	 * @return if the version is valid
	 */
	public static boolean isValidVersionNum(final String version) {
		return VERSION_PATTERN.matcher(version).matches();
	}

	/**
	 * @param enconding
	 *            the encoding in the xml document declaration
	 * @return if the encoding is valid
	 */
	public static boolean isValidEncoding(final String enconding) {
		return ENCODING_PATTERN.matcher(enconding).matches();
	}

	/**
	 * @param name
	 *            the name to test
	 * @return if the name is valid
	 */
	public static boolean isValidName(final String name) {
		return NAME_PATTERN.matcher(name).matches();
	}

	/**
	 * @param uri
	 *            the uri
	 * @return if the given uri is valid
	 */
	public static boolean isValidNamespaceURI(final String uri) {
		boolean result = true;
		try {
			new URI(uri);
		} catch (URISyntaxException e) {
			result = false;
		}

		return result;
	}

	/**
	 * @param value
	 *            the attribute value to test
	 * @return if the attribute is valid
	 */
	public static boolean isValidAttributeValue(final String value) {

		boolean result;

		result = !value.contains("\"");
		result &= !value.contains("<");
		result &= !AMPERSAND_PATTERN.matcher(value).find();

		return result;
	}

	/**
	 * @param name
	 *            the name to test
	 * @return if the name is valid
	 */
	@Deprecated
	public static boolean isValidPITargetName(final String name) {
		boolean result;

		result = !name.equalsIgnoreCase("xml");
		result &= NAME_PATTERN.matcher(name).matches();

		return result;
	}

	/**
	 * @param content
	 *            the content to test
	 * @return if the content is valid
	 */
	@Deprecated
	public static boolean isValidPIContent(final String content) {
		boolean result;

		result = !content.contains("?>");

		return result;
	}

	/**
	 * @param content
	 *            the content to test
	 * @return if the content is valid
	 */
	@Deprecated
	public static boolean isValidCDataContent(final String content) {
		boolean result;

		result = !content.contains("]]>");

		return result;
	}

	/**
	 * @param comment
	 *            the comment to test
	 * @return if the comment is valid
	 */
	@Deprecated
	public static boolean isValidComment(final String comment) {

		boolean result;
		result = !comment.endsWith("-");
		result &= !comment.contains("--");

		return result;
	}

	/**
	 * @param text
	 *            the text to test
	 * @return if the text is valid
	 */
	@Deprecated
	public static boolean isValidText(final String text) {

		boolean result;

		result = !text.contains("<");
		result &= !AMPERSAND_PATTERN.matcher(text).find();

		return result;
	}

	/**
	 * @param prefix
	 *            a namespace prefix
	 * @param node
	 *            an xml node
	 * @param attrs
	 *            a list of attributes
	 * @param attribute
	 *            is the namespace for an attribute item
	 * @return if the namespace is valid according to the node context
	 */
	public static boolean isValidNamespace(final String prefix,
			final XmlNode node, final List<XmlAttribute> attrs,
			final boolean attribute) {
		boolean result = false;

		if (!TextUtils.isEmpty(prefix)) {
			if (attribute && prefix.equalsIgnoreCase(XML_NS)) {
				result = true;
			} else if (isLocalNamespace(prefix, attrs)) {
				result = true;
			} else if (isParentNamespace(prefix, node)) {
				result = true;
			}
		}

		return result;
	}

	/**
	 * @param prefix
	 *            a namespace prefix
	 * @param node
	 *            an xml node
	 * @return if the namespace is valid according to the node hierarchy
	 */
	protected static boolean isParentNamespace(final String prefix,
			final XmlNode node) {
		boolean result = false;

		if ((node != null) && (node.getContent().isElement())) {
			for (XmlAttribute attr : node.getParent().getContent()
					.getNamespaceAttributes()) {
				if (attr.getName().equals(prefix)) {
					result = true;
					break;
				}
			}
		}

		return result;
	}

	/**
	 * @param prefix
	 *            the prefix to test
	 * @param attrs
	 *            the local attributes
	 * @return if the local attributes define the given namespace
	 */
	protected static boolean isLocalNamespace(final String prefix,
			final List<XmlAttribute> attrs) {
		boolean result = false;

		for (XmlAttribute attr : attrs) {
			if ((attr.getPrefix().equalsIgnoreCase(XML_NS))
					&& (attr.getName().equals(prefix))) {
				result = true;
				break;
			}
		}

		return result;
	}

}
