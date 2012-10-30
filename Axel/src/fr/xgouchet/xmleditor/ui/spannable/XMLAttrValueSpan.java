package fr.xgouchet.xmleditor.ui.spannable;

import android.content.Context;
import android.text.style.TextAppearanceSpan;
import fr.xgouchet.xmleditor.R;

/** 
 * 
 */
public class XMLAttrValueSpan extends TextAppearanceSpan {

	/**
	 * @param context
	 *            the current application context
	 */
	public XMLAttrValueSpan(Context context) {
		super(context, R.style.Axel_Xml_Tag_AttributeValue);
	}

}