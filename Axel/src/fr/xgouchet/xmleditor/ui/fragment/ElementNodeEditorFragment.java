package fr.xgouchet.xmleditor.ui.fragment;

import java.util.LinkedList;
import java.util.List;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import fr.xgouchet.xmleditor.R;
import fr.xgouchet.xmleditor.common.AxelUtils;
import fr.xgouchet.xmleditor.common.Settings;
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

	/** XML Schema Namespace URI */
	protected static final String XSI_URI = "http://www.w3.org/2001/XMLSchema-instance";

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

	// ////////////////////////////////////////////////////////////////////////////////////
	// OPTIONS MENU
	// ////////////////////////////////////////////////////////////////////////////////////

	@Override
	public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);

		inflater.inflate(R.menu.editor_element, menu);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {

		boolean res = true;

		switch (item.getItemId()) {
		case R.id.action_attr_custom:
			addAttribute();
			break;
		case R.id.action_attr_namespace:
			addNamespaceAttribute(false);
			break;
		case R.id.action_attr_default_namespace:
			addNamespaceAttribute(true);
			break;
		case R.id.action_attr_xml_schema_public:
			addPublicSchema();
			break;
		case R.id.action_attr_xml_schema_local:
			addLocalSchema();
			break;
		default:
			res = super.onOptionsItemSelected(item);
			break;
		}

		return res;
	}

	/**
	 * Adds an attribute to the current list
	 */
	protected void addAttribute() {
		XmlAttribute attr;

		attr = new XmlAttribute(null, getAvailableAttributeName(null, "name"),
				"value");
		mAttributes.add(attr);
		mAttributeAdapter.notifyDataSetChanged();

		if (Settings.sEditOnCreate) {
			mAttributeAdapter.editAttribute(attr);
		}
	}

	/**
	 * Adds a namespace attribute to the current list
	 * 
	 * @param def
	 *            make the namespace the default one ?
	 */
	protected void addNamespaceAttribute(final boolean def) {
		XmlAttribute attr;

		if (def) {
			attr = new XmlAttribute("xmlns", "uri");
		} else {
			attr = new XmlAttribute("xmlns", getAvailableAttributeName("xmlns",
					"prefix"), "uri");
		}
		mAttributes.add(attr);
		mAttributeAdapter.notifyDataSetChanged();

		if (Settings.sEditOnCreate) {
			mAttributeAdapter.editAttribute(attr);
		}
	}

	/**
	 * Adds a XSD Public schema attribute to the current list
	 */
	protected void addPublicSchema() {
		String xsi = getSchemaInstancePrefix(true);

		XmlAttribute attr;
		attr = new XmlAttribute(
				xsi,
				"schemaLocation",
				"http://www.sample.org/schema/version http://www.sample.org/schema/version/schema.xsd");
		mAttributes.add(attr);
		mAttributeAdapter.notifyDataSetChanged();

		if (Settings.sEditOnCreate) {
			mAttributeAdapter.editAttribute(attr);
		}
	}

	/**
	 * Adds a XSD schema attribute to the current list
	 */
	protected void addLocalSchema() {
		String xsi = getSchemaInstancePrefix(true);

		XmlAttribute attr;
		attr = new XmlAttribute(xsi, "noNamespaceSchemaLocation", "schema.xsd");
		mAttributes.add(attr);
		mAttributeAdapter.notifyDataSetChanged();

		if (Settings.sEditOnCreate) {
			mAttributeAdapter.editAttribute(attr);
		}
	}

	/**
	 * @param add
	 *            add the attribute for XSI namespace if needed
	 * @return the prefix for the Xml Schema Instance namespace or null
	 */
	protected String getSchemaInstancePrefix(final boolean add) {
		String prefix = null;

		for (XmlAttribute attr : mAttributes) {
			if (XSI_URI.equals(attr.getValue())
					&& ("xmlns".equals(attr.getPrefix()))) {
				prefix = attr.getName();
			}
		}

		if (TextUtils.isEmpty(prefix) && add) {
			XmlAttribute attr;

			attr = new XmlAttribute("xmlns", "xsi",
					"http://www.w3.org/2001/XMLSchema-instance");
			mAttributes.add(attr);
			prefix = "xsi";
		}

		return prefix;
	}

	/**
	 * @param prefix
	 *            the prefix to use
	 * @param base
	 *            the desired base name
	 * @return the available name to use for a new attribute
	 */
	protected String getAvailableAttributeName(final String prefix,
			final String base) {
		String name, pr;
		int count = 1;
		boolean unique;
		pr = (prefix == null) ? "" : prefix;
		name = base;

		do {
			unique = true;
			for (XmlAttribute attr : mAttributes) {
				if (TextUtils.equals(pr, attr.getPrefix())
						&& name.equals(attr.getName())) {
					unique = false;
					break;
				}
			}

			if (!unique) {
				name = base + (++count);
			}
		} while (!unique);

		return name;
	}

	// ////////////////////////////////////////////////////////////////////////////////////
	// NODE EDITING
	// ////////////////////////////////////////////////////////////////////////////////////

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
