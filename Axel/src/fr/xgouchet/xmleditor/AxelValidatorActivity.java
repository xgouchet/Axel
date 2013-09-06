package fr.xgouchet.xmleditor;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import fr.xgouchet.androidlib.ui.Toaster;

/**
 * Idependant activity checking for Errors in XML files
 * 
 * @author xgouchet
 * 
 */
public class AxelValidatorActivity extends Activity {

	/**
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.layout_validator);

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
				file = new File(new URI(intent.getData().toString()));
				// TODO open file and check errors
			} catch (URISyntaxException e) {
				Toaster.showToast(this, R.string.toast_intent_invalid_uri, true);
				finish();
			} catch (IllegalArgumentException e) {
				Toaster.showToast(this, R.string.toast_intent_illegal, true);
				finish();
			}
		}
	}

}
