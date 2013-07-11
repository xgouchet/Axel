package fr.xgouchet.xmleditor;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.ActionMode;
import android.view.ActionMode.Callback;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import de.neofonie.mobile.app.android.widget.crouton.Crouton;
import de.neofonie.mobile.app.android.widget.crouton.Style;
import fr.xgouchet.androidlib.data.ClipboardUtils.ClipboardProxy;
import fr.xgouchet.androidlib.data.FileUtils;
import fr.xgouchet.androidlib.data.TextFileUtils;
import fr.xgouchet.xmleditor.common.AxelChangeLog;
import fr.xgouchet.xmleditor.common.AxelUtils;
import fr.xgouchet.xmleditor.common.Constants;
import fr.xgouchet.xmleditor.common.RecentFiles;
import fr.xgouchet.xmleditor.common.Settings;
import fr.xgouchet.xmleditor.common.TemplateFiles;
import fr.xgouchet.xmleditor.data.XmlEditor;
import fr.xgouchet.xmleditor.data.XmlEditor.XmlEditorListener;
import fr.xgouchet.xmleditor.data.XmlFileLoaderResult;
import fr.xgouchet.xmleditor.data.XmlFileWriterResult;
import fr.xgouchet.xmleditor.data.tree.TreeNode;
import fr.xgouchet.xmleditor.data.xml.XmlAttribute;
import fr.xgouchet.xmleditor.data.xml.XmlData;
import fr.xgouchet.xmleditor.data.xml.XmlHighlighter;
import fr.xgouchet.xmleditor.data.xml.XmlNode;
import fr.xgouchet.xmleditor.data.xml.XmlNodeStyler;
import fr.xgouchet.xmleditor.parser.xml.XmlTreeParserException.XmlError;
import fr.xgouchet.xmleditor.ui.adapter.TreeAdapter;
import fr.xgouchet.xmleditor.ui.adapter.TreeAdapter.TreeNodeEventListener;
import fr.xgouchet.xmleditor.ui.dialog.AttributeEditDialog;

/**
 * 
 */
public class AxelActivity extends Activity implements
		TreeNodeEventListener<XmlData>, OnQueryTextListener, XmlEditorListener {

	/** The underlying editor */
	private XmlEditor mEditor;

	/**
	 * 
	 */
	public AxelActivity() {
		mCurrentSelectedViews = new LinkedList<View>();
		mEditor = new XmlEditor(this, this);
	}

	/**
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.layout_editor);

		// Widgets
		mListView = (ListView) findViewById(android.R.id.list);
		mListView.setFastScrollEnabled(true);
		registerForContextMenu(mListView);

		// Editor
		mEditor.doClearContents();

		// Activity
		readIntent();
	}

	/**
	 * @see android.app.Activity#onStart()
	 */
	@Override
	protected void onStart() {
		super.onStart();
		AxelChangeLog changeLog;
		SharedPreferences prefs;

		changeLog = new AxelChangeLog();
		prefs = getSharedPreferences(Constants.PREFERENCES_NAME,
				Context.MODE_PRIVATE);
		Settings.updateFromPreferences(prefs);

		if (changeLog.displayChangeLog(this, prefs)) {
			TemplateFiles.copyTemplatesFromAssets(this);
		}

	}

	/**
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();

		if (mDocument == null) {
			mEditor.doClearContents();
		} else if (!TextUtils.isEmpty(mCurrentFilePath)) {
			File file = new File(mCurrentFilePath);
			if (file.exists()) {
				String hash = FileUtils.getFileHash(file);
				if (!hash.equals(mCurrentFileHash)) {
					promptFileChanged();
				}
			} else {
				promptFileDeleted();
			}

		}

		Settings.updateFromPreferences(getSharedPreferences(
				Constants.PREFERENCES_NAME, MODE_PRIVATE));

		mAdapter.updatePadding();
		mAdapter.notifyDataSetChanged();
	}

	/**
	 * @see Activity#onKeyUp(int, KeyEvent)
	 */
	@Override
	public boolean onKeyUp(final int keyCode, final KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_SEARCH) {
			onSearchRequested();
			return true;
		}
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			// prompt to save before quitting
			mAfterSave = new Runnable() {
				@Override
				public void run() {
					finish();
				}
			};

			promptSaveDirty();
			return true;
		}
		return super.onKeyUp(keyCode, event);
	}

	/**
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onPause() {
		super.onPause();
		mEditor.doCancelOperations();
	}

	/**
	 * @see android.app.Activity#onStop()
	 */
	@Override
	protected void onStop() {
		super.onStop();
		Crouton.clearCroutonsForActivity(this);
	}

	/**
	 * @see android.app.Activity#onActivityResult(int, int,
	 *      android.content.Intent)
	 */
	@Override
	protected void onActivityResult(final int requestCode,
			final int resultCode, final Intent data) {
		Bundle extras;

		if (resultCode == RESULT_OK) {

			extras = data.getExtras();
			if (extras != null) {
				switch (requestCode) {
				case Constants.REQUEST_SAVE_AS:
					mEditor.doSaveFile(extras.getString(Constants.EXTRA_PATH));
					break;
				case Constants.REQUEST_OPEN:
					File file = new File(extras.getString(Constants.EXTRA_PATH));
					mEditor.doOpenFile(file, extras.getBoolean(
							Constants.EXTRA_IGNORE_FILE, false));
					break;
				case Constants.REQUEST_EDIT_NODE:
					getAxelApplication().getCurrentSelection()
							.onContentChanged();
					// FIXME onXmlContentChanged(true);
					break;
				case Constants.REQUEST_SORT_CHILDREN:
					// FIXME onXmlContentChanged(true);
					break;
				}
			}
		}
	}

	/**
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		super.onCreateOptionsMenu(menu);

		MenuInflater inflater = new MenuInflater(this);
		inflater.inflate(R.menu.main, menu);

		// generate search view for the search menu
		SearchView search;
		if (VERSION.SDK_INT >= VERSION_CODES.ICE_CREAM_SANDWICH) {
			search = new SearchView(getActionBar().getThemedContext());
		} else {
			search = new SearchView(this);
		}
		search.setQueryHint(getString(android.R.string.search_go));
		search.setOnQueryTextListener(this);

		menu.findItem(R.id.menu_search).setActionView(search);

		return true;
	}

	/**
	 * @see android.app.Activity#onPrepareOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onPrepareOptionsMenu(final Menu menu) {
		super.onPrepareOptionsMenu(menu);

		menu.findItem(R.id.menu_save).setEnabled(mEditor.isReadOnly());

		menu.findItem(R.id.menu_preview_in_browser).setEnabled(
				getAxelApplication().canBePreviewed());

		return true;
	}

	/**
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		boolean result;

		result = true;
		switch (item.getItemId()) {
		case R.id.menu_new_empty:
			newContent();
			break;
		case R.id.menu_open_file:
			openFile();
			break;
		case R.id.menu_open_recent:
			openRecentFile();
			break;
		case R.id.menu_new_template:
			openTemplateFile();
			break;
		case R.id.menu_preview_in_browser:
			previewFile();
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
		case R.id.menu_help:
			startActivity(new Intent(getApplicationContext(),
					AxelHelpActivity.class));
			break;
		case R.id.menu_settings:
			startActivity(new Intent(getApplicationContext(),
					AxelSettingsActivity.class));
			break;
		case R.id.menu_about:
			startActivity(new Intent(getApplicationContext(),
					AxelAboutActivity.class));
			break;
		default:
			result = super.onOptionsItemSelected(item);
			break;
		}

		return result;
	}

	// @Override
	public void onItemClick(final Object source, final int pos,
			final int actionId) {
		switch (actionId) {
		case R.id.action_delete:
			promptDeleteNode();
			break;
		case R.id.action_add_child:
			promptNodeAddChild();
			break;
		case R.id.action_add_attr:
			promptElementAddAttribute();
			break;
		case R.id.action_edit:
			doEditNode();
			break;
		case R.id.action_sort_children:
			doSortChildren();
			break;
		case R.id.action_comment:
			mEditor.doCommentUncommentNode();
			break;
		case R.id.action_cut:
			mEditor.doCutNode();
			break;
		case R.id.action_copy:
			mEditor.doCopyNode();
			break;
		case R.id.action_paste:
			mEditor.doPasteContentInNode();
			break;
		}
	}

	/**
	 * @see fr.xgouchet.xmleditor.ui.adapter.TreeAdapter.TreeNodeEventListener#onNodeLongPressed(fr.xgouchet.xmleditor.data.tree.TreeNode,
	 *      android.view.View, int)
	 */
	@Override
	public void onNodeLongPressed(final TreeNode<XmlData> node,
			final View view, final int position) {
		performQuickAction(node, view, Settings.sLongPressQA);
	}

	/**
	 * @see fr.xgouchet.xmleditor.ui.adapter.TreeAdapter.TreeNodeEventListener#onNodeTapped(fr.xgouchet.xmleditor.data.tree.TreeNode,
	 *      android.view.View, int)
	 */
	@Override
	public void onNodeTapped(final TreeNode<XmlData> node, final View view,
			final int position) {
		performQuickAction(node, view, Settings.sSingleTapQA);
	}

	/**
	 * @see fr.xgouchet.xmleditor.ui.adapter.TreeAdapter.TreeNodeEventListener#onNodeDoubleTapped(fr.xgouchet.xmleditor.data.tree.TreeNode,
	 *      android.view.View, int)
	 */
	@Override
	public void onNodeDoubleTapped(final TreeNode<XmlData> node,
			final View view, final int position) {
		performQuickAction(node, view, Settings.sDoubleTapQA);
	}

	/**
	 * Callback when the file has been read
	 * 
	 * @param result
	 */
	@Deprecated
	public void onFileOpened(final XmlFileLoaderResult result) {
		File file = result.getFile();

		if (result.getError() == XmlError.noError) {
			mDocument = result.getDocument();

			if ((file == null) || (result.hasIgnoreFile())) {
				mCurrentEncoding = null;
				mCurrentFileName = null;
				mCurrentFilePath = null;
				// mReadOnly = false;
			} else {
				mCurrentEncoding = result.getEncoding();
				mCurrentFileName = file.getName();
				mCurrentFilePath = file.getPath();
				mCurrentFileHash = result.getFileHash();
				// mReadOnly = result.hasForceReadOnly() || (!file.canWrite());

				// if (mReadOnly) {
				// Crouton.showText(this, R.string.toast_open_read_only,
				// Style.INFO);
				// }

				RecentFiles.updateRecentList(mCurrentFilePath);
				RecentFiles.saveRecentList(getSharedPreferences(
						Constants.PREFERENCES_NAME, MODE_PRIVATE));
			}

			if (result.isHtmlSoup()) {
				Crouton.showText(this, R.string.toast_html_soup, Style.INFO);
			}

			// TODO onXmlDocumentChanged();
		} else {
			switch (result.getError()) {
			case featureUnavailable:
				Crouton.showText(this, R.string.toast_xml_unsupported_feature,
						Style.ALERT);
				break;
			case noParser:
				Crouton.showText(this, R.string.toast_xml_no_parser_found,
						Style.ALERT);
				break;
			case fileNotFound:
			case noInput:
				Crouton.showText(this, R.string.toast_open_not_found_error,
						Style.ALERT);
				break;
			case outOfMemory:
				Crouton.showText(this, R.string.toast_open_memory_error,
						Style.ALERT);
				break;
			case noError:
				break;
			case parseException:
				if (AxelUtils.isHtmlDocument(file)) {
					promptOpenHtmlError(file, result.hasForceReadOnly());
				} else if (AxelUtils.isXmlDocument(file)) {
					promptOpenError(file);
				} else {
					Crouton.showText(this, R.string.toast_xml_parse_error,
							Style.ALERT);
				}
				break;
			case ioException:
				Crouton.showText(this, R.string.toast_xml_io_exception,
						Style.ALERT);
				break;
			case unknown:
			default:
				Crouton.showText(this, R.string.toast_open_error, Style.ALERT);
				break;

			}
		}
	}

	/**
	 * @param result
	 */
	@Deprecated
	public void onFileSaved(final XmlFileWriterResult result) {

		if (result.getError() == XmlError.noError) {
			File file = new File(result.getPath());
			mCurrentFilePath = FileUtils.getCanonizePath(file);
			mCurrentFileName = file.getName();
			mCurrentFileHash = FileUtils.getFileHash(file);
			RecentFiles.updateRecentList(result.getPath());
			RecentFiles.saveRecentList(getSharedPreferences(
					Constants.PREFERENCES_NAME, MODE_PRIVATE));
			// mReadOnly = false;
			// mDirty = false;
			// TODO onXmlDocumentChanged();

			Crouton.showText(this, R.string.toast_save_success, Style.CONFIRM);

			runAfterSave();
		} else {
			switch (result.getError()) {
			case delete:
				Crouton.showText(this, R.string.toast_save_delete, Style.ALERT);
				break;
			case rename:
				Crouton.showText(this, R.string.toast_save_rename, Style.ALERT);
				break;
			case write:
				Crouton.showText(this, R.string.toast_save_temp, Style.ALERT);
				break;
			case outOfMemory:
				Crouton.showText(this, R.string.toast_open_memory_error,
						Style.ALERT);
				break;
			default:
				Crouton.showText(this, R.string.toast_save_null, Style.ALERT);
				break;
			}
		}
	}

	/**
	 * @see android.widget.SearchView.OnQueryTextListener#onQueryTextChange(java.lang.String)
	 */
	@Override
	public boolean onQueryTextChange(final String newText) {
		if (TextUtils.isEmpty(newText)) {
			mAdapter.setHighlighter(null);
			mAdapter.notifyDataSetChanged();
		} else {
			mAdapter.setHighlighter(new XmlHighlighter(newText));
			mAdapter.notifyDataSetChanged();
		}
		return true;
	}

	/**
	 * @see android.widget.SearchView.OnQueryTextListener#onQueryTextSubmit(java.lang.String)
	 */
	@Override
	public boolean onQueryTextSubmit(final String query) {

		return true;
	}

	/**
	 * @see fr.xgouchet.xmleditor.data.XmlEditor.XmlEditorListener#onXmlDocumentChanged(fr.xgouchet.xmleditor.data.xml.XmlNode)
	 */
	@Override
	public void onXmlDocumentChanged(final XmlNode root, final String name,
			final String path) {
		getAxelApplication().setCurrentDocument(root, name, path);

		mAdapter = new TreeAdapter<XmlData>(this, root);
		mAdapter.setNodeStyler(new XmlNodeStyler());
		mAdapter.setListener(this);
		mListView.setAdapter(mAdapter);
		mAdapter.setHighlighter(null);
	}

	/**
	 * @see fr.xgouchet.xmleditor.data.XmlEditor.XmlEditorListener#onXmlContentChanged()
	 */
	@Override
	public void onXmlContentChanged() {
		if (mAdapter != null) {
			mAdapter.notifyDataSetChanged();
		}

		updateTitle();
		getAxelApplication().documentContentChanged();

		clearSelectedViews(true);
	}

	/**
	 * @see fr.xgouchet.xmleditor.data.XmlEditor.XmlEditorListener#onXmlConfirmNotification(java.lang.String)
	 */
	@Override
	public void onXmlConfirmNotification(final String message) {
		Crouton.makeText(this, message, Style.CONFIRM).show();
	}

	/**
	 * @see fr.xgouchet.xmleditor.data.XmlEditor.XmlEditorListener#onXmlErrorNotification(java.lang.String)
	 */
	@Override
	public void onXmlErrorNotification(final String message) {
		Crouton.makeText(this, message, Style.ALERT).show();
	}

	/**
	 * Performs a quick action on a node element
	 */
	private void performQuickAction(final TreeNode<XmlData> node,
			final View view, final String action) {

		if (Constants.QUICK_ACTION_NONE.equals(action)) {
			// do nothing, yeah !!!
		} else if (Constants.QUICK_ACTION_DISPLAY_MENU.equals(action)) {
			// displayQuickAction((XmlNode) node, view);
			startNodeActionMode((XmlNode) node);
		} else if (mEditor.isReadOnly()) {
			mCurrentSelection = (XmlNode) node;
			if (Constants.QUICK_ACTION_ADD_CHILD.equals(action)) {
				if (mCurrentSelection.isElement()
						|| mCurrentSelection.isDocument()) {
					// doAddChildToNode(XmlNode.createElement("element"), true);
					promptNodeAddChild();
				}
			} else if (Constants.QUICK_ACTION_COMMENT_TOGGLE.equals(action)) {
				if ((!mCurrentSelection.isDocument())
						&& (!mCurrentSelection.isDocumentDeclaration())) {
					mEditor.doCommentUncommentNode();
				}
			} else if (Constants.QUICK_ACTION_DELETE.equals(action)) {
				if ((!mCurrentSelection.isDocument())
						&& (!mCurrentSelection.isDocumentDeclaration())) {
					promptDeleteNode();
				}
			} else if (Constants.QUICK_ACTION_EDIT.equals(action)) {
				if (!mCurrentSelection.isDocument()) {
					doEditNode();
				}
			} else if (Constants.QUICK_ACTION_ORDER_CHILDREN.equals(action)) {
				if (mCurrentSelection.hasChildren()) {
					doSortChildren();
				}
			}
		}
	}

	private void startNodeActionMode(final XmlNode node) {
		startActionMode(new Callback() {

			@Override
			public boolean onCreateActionMode(final ActionMode mode,
					final Menu menu) {
				// TODO Auto-generated method stub
				return true;
			}

			@Override
			public boolean onPrepareActionMode(final ActionMode mode,
					final Menu menu) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public void onDestroyActionMode(final ActionMode mode) {
				// TODO Auto-generated method stub

			}

			@Override
			public boolean onActionItemClicked(final ActionMode mode,
					final MenuItem item) {
				// TODO Auto-generated method stub
				return false;
			}
		});
	}

	/**
	 * 
	 * @param node
	 * @param view
	 */
	@Deprecated
	private void displayQuickAction(final XmlNode node, final View view) {
		if ((node == null) || (node.getContent() == null)) {
			return;
		}

		mCurrentSelection = node;
		// QuickAction quickAction = new QuickAction(this);

		if (node.isDocument() || node.isElement()) {
			// quickAction.addActionItem(R.id.action_add_child,
			// R.string.action_add_child, R.drawable.ic_action_add_child);
		}

		if (node.isElement()) {
			// quickAction.addActionItem(R.id.action_add_attr,
			// R.string.menu_add_attribute, R.drawable.ic_action_add_attr);

			if (node.getChildrenCount() > 1) {
				// quickAction.addActionItem(R.id.action_sort_children,
				// R.string.action_sort_children,
				// R.drawable.ic_action_sort);
			}
		}

		if (!node.isDocument()) {
			// quickAction.addActionItem(R.id.action_edit, R.string.action_edit,
			// R.drawable.ic_action_edit);

			if (node.isComment()) {
				// quickAction.addActionItem(R.id.action_comment,
				// R.string.action_uncomment,
				//
				// R.drawable.ic_action_comment);
			} else if (!node.isDocumentDeclaration()) {
				// quickAction.addActionItem(R.id.action_comment,
				// R.string.action_comment, R.drawable.ic_action_comment);
			}
			if (!node.isDocumentDeclaration()) {
				// quickAction.addActionItem(R.id.action_delete,
				// R.string.action_delete, R.drawable.ic_action_delete);
			}
		}

		if (!(node.isDocument() || node.isDocumentDeclaration())) {
			// quickAction.addActionItem(R.id.action_cut, R.string.action_cut,
			// R.drawable.ic_action_cut);
		}
		if (!node.isDocumentDeclaration()) {
			// quickAction.addActionItem(R.id.action_copy, R.string.action_copy,
			// R.drawable.ic_action_copy);
		}

		if (node.isDocument() || node.isElement()) {
			if (mClipboard.hasTextContent()) {
				// quickAction.addActionItem(R.id.action_paste,
				// R.string.action_paste, R.drawable.ic_action_paste);
			}
		}

		// quickAction.setOnActionItemClickListener(this);
		// quickAction.show(view);
	}

	/**
	 * Read the intent used to start this activity (open the text file) as well
	 * as the non configuration instance if activity is started after a screen
	 * rotate
	 */
	private void readIntent() {
		Intent intent;
		String action;
		File file;

		intent = getIntent();
		if (intent == null) {
			mEditor.doClearContents();
			return;
		}

		action = intent.getAction();
		if (action == null) {
			mEditor.doClearContents();
		} else if ((action.equals(Intent.ACTION_VIEW))
				|| (action.equals(Intent.ACTION_EDIT))) {
			try {
				file = new File(new URI(intent.getData().toString()));
				mEditor.doOpenFile(file, false);
			} catch (URISyntaxException e) {
				Crouton.makeText(this, R.string.toast_intent_invalid_uri,
						Style.ALERT).show();
			} catch (IllegalArgumentException e) {
				Crouton.makeText(this, R.string.toast_intent_illegal,
						Style.ALERT).show();
			}
		} else {
			mEditor.doClearContents();
		}
	}

	/**
	 * Clears the current content to make a new file
	 */
	private void newContent() {
		mAfterSave = new Runnable() {
			@Override
			public void run() {
				mEditor.doClearContents();
			}
		};

		promptSaveDirty();
	}

	/**
	 * Starts an activity to choose a file to open
	 */
	private void openFile() {
		mAfterSave = new Runnable() {
			@Override
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
	private void openRecentFile() {

		if (RecentFiles.getRecentFiles().size() == 0) {
			Crouton.makeText(this, R.string.toast_no_recent_files, Style.INFO)
					.show();
		} else {
			mAfterSave = new Runnable() {
				@Override
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
	private void openTemplateFile() {

		if (TemplateFiles.getTemplateFiles(this).size() == 0) {
			Crouton.makeText(this, R.string.toast_no_template_files, Style.INFO)
					.show();
		} else {
			mAfterSave = new Runnable() {
				@Override
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
	 * Prompts to save if dirty before openning the current file
	 */
	private void previewFile() {
		mAfterSave = new Runnable() {
			@Override
			public void run() {
				doPreviewFile();
			}
		};

		promptSaveDirty();
	}

	/**
	 * General save command : check if a path exist for the current content,
	 * then save it , else invoke the {@link AxelActivity#saveContentAs()}
	 * method
	 */
	private void saveContent() {
		if (mEditor.hasPath()) {
			saveContentAs();
		} else {
			mEditor.doSaveFile();
		}
	}

	/**
	 * General Save as command : prompt the user for a location and file name,
	 * then save the editor's content
	 */
	private void saveContentAs() {
		Intent saveAs;

		saveAs = new Intent(getApplicationContext(), AxelSaveAsActivity.class);
		startActivityForResult(saveAs, Constants.REQUEST_SAVE_AS);
	}

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
	 * Prompt the user before deleting a Node
	 */
	private void promptDeleteNode() {
		final AlertDialog.Builder builder;

		builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.ui_delete);
		builder.setCancelable(true);
		builder.setMessage(R.string.ui_prompt_delete_node);

		builder.setPositiveButton(R.string.ui_delete,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(final DialogInterface dialog,
							final int which) {
						mEditor.doDeleteNode();
					}
				});
		builder.setNegativeButton(R.string.ui_cancel,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(final DialogInterface dialog,
							final int which) {
					}
				});

		builder.create().show();
	}

	/**
	 * Prompts the user that the source file has been deleted
	 */
	private void promptFileDeleted() {
		final AlertDialog.Builder builder;

		builder = new AlertDialog.Builder(this);
		builder.setIcon(R.drawable.ic_dialog_alert);
		builder.setTitle(R.string.ui_open_error);
		builder.setCancelable(true);
		builder.setMessage(R.string.ui_prompt_file_deleted);

		builder.setPositiveButton(R.string.ui_save,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(final DialogInterface dialog,
							final int which) {
						saveContent();
					}
				});

		builder.setNeutralButton(R.string.menu_save_as,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(final DialogInterface dialog,
							final int which) {
						saveContentAs();
					}
				});

		builder.create().show();
	}

	/**
	 * Prompts the user that the source file has changed on disk
	 */
	private void promptFileChanged() {
		final AlertDialog.Builder builder;

		builder = new AlertDialog.Builder(this);
		builder.setIcon(R.drawable.ic_dialog_alert);
		builder.setTitle(R.string.ui_open_error);
		builder.setCancelable(true);
		builder.setMessage(R.string.ui_prompt_file_changed);

		builder.setPositiveButton(R.string.ui_yes,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(final DialogInterface dialog,
							final int which) {
						mEditor.doReloadCurrentFile();
					}
				});
		builder.setNegativeButton(R.string.ui_no,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(final DialogInterface dialog,
							final int which) {
					}
				});

		builder.create().show();
	}

	/**
	 * Prompts the user what to do when an error occurs parsing the file
	 * 
	 * @param file
	 *            the file to open
	 */
	private void promptOpenError(final File file) {
		final AlertDialog.Builder builder;

		builder = new AlertDialog.Builder(this);
		builder.setIcon(R.drawable.ic_dialog_alert);
		builder.setTitle(R.string.ui_open_error);
		builder.setCancelable(true);
		builder.setMessage(R.string.ui_prompt_open_error);

		builder.setPositiveButton(R.string.ui_send_file,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(final DialogInterface dialog,
							final int which) {
						doSendFile(file);
					}
				});
		builder.setNegativeButton(R.string.ui_cancel,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(final DialogInterface dialog,
							final int which) {
					}
				});

		builder.create().show();
	}

	/**
	 * Prompts the user what to do when an error occurs parsing an HTML file
	 * 
	 * @param file
	 *            the file to open
	 * @param forceReadOnly
	 *            force the file as read only
	 */
	private void promptOpenHtmlError(final File file,
			final boolean forceReadOnly) {
		final AlertDialog.Builder builder;

		builder = new AlertDialog.Builder(this);
		builder.setIcon(R.drawable.ic_dialog_alert);
		builder.setTitle(R.string.ui_open_error);
		builder.setCancelable(true);
		builder.setMessage(R.string.ui_prompt_open_error_html);

		builder.setPositiveButton(R.string.ui_send_file,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(final DialogInterface dialog,
							final int which) {
						doSendFile(file);
					}
				});
		builder.setNeutralButton(R.string.ui_convert_html,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(final DialogInterface dialog,
							final int which) {
						mEditor.doOpenFileAsHtml(file, forceReadOnly);
					}
				});
		builder.setNegativeButton(R.string.ui_cancel,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(final DialogInterface dialog,
							final int which) {
					}
				});

		builder.create().show();
	}

	/**
	 * Prompt the user to save the current file before doing something else
	 */
	private void promptSaveDirty() {
		Builder builder;

		if (!mEditor.isDirty()) {
			runAfterSave();
			return;
		}

		builder = new Builder(this);
		builder.setTitle(R.string.app_name);
		builder.setMessage(R.string.ui_save_text);

		builder.setPositiveButton(R.string.ui_save,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(final DialogInterface dialog,
							final int which) {
						saveContent();
					}
				});
		builder.setNegativeButton(R.string.ui_cancel,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(final DialogInterface dialog,
							final int which) {

					}
				});
		builder.setNeutralButton(R.string.ui_no_save,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(final DialogInterface dialog,
							final int which) {
						runAfterSave();
					}
				});

		builder.create().show();

	}

	/**
	 * Prompt the user for a template name
	 */
	private void promptTemplateName() {

		final EditText input = new EditText(this);
		input.setHint(R.string.ui_hint_name);

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.ui_hint_template_name);
		builder.setView(input);

		builder.setNegativeButton(R.string.ui_cancel, null);
		builder.setPositiveButton(R.string.ui_save, null);
		final AlertDialog dialog = builder.create();
		dialog.show();

		dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(final View v) {
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

	private void promptElementAddAttribute() {
		final AttributeEditDialog dlg;

		dlg = new AttributeEditDialog(this, LayoutInflater.from(this), null);
		dlg.setNode(mCurrentSelection);
		dlg.setSiblingsAttribute(mCurrentSelection.getContent().getAttributes());

		dlg.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss(final DialogInterface dialog) {
				XmlAttribute attr = dlg.getAttribute();
				if (attr != null) {
					mCurrentSelection.getContent().addAttribute(attr);
					// FIXME onXmlContentChanged(true);
				}
			}
		});
		dlg.show();
	}

	private void promptNodeAddChild() {
		Builder builder = new Builder(this);

		final String[] options;
		if (mCurrentSelection.isElement()) {
			options = new String[] { getString(R.string.action_add_element),
					getString(R.string.action_add_text),
					getString(R.string.action_add_cdata),
					getString(R.string.action_add_pi),
					getString(R.string.action_add_comment) };
		} else if (mCurrentSelection.isDocument()) {
			if (mCurrentSelection.hasDoctype()) {
				if (mCurrentSelection.hasRootChild()) {
					options = new String[] { getString(R.string.action_add_pi),
							getString(R.string.action_add_comment) };
				} else {
					options = new String[] {
							getString(R.string.action_add_element),
							getString(R.string.action_add_pi),
							getString(R.string.action_add_comment) };
				}
			} else {
				if (mCurrentSelection.hasRootChild()) {
					options = new String[] {
							getString(R.string.action_add_doctype),
							getString(R.string.action_add_pi),
							getString(R.string.action_add_comment) };
				} else {
					options = new String[] {
							getString(R.string.action_add_element),
							getString(R.string.action_add_doctype),
							getString(R.string.action_add_pi),
							getString(R.string.action_add_comment) };
				}
			}
		} else {
			return;
		}

		builder.setTitle(R.string.action_add_child);
		builder.setItems(options, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(final DialogInterface dialog, final int which) {
				XmlNode node = getXmlNode(options[which]);
				if (node != null) {
					doAddChildToNode(node, true);
				}
			}
		});
		builder.setCancelable(true);

		builder.create().show();
	}

	/**
	 * Called when a file is successfully parsed
	 * 
	 * @param file
	 *            the source file
	 * @param document
	 *            the root node (document)
	 * @param encoding
	 *            the file encoding
	 * @param forceReadOnly
	 *            force the editor to be read only
	 * @param ignoreFile
	 *            ignore the file
	 */
	// private void onFileParsed(final File file, final XmlNode document,
	// final String encoding, final boolean forceReadOnly,
	// final boolean ignoreFile) {
	// mDocument = document;
	// mReadOnly = forceReadOnly;
	//
	// if (!ignoreFile) {
	// mCurrentFileName = file.getName();
	// mCurrentFilePath = FileUtils.getCanonizePath(file);
	// mCurrentEncoding = encoding;
	//
	// RecentFiles.updateRecentList(mCurrentFilePath);
	// RecentFiles.saveRecentList(getSharedPreferences(
	// Constants.PREFERENCES_NAME, MODE_PRIVATE));
	//
	// mReadOnly |= !file.canWrite();
	// }
	//
	// onXmlDocumentChanged();
	// }

	/**
	 * Opens a Web view to preview the current file
	 */
	private void doPreviewFile() {
		Intent intent = new Intent();
		intent.setClass(getBaseContext(), AxelPreviewActivity.class);
		startActivity(intent);
	}

	/**
	 * @param fileName
	 *            saves the template
	 */
	private void doSaveTemplate(final String fileName) {
		StringBuilder builder;
		String path = TemplateFiles.getOuputPath(this, fileName);

		builder = new StringBuilder();
		mDocument.buildXmlString(builder);

		if (!TextFileUtils.writeTextFile(path, builder.toString(),
				mCurrentEncoding)) {
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
	private void doSendFile(final File file) {

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
	private void doAddChildToNode(final XmlNode node, final boolean edit) {
		if (mCurrentSelection.addChildNode(node)) {
			if (mCurrentSelection.isDocument()) {
				mCurrentSelection.reorderDocumentChildren();
			} else {
				// todo reorder element chidlren based on validator
			}

			node.updateChildViewCount(false);
			mCurrentSelection.updateChildViewCount(false);
			mCurrentSelection.setExpanded(true, false);

			if (edit && Settings.sEditOnCreate) {
				mCurrentSelection = node;
				doEditNode();
			} else {
				mCurrentSelection = null;
			}

			// FIXME onXmlContentChanged(true);

		}
	}

	/**
	 * Opens an editor for the selected node
	 */
	private void doEditNode() {
		getAxelApplication().setCurrentSelection(mCurrentSelection);

		Intent edit = new Intent(getApplicationContext(),
				AxelNodeEditorActivity.class);
		edit.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
		startActivityForResult(edit, Constants.REQUEST_EDIT_NODE);
	}

	/**
	 * Opens an editor to sort an element's children;
	 */
	private void doSortChildren() {
		getAxelApplication().setCurrentSelection(mCurrentSelection);

		Intent edit = new Intent(getApplicationContext(),
				AxelSortActivity.class);

		startActivityForResult(edit, Constants.REQUEST_SORT_CHILDREN);
	}

	/**
	 * Update the window title
	 */
	private void updateTitle() {
		String title;
		String name;

		name = "?";
		if ((mCurrentFileName != null) && (mCurrentFileName.length() > 0)) {
			name = mCurrentFileName;
		}

		if (mEditor.isReadOnly()) {
			title = getString(R.string.title_editor_readonly, name);
		} else if (mEditor.isDirty()) {
			title = getString(R.string.title_editor_dirty, name);
		} else {
			title = getString(R.string.title_editor, name);
		}

		setTitle(title);

		invalidateOptionsMenu();
	}

	/**
	 * @return the current Application
	 */
	private AxelApplication getAxelApplication() {
		if (mAxelApplication == null) {
			mAxelApplication = (AxelApplication) getApplication();
		}
		return mAxelApplication;
	}

	/**
	 * @param name
	 *            the displayed name to prompt for a child node
	 * @return the children node
	 */
	private XmlNode getXmlNode(final String name) {
		XmlNode node;
		if (getString(R.string.action_add_element).equals(name)) {
			node = XmlNode.createElement("element");
		} else if (getString(R.string.action_add_doctype).equals(name)) {
			node = XmlNode
					.createDoctypeDeclaration("root SYSTEM \"DTD location\"");
		} else if (getString(R.string.action_add_pi).equals(name)) {
			node = XmlNode.createProcessingInstruction("target", "instruction");
		} else if (getString(R.string.action_add_comment).equals(name)) {
			node = XmlNode.createComment("Comment");
		} else if (getString(R.string.action_add_text).equals(name)) {
			node = XmlNode.createText("Text");
		} else if (getString(R.string.action_add_cdata).equals(name)) {
			node = XmlNode.createCDataSection("Unparsed data");
		} else {
			node = null;
		}
		return node;
	}

	/**
	 * Clear all selected views
	 */
	@SuppressWarnings("deprecation")
	private void clearSelectedViews(final boolean editBackground) {
		if (editBackground) {
			for (View view : mCurrentSelectedViews) {
				view.setBackgroundDrawable(null);
			}
		}
		mCurrentSelectedViews.clear();
	}

	/** */
	private XmlNode mCurrentSelection;
	private final List<View> mCurrentSelectedViews;

	/** */
	@Deprecated
	private XmlNode mDocument;
	/** */
	private ListView mListView;
	/** */
	private TreeAdapter<XmlData> mAdapter;

	/** the path of the file currently opened */
	@Deprecated
	private String mCurrentFilePath;
	/** the last known hash of the file currently opened */
	@Deprecated
	private String mCurrentFileHash;
	/** the name of the file currently opened */
	@Deprecated
	private String mCurrentFileName;
	/** the file's encoding */
	@Deprecated
	private String mCurrentEncoding;

	/** the runable to run after a save */
	private Runnable mAfterSave; // Mennen ? Axe ?

	private AxelApplication mAxelApplication;
	private ClipboardProxy mClipboard;

}
