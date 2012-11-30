package fr.xgouchet.xmleditor;

import java.io.File;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import de.neofonie.mobile.app.android.widget.crouton.Crouton;
import de.neofonie.mobile.app.android.widget.crouton.Style;
import fr.xgouchet.androidlib.ui.activity.BrowsingActivity;
import fr.xgouchet.xmleditor.common.Constants;

/**
 * 
 */
public class AxelOpenActivity extends BrowsingActivity implements
		OnClickListener {

	/**
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Setup content view
		setContentView(R.layout.layout_open);

		// buttons
		findViewById(R.id.buttonCancel).setOnClickListener(this);

		// set default result
		setResult(RESULT_CANCELED, null);
	}

	/**
	 * @see android.app.Activity#onStop()
	 */
	protected void onStop() {
		super.onStop();
		Crouton.clearCroutonsForActivity(this);
	}

	/**
	 * @see Activity#onKeyUp(int, KeyEvent)
	 */
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			// navigate to parent folder
			File parent = mCurrentFolder.getParentFile();
			if ((parent != null) && (parent.exists())) {
				fillFolderView(parent);
				return true;
			}
		}
		return super.onKeyUp(keyCode, event);
	}

	/**
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	public void onClick(View v) {
		if (v.getId() == R.id.buttonCancel) {
			setResult(RESULT_CANCELED);
			finish();
		}
	}

	/**
	 * @see fr.xgouchet.androidlib.ui.activity.BrowsingActivity#onFolderClick(java.io.File)
	 */
	protected boolean onFolderClick(File folder) {
		return true;
	}

	/**
	 * @see fr.xgouchet.androidlib.ui.activity.BrowsingActivity#onFolderViewFilled()
	 */
	protected void onFolderViewFilled() {

	}

	/**
	 * @see fr.xgouchet.androidlib.ui.activity.BrowsingActivity#onFileClick(java.io.File)
	 */
	protected void onFileClick(File file) {
		if (setOpenResult(file))
			finish();
	}

	/**
	 * Set the result of this activity to open a file
	 * 
	 * @param file
	 *            the file to return
	 * @return if the result was set correctly
	 */
	protected boolean setOpenResult(File file) {
		Intent result;

		if (!file.canRead()) {
			Crouton.makeText(this, R.string.toast_file_cant_read, Style.ALERT)
					.show();

			return false;
		}

		result = new Intent();
		result.putExtra(Constants.EXTRA_PATH, file.getAbsolutePath());

		setResult(RESULT_OK, result);
		return true;
	}
}
