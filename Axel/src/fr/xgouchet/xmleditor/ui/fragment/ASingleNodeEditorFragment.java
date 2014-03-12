package fr.xgouchet.xmleditor.ui.fragment;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import fr.xgouchet.xmleditor.R;

/**
 * Base class for a fragment editing a single XML Node with a validation system
 * 
 * @author Xavier Gouchet
 * 
 */
public abstract class ASingleNodeEditorFragment extends ANodeEditorFragment {

	@Override
	public void onAttach(final Activity activity) {
		super.onAttach(activity);

		// show the done / discard pattern
		showDoneDiscardButtons();
	}

	// ////////////////////////////////////////////////////////////////////////////////////
	// UI
	// ////////////////////////////////////////////////////////////////////////////////////

	protected void showDoneDiscardButtons() {
		LayoutInflater inflater;

		final ActionBar actionBar = getActivity().getActionBar();

		inflater = (LayoutInflater) getActivity().getActionBar()
				.getThemedContext()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		final View custom = inflater.inflate(R.layout.ab_done_discard, null);

		custom.findViewById(R.id.buttonDiscard).setOnClickListener(
				mDoneDiscardClickListener);
		custom.findViewById(R.id.buttonDone).setOnClickListener(
				mDoneDiscardClickListener);

		// Show the custom action bar views
		// hide the normal Home icon and title.

		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM,
				ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME
						| ActionBar.DISPLAY_SHOW_TITLE);
		actionBar.setCustomView(custom, new ActionBar.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT));

	}

	protected void hideDoneDiscardButton() {
		getActivity().getActionBar().setDisplayOptions(
				ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE,
				ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME
						| ActionBar.DISPLAY_SHOW_TITLE);
	}

	protected void hideSoftKeyboard() {
		InputMethodManager imm = (InputMethodManager) getActivity()
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		if (imm != null) {
			imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
		}
	}

	private OnClickListener mDoneDiscardClickListener = new OnClickListener() {

		@Override
		public void onClick(final View v) {
			boolean close = false;

			switch (v.getId()) {
			case R.id.buttonDone:
				if (onValidate()) {
					close = onApply();
				}
				break;
			case R.id.buttonDiscard:
				close = onDiscard();
				break;
			default:
				break;
			}

			if (close) {
				getFragmentManager().popBackStack();
				hideDoneDiscardButton();
				hideSoftKeyboard();
			}
		}
	};

	/**
	 * Called to validate the current data represented by this fragment. This is
	 * called on the UI thread so implementation can use this to provide
	 * information to the user as to why the data is invalid
	 * 
	 * @return true if the data is valid according to XML specifications.
	 */
	protected abstract boolean onValidate();

	/**
	 * Called when the user presses the "Done" button and the data is validated
	 * 
	 * @return true if the fragment should be closed, false to keep it open (eg
	 *         : when an input is invalid for instance)
	 */
	protected abstract boolean onApply();

	/**
	 * Called when the user presses the "Discard" button
	 * 
	 * @return true if the fragment should be closed, false to keep it open (eg
	 *         : when an input is invalid for instance)
	 */
	protected abstract boolean onDiscard();
}
