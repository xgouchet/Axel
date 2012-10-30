package fr.xgouchet.xmleditor;

import static fr.xgouchet.androidlib.data.FileUtils.deleteItem;
import static fr.xgouchet.androidlib.data.FileUtils.getCanonizePath;
import static fr.xgouchet.androidlib.data.FileUtils.renameItem;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.EditText;
import android.widget.ListView;
import de.neofonie.mobile.app.android.widget.crouton.Crouton;
import de.neofonie.mobile.app.android.widget.crouton.Style;
import fr.xgouchet.androidlib.R;
import fr.xgouchet.androidlib.data.FileUtils;
import fr.xgouchet.xmleditor.common.AxelChangeLog;
import fr.xgouchet.xmleditor.common.Constants;
import fr.xgouchet.xmleditor.common.RecentFiles;
import fr.xgouchet.xmleditor.common.Settings;
import fr.xgouchet.xmleditor.common.TemplateFiles;
import fr.xgouchet.xmleditor.common.TextFileUtils;
import fr.xgouchet.xmleditor.data.html.HtmlCleanerParser;
import fr.xgouchet.xmleditor.data.tree.TreeNode;
import fr.xgouchet.xmleditor.data.xml.XmlData;
import fr.xgouchet.xmleditor.data.xml.XmlNode;
import fr.xgouchet.xmleditor.data.xml.XmlNodeStyler;
import fr.xgouchet.xmleditor.data.xml.XmlTreeParserException;
import fr.xgouchet.xmleditor.data.xml.XmlTreePullParser;
import fr.xgouchet.xmleditor.ui.adapter.TreeAdapter;

/**
 * 
 */
public class AxelActivity extends Activity {

	/**
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.layout_editor);

		Settings.updateFromPreferences(getSharedPreferences(
				Constants.PREFERENCES_NAME, MODE_PRIVATE));

		//
		mIsResumingFromRequest = false;

		// Editor
		mListView = (ListView) findViewById(android.R.id.list);
		registerForContextMenu(mListView);
		doClearContents();

		readIntent();
	}

	/**
	 * @see android.app.Activity#onStart()
	 */
	protected void onStart() {
		super.onStart();
		AxelChangeLog changeLog;
		SharedPreferences prefs;

		changeLog = new AxelChangeLog();
		prefs = getSharedPreferences(Constants.PREFERENCES_NAME,
				Context.MODE_PRIVATE);

		if (changeLog.isFirstLaunch(this, prefs)) {
			Builder builder = new Builder(this);
			String message = getString(changeLog.getTitleResource(this))
					+ "\n\n" + getString(changeLog.getChangeLogResource(this));
			builder.setTitle(R.string.ui_whats_new);
			builder.setMessage(message);
			builder.setCancelable(true);
			builder.setPositiveButton(android.R.string.ok,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});

			builder.create().show();

			TemplateFiles.copyTemplatesFromAssets(this);
		}

		changeLog.saveCurrentVersion(this, prefs);
	}

	/**
	 * @see android.app.Activity#onResume()
	 */
	protected void onResume() {
		super.onResume();

		mIsResumingFromRequest = false;

		if (mDocument == null) {
			mDocument = XmlNode.createDocument();
			((AxelApplication) getApplication()).mCurrentDocument = mDocument;
		}

		onXmlContentChanged();
		updateFromSettings();
	}

	/**
	 * @see android.app.Activity#onStop()
	 */
	protected void onStop() {
		super.onStop();
		Crouton.clearCroutonsForActivity(this);
	}

	/**
	 * @see android.app.Activity#onActivityResult(int, int,
	 *      android.content.Intent)
	 */
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Bundle extras;

		if (resultCode == RESULT_OK) {

			extras = data.getExtras();
			if (extras != null) {
				switch (requestCode) {
				case Constants.REQUEST_SAVE_AS:
					doSaveFile(extras.getString(Constants.EXTRA_PATH));
					break;
				case Constants.REQUEST_OPEN:
					doOpenFile(
							new File(extras.getString(Constants.EXTRA_PATH)),
							false, extras.getBoolean(
									Constants.EXTRA_IGNORE_FILE, false));
					break;
				case Constants.REQUEST_EDIT_NODE:
					((AxelApplication) getApplication()).mCurrentSelection
							.onContentChanged();
				}
			}
		}
	}

	/**
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		MenuInflater inflater = new MenuInflater(this);
		inflater.inflate(R.menu.main, menu);

		return true;
	}

	/**
	 * @see android.app.Activity#onPrepareOptionsMenu(android.view.Menu)
	 */
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);

		menu.findItem(R.id.menu_save).setEnabled(!mReadOnly);

		boolean isRootExpanded = (mAdapter != null)
				&& mAdapter.isRootExpanded();
		if (isRootExpanded) {
			menu.findItem(R.id.menu_expand_collapse_all)
					.setTitle(R.string.menu_collapse_all)
					.setIcon(R.drawable.ic_menu_collapse);
		} else {
			menu.findItem(R.id.menu_expand_collapse_all)
					.setTitle(R.string.menu_expand_all)
					.setIcon(R.drawable.ic_menu_expand);
		}

		return true;
	}

	/**
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean result;

		result = true;
		switch (item.getItemId()) {
		case R.id.menu_new:
			newContent();
			break;
		case R.id.menu_open:
			openFile();
			break;
		case R.id.menu_open_recent:
			openRecentFile();
			break;
		case R.id.menu_open_template:
			openTemplateFile();
			break;
		case R.id.menu_save:
			saveContent();
			break;
		case R.id.menu_save_as:
			saveContentAs();
			break;
		case R.id.menu_save_as_template:
			promptTemplateName();
			break;
		case R.id.menu_expand_collapse_all:
			expandCollapseAll();
			break;
		case R.id.menu_settings:
			settingsActivity();
			break;
		case R.id.menu_about:
			aboutActivity();
			break;
		default:
			result = super.onOptionsItemSelected(item);
			break;
		}

		return result;
	}

	/**
	 * @see android.app.Activity#onCreateContextMenu(android.view.ContextMenu,
	 *      android.view.View, android.view.ContextMenu.ContextMenuInfo)
	 */
	public void onCreateContextMenu(ContextMenu menu, View view,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, view, menuInfo);

		if (view == mListView) {
			onCreateListItemContextMenu(menu, view, menuInfo);
		}

	}

	/**
	 * @param menu
	 *            The context menu that is being built
	 * @param view
	 *            The view for which the context menu is being built
	 * @param menuInfo
	 *            Extra information about the item for which the context menu
	 *            should be shown. This information will vary depending on the
	 *            class of view.
	 */
	protected void onCreateListItemContextMenu(ContextMenu menu, View view,
			ContextMenuInfo menuInfo) {
		MenuInflater inflater = new MenuInflater(this);
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
		XmlNode node = (XmlNode) mAdapter.getNode(info.position);
		mCurrentSelection = node;

		if (!mReadOnly) {
			if (node.getContent().isDocument()) {
				inflater.inflate(R.menu.xml_doc_context, menu);
				menu.findItem(R.id.action_add_element).setVisible(
						!mDocument.hasRootChild());
				menu.findItem(R.id.action_add_doctype).setVisible(
						!mDocument.hasDoctype());
			} else {
				if (node.getContent().isElement()) {
					inflater.inflate(R.menu.xml_tag_context, menu);

					menu.findItem(R.id.action_sort_children).setVisible(
							node.getChildrenCount() > 1);
				}

				inflater.inflate(R.menu.xml_context, menu);

				if (node.getContent().isComment()) {
					menu.findItem(R.id.action_comment_uncomment).setTitle(
							R.string.action_uncomment);
				} else if (node.getContent().isDocumentDeclaration()) {
					menu.findItem(R.id.action_comment_uncomment).setVisible(
							false);
					menu.findItem(R.id.action_delete).setVisible(false);
				} else {
					menu.findItem(R.id.action_comment_uncomment).setTitle(
							R.string.action_comment);
				}
			}

			menu.setHeaderTitle(node.toString());
		}
	}

	/**
	 * @see android.app.Activity#onContextItemSelected(android.view.MenuItem)
	 */
	public boolean onContextItemSelected(MenuItem item) {

		boolean result = true;

		switch (item.getItemId()) {
		case R.id.action_delete:
			promptDeleteNode();
			break;
		case R.id.action_add_comment:
			doAddChildToNode(XmlNode.createComment("Comment"), true);
			break;
		case R.id.action_add_text:
			doAddChildToNode(XmlNode.createText("Text"), true);
			break;
		case R.id.action_add_cdata:
			doAddChildToNode(XmlNode.createCDataSection("Unparsed data"), true);
			break;
		case R.id.action_add_element:
			doAddChildToNode(XmlNode.createElement("element"), true);
			break;
		case R.id.action_add_doctype:
			doAddChildToNode(
					XmlNode.createDoctypeDeclaration("root SYSTEM \"DTD location\""),
					true);
			break;
		case R.id.action_add_process:
			doAddChildToNode(XmlNode.createProcessingInstruction("target",
					"instruction"), true);
			break;
		case R.id.action_edit:
			doEditNode();
			break;
		case R.id.action_sort_children:
			doSortChildren();
			break;
		case R.id.action_comment_uncomment:
			doCommentUncommentNode();
			break;
		default:
			result = super.onContextItemSelected(item);
		}

		return result;
	}

	/**
	 * Call this when the document changes
	 */
	protected void onXmlDocumentChanged() {
		((AxelApplication) getApplication()).mCurrentDocument = mDocument;

		mAdapter = new TreeAdapter<XmlData>(this, mDocument);
		mAdapter.setExpanded(true, true);
		mAdapter.setNodeStyler(new XmlNodeStyler());
		mListView.setAdapter(mAdapter);

		onXmlContentChanged();
	}

	/**
	 * Call when something in the xml tree changes
	 */
	protected void onXmlContentChanged() {
		updateTreeView();
		updateTitle();
	}

	/**
	 * updates the tree view
	 */
	protected void updateTreeView() {
		if (mAdapter != null) {
			mAdapter.notifyDataSetChanged();
		}
	}

	/**
	 * Read the intent used to start this activity (open the text file) as well
	 * as the non configuration instance if activity is started after a screen
	 * rotate
	 */
	protected void readIntent() {
		Intent intent;
		String action;
		File file;

		intent = getIntent();
		if (intent == null) {
			doDefaultAction();
			return;
		}

		action = intent.getAction();
		if (action == null) {
			doDefaultAction();
		} else if ((action.equals(Intent.ACTION_VIEW))
				|| (action.equals(Intent.ACTION_EDIT))) {
			try {
				file = new File(new URI(intent.getData().toString()));
				doOpenFile(file, false, false);
			} catch (URISyntaxException e) {
				Crouton.makeText(this, R.string.toast_intent_invalid_uri,
						Style.ALERT).show();
			} catch (IllegalArgumentException e) {
				Crouton.makeText(this, R.string.toast_intent_illegal,
						Style.ALERT).show();
			}
		} else {
			doDefaultAction();
		}
	}

	/**
	 * Expands / collapses the whole tree
	 */
	@TargetApi(11)
	protected void expandCollapseAll() {
		if (mAdapter != null) {
			boolean isRootExpanded = mAdapter.isRootExpanded();

			mAdapter.setExpanded(!isRootExpanded, true);
			mAdapter.notifyDataSetChanged();

			if (VERSION.SDK_INT > VERSION_CODES.HONEYCOMB) {
				invalidateOptionsMenu();
			}
		}
	}

	/**
	 * Clears the current content to make a new file
	 */
	protected void newContent() {
		mAfterSave = new Runnable() {
			public void run() {
				doClearContents();
			}
		};

		promptSaveDirty();
	}

	/**
	 * Starts an activity to choose a file to open
	 */
	protected void openFile() {

		mAfterSave = new Runnable() {
			public void run() {
				Intent open;

				open = new Intent(getApplicationContext(),
						AxelOpenActivity.class);

				startActivityForResult(open, Constants.REQUEST_OPEN);
			}
		};

		promptSaveDirty();
	}

	/**
	 * Open the recent files activity to open
	 */
	protected void openRecentFile() {

		if (RecentFiles.getRecentFiles().size() == 0) {
			Crouton.makeText(this, R.string.toast_no_recent_files, Style.INFO)
					.show();
		} else {
			mAfterSave = new Runnable() {
				public void run() {
					Intent open;

					open = new Intent(getApplicationContext(),
							AxelOpenRecentActivity.class);

					startActivityForResult(open, Constants.REQUEST_OPEN);

				}
			};

			promptSaveDirty();
		}
	}

	/**
	 * Open the template files activity to open a template
	 */
	protected void openTemplateFile() {

		if (TemplateFiles.getTemplateFiles(this).size() == 0) {
			Crouton.makeText(this, R.string.toast_no_template_files, Style.INFO)
					.show();
		} else {
			mAfterSave = new Runnable() {
				public void run() {
					Intent open;

					open = new Intent(getApplicationContext(),
							AxelOpenTemplateActivity.class);

					startActivityForResult(open, Constants.REQUEST_OPEN);
				}
			};

			promptSaveDirty();
		}
	}

	/**
	 * General save command : check if a path exist for the current content,
	 * then save it , else invoke the {@link AxelActivity#saveContentAs()}
	 * method
	 */
	protected void saveContent() {
		if ((mCurrentFilePath == null) || (mCurrentFilePath.length() == 0)) {
			saveContentAs();
		} else {
			doSaveFile(mCurrentFilePath);
		}
	}

	/**
	 * General Save as command : prompt the user for a location and file name,
	 * then save the editor'd content
	 */
	protected void saveContentAs() {
		Intent saveAs;

		saveAs = new Intent(getApplicationContext(), AxelSaveAsActivity.class);
		startActivityForResult(saveAs, Constants.REQUEST_SAVE_AS);
	}

	/**
	 * Runs the after save to complete
	 */
	protected void runAfterSave() {
		if (mAfterSave != null) {
			mAfterSave.run();
			mAfterSave = null;
		}
	}

	/**
	 * Prompt the user before deleting a Node
	 */
	protected void promptDeleteNode() {
		final AlertDialog.Builder builder;

		builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.ui_delete);
		builder.setCancelable(true);
		builder.setMessage(R.string.ui_prompt_delete_node);

		builder.setPositiveButton(R.string.ui_delete,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						doDeleteNode();
					}
				});
		builder.setNegativeButton(R.string.ui_cancel,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
					}
				});

		builder.create().show();
	}

	/**
	 * Prompts the user what to do when an error occurs parsing the file
	 */
	protected void promptOpenError(final File file,
			final boolean forceReadOnly, final boolean ignoreFile) {
		final AlertDialog.Builder builder;

		builder = new AlertDialog.Builder(this);
		builder.setIcon(R.drawable.ic_dialog_alert);
		builder.setTitle(R.string.ui_open_error); // TODO STR
		builder.setCancelable(true);
		builder.setMessage(R.string.ui_prompt_open_error);

		builder.setPositiveButton(R.string.ui_send_file,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						doSendFile(file);
					}
				});
		builder.setNeutralButton(R.string.ui_convert_html,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						doOpenFileAsHtml(file, forceReadOnly, ignoreFile);
					}
				});
		builder.setNegativeButton(R.string.ui_cancel,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
					}
				});

		builder.create().show();
	}

	/**
	 * Prompt the user to save the current file before doing something else
	 */
	protected void promptSaveDirty() {
		Builder builder;

		if (!mDirty) {
			runAfterSave();
			return;
		}

		builder = new Builder(this);
		builder.setTitle(R.string.app_name);
		builder.setMessage(R.string.ui_save_text);

		builder.setPositiveButton(R.string.ui_save,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						saveContent();
					}
				});
		builder.setNegativeButton(R.string.ui_cancel,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {

					}
				});
		builder.setNeutralButton(R.string.ui_no_save,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						runAfterSave();
					}
				});

		builder.create().show();

	}

	/**
	 * Prompt the user for a template name
	 */
	protected void promptTemplateName() {

		final EditText input = new EditText(this);
		input.setHint(R.string.ui_hint_name);

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Set template name"); // TODO string
		builder.setView(input);

		builder.setNegativeButton(R.string.ui_cancel, null);
		builder.setPositiveButton(R.string.ui_save, null);
		final AlertDialog dialog = builder.create();
		dialog.show();

		dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(
				new View.OnClickListener() {
					public void onClick(View v) {
						String name = input.getText().toString().trim();

						if (TextUtils.isEmpty(name)) {
							input.setError(getString(R.string.toast_filename_empty));
						} else if (!TemplateFiles.validateTemplateName(
								getApplicationContext(), name)) {
							input.setError(getString(R.string.toast_template_file_exists));
						} else {
							doSaveTemplate(name);
							dialog.dismiss();
						}
					}
				});
	}

	/**
	 * Run the default startup action
	 */
	protected void doDefaultAction() {
		doClearContents();
	}

	/**
	 * Clears the content of the editor. Assumes that user was prompted and
	 * previous data was saved
	 */
	protected void doClearContents() {
		mCurrentFilePath = null;
		mCurrentFileName = null;
		mDirty = false;
		mReadOnly = false;

		mDocument = XmlNode.createDocument();
		mDocument.addChildNode(XmlNode.createDocumentDeclaration("1.0",
				"UTF-8", null));

		onXmlDocumentChanged();
	}

	/**
	 * Opens the given file and replace the editors content with the file.
	 * Assumes that user was prompted and previous data was saved
	 * 
	 * @param file
	 *            the file to load
	 * @param forceReadOnly
	 *            force the file to be used as read only
	 * @param ignoreFile
	 *            ignore the file link
	 * @return if the file was loaded successfully
	 */
	protected boolean doOpenFile(File file, boolean forceReadOnly,
			boolean ignoreFile) {
		boolean result = false;

		Reader input = null;
		if (file != null) {
			try {
				XmlNode document;
				input = new InputStreamReader(new FileInputStream(file));
				document = parseXml(input, true);

				if (document != null) {
					result = true;
					onFileParsed(file, document, forceReadOnly, ignoreFile);
				} else {
					promptOpenError(file, forceReadOnly, ignoreFile);
				}
			} catch (FileNotFoundException e) {
				Crouton.makeText(this, R.string.toast_open_not_found_error,
						Style.ALERT).show();
			} catch (OutOfMemoryError e) {
				Crouton.makeText(this, R.string.toast_open_memory_error,
						Style.ALERT).show();
			} finally {
				try {
					if (input != null) {
						input.close();
					}
				} catch (IOException e) {
					Log.w("Axel", "Error while closing input reader");
				}
			}
		}

		return result;
	}

	/**
	 * Opens the given file as an HTML file (parsing the soup) and replace the
	 * editors content with the file. Assumes that user was prompted and
	 * previous data was saved
	 * 
	 * @param file
	 *            the file to load
	 * @param forceReadOnly
	 *            force the file to be used as read only
	 * @param ignoreFile
	 *            ignore the file link
	 * @return if the file was loaded successfully
	 */
	protected boolean doOpenFileAsHtml(File file, boolean forceReadOnly,
			boolean ignoreFile) {
		boolean result = false;

		Reader input = null;

		if (file != null) {
			try {
				XmlNode document;
				input = new InputStreamReader(new FileInputStream(file));
				document = parseHtml(input);

				if (document != null) {
					result = true;
					onFileParsed(file, document, forceReadOnly, ignoreFile);
					Crouton.makeText(
							this,
							"This is a retrieved, well formed version of your HTML document. Check if nothing was lost before saving it.",
							Style.INFO).show();
				}

			} catch (FileNotFoundException e) {
				Crouton.makeText(this, R.string.toast_open_not_found_error,
						Style.ALERT).show();
			} catch (OutOfMemoryError e) {
				Crouton.makeText(this, R.string.toast_open_memory_error,
						Style.ALERT).show();
			} finally {
				try {
					if (input != null) {
						input.close();
					}
				} catch (IOException e) {
					Log.w("Axel", "Error while closing input reader");
				}
			}
		}

		return result;
	}

	/**
	 * Called when a file is successfully parsed
	 * 
	 * @param file
	 *            the source file
	 * @param document
	 *            the root node (document)
	 * @param forceReadOnly
	 *            force the editor to be read only
	 * @param ignoreFile
	 *            ignore the file
	 */
	protected void onFileParsed(File file, XmlNode document,
			boolean forceReadOnly, boolean ignoreFile) {
		mDocument = document;
		mReadOnly = forceReadOnly;

		if (!ignoreFile) {
			mCurrentFileName = file.getName();
			mCurrentFilePath = FileUtils.getCanonizePath(file);

			RecentFiles.updateRecentList(mCurrentFilePath);
			RecentFiles.saveRecentList(getSharedPreferences(
					Constants.PREFERENCES_NAME, MODE_PRIVATE));

			mReadOnly |= !file.canWrite();
		}

		onXmlDocumentChanged();
	}

	/**
	 * Parses an input to create a document hierarchy
	 * 
	 * @param input
	 *            the input node
	 * @param createDocDecl
	 *            create a document declaration in the hierarchy
	 * @return the root document node
	 */
	protected XmlNode parseXml(Reader input, boolean isFile) {
		XmlNode node = null;

		try {
			node = XmlTreePullParser.parseXmlTree(input, isFile);
		} catch (XmlTreeParserException e) {
			if (!isFile) {
				Crouton.makeText(this, e.getMessage(this), Style.ALERT).show();
			}
		}

		return node;
	}

	/**
	 * Parses an input to create a document hierarchy
	 * 
	 * @param input
	 *            the input node
	 * @return the root document node
	 */
	protected XmlNode parseHtml(Reader input) {
		XmlNode node = null;

		try {
			node = HtmlCleanerParser.parseHtmlTree(input);
		} catch (XmlTreeParserException e) {
			Crouton.makeText(this, e.getMessage(this), Style.ALERT).show();
		}

		return node;
	}

	/**
	 * Saves the text editor's content into a file at the given path. If an
	 * after save {@link Runnable} exists, run it
	 * 
	 * @param path
	 *            the path to the file (must be a valid path and not null)
	 */
	protected void doSaveFile(String path) {
		StringBuilder builder;

		if (path == null) {
			Crouton.makeText(this, R.string.toast_save_null, Style.ALERT)
					.show();
			return;
		}

		builder = new StringBuilder();
		mDocument.buildXmlString(builder);

		if (!TextFileUtils.writeTextFile(path + ".tmp", builder.toString())) {
			Crouton.makeText(this, R.string.toast_save_temp, Style.ALERT)
					.show();
			return;
		}

		if (!deleteItem(path)) {
			Crouton.makeText(this, R.string.toast_save_delete, Style.ALERT)
					.show();
			return;
		}

		if (!renameItem(path + ".tmp", path)) {
			Crouton.makeText(this, R.string.toast_save_rename, Style.ALERT)
					.show();
			return;
		}

		mCurrentFilePath = getCanonizePath(new File(path));
		mCurrentFileName = (new File(path)).getName();
		RecentFiles.updateRecentList(path);
		RecentFiles.saveRecentList(getSharedPreferences(
				Constants.PREFERENCES_NAME, MODE_PRIVATE));
		mReadOnly = false;
		mDirty = false;
		onXmlContentChanged();
		Crouton.makeText(this, R.string.toast_save_success, Style.CONFIRM)
				.show();

		runAfterSave();
	}

	/**
	 * @param fileName
	 *            saves the template
	 */
	protected void doSaveTemplate(String fileName) {
		StringBuilder builder;
		String path = TemplateFiles.getOuputPath(this, fileName);

		builder = new StringBuilder();
		mDocument.buildXmlString(builder);

		if (!TextFileUtils.writeTextFile(path, builder.toString())) {
			Crouton.makeText(this, R.string.toast_save_template_error,
					Style.ALERT).show();
			return;
		}
		Crouton.makeText(this, R.string.toast_save_template_success,
				Style.CONFIRM).show();
	}

	/**
	 * @param file
	 *            the file to send for report
	 */
	protected void doSendFile(File file) {

		Intent intent = new Intent(Intent.ACTION_SEND);
		// intent.setData(Uri.fromFile(file));
		intent.setType("text/xml");

		intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
		intent.putExtra(Intent.EXTRA_EMAIL, new String[] { getResources()
				.getString(R.string.ui_mail) });
		intent.putExtra(Intent.EXTRA_SUBJECT, "Axel - File open error");
		startActivity(Intent.createChooser(intent,
				getString(R.string.ui_choose_mail)));
	}

	/**
	 * Adds a child to the given node
	 * 
	 * @param node
	 *            the node to add
	 * @param edit
	 *            edit the added node ?
	 */
	protected void doAddChildToNode(XmlNode node, boolean edit) {
		if (mCurrentSelection.addChildNode(node)) {
			if (mCurrentSelection.getContent().isDocument()) {
				mCurrentSelection.reorderDocumentChildren();
			} else {
				// todo reorder element chidlren based on validator
			}

			mCurrentSelection.setExpanded(true, false);

			mDirty = true;

			if (edit && Settings.sEditOnCreate) {
				mCurrentSelection = node;
				doEditNode();
			} else {
				mCurrentSelection = null;
			}

			onXmlContentChanged();

		}
	}

	/**
	 * Comment or uncomment a node
	 */
	protected void doCommentUncommentNode() {
		if (mCurrentSelection.getContent().isComment()) {
			doUncommentNode();
		} else {
			doCommentNode();
		}

		mDirty = true;
		onXmlContentChanged();
	}

	/**
	 * Uncomment a comment node
	 */
	protected void doUncommentNode() {
		Reader input;
		XmlNode doc, node, parent;
		int index;

		input = new StringReader(mCurrentSelection.getContent().getText());
		doc = parseXml(input, false);

		if ((doc != null) && (doc.getChildrenCount() > 0)) {
			node = (XmlNode) doc.getChildAtPos(0);
			parent = (XmlNode) mCurrentSelection.getParent();
			if (parent.canHasChild(node)) {
				index = parent.getChildPosition(mCurrentSelection);
				parent.removeChildNode(mCurrentSelection);
				parent.addChildNode(node, index);
				node.setExpanded(true);

				mDirty = true;
				onXmlContentChanged();
			} else {
				Crouton.makeText(this, R.string.toast_xml_uncomment,
						Style.ALERT).show();
			}
		}
	}

	/**
	 * Comment a node
	 */
	protected void doCommentNode() {

		TreeNode<XmlData> parent = mCurrentSelection.getParent();
		int index = parent.getChildPosition(mCurrentSelection);
		parent.removeChildNode(mCurrentSelection);

		StringBuilder builder = new StringBuilder();
		mCurrentSelection.buildXmlString(builder);
		String content = builder.toString().trim();

		parent.addChildNode(XmlNode.createComment(content), index);
	}

	/**
	 * Opens an editor for the selected node
	 */
	protected void doEditNode() {
		((AxelApplication) getApplication()).mCurrentSelection = mCurrentSelection;

		Intent edit = new Intent(getApplicationContext(),
				AxelNodeEditorActivity.class);

		startActivityForResult(edit, Constants.REQUEST_EDIT_NODE);
		mDirty = true;
	}

	/**
	 * Opens an editor to sort an element's children;
	 */
	protected void doSortChildren() {
		((AxelApplication) getApplication()).mCurrentSelection = mCurrentSelection;

		Intent edit = new Intent(getApplicationContext(),
				AxelSortActivity.class);

		startActivity(edit);
		mDirty = true;
		updateTitle();
	}

	/**
	 * Deletes a node from its parent
	 */
	protected void doDeleteNode() {

		if ((mCurrentSelection != null) && mCurrentSelection.removeFromParent()) {
			mCurrentSelection = null;
			mDirty = true;
			onXmlContentChanged();
		}
	}

	/**
	 * Update the window title
	 */
	@TargetApi(11)
	protected void updateTitle() {
		String title;
		String name;

		name = "?";
		if ((mCurrentFileName != null) && (mCurrentFileName.length() > 0)) {
			name = mCurrentFileName;
		}

		if (mReadOnly) {
			title = getString(R.string.title_editor_readonly, name);
		} else if (mDirty) {
			title = getString(R.string.title_editor_dirty, name);
		} else {
			title = getString(R.string.title_editor, name);
		}

		setTitle(title);

		if (VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB) {
			invalidateOptionsMenu();
		}
	}

	/**
	 * Update the adapter's settings
	 */
	protected void updateFromSettings() {
		Settings.updateFromPreferences(getSharedPreferences(
				Constants.PREFERENCES_NAME, MODE_PRIVATE));
	}

	/**
	 * Opens the about activity
	 */
	protected void settingsActivity() {
		Intent about = new Intent(getApplicationContext(),
				AxelSettingsActivity.class);

		startActivity(about);
	}

	/**
	 * Opens the about activity
	 */
	protected void aboutActivity() {
		Intent about = new Intent(getApplicationContext(),
				AxelAboutActivity.class);

		startActivity(about);
	}

	/** */
	protected XmlNode mCurrentSelection;
	/** */
	protected XmlNode mDocument;
	/** */
	protected ListView mListView;
	/** */
	protected TreeAdapter<XmlData> mAdapter;

	/** the path of the file currently opened */
	protected String mCurrentFilePath;
	/** the name of the file currently opened */
	protected String mCurrentFileName;
	/** the runable to run after a save */
	protected Runnable mAfterSave; // Mennen ? Axe ?

	/** is dirty ? */
	protected boolean mDirty;
	/** is read only */
	protected boolean mReadOnly;

	/** the search layout root */
	protected View mSearchLayout;
	/** the search input */
	protected EditText mSearchInput;

	/** are we in a post activity result ? */
	protected boolean mIsResumingFromRequest;

}
