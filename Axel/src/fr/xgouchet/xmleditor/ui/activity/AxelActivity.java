package fr.xgouchet.xmleditor.ui.activity;

import java.io.File;
import java.util.List;
import java.util.SortedSet;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import de.neofonie.mobile.app.android.widget.crouton.Crouton;
import de.neofonie.mobile.app.android.widget.crouton.Style;
import fr.xgouchet.xmleditor.AxelAboutActivity;
import fr.xgouchet.xmleditor.AxelHelpActivity;
import fr.xgouchet.xmleditor.AxelSettingsActivity;
import fr.xgouchet.xmleditor.AxelValidatorActivity;
import fr.xgouchet.xmleditor.R;
import fr.xgouchet.xmleditor.common.AxelUtils;
import fr.xgouchet.xmleditor.common.Constants;
import fr.xgouchet.xmleditor.common.PreferencesUtils;
import fr.xgouchet.xmleditor.common.RecentUtils;
import fr.xgouchet.xmleditor.common.RecentUtils.RecentEntry;
import fr.xgouchet.xmleditor.common.TemplateFiles;
import fr.xgouchet.xmleditor.data.XmlEditor;
import fr.xgouchet.xmleditor.data.XmlEditorListener;
import fr.xgouchet.xmleditor.data.xml.XmlNode;
import fr.xgouchet.xmleditor.ui.dialog.PromptDialogHelper;
import fr.xgouchet.xmleditor.ui.dialog.PromptDialogHelper.PromptListener;
import fr.xgouchet.xmleditor.ui.fragment.ADocumentEditorFragment;
import fr.xgouchet.xmleditor.ui.fragment.SimpleEditorFragment;

/**
 * 
 * 
 * @author Xavier Gouchet
 * 
 */
public class AxelActivity extends Activity implements XmlEditorListener {

	/** the editor */
	private final XmlEditor mXmlEditor;

	/** the fragment displaying the current document */
	private ADocumentEditorFragment mEditorFragment;

	/** the runable to run after a save */
	private Runnable mAfterSave; // Mennen ? Axe ?

	public AxelActivity() {
		mXmlEditor = new XmlEditor(this);
		mXmlEditor.addListener(this);
	}

	// ////////////////////////////////////////////////////////////////////////////////////
	// Activity Lifecycle
	// ////////////////////////////////////////////////////////////////////////////////////

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Set content view
		setContentView(R.layout.activity_editor);

		// Set fragment (TODO read type from prefs)
		mEditorFragment = new SimpleEditorFragment();
		mEditorFragment.setXmlEditor(mXmlEditor);
		getFragmentManager().beginTransaction()
				.add(android.R.id.content, mEditorFragment).commit();

		// Setup Editor
		mXmlEditor.onStart();
		mXmlEditor.doClearContents();

		// Activity
		readIntent();
	}

	@Override
	protected void onStart() {
		super.onStart();

		// TODO Display change log if needed
		// AxelChangeLog changeLog;
		// changeLog = new AxelChangeLog();
		//
		// if (changeLog.displayChangeLog(this, prefs)) {
		// // copy templates from the assets
		// TemplateFiles.copyTemplatesFromAssets(this);
		// }

	}

	@Override
	protected void onResume() {
		super.onResume();

		if (mXmlEditor.hasRoot()) {
			if (mXmlEditor.canSave()) {
				if (mXmlEditor.fileExists()) {
					if (mXmlEditor.fileChanged()) {
						// TODO promptFileChanged();
					}
				} else {
					// TODO promptFileDeleted();
				}
			}
		} else {
			mXmlEditor.doClearContents();
		}

		PreferencesUtils.onResume(this);
		RecentUtils.onResume(this);
	}

	@Override
	protected void onActivityResult(final int requestCode,
			final int resultCode, final Intent data) {

		// check result
		if (resultCode != RESULT_OK) {
			return;
		}

		// check data
		if (data == null) {
			return;
		}

		Uri selectedUri = data.getData();

		switch (requestCode) {
		case Constants.REQUEST_OPEN_DOCUMENT:
		case Constants.REQUEST_GET_CONTENT:
			mXmlEditor.loadDocument(selectedUri, false);
			break;
		case Constants.REQUEST_SAVE_AS:
			mXmlEditor.saveDocument(selectedUri, true);
		}

	}

	@Override
	public void onBackPressed() {
		mAfterSave = new Runnable() {

			@Override
			public void run() {
				finish();
			}
		};

		promptSaveIfDirty();
	}

	// ////////////////////////////////////////////////////////////////////////////////////
	// OPTIONS MENU
	// ////////////////////////////////////////////////////////////////////////////////////

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		super.onCreateOptionsMenu(menu);

		getMenuInflater().inflate(R.menu.main, menu);

		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(final Menu menu) {
		super.onPrepareOptionsMenu(menu);

		// disable save for read only files
		menu.findItem(R.id.action_save).setEnabled(!mXmlEditor.isReadOnly());

		// disable preview for non previewable files (duh...)
		menu.findItem(R.id.action_preview_in_browser).setEnabled(
				AxelUtils.canBePreviewed(mXmlEditor.getCurrentDocumentNode(),
						mXmlEditor.getCurrentDocumentUri()));

		// add templates as submenus
		MenuItem newTemplate = menu.findItem(R.id.action_new_template);
		SubMenu templates = newTemplate.getSubMenu();
		templates.clear();

		List<File> templateFiles = TemplateFiles.getTemplateFiles(this);
		for (File template : templateFiles) {
			templates.add(R.id.action_group_template, 0, Menu.NONE,
					template.getName());
		}

		// add recent documents as submenu
		MenuItem openRecent = menu.findItem(R.id.action_open_recent);
		SubMenu recents = openRecent.getSubMenu();
		recents.clear();

		SortedSet<RecentEntry> recentEntries = RecentUtils.getRecentEntries();
		for (RecentEntry entry : recentEntries) {
			MenuItem item = recents.add(R.id.action_group_recents,
					(int) (entry.getTimestamp() / 1000L), Menu.NONE,
					entry.getName());
			item.setTitleCondensed(entry.getUri().toString());
		}

		if (recents.size() == 0) {
			openRecent.setEnabled(false);
		}

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {

		// Check for template
		if (item.getGroupId() == R.id.action_group_template) {
			loadTemplate(item.getTitle().toString());
			return true;
		}

		// Check for recent file
		if (item.getGroupId() == R.id.action_group_recents) {
			Uri uri = Uri.parse(item.getTitleCondensed().toString());
			mXmlEditor.loadDocument(uri, false);
		}

		// Other actions
		boolean result = true;
		switch (item.getItemId()) {
		case R.id.action_new_empty:
			newDocument();
			break;
		case R.id.action_open_file:
			loadDocument();
			break;
		case R.id.action_save:
			saveDocument();
			break;
		case R.id.action_save_as_file:
			saveDocumentAs();
			break;
		case R.id.action_save_as_template:
			// TODO promptTemplateName();
			break;
		case R.id.action_preview_in_browser:
			// TODO previewFile();
			break;
		case R.id.action_help:
			startActivity(new Intent(getApplicationContext(),
					AxelHelpActivity.class));
			break;
		case R.id.action_settings:
			startActivity(new Intent(getApplicationContext(),
					AxelSettingsActivity.class));
			break;
		case R.id.action_about:
			startActivity(new Intent(getApplicationContext(),
					AxelAboutActivity.class));
			break;
		default:
			result = super.onOptionsItemSelected(item);
			break;
		}

		return result;
	}

	// ////////////////////////////////////////////////////////////////////////////////////
	// EDITOR ACTIONS
	// ////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Runs the after save to complete
	 */
	private void runAfterSave() {
		if (mAfterSave != null) {
			mAfterSave.run();
			mAfterSave = null;
		}
	}

	/**
	 * Read the intent used to start this activity (open the text file) as well
	 * as the non configuration instance if activity is started after a screen
	 * rotate
	 */
	private void readIntent() {
		Intent intent;
		String action;

		intent = getIntent();
		if (intent == null) {
			mXmlEditor.doClearContents();
			return;
		}

		action = intent.getAction();
		if (action == null) {
			mXmlEditor.doClearContents();
		} else if ((action.equals(Intent.ACTION_VIEW))
				|| (action.equals(Intent.ACTION_EDIT))) {
			mXmlEditor.loadDocument(intent.getData(), false);
		} else {
			mXmlEditor.doClearContents();
		}
	}

	/**
	 * Clears the current content to make a new file
	 */
	private void newDocument() {
		mAfterSave = new Runnable() {

			@Override
			public void run() {
				mXmlEditor.doClearContents();
			}
		};

		promptSaveIfDirty();
	}

	/**
	 * Starts an activity to choose a file to open
	 */
	@TargetApi(VERSION_CODES.KITKAT)
	private void loadDocument() {
		mAfterSave = new Runnable() {

			@Override
			public void run() {
				Intent open;
				int requestCode;
				boolean createChooser;

				if (VERSION.SDK_INT >= VERSION_CODES.KITKAT) {
					open = new Intent(Intent.ACTION_OPEN_DOCUMENT);
					requestCode = Constants.REQUEST_OPEN_DOCUMENT;
					createChooser = false;
				} else {
					open = new Intent(Intent.ACTION_GET_CONTENT);
					requestCode = Constants.REQUEST_GET_CONTENT;
					createChooser = true;
				}

				// Limit to openable data
				open.addCategory(Intent.CATEGORY_OPENABLE);

				// Set Mime Types
				open.setType("text/xml");
				open.putExtra(Intent.EXTRA_MIME_TYPES, AxelUtils.XML_MIME_TYPE);

				if (createChooser) {
					startActivityForResult(Intent.createChooser(open,
							getString(R.string.ui_open_with)), requestCode);
				} else {
					startActivityForResult(open, requestCode);
				}
			}
		};

		promptSaveIfDirty();
	}

	/**
	 * Loads a template document
	 * 
	 * @param template
	 */
	private void loadTemplate(final String template) {
		String templatePath = TemplateFiles.getOuputPath(this, template);
		final File file = new File(templatePath);

		mAfterSave = new Runnable() {

			@Override
			public void run() {
				mXmlEditor.loadDocument(Uri.fromFile(file), true);
			}
		};

		promptSaveIfDirty();
	}

	/**
	 * Saves the current document to it's latest known location, if any, or
	 * prompt the user for a new location
	 */
	private void saveDocument() {
		if (mXmlEditor.canSave()) {
			mXmlEditor.saveDocument();
		} else {
			saveDocumentAs();
		}
	}

	/**
	 * Starts an activity to ask the user for a saving location
	 */
	@TargetApi(VERSION_CODES.KITKAT)
	private void saveDocumentAs() {
		Intent save;
		int requestCode;

		if (VERSION.SDK_INT >= VERSION_CODES.KITKAT) {
			save = new Intent(Intent.ACTION_CREATE_DOCUMENT);
			requestCode = Constants.REQUEST_SAVE_AS;
		} else {
			// save = new Intent(Intent.ACTION_GET_CONTENT);
			// requestCode = Constants.REQUEST_SAVE_AS;
			// createChooser = true;
			// TODO find a generic way to do so before Kit Kat ! ?
			return;
		}

		// Limit to openable data
		save.addCategory(Intent.CATEGORY_OPENABLE);

		// Set Mime Type
		save.setType(AxelUtils.getMimeType(mXmlEditor.getCurrentDocumentNode(),
				mXmlEditor.getCurrentDocumentUri()));
		startActivityForResult(save, requestCode);
	}

	/**
	 * TODO add an option to validate from action bar Validates a file with the
	 * W3C validator API
	 * 
	 * @param file
	 *            the file to validate
	 */
	private void validateDocument(final Uri uri) {
		Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		intent.setClass(getBaseContext(), AxelValidatorActivity.class);
		startActivity(intent);
	}

	/**
	 * If the current document is dirty, prompts the user to save it. Then runs
	 * the After Save Action
	 */
	private void promptSaveIfDirty() {
		if (!mXmlEditor.isDirty()) {
			runAfterSave();
			return;
		}

		PromptDialogHelper.promptDirtyDocumentAction(this,
				new PromptListener() {

					@Override
					public void onPromptEvent(final int id, final int choice,
							final Object result) {
						switch (choice) {
						case PromptDialogHelper.CHOICE_SAVE:
							saveDocument();
							break;
						case PromptDialogHelper.CHOICE_DONT_SAVE:
							runAfterSave();
							break;
						case PromptDialogHelper.CHOICE_CANCEL_IGNORE:
							mAfterSave = null;
							break;
						}
					}
				});
	}

	private void updateTitle() {
		String name = mXmlEditor.getCurrentDocumentName();
		if (TextUtils.isEmpty(name)) {
			name = "?";
		}

		int titleId = mXmlEditor.isReadOnly() ? R.string.title_editor_readonly
				: (mXmlEditor.isDirty() ? R.string.title_editor_dirty
						: R.string.title_editor);

		setTitle(getString(titleId, name));
	}

	// ////////////////////////////////////////////////////////////////////////////////////
	// XmlEditorListener Implementation
	// ////////////////////////////////////////////////////////////////////////////////////

	@Override
	public void onXmlDocumentChanged(final XmlNode root, final String name,
			final Uri uri) {
		invalidateOptionsMenu();
		updateTitle();
	}

	@Override
	public void onXmlContentChanged() {
		invalidateOptionsMenu();
		updateTitle();
	}

	@Override
	public void onXmlDocumentSaved() {
		invalidateOptionsMenu();
		updateTitle();
	}

	@Override
	public void onXmlParseError(final Uri uri, final String message) {
		PromptDialogHelper.promptXmlParseErrorAction(this,
				new PromptListener() {

					@Override
					public void onPromptEvent(final int id, final int choice,
							final Object result) {
						switch (choice) {
						case PromptDialogHelper.CHOICE_W3C_VALIDATION:
							validateDocument(uri);
							break;
						default:
							break;
						}
					}
				});
	}

	@Override
	public void onHtmlParseError(final Uri uri, final String message) {
		PromptDialogHelper.promptHtmlParseErrorAction(this,
				new PromptListener() {

					@Override
					public void onPromptEvent(final int id, final int choice,
							final Object result) {
						switch (choice) {
						case PromptDialogHelper.CHOICE_PARSE_HTML_SOUP:
							mXmlEditor.loadHtmlDocument(uri);
							break;
						case PromptDialogHelper.CHOICE_W3C_VALIDATION:
							validateDocument(uri);
							break;
						default:
							break;
						}
					}
				});
	}

	@Override
	public void onConfirmNotification(final String message) {
		Crouton.makeText(this, message, Style.CONFIRM).show();
	}

	@Override
	public void onInfoNotification(final String message) {
		Crouton.makeText(this, message, Style.INFO).show();
	}

	@Override
	public void onErrorNotification(final String message) {
		Crouton.makeText(this, message, Style.ALERT).show();
	}
}
