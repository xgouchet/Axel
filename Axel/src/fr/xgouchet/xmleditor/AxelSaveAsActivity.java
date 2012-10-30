package fr.xgouchet.xmleditor;

import java.io.File;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import de.neofonie.mobile.app.android.widget.crouton.Crouton;
import de.neofonie.mobile.app.android.widget.crouton.Style;
import fr.xgouchet.androidlib.ui.activity.BrowsingActivity;
import fr.xgouchet.xmleditor.common.Constants;

/**
 *  
 */
public class AxelSaveAsActivity extends BrowsingActivity implements
		OnClickListener {

	/**
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Setup content view
		setContentView(R.layout.layout_save_as);

		// buttons
		findViewById(R.id.buttonCancel).setOnClickListener(this);
		findViewById(R.id.buttonOk).setOnClickListener(this);
		((Button) findViewById(R.id.buttonOk)).setText(R.string.ui_save);

		// widgets
		mFileName = (EditText) findViewById(R.id.editFileName);

		// drawables
		mWriteable = getResources().getDrawable(R.drawable.folder_rw);
		mLocked = getResources().getDrawable(R.drawable.folder_r);
	}

	/**
	 * @see android.app.Activity#onStop()
	 */
	protected void onStop() {
		super.onStop();
		Crouton.clearCroutonsForActivity(this);
	}

	/**
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.buttonCancel:
			setResult(RESULT_CANCELED);
			finish();
			break;
		case R.id.buttonOk:
			if (setSaveResult())
				finish();
		}
	}

	/**
	 * @see fr.xgouchet.androidlib.ui.activity.BrowsingActivity#onFileClick(java.
	 *      io.File)
	 */
	protected void onFileClick(File file) {
		if (file.canWrite())
			mFileName.setText(file.getName());
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
	 * Sets the result data when the user presses save
	 * 
	 * @return if the result is OK (if not, it means the user must change its
	 *         selection / input)
	 */
	protected boolean setSaveResult() {
		Intent result;
		String fileName;

		if ((mCurrentFolder == null) || (!mCurrentFolder.exists())) {
			Crouton.makeText(this, R.string.toast_folder_doesnt_exist,
					Style.ALERT).show();
			return false;
		}

		if (!mCurrentFolder.canWrite()) {
			Crouton.makeText(this, R.string.toast_folder_cant_write,
					Style.ALERT).show();
			return false;
		}

		fileName = mFileName.getText().toString();
		if (fileName.length() == 0) {
			mFileName.setError(getString(R.string.toast_filename_empty));
			return false;
		}

		result = new Intent();
		result.putExtra(Constants.EXTRA_PATH, mCurrentFolder.getAbsolutePath()
				+ File.separator + fileName);

		setResult(RESULT_OK, result);
		return true;
	}

	/** the edit text input */
	protected EditText mFileName;

	/** */
	protected Drawable mWriteable;
	/** */
	protected Drawable mLocked;

}
