package fr.xgouchet.xmleditor.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import fr.xgouchet.xmleditor.R;
import fr.xgouchet.xmleditor.data.xml.XmlValidator;
import fr.xgouchet.xmleditor.data.xml.XmlValidator.InvalidRegion;

/**
 * TODO add a generic known Doctype map, with more doctypes
 */
public class DoctypeNodeEditorFragment extends ASingleNodeEditorFragment {

	/** XHTML Strict dtd declaration */
	protected static final String DTD_XHTML_STRICT_1_0 = "html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\"";
	/** XHTML Transitional dtd declaration */
	protected static final String DTD_XHTML_TRANSITIONAL_1_0 = "html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\"";
	/** XHTML Frameset dtd declaration */
	protected static final String DTD_XHTML_FRAMESET_1_0 = "html PUBLIC \"-//W3C//DTD XHTML 1.0 Frameset//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-frameset.dtd\"";
	/** XHTML 1.1 dtd declaration */
	protected static final String DTD_XHTML_1_1 = "html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml11.dtd\"";

	private EditText mEditText;

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

		View root = inflater.inflate(R.layout.fragment_node_text_editor,
				container, false);

		// Setup Text input
		mEditText = (EditText) root.findViewById(R.id.edit_text_content);
		mEditText.setText(mXmlNode.getContent().getText());

		return root;
	}

	// ////////////////////////////////////////////////////////////////////////////////////
	// OPTIONS MENU
	// ////////////////////////////////////////////////////////////////////////////////////

	@Override
	public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);

		inflater.inflate(R.menu.editor_doctype, menu);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {

		boolean res = true;

		switch (item.getItemId()) {
		case R.id.action_doctype_xhtml_strict:
			setDoctype(DTD_XHTML_STRICT_1_0);
			break;
		case R.id.action_doctype_xhtml_transitional:
			setDoctype(DTD_XHTML_TRANSITIONAL_1_0);
			break;
		case R.id.action_doctype_xhtml_frameset:
			setDoctype(DTD_XHTML_FRAMESET_1_0);
			break;
		case R.id.action_doctype_xhtml_1_1:
			setDoctype(DTD_XHTML_1_1);
			break;
		default:
			res = super.onOptionsItemSelected(item);
			break;
		}

		return res;
	}

	private void setDoctype(final String doctype) {
		mEditText.setText(doctype);
	}

	// ////////////////////////////////////////////////////////////////////////////////////
	// NODE EDITING
	// ////////////////////////////////////////////////////////////////////////////////////

	@Override
	protected boolean onValidate() {

		// get the text to validate
		String text = mEditText.getText().toString();

		InvalidRegion region = XmlValidator.getDoctypeInvalidRegion(text);
		if (region != null) {
			mEditText.setSelection(region.start, region.end);

			mEditText.setError(getString(R.string.ui_invalid_syntax));
			return false;
		}

		return true;
	}

	@Override
	protected boolean onApply() {
		mXmlNode.getContent().setText(mEditText.getText().toString());
		return true;
	}

	@Override
	protected boolean onDiscard() {
		return true;
	}

}
