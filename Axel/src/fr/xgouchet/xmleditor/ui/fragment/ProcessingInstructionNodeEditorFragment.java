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

public class ProcessingInstructionNodeEditorFragment extends
		ASingleNodeEditorFragment {

	private EditText mEditTextTarget, mEditTextInstructions;

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

	// ////////////////////////////////////////////////////////////////////////////////////
	// OPTIONS MENU
	// ////////////////////////////////////////////////////////////////////////////////////

	@Override
	public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);

		inflater.inflate(R.menu.editor_processing, menu);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {

		boolean res = true;

		switch (item.getItemId()) {
		case R.id.action_pi_xsl_link:
			setXslLink();
			break;
		default:
			res = super.onOptionsItemSelected(item);
			break;
		}

		return res;
	}

	private void setXslLink() {
		mEditTextTarget.setText("xml-stylesheet");
		mEditTextInstructions
				.setText("type=\"text/xsl\" href=\"stylesheet.xsl\"");
	}

	// ////////////////////////////////////////////////////////////////////////////////////
	// NODE EDITING
	// ////////////////////////////////////////////////////////////////////////////////////

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
