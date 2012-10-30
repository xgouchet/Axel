package fr.xgouchet.xmleditor.ui.adapter;

import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.TextView.BufferType;
import fr.xgouchet.xmleditor.R;
import fr.xgouchet.xmleditor.data.xml.XmlAttribute;
import fr.xgouchet.xmleditor.data.xml.XmlNode;
import fr.xgouchet.xmleditor.data.xml.XmlNodeStyler;
import fr.xgouchet.xmleditor.ui.AttributeEditDialog;

/**
 * 
 */
public class XmlAttributeAdapter extends ArrayAdapter<XmlAttribute> {

	/**
	 * @param context
	 *            the current application context
	 * @param list
	 *            the list to adapt
	 * @param node
	 *            the node containing those attributes
	 */
	public XmlAttributeAdapter(Context context, List<XmlAttribute> list,
			XmlNode node) {
		super(context, R.layout.item_attribute, list);
		mInflater = (LayoutInflater) getContext().getSystemService(
				Context.LAYOUT_INFLATER_SERVICE);
		mList = list;
		mNode = node;
	}

	/**
	 * @see android.widget.ArrayAdapter#getView(int, android.view.View,
	 *      android.view.ViewGroup)
	 */
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;

		if (v == null) {
			v = mInflater.inflate(R.layout.item_attribute, parent, false);
		}

		final XmlAttribute attr = getItem(position);

		if (attr != null) {
			TextView text;
			text = (TextView) v.findViewById(R.id.textAttribute);
			text.setText(XmlNodeStyler.getAttributeSpan(attr, getContext()),
					BufferType.SPANNABLE);

			text.setHorizontallyScrolling(true);
			text.setMovementMethod(new ScrollingMovementMethod());
			text.scrollTo(0, 0);

			v.findViewById(R.id.buttonDelete).setOnClickListener(
					new OnClickListener() {
						public void onClick(View v) {
							promptDeleteAttribute(attr);
						}
					});

			v.findViewById(R.id.buttonEdit).setOnClickListener(
					new OnClickListener() {
						public void onClick(View v) {
							promptEditAttribute(attr);
						}
					});
		}

		return v;
	}

	/**
	 * @param attr
	 *            the attribute to delete
	 */
	protected void promptDeleteAttribute(final XmlAttribute attr) {
		final AlertDialog.Builder builder;

		builder = new AlertDialog.Builder(getContext());
		builder.setTitle(R.string.ui_delete);
		builder.setCancelable(true);
		builder.setMessage("Are you sure you want to delete this attribute ?");

		builder.setPositiveButton(R.string.ui_delete,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						remove(attr);
						notifyDataSetChanged();
					}
				});
		builder.setNegativeButton(R.string.ui_cancel,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
					}
				});

		builder.create().show();
	}

	/**
	 * @param attr
	 *            the attribute to edit
	 */
	protected void promptEditAttribute(final XmlAttribute attr) {
		AttributeEditDialog dlg;

		dlg = new AttributeEditDialog(getContext(), mInflater, attr);
		dlg.setNode(mNode);
		dlg.setSiblingsAttribute(mList);

		dlg.setOnDismissListener(new OnDismissListener() {
			public void onDismiss(DialogInterface dialog) {
				notifyDataSetChanged();
			}
		});
		dlg.show();
	}

	/**
	 * Edits the given attribute
	 * 
	 * @param attr
	 *            the attribute to edit
	 */
	public void editAttribute(XmlAttribute attr) {
		if (mList.contains(attr)) {
			promptEditAttribute(attr);
		}
	}

	private LayoutInflater mInflater;
	private List<XmlAttribute> mList;
	private XmlNode mNode;

}
