package fr.xgouchet.xmleditor;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.EditText;
import android.widget.ListView;
import de.neofonie.mobile.app.android.widget.crouton.Crouton;
import de.neofonie.mobile.app.android.widget.crouton.Style;
import fr.xgouchet.androidlib.data.FileUtils;
import fr.xgouchet.androidlib.data.TextFileUtils;
import fr.xgouchet.xmleditor.common.AxelChangeLog;
import fr.xgouchet.xmleditor.common.AxelUtils;
import fr.xgouchet.xmleditor.common.Constants;
import fr.xgouchet.xmleditor.common.RecentFiles;
import fr.xgouchet.xmleditor.common.Settings;
import fr.xgouchet.xmleditor.common.TemplateFiles;
import fr.xgouchet.xmleditor.data.AsyncHtmlFileLoader;
import fr.xgouchet.xmleditor.data.AsyncXmlFileLoader;
import fr.xgouchet.xmleditor.data.AsyncXmlFileWriter;
import fr.xgouchet.xmleditor.data.XmlFileLoaderResult;
import fr.xgouchet.xmleditor.data.XmlFileWriterResult;
import fr.xgouchet.xmleditor.data.tree.TreeNode;
import fr.xgouchet.xmleditor.data.xml.XmlAttribute;
import fr.xgouchet.xmleditor.data.xml.XmlData;
import fr.xgouchet.xmleditor.data.xml.XmlNode;
import fr.xgouchet.xmleditor.data.xml.XmlNodeStyler;
import fr.xgouchet.xmleditor.data.xml.XmlTreeParserException;
import fr.xgouchet.xmleditor.data.xml.XmlTreeParserException.XmlError;
import fr.xgouchet.xmleditor.data.xml.XmlTreePullParser;
import fr.xgouchet.xmleditor.ui.AttributeEditDialog;
import fr.xgouchet.xmleditor.ui.adapter.TreeAdapter;
import fr.xgouchet.xmleditor.ui.adapter.TreeAdapter.TreeNodeEventListener;

/**
 * 
 */
public class AxelActivity extends Activity implements
		TreeNodeEventListener<XmlData> {

	/**
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.layout_editor);

		Settings.updateFromPreferences(getSharedPreferences(
				Constants.PREFERENCES_NAME, MODE_PRIVATE));

		// Editor
		mListView = (ListView) findViewById(android.R.id.list);
		mListView.setFastScrollEnabled(true);
		registerForContextMenu(mListView);
		doClearContents();

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
			doClearContents();
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
		if (mLoader != null) {
			mLoader.cancel(true);
		}
	}

	/**
	 * @see android.app.Activity#onConfigurationChanged(android.content.res.Configuration)
	 */
	@Override
	public void onConfigurationChanged(final Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		Log.i("Axel", "onConfigurationChanged");
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
					doSaveFile(extras.getString(Constants.EXTRA_PATH));
					break;
				case Constants.REQUEST_OPEN:
					File file = new File(extras.getString(Constants.EXTRA_PATH));
					doOpenFile(file, extras.getBoolean(
							Constants.EXTRA_IGNORE_FILE, false));
					break;
				case Constants.REQUEST_EDIT_NODE:
					getAxelApplication().getCurrentSelection()
							.onContentChanged();
					onXmlContentChanged(true);
					break;
				case Constants.REQUEST_SORT_CHILDREN:
					onXmlContentChanged(true);
					break;
				}
			}
		}
	}

	/**
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		super.onCreateOptionsMenu(menu);

		MenuInflater inflater = new MenuInflater(this);
		inflater.inflate(R.menu.main, menu);

		return true;
	}

	/**
	 * @see android.app.Activity#onPrepareOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onPrepareOptionsMenu(final Menu menu) {
		super.onPrepareOptionsMenu(menu);

		menu.findItem(R.id.menu_save).setEnabled(!mReadOnly);

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

	/**
	 * @see android.app.Activity#onCreateContextMenu(android.view.ContextMenu,
	 *      android.view.View, android.view.ContextMenu.ContextMenuInfo)
	 */
	@Override
	public void onCreateContextMenu(final ContextMenu menu, final View view,
			final ContextMenuInfo menuInfo) {
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
	protected void onCreateListItemContextMenu(final ContextMenu menu,
			final View view, final ContextMenuInfo menuInfo) {
		MenuInflater inflater = new MenuInflater(this);
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
		XmlNode node = (XmlNode) mAdapter.getNode(info.position);
		if ((node == null) || (node.getContent() == null)) {
			return;
		}

		mCurrentSelection = node;

		if (!mReadOnly) {
			if (node.isDocument()) {
				inflater.inflate(R.menu.xml_doc_context, menu);
				menu.findItem(R.id.action_add_element).setVisible(
						!mDocument.hasRootChild());
				menu.findItem(R.id.action_add_doctype).setVisible(
						!mDocument.hasDoctype());
			} else {
				if (node.isElement()) {
					inflater.inflate(R.menu.xml_tag_context, menu);

					menu.findItem(R.id.action_sort_children).setVisible(
							node.getChildrenCount() > 1);
				}

				inflater.inflate(R.menu.xml_context, menu);

				if (node.isComment()) {
					menu.findItem(R.id.action_comment_uncomment).setTitle(
							R.string.action_uncomment);
				} else if (node.isDocumentDeclaration()) {
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
	@Override
	public boolean onContextItemSelected(final MenuItem item) {

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
		case R.id.menu_add_attribute:
			promptElementAddAttribute();
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
	 * @see fr.xgouchet.xmleditor.ui.adapter.TreeAdapter.TreeNodeEventListener#onNodeLongPressed(fr.xgouchet.xmleditor.data.tree.TreeNode,
	 *      android.view.View)
	 */
	@Override
	public void onNodeLongPressed(final TreeNode<XmlData> node, final View view) {
		String action = Settings.sLongPressQA;
		performQuickAction(node, view, action);
	}

	/**
	 * @see fr.xgouchet.xmleditor.ui.adapter.TreeAdapter.TreeNodeEventListener#onNodeTapped(fr.xgouchet.xmleditor.data.tree.TreeNode,
	 *      android.view.View)
	 */
	@Override
	public void onNodeTapped(final TreeNode<XmlData> node, final View view) {
		String action = Settings.sSingleTapQA;
		performQuickAction(node, view, action);
	}

	/**
	 * @see fr.xgouchet.xmleditor.ui.adapter.TreeAdapter.TreeNodeEventListener#onNodeDoubleTapped(fr.xgouchet.xmleditor.data.tree.TreeNode,
	 *      android.view.View)
	 */
	@Override
	public void onNodeDoubleTapped(final TreeNode<XmlData> node, final View view) {
		String action = Settings.sDoubleTapQA;
		performQuickAction(node, view, action);
	}

	/**
	 * Callback when the file has been read
	 * 
	 * @param result
	 */
	public void onFileOpened(final XmlFileLoaderResult result) {
		File file = result.getFile();

		if (result.getError() == XmlError.noError) {
			mDocument = result.getDocument();

			if ((file == null) || (result.hasIgnoreFile())) {
				mCurrentEncoding = null;
				mCurrentFileName = null;
				mCurrentFilePath = null;
				mReadOnly = false;
			} else {
				mCurrentEncoding = result.getEncoding();
				mCurrentFileName = file.getName();
				mCurrentFilePath = file.getPath();
				mCurrentFileHash = result.getFileHash();
				mReadOnly = result.hasForceReadOnly() || (!file.canWrite());

				RecentFiles.updateRecentList(mCurrentFilePath);
				RecentFiles.saveRecentList(getSharedPreferences(
						Constants.PREFERENCES_NAME, MODE_PRIVATE));
			}

			if (result.isHtmlSoup()) {
				Crouton.showText(this, R.string.toast_html_soup, Style.INFO);
			}

			onXmlDocumentChanged();
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

		mLoader = null;
	}

	/**
	 * @param result
	 */
	public void onFileSaved(final XmlFileWriterResult result) {

		if (result.getError() == XmlError.noError) {
			File file = new File(result.getPath());
			mCurrentFilePath = FileUtils.getCanonizePath(file);
			mCurrentFileName = file.getName();
			mCurrentFileHash = FileUtils.getFileHash(file);
			RecentFiles.updateRecentList(result.getPath());
			RecentFiles.saveRecentList(getSharedPreferences(
					Constants.PREFERENCES_NAME, MODE_PRIVATE));
			mReadOnly = false;
			mDirty = false;
			onXmlDocumentChanged();

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
			default:
				Crouton.showText(this, R.string.toast_save_null, Style.ALERT);
				break;
			}
		}

		mWriter = null;
	}

	/**
	 * Call this when the document changes
	 */
	private void onXmlDocumentChanged() {
		getAxelApplication().setCurrentDocument(mDocument, mCurrentFileName,
				mCurrentFilePath);

		mAdapter = new TreeAdapter<XmlData>(this, mDocument);
		mAdapter.setNodeStyler(new XmlNodeStyler());
		mAdapter.setListener(this);
		mListView.setAdapter(mAdapter);

		onXmlContentChanged(false);
	}

	/**
	 * Call when something in the xml tree changes
	 */
	private void onXmlContentChanged(final boolean dirty) {
		updateTreeView();
		if (dirty) {
			mDirty = dirty;
		}
		updateTitle();
		getAxelApplication().documentContentChanged();
	}

	/**
	 * Performs a quick action on a node element
	 */
	private void performQuickAction(final TreeNode<XmlData> node,
			final View view, final String action) {

		if (Constants.QUICK_ACTION_NONE.equals(action)) {
			// do nothing, yeah !!!
		} else if (Constants.QUICK_ACTION_DISPLAY_MENU.equals(action)) {
			mListView.showContextMenuForChild(view);
		} else if (!mReadOnly) {
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
					doCommentUncommentNode();
				}
			} else if (Constants.QUICK_ACTION_DELETE.equals(action)) {
				promptDeleteNode();
			} else if (Constants.QUICK_ACTION_EDIT.equals(action)) {
				doEditNode();
			} else if (Constants.QUICK_ACTION_ORDER_CHILDREN.equals(action)) {
				if (mCurrentSelection.hasChildren()) {
					doSortChildren();
				}
			}
		}

	}

	/**
	 * updates the tree view
	 */
	private void updateTreeView() {
		if (mDocument != null) {
			// mDocument.updateViewCount();
		}
		if (mAdapter != null) {
			mAdapter.notifyDataSetChanged();
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
				doOpenFile(file, false);
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
	private void expandCollapseAll() {
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
	private void newContent() {
		mAfterSave = new Runnable() {
			@Override
			public void run() {
				doClearContents();
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
						doDeleteNode();
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
						doReloadCurrentFile();
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
						doOpenFileAsHtml(file, forceReadOnly);
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

		if (!mDirty) {
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

		dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(
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
					onXmlContentChanged(true);
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
				Log.i("Axel", "Lets add a " + options[which]);
				XmlNode node = getXmlNode(options[which]);
				if (node != null) {
					doAddChildToNode(node, true);
				}
			}
		});
		builder.setNegativeButton(R.string.ui_cancel, null);

		builder.create().show();
	}

	/**
	 * Run the default startup action
	 */
	private void doDefaultAction() {
		doClearContents();
	}

	/**
	 * Clears the content of the editor. Assumes that user was prompted and
	 * previous data was saved
	 */
	private void doClearContents() {
		mCurrentFilePath = null;
		mCurrentFileName = null;
		mCurrentEncoding = null;
		mDirty = false;
		mReadOnly = false;

		mDocument = XmlNode.createDocument();
		mDocument.addChildNode(XmlNode.createDocumentDeclaration("1.0",
				"UTF-8", null));
		mDocument.setExpanded(true, true);
		mDocument.updateChildViewCount(true);

		onXmlDocumentChanged();
	}

	/**
	 * Send the order to open a file into the app
	 * 
	 * @param file
	 *            the source file
	 * @param ignore
	 *            ignore the file link
	 */
	private void doOpenFile(final File file, final boolean ignore) {
		if (mLoader == null) {
			int flags = 0;
			if (ignore) {
				flags |= XmlFileLoaderResult.FLAG_IGNORE_FILE;
			}

			mLoader = new AsyncXmlFileLoader(this, flags);
			mLoader.execute(file);
		}
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
	 * @return if the file was loaded successfully
	 */
	private void doOpenFileAsHtml(final File file, final boolean forceReadOnly) {
		if (mLoader == null) {
			int flags = XmlFileLoaderResult.FLAG_HTML_SOUP;

			mLoader = new AsyncHtmlFileLoader(this, flags);
			mLoader.execute(file);
		}
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
	 * Reloads the current file
	 */
	private void doReloadCurrentFile() {
		final File file = new File(mCurrentFilePath);
		doOpenFile(file, false);
	}

	/**
	 * Saves the text editor's content into a file at the given path. If an
	 * after save {@link Runnable} exists, run it
	 * 
	 * @param path
	 *            the path to the file (must be a valid path and not null)
	 */
	private void doSaveFile(final String path) {
		if (path == null) {
			Crouton.makeText(this, R.string.toast_save_null, Style.ALERT)
					.show();
			return;
		}

		if (mWriter == null) {
			mWriter = new AsyncXmlFileWriter(this, mDocument, mCurrentEncoding);
			mWriter.execute(path);
		}

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

			onXmlContentChanged(true);

		}
	}

	/**
	 * Comment or uncomment a node
	 */
	private void doCommentUncommentNode() {
		if (mCurrentSelection.isComment()) {
			doUncommentNode();
		} else {
			doCommentNode();
		}
	}

	/**
	 * Uncomment a comment node TODO uncomment a CDATA BLOCK, uncomment a PI
	 */
	private void doUncommentNode() {
		XmlNode node = null, parent;
		int index;

		node = getCommentContent();

		if (node != null) {
			parent = (XmlNode) mCurrentSelection.getParent();
			if (parent.canHasChild(node)) {
				node.setExpanded(true);
				node.updateChildViewCount(true);

				index = parent.getChildPosition(mCurrentSelection);
				parent.removeChildNode(mCurrentSelection);
				parent.addChildNode(node, index);

				parent.updateParentViewCount();

				onXmlContentChanged(true);
			} else {
				Crouton.makeText(this, R.string.toast_xml_uncomment,
						Style.ALERT).show();
			}
		}
	}

	/**
	 * @return the content of the current selected node (comment) as an XML node
	 */
	private XmlNode getCommentContent() {
		XmlNode node = null, doc = null;
		InputStream input;

		String content = mCurrentSelection.getContent().getText();
		if (content.indexOf('<') < 0) {
			node = XmlNode.createText(content);
		} else if (content
				.matches("^\\s*<\\?\\s*([a-zA-Z:_])([a-zA-Z0-9:_]*)\\s(.(?!>))*\\?>\\s*$")) {
			node = XmlNode.createProcessingInstruction(content);
		} else if (content.matches("^\\s*<!\\[CDATA\\[([\\w\\W\\s]*)]]>\\s*$")) {
			int start = content.indexOf("<![CDATA[") + 9;
			int end = content.indexOf("]]>");
			node = XmlNode.createCDataSection(content.substring(start, end));
		} else {
			input = new ByteArrayInputStream(mCurrentSelection.getContent()
					.getText().getBytes());
			try {
				doc = XmlTreePullParser.parseXmlTree(input, false, null);
			} catch (XmlTreeParserException e) {
				Crouton.makeText(this, e.getMessage(this), Style.ALERT).show();
			}

			if ((doc != null) && (doc.getChildrenCount() > 0)) {
				node = (XmlNode) doc.getChildAtPos(0);
			}
		}

		return node;
	}

	/**
	 * Comment a node
	 */
	private void doCommentNode() {

		TreeNode<XmlData> parent = mCurrentSelection.getParent();
		int index = parent.getChildPosition(mCurrentSelection);
		parent.removeChildNode(mCurrentSelection);

		StringBuilder builder = new StringBuilder();
		mCurrentSelection.buildXmlString(builder);
		String content = builder.toString().trim();

		XmlNode comment = XmlNode.createComment(content);
		parent.addChildNode(comment, index);
		comment.updateChildViewCount(true);
		parent.updateParentViewCount();

		onXmlContentChanged(true);
	}

	/**
	 * Opens an editor for the selected node
	 */
	private void doEditNode() {
		getAxelApplication().setCurrentSelection(mCurrentSelection);

		Intent edit = new Intent(getApplicationContext(),
				AxelNodeEditorActivity.class);

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
	 * Deletes a node from its parent
	 */
	private void doDeleteNode() {

		if (mCurrentSelection != null) {
			if (mCurrentSelection.removeFromParent()) {
				mCurrentSelection = null;
				onXmlContentChanged(true);
			}
		}
	}

	/**
	 * Update the window title
	 */
	@TargetApi(11)
	private void updateTitle() {
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

	/** */
	private XmlNode mCurrentSelection;
	/** */
	private XmlNode mDocument;
	/** */
	private ListView mListView;
	/** */
	private TreeAdapter<XmlData> mAdapter;

	/** the path of the file currently opened */
	private String mCurrentFilePath;
	/** the last known hash of the file currently opened */
	private String mCurrentFileHash;
	/** the name of the file currently opened */
	private String mCurrentFileName;
	/** the file's encoding */
	private String mCurrentEncoding;

	/** the loader for async load */
	private AsyncXmlFileLoader mLoader;
	/** the writer for async write */
	private AsyncXmlFileWriter mWriter;

	/** the runable to run after a save */
	private Runnable mAfterSave; // Mennen ? Axe ?

	/** is dirty ? */
	private boolean mDirty;
	/** is read only */
	private boolean mReadOnly;

	private AxelApplication mAxelApplication;

}
