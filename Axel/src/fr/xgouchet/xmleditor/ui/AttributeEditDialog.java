package fr.xgouchet.xmleditor.ui;

import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.DialogInterface.OnShowListener;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import fr.xgouchet.xmleditor.R;
import fr.xgouchet.xmleditor.data.xml.XmlAttribute;
import fr.xgouchet.xmleditor.data.xml.XmlNode;
import fr.xgouchet.xmleditor.data.xml.XmlValidator;

/**
 * 
 */
public class AttributeEditDialog implements OnShowListener {

	/**
	 * @param context
	 *            the current application context
	 * @param inflater
	 *            the layout inflater
	 * @param attribute
	 *            the attribute to edit
	 * 
	 */
	public AttributeEditDialog(final Context context,
			final LayoutInflater inflater, final XmlAttribute attribute) {
		mContext = context;
		mAttribute = attribute;

		final AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle("Edit");
		builder.setCancelable(true);

		builder.setView(inflater
				.inflate(R.layout.dialog_attribute, null, false));

		builder.setPositiveButton(R.string.ui_ok, null);
		builder.setNeutralButton(R.string.ui_reset, null);
		builder.setNegativeButton(R.string.ui_cancel, null);

		mDialog = builder.create();
		mDialog.setOnShowListener(this);
	}

	/**
	 * @see android.content.DialogInterface.OnShowListener#onShow(android.content.DialogInterface)
	 */
	public void onShow(final DialogInterface dialog) {

		mDialog.setOnDismissListener(mListener);

		mEditPrefix = (EditText) mDialog.findViewById(R.id.editTextPrefix);
		mEditName = (EditText) mDialog.findViewById(R.id.editTextName);
		mEditValue = (EditText) mDialog.findViewById(R.id.editTextValue);

		mDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(
				new OnClickListener() {
					public void onClick(final View view) {
						if (validateValues()) {
							applyValues();
							mDialog.dismiss();
						}
					}
				});

		mDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(
				new OnClickListener() {
					public void onClick(final View view) {
						resetValues();
					}
				});

		mDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(
				new OnClickListener() {
					public void onClick(final View view) {
						mDialog.dismiss();
					}
				});

		resetValues();
	}

	/**
	 * @param attr
	 *            the attribute to edit
	 */
	public void setAttribute(final XmlAttribute attr) {
		mAttribute = attr;
	}

	/**
	 * Reset the fields to the original value
	 */
	protected void resetValues() {
		if (mAttribute != null) {
			mEditPrefix.setText(mAttribute.getPrefix());
			mEditName.setText(mAttribute.getName());
			mEditValue.setText(mAttribute.getValue());
		} else {
			mEditPrefix.setText("");
			mEditName.setText("");
			mEditValue.setText("");
		}
	}

	/**
	 * validates the values according to W3C
	 * 
	 * @return if the values are all valid
	 */
	protected boolean validateValues() {
		boolean result = true;

		final String prefix = mEditPrefix.getText().toString();
		result = validatePrefix(prefix);

		final String name = mEditName.getText().toString();
		if (!XmlValidator.isValidName(name)) {
			mEditName.setError(mContext.getString(R.string.ui_invalid_syntax));
			result = false;
		}

		if (result) {
			final String fullName = getFullName(prefix, name);
			result = validateFullNameUnicity(fullName);
		}

		final String value = mEditValue.getText().toString();
		if (!XmlValidator.isValidAttributeValue(value)) {
			mEditValue.setError(mContext.getString(R.string.ui_invalid_syntax));
			result = false;
		} else if (prefix.equalsIgnoreCase(XmlValidator.XML_NS)
				&& !XmlValidator.isValidNamespaceURI(value)) {
			mEditValue.setError(mContext
					.getString(R.string.ui_invalid_namespace_uri));
			result = false;
		}

		return result;
	}

	/**
	 * @param prefix
	 *            the prefix
	 * @return if the prefix is valid
	 */
	protected boolean validatePrefix(final String prefix) {
		boolean result;
		if (!TextUtils.isEmpty(prefix)) {
			if (!XmlValidator.isValidName(prefix)) {
				mEditPrefix.setError(mContext
						.getString(R.string.ui_invalid_syntax));
				result = false;
			} else if (!XmlValidator.isValidNamespace(prefix, mNode,
					mSiblingAttrs, true)) {
				mEditPrefix.setError(mContext
						.getString(R.string.ui_invalid_namespace));
				result = false;
			} else {
				result = true;
			}
		} else {
			result = true;
		}
		return result;
	}

	/**
	 * @param fullName
	 * @return if the fullname is unique in this element
	 */
	protected boolean validateFullNameUnicity(final String fullName) {
		boolean result = true;
		for (XmlAttribute attr : mSiblingAttrs) {
			if ((attr != mAttribute) && (attr.getFullName().equals(fullName))) {
				mEditName.setError(mContext
						.getString(R.string.ui_invalid_duplicate));
				result = false;
				break;
			}
		}
		return result;
	}

	/**
	 * @param prefix
	 * @param name
	 * @return either prefix:name or just name if prefix is null or empty
	 */
	protected String getFullName(final String prefix, final String name) {
		final StringBuilder builder = new StringBuilder();
		if (!TextUtils.isEmpty(prefix)) {
			builder.append(prefix);
			builder.append(":");
		}
		builder.append(name);
		return builder.toString();
	}

	/**
	 * Apply the fields values to the attribute
	 */
	protected void applyValues() {
		if (mAttribute == null) {
			mAttribute = new XmlAttribute("", "", "");
		}

		mAttribute.setPrefix(mEditPrefix.getText().toString());
		mAttribute.setName(mEditName.getText().toString());
		mAttribute.setValue(mEditValue.getText().toString());
	}

	/**
	 * @param listener
	 *            the listener
	 */
	public void setOnDismissListener(final OnDismissListener listener) {
		mListener = listener;
	}

	/**
	 * @param node
	 *            the node containing this attribute
	 */
	public void setNode(final XmlNode node) {
		mNode = node;
	}

	/**
	 * @param siblings
	 *            the siblings attributes
	 */
	public void setSiblingsAttribute(final List<XmlAttribute> siblings) {
		mSiblingAttrs = siblings;
	}

	public XmlAttribute getAttribute() {
		return mAttribute;
	}

	/**
	 * Shows the dialog
	 */
	public void show() {
		mDialog.show();
	}

	private XmlNode mNode;
	private XmlAttribute mAttribute;
	private List<XmlAttribute> mSiblingAttrs;

	private EditText mEditPrefix, mEditName, mEditValue;
	private final AlertDialog mDialog;

	private OnDismissListener mListener;
	private final Context mContext;
}
