package fr.xgouchet.xmleditor.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import fr.xgouchet.xmleditor.R;
import fr.xgouchet.xmleditor.data.xml.XmlNode;
import fr.xgouchet.xmleditor.data.xml.XmlValidator;

public class DocumentDeclarationNodeEditorFragment extends
		ASingleNodeEditorFragment {

	private EditText mEditTextVersion, mEditTextEncoding;
	private CheckBox mCheckBoxStandalone;

	// ////////////////////////////////////////////////////////////////////////////////////
	// FRAGMENT LIFECYCLE
	// ////////////////////////////////////////////////////////////////////////////////////

	@Override
	public View onCreateView(final LayoutInflater inflater,
			final ViewGroup container, final Bundle savedInstanceState) {

		View root = inflater.inflate(
				R.layout.fragment_node_document_declaration_editor, container,
				false);

		// Setup Text input
		mEditTextVersion = (EditText) root.findViewById(R.id.edit_text_version);
		mEditTextVersion.setText(mXmlNode.getContent().getAttributeValue(
				"version"));

		// Setup Text input
		mEditTextEncoding = (EditText) root
				.findViewById(R.id.edit_text_encoding);
		mEditTextEncoding.setText(mXmlNode.getContent().getAttributeValue(
				"encoding"));

		// Setup CheckBox
		mCheckBoxStandalone = (CheckBox) root
				.findViewById(R.id.check_standalone);
		mCheckBoxStandalone.setEnabled(((XmlNode) mXmlNode.getParent())
				.hasDoctype());
		if (mCheckBoxStandalone.isEnabled()) {
			String value = mXmlNode.getContent()
					.getAttributeValue("standalone");
			if ("true".equalsIgnoreCase(value)) {
				mCheckBoxStandalone.setChecked(true);
			}
		}

		return root;
	}

	@Override
	protected boolean onValidate() {

		String version = mEditTextVersion.getText().toString();
		String encoding = mEditTextEncoding.getText().toString();

		// check version
		if (!XmlValidator.isValidVersionNumber(version)) {
			mEditTextVersion.setSelection(0, version.length());
			mEditTextVersion.setError(getString(R.string.ui_invalid_syntax));
			return false;
		}

		// check encoding
		if (!XmlValidator.isValidEncoding(encoding)) {
			mEditTextEncoding.setSelection(0, encoding.length());
			mEditTextEncoding.setError(getString(R.string.ui_invalid_syntax));
			return false;
		}

		return true;
	}

	@Override
	protected boolean onApply() {
		mXmlNode.getContent().setAttribute("version",
				mEditTextVersion.getText().toString());
		mXmlNode.getContent().setAttribute("encoding",
				mEditTextEncoding.getText().toString());
		if (mCheckBoxStandalone.isEnabled()) {
			mXmlNode.getContent().setAttribute("standalone",
					mCheckBoxStandalone.isChecked() ? "yes" : "no");
		}
		return true;
	}

	@Override
	protected boolean onDiscard() {
		return true;
	}

}
