package fr.xgouchet.xmleditor;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import fr.xgouchet.androidlib.ui.Toaster;
import fr.xgouchet.xmleditor.network.ValidateFileTask;
import fr.xgouchet.xmleditor.network.ValidateFileTask.ValidationListener;

/**
 * Idependant activity checking for Errors in XML files
 * 
 * @author xgouchet
 * 
 */
public class AxelValidatorActivity extends Activity implements
		ValidationListener {

	private File mCurrentFile;
	private ValidateFileTask mValidateTask;

	/**
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_PROGRESS);
		setContentView(R.layout.layout_validator);

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
		File file;

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
				Toaster.showToast(this, R.string.toast_intent_invalid_uri, true);
				finish();
			} catch (IllegalArgumentException e) {
				Toaster.showToast(this, R.string.toast_intent_illegal, true);
				finish();
			}
		}
	}

	/**
	 * 
	 */
	private void validateCurrentFile() {
		mValidateTask = new ValidateFileTask();
		mValidateTask.setListener(this);
		mValidateTask.execute(mCurrentFile);
	}

	@Override
	public void onValidationRequestProgress(final int progress) {
		setProgressBarIndeterminate(false);
		setProgress(progress);
	}
}
