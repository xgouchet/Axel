package fr.xgouchet.xmleditor.ui.spannable;

import java.util.LinkedList;

import android.text.SpannableString;

/**
 * A utility class to build a {@link SpannableString} easily
 */
public class XMLSpanBuilder {

	private final class XMLSpanInfo {
		public int start, end;
		public Object span;
	}

	/**
	 */
	public XMLSpanBuilder() {
		mBuilder = new StringBuilder();
		mSpans = new LinkedList<XMLSpanBuilder.XMLSpanInfo>();
	}

	/**
	 * Appends the contents of the specified string. If the string is null, then
	 * the string "null" is appended.
	 * 
	 * @param str
	 *            the string to append
	 */
	public void append(String str) {
		mBuilder.append(str);
	}

	/**
	 * Appends the contents of the specified string. If the string is null, then
	 * the string "null" is appended.
	 * 
	 * @param str
	 *            the string to append
	 * @param span
	 *            the span to apply to the appended string
	 */
	public void append(String str, Object span) {
		int start = mBuilder.length();
		mBuilder.append(str);
		int end = mBuilder.length();
		setSpan(span, start, end);
	}

	/**
	 * Sets a span info for the whole text. If text is appended, the given span
	 * won't take it into account
	 * 
	 * @param span
	 *            the span object
	 */
	public void setSpan(Object span) {
		setSpan(span, 0, mBuilder.length());
	}

	/**
	 * Sets a span info for a range of text.
	 * 
	 * @param span
	 *            the span object
	 * @param start
	 *            the start index
	 * @param end
	 *            the end index
	 */
	public void setSpan(Object span, int start, int end) {
		XMLSpanInfo info = new XMLSpanInfo();
		info.span = span;
		info.start = start;
		info.end = end;
		mSpans.add(info);
	}

	/**
	 * @return a spannable string from this {@link XMLSpanBuilder} content
	 */
	public SpannableString buildString() {
		SpannableString string;
		string = new SpannableString(mBuilder.toString());

		for (XMLSpanInfo info : mSpans) {
			string.setSpan(info.span, info.start, info.end, 0);
		}

		return string;
	}

	/** the string builder */
	protected StringBuilder mBuilder;
	/** the list of spans */
	protected LinkedList<XMLSpanInfo> mSpans;

}
