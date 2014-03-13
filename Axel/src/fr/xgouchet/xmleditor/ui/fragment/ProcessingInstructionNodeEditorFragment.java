package fr.xgouchet.xmleditor.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import fr.xgouchet.xmleditor.R;
import fr.xgouchet.xmleditor.data.xml.XmlValidator;
import fr.xgouchet.xmleditor.data.xml.XmlValidator.InvalidRegion;

public class ProcessingInstructionNodeEditorFragment extends
		ASingleNodeEditorFragment {

	private EditText mEditTextTarget, mEditTextInstructions;

	// ////////////////////////////////////////////////////////////////////////////////////
	// FRAGMENT LIFECYCLE
	// ////////////////////////////////////////////////////////////////////////////////////

	@Override
	public View onCreateView(final LayoutInflater inflater,
			final ViewGroup container, final Bundle savedInstanceState) {

		View root = inflater.inflate(
				R.layout.fragment_node_processing_instruction_editor,
				container, false);

		// Setup Text input
		mEditTextTarget = (EditText) root.findViewById(R.id.edit_text_target);
		mEditTextTarget.setText(mXmlNode.getContent().getName());

		// Setup Text input
		mEditTextInstructions = (EditText) root
				.findViewById(R.id.edit_text_instructions);
		mEditTextInstructions.setText(mXmlNode.getContent().getText());

		return root;
	}

	@Override
	protected boolean onValidate() {

		InvalidRegion region;
		String target = mEditTextTarget.getText().toString();
		String content = mEditTextInstructions.getText().toString();

		// check PI Target
		region = XmlValidator.getPITargetInvalidRegion(target);
		if (region != null) {
			mEditTextTarget.setSelection(region.start, region.end);

			mEditTextTarget.setError(getString(R.string.ui_invalid_syntax));
			return false;
		}

		// check PI Instructions
		region = XmlValidator.getPIContentInvalidRegion(content);
		if (region != null) {
			mEditTextInstructions.setSelection(region.start, region.end);
			mEditTextInstructions
					.setError(getString(R.string.ui_invalid_syntax));
			return false;
		}

		return true;
	}

	@Override
	protected boolean onApply() {
		mXmlNode.getContent().setName(mEditTextTarget.getText().toString());
		mXmlNode.getContent().setText(
				mEditTextInstructions.getText().toString());
		return true;
	}

	@Override
	protected boolean onDiscard() {
		return true;
	}

}
