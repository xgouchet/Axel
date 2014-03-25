package fr.xgouchet.xmleditor;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.builder.Builders.Any.B;

import fr.xgouchet.xmleditor.network.ValidateFileTask;
import fr.xgouchet.xmleditor.network.ValidateFileTask.ValidationListener;
import fr.xgouchet.xmleditor.parser.validator.ValidatorEntry;
import fr.xgouchet.xmleditor.parser.validator.ValidatorError;
import fr.xgouchet.xmleditor.parser.validator.ValidatorParser;
import fr.xgouchet.xmleditor.parser.validator.ValidatorResult;
import fr.xgouchet.xmleditor.ui.adapter.ValidatorEntryAdapter;

/**
 * Idependant activity checking for Errors in XML files
 * 
 * @author xgouchet
 * 
 */
public class AxelValidatorActivity extends Activity implements
		ValidationListener, FutureCallback<String> {

	private File mCurrentFile;
	private ValidateFileTask mValidateTask;

	private ExpandableListView mList;
	private TextView mMessage;

	/**
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_PROGRESS);
		setContentView(R.layout.layout_validator);

		mList = (ExpandableListView) findViewById(android.R.id.list);

		mMessage = (TextView) findViewById(android.R.id.message);

		mMessage.setText(R.string.ui_validating);
		mMessage.setCompoundDrawablesWithIntrinsicBounds(0,
				R.drawable.ic_loading, 0, 0);

		mList.setEmptyView(mMessage);

		setProgressBarIndeterminate(true);
		setProgressBarVisibility(true);

		// Activity
		readIntent();

	}

	/**
	 * Read the intent used to start this activity (open the xml file)
	 */
	@SuppressWarnings("deprecation")
	private void readIntent() {
		Intent intent;
		String action;

		intent = getIntent();
		if (intent == null) {
			// TODO toast error
			finish();
			return;
		}

		action = intent.getAction();
		if (action == null) {
			// TODO toast error
			finish();
			return;
		}

		if ((action.equals(Intent.ACTION_VIEW))
				|| (action.equals(Intent.ACTION_EDIT))) {
			try {
				mCurrentFile = new File(new URI(intent.getData().toString()));
				setTitle(getString(R.string.title_validator,
						mCurrentFile.getName()));
				validateCurrentFile();
			} catch (URISyntaxException e) {
//				Toaster.showToast(this, R.string.toast_intent_invalid_uri, true);
				finish();
			} catch (IllegalArgumentException e) {
//				Toaster.showToast(this, R.string.toast_intent_illegal, true);
				finish();
			}
		}
	}

	/**
	 * 
	 */
	private void validateCurrentFile() {

		// TODO rewrite all the network thing to get rid of 3 heavy libraries
		// mValidateTask = new ValidateFileTask();
		// mValidateTask.setListener(this);
		// mValidateTask.execute(mCurrentFile);

		B requestBuilder = Ion.with(this, ValidateFileTask.VALIDATOR_URL);
		requestBuilder.setMultipartParameter("output", "soap12");
		requestBuilder.setMultipartParameter("debug", "1");
		requestBuilder.setMultipartFile("uploaded_file", mCurrentFile);

		requestBuilder.asString().setCallback(this);
	}

	@Override
	public void onCompleted(final Exception e, final String response) {
		if (e == null) {
			InputStream input = new ByteArrayInputStream(response.getBytes());
			ValidatorResult result = null;
			try {
				result = ValidatorParser.parseValidatorResponse(input);
				displayResult(result);
			} catch (Exception e1) {
				Log.e("RESPONSE", "PARSE ERROR", e1);
			}

		} else {
			mMessage.setText(e.getMessage());
			mMessage.setCompoundDrawablesWithIntrinsicBounds(0,
					R.drawable.ic_error, 0, 0);

			Log.e("RESPONSE", "REQUEST ERROR", e);
		}
	}

	@Override
	public void onValidationRequestProgress(final int progress) {
		setProgressBarIndeterminate(false);
		setProgress(progress);
	}

	/**
	 * Displays the result
	 * 
	 * @param result
	 */
	private void displayResult(final ValidatorResult result) {
		setProgressBarVisibility(false);

		// Filter unwanted entries
		List<ValidatorEntry> entries = result.getEntries();
		List<ValidatorEntry> unwanted = new LinkedList<ValidatorEntry>();
		for (ValidatorEntry entry : entries) {
			String id = entry.getMessageId();
			if (ValidatorError.NO_DOCTYPE.equals(id)) {
				unwanted.add(entry);
			} else if (ValidatorError.SHORT_TAGS.equals(id)) {
				unwanted.add(entry);
			}

		}
		entries.removeAll(unwanted);

		ValidatorEntryAdapter adapter = new ValidatorEntryAdapter(this, entries);
		mList.setAdapter(adapter);

		if (entries.size() == 0) {
			mMessage.setText(R.string.ui_validated);
			mMessage.setCompoundDrawablesWithIntrinsicBounds(0,
					R.drawable.ic_validated, 0, 0);
		}
	}
}
