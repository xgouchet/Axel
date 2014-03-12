package fr.xgouchet.xmleditor.ui.fragment;

import java.util.LinkedList;
import java.util.List;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import fr.xgouchet.xmleditor.R;
import fr.xgouchet.xmleditor.common.AxelUtils;
import fr.xgouchet.xmleditor.data.xml.XmlAttribute;
import fr.xgouchet.xmleditor.data.xml.XmlData;
import fr.xgouchet.xmleditor.ui.adapter.XmlAttributeAdapter;

/**
 * 
 * 
 * @author Xavier Gouchet
 * 
 */
public class ElementNodeEditorFragment extends ASingleNodeEditorFragment {

	private EditText mEditPrefix;
	private EditText mEditName;
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
		mEditPrefix = (EditText) root.findViewById(R.id.edit_text_prefix);
		mEditPrefix.setText(data.getPrefix());
		AxelUtils.setupPrefixEditText(mEditPrefix, mXmlNode, false);

		// Setup Tag Name input
		mEditName = (EditText) root.findViewById(R.id.edit_text_name);
		mEditName.setText(data.getName());

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
		return false;
	}

	@Override
	protected boolean onApply() {
		return false;
	}

	@Override
	protected boolean onDiscard() {
		return false;
	}
}
