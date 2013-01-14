package fr.xgouchet.xmleditor.data.xml;

import java.util.List;

import android.content.Context;
import android.text.SpannableString;
import android.text.TextUtils;
import fr.xgouchet.xmleditor.common.Settings;
import fr.xgouchet.xmleditor.ui.adapter.AbstractTreeNodeStyler;
import fr.xgouchet.xmleditor.ui.spannable.XMLAttrSpan;
import fr.xgouchet.xmleditor.ui.spannable.XMLAttrValueSpan;
import fr.xgouchet.xmleditor.ui.spannable.XMLCDataSpan;
import fr.xgouchet.xmleditor.ui.spannable.XMLCommentSpan;
import fr.xgouchet.xmleditor.ui.spannable.XMLDocSpan;
import fr.xgouchet.xmleditor.ui.spannable.XMLDoctypeSpan;
import fr.xgouchet.xmleditor.ui.spannable.XMLProcSpan;
import fr.xgouchet.xmleditor.ui.spannable.XMLSpanBuilder;
import fr.xgouchet.xmleditor.ui.spannable.XMLTagSpan;
import fr.xgouchet.xmleditor.ui.spannable.XMLTextSpan;

/**
 * A utility class to build {@link SpannableString} to represent an
 * {@link XmlNode}
 */
public class XmlNodeStyler extends AbstractTreeNodeStyler<XmlData> {

	/**
	 * @param attr
	 *            the attribute to style
	 * @param context
	 *            the current application context
	 * @return the attribute styled
	 */
	public static SpannableString getAttributeSpan(XmlAttribute attr,
			Context context) {
		XMLSpanBuilder builder;
		builder = new XMLSpanBuilder();

		(new XmlNodeStyler()).buildAttributeString(builder, attr, context);

		return builder.buildString();
	}

	/**
	 * @see fr.xgouchet.xmleditor.ui.adapter.TreeNodeStyler#getSpannableForContent(java.lang.Object,
	 *      android.content.Context)
	 */
	public SpannableString getSpannableForContent(XmlData data, Context context) {
		XMLSpanBuilder builder;
		builder = new XMLSpanBuilder();

		switch (data.getType()) {
		case XmlData.XML_TEXT:
			builder.append(data.getText());
			builder.setSpan(new XMLTextSpan(context));
			break;
		case XmlData.XML_COMMENT:
			builder.append("<!--");
			builder.append(data.getText());
			builder.append("-->");
			builder.setSpan(new XMLCommentSpan(context));
			break;
		case XmlData.XML_ELEMENT:
			buildElementString(builder, data, context);
			break;
		case XmlData.XML_CDATA:
			builder.append("<![CDATA[", new XMLCDataSpan(context));
			builder.append(data.getText(), new XMLCDataSpan(context));
			builder.append("]]>", new XMLCDataSpan(context));
			break;
		case XmlData.XML_DOCUMENT:
			builder.append("[Document]");
			builder.setSpan(new XMLDocSpan(context));
			break;
		case XmlData.XML_DOCUMENT_DECLARATION:
			buildDeclarationString(builder, data, context);
			break;
		case XmlData.XML_PROCESSING_INSTRUCTION:
			builder.append("<?", new XMLProcSpan(context));
			builder.append(data.getName(), new XMLProcSpan(context));
			builder.append(" ");
			builder.append(data.getText(), new XMLProcSpan(context));
			builder.append("?>", new XMLProcSpan(context));
			break;
		case XmlData.XML_DOCTYPE:
			builder.append("<!", new XMLProcSpan(context));
			builder.append("DOCTYPE ", new XMLDoctypeSpan(context));
			builder.append(data.getText(), new XMLProcSpan(context));
			builder.append(">", new XMLProcSpan(context));
			break;
		default:
			break;
		}

		return builder.buildString();
	}

	/**
	 * Fills an {@link XMLSpanBuilder} with the content to represent an Document
	 * Declaration Xml node
	 * 
	 * @param builder
	 *            the builder to use
	 * @param data
	 *            the data to represent
	 * @param context
	 *            the current application context
	 */
	protected void buildDeclarationString(XMLSpanBuilder builder, XmlData data,
			Context context) {
		builder.append("<?", new XMLProcSpan(context));
		builder.append(data.getName(), new XMLProcSpan(context));

		List<XmlAttribute> attrs = data.getAttributes();
		for (XmlAttribute attr : attrs) {
			builder.append(" ");
			buildAttributeString(builder, attr, context);
		}

		builder.append("?>", new XMLProcSpan(context));
	}

	/**
	 * Fills an {@link XMLSpanBuilder} with the content to represent an Element
	 * Xml node
	 * 
	 * @param builder
	 *            the builder to use
	 * @param data
	 *            the data to represent
	 * @param context
	 *            the current application context
	 */
	protected void buildElementString(XMLSpanBuilder builder, XmlData data,
			Context context) {
		builder.append("<", new XMLTagSpan(context));
		if (!TextUtils.isEmpty(data.getPrefix())) {
			builder.append(data.getPrefix() + ":", new XMLTagSpan(context));
		}
		builder.append(data.getName(), new XMLTagSpan(context));

		List<XmlAttribute> attrs = data.getAttributes();
		for (XmlAttribute attr : attrs) {
			if (!Settings.sShowAttrInline) {
				builder.append("\n    ");
			} else {
				builder.append(" ");
			}
			buildAttributeString(builder, attr, context);
		}

		if (data.hasFlag(XmlData.FLAG_EMPTY)) {
			builder.append("/", new XMLTagSpan(context));
		}
		builder.append(">", new XMLTagSpan(context));
	}

	/**
	 * Fills an {@link XMLSpanBuilder} with the content to represent an
	 * {@link XmlAttribute}
	 * 
	 * @param builder
	 *            the builder to use
	 * @param attr
	 *            the attribute to represent
	 * @param context
	 *            the current application context
	 */
	protected void buildAttributeString(XMLSpanBuilder builder,
			XmlAttribute attr, Context context) {
		if (!TextUtils.isEmpty(attr.getPrefix())) {
			builder.append(attr.getPrefix() + ":", new XMLAttrSpan(context));
		}
		builder.append(attr.getName(), new XMLAttrSpan(context));
		builder.append("=");
		builder.append("\"", new XMLAttrValueSpan(context));
		builder.append(attr.getValue(), new XMLAttrValueSpan(context));
		builder.append("\"", new XMLAttrValueSpan(context));
	}

}
