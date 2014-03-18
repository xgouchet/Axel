package fr.xgouchet.xmleditor.ui.fragment;

import java.util.LinkedList;
import java.util.List;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import fr.xgouchet.xmleditor.R;
import fr.xgouchet.xmleditor.common.AxelUtils;
import fr.xgouchet.xmleditor.data.xml.XmlAttribute;
import fr.xgouchet.xmleditor.data.xml.XmlData;
import fr.xgouchet.xmleditor.data.xml.XmlValidator;
import fr.xgouchet.xmleditor.ui.adapter.XmlAttributeAdapter;

/**
 * 
 * 
 * @author Xavier Gouchet
 * 
 */
public class ElementNodeEditorFragment extends ASingleNodeEditorFragment {

	private EditText mEditTextPrefix;
	private EditText mEditTextName;
	private List<XmlAttribute> mAttributes;
	private XmlAttributeAdapter mAttributeAdapter;
	private ListView mAttributeListView;

	// ////////////////////////////////////////////////////////////////////////////////////
	// FRAGMENT LIFECYCLE
	// ////////////////////////////////////////////////////////////////////////////////////

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(final LayoutInflater inflater,
			final ViewGroup container, final Bundle savedInstanceState) {

		View root = inflater.inflate(R.layout.fragment_node_element_editor,
				container, false);

		XmlData data = mXmlNode.getContent();

		// Setup Prefix input
		mEditTextPrefix = (EditText) root.findViewById(R.id.edit_text_prefix);
		mEditTextPrefix.setText(data.getPrefix());
		AxelUtils.setupPrefixEditText(mEditTextPrefix, mXmlNode, false);

		// Setup Tag Name input
		mEditTextName = (EditText) root.findViewById(R.id.edit_text_name);
		mEditTextName.setText(data.getName());

		// Get the list of attributes
		mAttributes = new LinkedList<XmlAttribute>();
		for (XmlAttribute attr : data.getAttributes()) {
			mAttributes.add(new XmlAttribute(attr));
		}

		// Setup the list
		mAttributeListView = (ListView) root.findViewById(android.R.id.list);
		mAttributeAdapter = new XmlAttributeAdapter(getActivity(), mAttributes,
				mXmlNode);
		mAttributeListView.setAdapter(mAttributeAdapter);

		return root;
	}

	@Override
	protected boolean onValidate() {

		String name = mEditTextName.getText().toString();
		String prefix = mEditTextPrefix.getText().toString();

		// check Tag name
		if (!XmlValidator.isValidName(name)) {
			mEditTextName.setSelection(0, name.length());
			mEditTextName.setError(getString(R.string.ui_invalid_syntax));
			return false;
		}

		if (!TextUtils.isEmpty(prefix)) {
			// check tag prefix (syntax)
			if (!XmlValidator.isValidName(prefix)) {
				mEditTextPrefix.setSelection(0, prefix.length());
				mEditTextPrefix.setError(getString(R.string.ui_invalid_syntax));
				return false;
			} else if (!XmlValidator.isValidNamespace(prefix, mXmlNode,
					mAttributes, false)) {
				mEditTextPrefix.setSelection(0, prefix.length());
				mEditTextPrefix
						.setError(getString(R.string.ui_invalid_namespace));
				return false;
			}
		}

		return true;
	}

	@Override
	protected boolean onApply() {
		XmlData data = mXmlNode.getContent();
		
		data.setPrefix(mEditTextPrefix.getText().toString());
		data.setName(mEditTextName.getText().toString());
		data.getAttributes().clear();
		data.getAttributes().addAll(mAttributes);
		
		return true;
	}

	@Override
	protected boolean onDiscard() {
		return true;
	}
}
