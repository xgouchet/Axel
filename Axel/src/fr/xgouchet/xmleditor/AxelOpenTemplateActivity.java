package fr.xgouchet.xmleditor;

import java.io.File;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import de.neofonie.mobile.app.android.widget.crouton.Crouton;
import de.neofonie.mobile.app.android.widget.crouton.Style;
import fr.xgouchet.androidlib.ui.adapter.FileListAdapter;
import fr.xgouchet.xmleditor.common.Constants;
import fr.xgouchet.xmleditor.common.TemplateFiles;

/**
 * 
 */
public class AxelOpenTemplateActivity extends Activity implements
		OnClickListener, OnItemClickListener {

	/**
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Setup content view
		setContentView(R.layout.layout_open);

		// buttons
		findViewById(R.id.buttonCancel).setOnClickListener(this);

		// widgets
		mFilesList = (ListView) findViewById(android.R.id.list);
		mFilesList.setOnItemClickListener(this);
		mFilesList.setOnCreateContextMenuListener(this);
	}

	/**
	 * @see android.app.Activity#onResume()
	 */
	protected void onResume() {
		super.onResume();

		fillTemplateFilesView();
	}

	/**
	 * @see android.app.Activity#onStop()
	 */
	protected void onStop() {
		super.onStop();
		Crouton.clearCroutonsForActivity(this);
	}

	/**
	 * @see android.app.Activity#onCreateContextMenu(android.view.ContextMenu,
	 *      android.view.View, android.view.ContextMenu.ContextMenuInfo)
	 */
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
		mContextFile = (File) mFilesList.getAdapter().getItem(info.position);

		menu.setHeaderTitle(mContextFile.getName());
		menu.add(Menu.NONE, 0, Menu.NONE, R.string.ui_delete);
	}

	/**
	 * @see android.app.Activity#onContextItemSelected(android.view.MenuItem)
	 */
	public boolean onContextItemSelected(MenuItem item) {

		TemplateFiles.removeFile(mContextFile);
		fillTemplateFilesView();
		Crouton.makeText(this, R.string.toast_template_file_deleted, Style.INFO)
				.show();

		return true;
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
	 * @see android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget.AdapterView,
	 *      android.view.View, int, long)
	 */
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		File template;

		template = mList.get(position);

		if (setOpenResult(template)) {
			finish();
		} else {
			TemplateFiles.removeFile(template);
			mListAdapter.notifyDataSetChanged();
			Crouton.makeText(this, R.string.toast_template_file_invalid,
					Style.ALERT).show();
		}

	}

	/**
	 * Fills the files list with the recent files
	 * 
	 */
	protected void fillTemplateFilesView() {
		mList = TemplateFiles.getTemplateFiles(this);

		if (mList.size() == 0) {
			setResult(RESULT_CANCELED);
			finish();
			return;
		}

		// create string list adapter
		mListAdapter = new FileListAdapter(this, mList, null);

		// set adpater
		mFilesList.setAdapter(mListAdapter);
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

		if ((file == null) || (!file.isFile()) || (!file.canRead())) {
			return false;
		}

		result = new Intent();
		result.putExtra(Constants.EXTRA_PATH, file.getAbsolutePath());
		result.putExtra(Constants.EXTRA_IGNORE_FILE, true);

		setResult(RESULT_OK, result);
		return true;
	}

	/** */
	protected File mContextFile;

	/** the dialog's list view */
	protected ListView mFilesList;
	/** The list adapter */
	protected FileListAdapter mListAdapter;

	/** the list of template files */
	protected List<File> mList;
}
