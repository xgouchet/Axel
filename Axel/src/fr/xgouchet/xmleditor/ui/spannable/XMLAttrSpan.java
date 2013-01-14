package fr.xgouchet.xmleditor.ui.spannable;

import android.content.Context;
import android.text.style.TextAppearanceSpan;
import fr.xgouchet.xmleditor.R;

/**
 *
 */
public class XMLAttrSpan extends TextAppearanceSpan {

	/**
	 * @param context
	 *            the current application context
	 */
	public XMLAttrSpan(final Context context) {
		super(context, R.style.Axel_Xml_Tag_Attribute);
	}

}
