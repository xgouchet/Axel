package fr.xgouchet.xmleditor.ui.adapter;

import android.content.Context;
import android.text.SpannableString;

/**
 * A utility class to build {@link SpannableString} to represent a
 * {@link TreeNode}
 * 
 * @param <T>
 *            the content type to display
 */
public abstract class TreeNodeStyler<T> {

	/**
	 * @param data
	 *            the data to represent
	 * @param context
	 *            the current application context
	 * @return a {@link SpannableString} to represent a {@link XmlNode} data in
	 *         a TextView
	 */
	public abstract SpannableString getSpannableForContent(T data,
			Context context);
}
