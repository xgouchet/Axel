package fr.xgouchet.xmleditor;

import java.util.LinkedList;
import java.util.List;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import fr.xgouchet.xmleditor.common.AxelUtils;
import fr.xgouchet.xmleditor.common.Settings;
import fr.xgouchet.xmleditor.data.xml.XmlAttribute;
import fr.xgouchet.xmleditor.data.xml.XmlData;
import fr.xgouchet.xmleditor.data.xml.XmlNode;
import fr.xgouchet.xmleditor.data.xml.XmlValidator;
import fr.xgouchet.xmleditor.ui.adapter.XmlAttributeAdapter;

/**
 * 
 */
public class AxelNodeEditorActivity extends Activity {

	/** XHTML Strict dtd declaration */
	protected static final String DTD_XHTML_STRICT_1_0 = "html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\"";
	/** XHTML Transitional dtd declaration */
	protected static final String DTD_XHTML_TRANSITIONAL_1_0 = "html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\"";
	/** XHTML Frameset dtd declaration */
	protected static final String DTD_XHTML_FRAMESET_1_0 = "html PUBLIC \"-//W3C//DTD XHTML 1.0 Frameset//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-frameset.dtd\"";
	/** XHTML 1.1 dtd declaration */
	protected static final String DTD_XHTML_1_1 = "html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml11.dtd\"";

	/** XML Schema Namespace URI */
	protected static final String XSI_URI = "http://www.w3.org/2001/XMLSchema-instance";

	/**
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_node_editor);

		mNode = ((AxelApplication) getApplication()).getCurrentSelection();
		mData = mNode.getContent();
		setTitle(getString(R.string.title_editor, mData.getTypeName()));

		setupDoneDiscardActionBar();

		setResult(RESULT_CANCELED);
	}

	/**
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();

		switch (mData.getType()) {
		case XmlData.XML_ELEMENT:
			setTagLayout();
			break;
		case XmlData.XML_PROCESSING_INSTRUCTION:
			setProcessingLayout();
			break;
		case XmlData.XML_DOCUMENT_DECLARATION:
			setDocumentDeclarationLayout();
			break;
		case XmlData.XML_COMMENT:
		case XmlData.XML_TEXT:
		case XmlData.XML_CDATA:
		case XmlData.XML_DOCTYPE:
			setTextLayout();
			break;
		default:
			break;
		}
	}

	/**
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {

		if (mData.isElement()) {
			(new MenuInflater(this)).inflate(R.menu.editor_tag_menu, menu);
		} else if (mData.isDoctype()) {
			(new MenuInflater(this)).inflate(R.menu.editor_doctype_menu, menu);
		}

		return true;
	}

	/**
	 * @see android.app.Activity#onPrepareOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onPrepareOptionsMenu(final Menu menu) {
		boolean result;

		if (mData.isElement()) {
			menu.findItem(R.id.menu_attr_default_namespace).setEnabled(
					!hasDefaultNamespace());

			menu.findItem(R.id.menu_attr_xml_schema_local).setEnabled(
					!hasLocalSchema());

			menu.findItem(R.id.menu_attr_xml_schema_public).setEnabled(
					!hasPublicSchema());

			result = true;
		} else {
			result = super.onPrepareOptionsMenu(menu);
		}
		return result;
	}

	private void onApply() {
		if (validateModifications()) {
			applyModifications();
			setResult(RESULT_OK, new Intent());
			finish();
		}
	}

	private void onDiscard() {
		finish();
	}

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	private void setupDoneDiscardActionBar() {
		LayoutInflater inflater;

		if (VERSION.SDK_INT >= VERSION_CODES.ICE_CREAM_SANDWICH) {
			inflater = (LayoutInflater) getActionBar().getThemedContext()
					.getSystemService(LAYOUT_INFLATER_SERVICE);
		} else {
			inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		}

		final View custom = inflater.inflate(R.layout.ab_done_discard, null);

		custom.findViewById(R.id.buttonDiscard).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(final View v) {
						onDiscard();
					}
				});
		custom.findViewById(R.id.buttonDone).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(final View v) {
						onApply();
					}
				});

		// Show the custom action bar view and hide the normal Home icon and
		// title.
		final ActionBar actionBar = getActionBar();
		if (actionBar != null) {
			actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM,
					ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME
							| ActionBar.DISPLAY_SHOW_TITLE);
			actionBar.setCustomView(custom, new ActionBar.LayoutParams(
					ViewGroup.LayoutParams.MATCH_PARENT,
					ViewGroup.LayoutParams.MATCH_PARENT));
		}
	}

	private boolean hasDefaultNamespace() {
		boolean result = false;

		for (XmlAttribute attr : mAttributes) {
			if ("xmlns".equals(attr.getFullName())) {
				result = true;
				break;
			}
		}

		return result;
	}

	/**
	 * 
	 */
	private boolean hasPublicSchema() {
		boolean result = false;
		String xsi = getSchemaInstancePrefix(false);

		if (xsi != null) {
			for (XmlAttribute attr : mAttributes) {
				if ("schemaLocation".equals(attr.getName())
						&& xsi.equals(attr.getPrefix())) {
					result = true;
					break;
				}
			}
		}

		return result;
	}

	/**
	 * 
	 */
	private boolean hasLocalSchema() {
		boolean result = false;
		String xsi = getSchemaInstancePrefix(false);

		if (xsi != null) {
			for (XmlAttribute attr : mAttributes) {
				if ("noNamespaceSchemaLocation".equals(attr.getName())
						&& xsi.equals(attr.getPrefix())) {
					result = true;
					break;
				}
			}
		}

		return result;
	}

	/**
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		boolean result;

		result = true;
		switch (item.getItemId()) {
		case R.id.menu_attr_custom:
			addAttribute();
			break;
		case R.id.menu_attr_namespace:
			addNamespaceAttribute(false);
			break;
		case R.id.menu_attr_default_namespace:
			addNamespaceAttribute(true);
			break;
		case R.id.menu_attr_xml_schema_public:
			addPublicSchema();
			break;
		case R.id.menu_attr_xml_schema_local:
			addLocalSchema();
			break;

		case R.id.menu_doctype_xhtml_strict:
			mEditText.setText(DTD_XHTML_STRICT_1_0);
			break;
		case R.id.menu_doctype_xhtml_transitional:
			mEditText.setText(DTD_XHTML_TRANSITIONAL_1_0);
			break;
		case R.id.menu_doctype_xhtml_frameset:
			mEditText.setText(DTD_XHTML_FRAMESET_1_0);
			break;
		case R.id.menu_doctype_xhtml_1_1:
			mEditText.setText(DTD_XHTML_1_1);
			break;
		default:
			result = super.onOptionsItemSelected(item);
		}

		return result;
	}

	/**
	 * Adds a document declaration layout
	 */
	protected void setDocumentDeclarationLayout() {
		ViewGroup parent = (ViewGroup) findViewById(R.id.frameEditor);
		parent.removeAllViews();
		LayoutInflater.from(this).inflate(R.layout.node_docdecl, parent);

		mEditVersion = (EditText) findViewById(R.id.editTextVersion);
		mEditVersion.setText(mData.getAttributeValue("version"));

		mEditEncoding = (EditText) findViewById(R.id.editTextEncoding);
		mEditEncoding.setText(mData.getAttributeValue("encoding"));

		mCheckStandalone = (CheckBox) findViewById(R.id.checkStandalone);
		mCheckStandalone.setEnabled(((AxelApplication) getApplication())
				.getCurrentDocument().hasDoctype());
	}

	/**
	 * Adds a tag layout
	 */
	protected void setTagLayout() {
		ViewGroup parent = (ViewGroup) findViewById(R.id.frameEditor);
		parent.removeAllViews();
		LayoutInflater.from(this).inflate(R.layout.node_tag, parent);

		mEditPrefix = (EditText) findViewById(R.id.editTextPrefix);
		mEditPrefix.setText(mData.getPrefix());
		AxelUtils.setupPrefixEditText(mEditPrefix, mNode, false);

		mEditName = (EditText) findViewById(R.id.editTextName);
		mEditName.setText(mData.getName());

		mAttributes = new LinkedList<XmlAttribute>();
		for (XmlAttribute attr : mData.getAttributes()) {
			mAttributes.add(new XmlAttribute(attr));
		}

		mAttributeListView = (ListView) findViewById(R.id.listAttributes);
		mAttributeAdapter = new XmlAttributeAdapter(this, mAttributes, mNode);
		mAttributeListView.setAdapter(mAttributeAdapter);
	}

	/**
	 * Adds a PI layout
	 */
	protected void setProcessingLayout() {
		ViewGroup parent = (ViewGroup) findViewById(R.id.frameEditor);
		parent.removeAllViews();
		LayoutInflater.from(this).inflate(R.layout.node_processing, parent);

		mEditName = (EditText) findViewById(R.id.editTextName);
		mEditName.setText(mData.getName());

		mEditText = (EditText) findViewById(R.id.editTextValue);
		mEditText.setText(mData.getText());
	}

	/**
	 * Adds a text layout
	 */
	protected void setTextLayout() {
		ViewGroup parent = (ViewGroup) findViewById(R.id.frameEditor);
		parent.removeAllViews();
		LayoutInflater.from(this).inflate(R.layout.node_text, parent);

		mEditText = (EditText) findViewById(R.id.editTextValue);
		mEditText.setText(mData.getText());
	}

	/**
	 * @return if the current values are valid
	 */
	protected boolean validateModifications() {
		boolean result = true;

		switch (mData.getType()) {
		case XmlData.XML_ELEMENT:
			result = validateElement();
			break;
		case XmlData.XML_PROCESSING_INSTRUCTION:
			result = validateProcessingInstruction();
			break;
		case XmlData.XML_CDATA:
			result = validateCData();
			break;
		case XmlData.XML_COMMENT:
			result = validateComment();
			break;
		case XmlData.XML_DOCUMENT_DECLARATION:
			result = validateDocumentDeclaration();
			break;
		case XmlData.XML_TEXT:
			result = validateText();
			break;
		default:
			break;
		}

		return result;
	}

	/**
	 * @return if the current document declaration is valid
	 */
	protected boolean validateDocumentDeclaration() {
		boolean result;

		result = true;

		if (!XmlValidator.isValidVersionNum(mEditVersion.getText().toString())) {
			mEditVersion.setError(getString(R.string.ui_invalid_syntax));
			result = false;
		} else {
			mEditVersion.setError(null);
		}

		if (!XmlValidator.isValidEncoding(mEditEncoding.getText().toString())) {
			mEditEncoding.setError(getString(R.string.ui_invalid_syntax));
			result = false;
		} else {
			mEditEncoding.setError(null);
		}

		return result;
	}

	/**
	 * @return if the current element is valid
	 */
	protected boolean validateElement() {
		boolean result;

		result = true;

		if (!XmlValidator.isValidName(mEditName.getText().toString())) {
			mEditName.setError(getString(R.string.ui_invalid_syntax));
			result = false;
		}

		String prefix = mEditPrefix.getText().toString();
		if (!TextUtils.isEmpty(prefix)) {
			if (!XmlValidator.isValidName(prefix)) {
				mEditPrefix.setError(getString(R.string.ui_invalid_syntax));
				result = false;
			} else if (!XmlValidator.isValidNamespace(prefix, mNode,
					mAttributes, false)) {
				mEditPrefix.setError(getString(R.string.ui_invalid_namespace));
				result = false;
			}
		}

		return result;
	}

	/**
	 * @return if the current processing instruction is valid
	 */
	protected boolean validateProcessingInstruction() {
		boolean result;

		result = true;

		if (!XmlValidator.isValidPITargetName(mEditName.getText().toString())) {
			mEditName.setError(getString(R.string.ui_invalid_syntax));
			result = false;
		}

		if (!XmlValidator.isValidPIContent(mEditText.getText().toString())) {
			mEditText.setError(getString(R.string.ui_invalid_syntax));
			result = false;
		}

		return result;
	}

	/**
	 * @return if the current comment is valid
	 */
	protected boolean validateCData() {
		boolean result;

		result = true;

		if (!XmlValidator.isValidCDataContent(mEditText.getText().toString())) {
			mEditText.setError(getString(R.string.ui_invalid_syntax));
			result = false;
		}

		return result;
	}

	/**
	 * @return if the current text is valid
	 */
	protected boolean validateText() {
		boolean result;

		result = true;

		String text = mEditText.getText().toString();
		if (Settings.sEscapeTextContent) {
			text = XmlValidator.escapeTextContent(text);

		}

		if (!XmlValidator.isValidText(text)) {
			mEditText.setError(getString(R.string.ui_invalid_syntax));
			result = false;
		}

		if (result) {
			mEditText.setText(text);
		}

		return result;
	}

	/**
	 * @return if the current comment is valid
	 */
	protected boolean validateComment() {
		boolean result;

		result = true;

		if (!XmlValidator.isValidComment(mEditText.getText().toString())) {
			mEditText.setError(getString(R.string.ui_invalid_syntax));
			result = false;
		}

		return result;
	}

	/**
	 * apply the modifications before quitting the activity
	 */
	protected void applyModifications() {
		switch (mData.getType()) {
		case XmlData.XML_ELEMENT:
			mData.setPrefix(mEditPrefix.getText().toString());
			mData.setName(mEditName.getText().toString());
			mData.getAttributes().clear();
			mData.getAttributes().addAll(mAttributes);
			break;
		case XmlData.XML_PROCESSING_INSTRUCTION:
			mData.setName(mEditName.getText().toString());
			mData.setText(mEditText.getText().toString());
			break;
		case XmlData.XML_DOCUMENT_DECLARATION:
			mData.setAttribute("version", mEditVersion.getText().toString());
			mData.setAttribute("encoding", mEditEncoding.getText().toString());
			if (mCheckStandalone.isEnabled()) {
				mData.setAttribute("standalone",
						mCheckStandalone.isChecked() ? "yes" : "no");
			}
			break;
		case XmlData.XML_COMMENT:
		case XmlData.XML_TEXT:
		case XmlData.XML_CDATA:
		case XmlData.XML_DOCTYPE:
			mData.setText(mEditText.getText().toString());
			break;
		default:
			break;
		}
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

	/** The node to edit */
	protected XmlNode mNode;
	/** the node's data */
	protected XmlData mData;
	/** a mirror list of attributes */
	protected List<XmlAttribute> mAttributes;

	private EditText mEditText, mEditPrefix, mEditName, mEditVersion,
			mEditEncoding;
	private CheckBox mCheckStandalone;
	private ListView mAttributeListView;
	private XmlAttributeAdapter mAttributeAdapter;

}
