package fr.xgouchet.xmleditor.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import fr.xgouchet.xmleditor.R;
import fr.xgouchet.xmleditor.data.xml.XmlValidator;
import fr.xgouchet.xmleditor.data.xml.XmlValidator.InvalidRegion;

public class CommentNodeEditorFragment extends ASingleNodeEditorFragment {

	private EditText mEditText;

	// ////////////////////////////////////////////////////////////////////////////////////
	// FRAGMENT LIFECYCLE
	// ////////////////////////////////////////////////////////////////////////////////////

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

	@Override
	protected boolean onValidate() {

		// get the text to validate
		String text = mEditText.getText().toString();

		InvalidRegion region = XmlValidator.getCommentInvalidRegion(text);
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
