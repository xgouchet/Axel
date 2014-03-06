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
import android.view.SubMenu;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import de.neofonie.mobile.app.android.widget.crouton.Crouton;
import de.neofonie.mobile.app.android.widget.crouton.Style;
import fr.xgouchet.xmleditor.common.AxelChangeLog;
import fr.xgouchet.xmleditor.common.Constants;
import fr.xgouchet.xmleditor.common.RecentFiles;
import fr.xgouchet.xmleditor.common.Settings;
import fr.xgouchet.xmleditor.common.TemplateFiles;
import fr.xgouchet.xmleditor.data.XmlEditor;
import fr.xgouchet.xmleditor.data.XmlEditorListener;
import fr.xgouchet.xmleditor.data.tree.TreeNode;
import fr.xgouchet.xmleditor.data.xml.XmlData;
import fr.xgouchet.xmleditor.data.xml.XmlHighlighter;
import fr.xgouchet.xmleditor.data.xml.XmlNode;
import fr.xgouchet.xmleditor.data.xml.XmlNodeStyler;
import fr.xgouchet.xmleditor.ui.adapter.NodeTreeAdapter;
import fr.xgouchet.xmleditor.ui.adapter.NodeTreeAdapter.TreeNodeEventListener;
import fr.xgouchet.xmleditor.ui.dialog.AttributeEditDialog;


/**
 * 
 */
public class AxelActivity extends Activity implements
        TreeNodeEventListener<XmlData>, OnQueryTextListener, XmlEditorListener {
    
    /** The underlying editor */
    private XmlEditor mEditor;
    
    /** the runable to run after a save */
    private Runnable mAfterSave; // Mennen ? Axe ?
    
    /** The List view displaying the XML data */
    private ListView mListView;
    
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
        // registerForContextMenu(mListView);
        
        // Editor
        mEditor.onStart();
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
        
        if (mEditor.hasRoot()) {
            if (mEditor.hasPath()) {
                if (mEditor.fileExists()) {
                    if (mEditor.fileChanged()) {
                        promptFileChanged();
                    }
                } else {
                    promptFileDeleted();
                }
            }
        } else {
            mEditor.doClearContents();
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
        mEditor.onPause();
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
     * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
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
//                    case Constants.REQUEST_OPEN:
//                        File file = new File(extras.getString(Constants.EXTRA_PATH));
//                        mEditor.doOpenFile(file, extras.getBoolean(
//                                Constants.EXTRA_IGNORE_FILE, false));
//                        break;
                    case Constants.REQUEST_EDIT_NODE:
                        getAxelApplication().getCurrentSelection()
                                .onContentChanged();
                        mEditor.setDirty();
                        break;
                    case Constants.REQUEST_SORT_CHILDREN:
                        getAxelApplication().getCurrentSelection()
                                .onContentChanged();
                        mEditor.setDirty();
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
        
        menu.findItem(R.id.action_search).setActionView(search);
        
        return true;
    }
    
    /**
     * @see android.app.Activity#onPrepareOptionsMenu(android.view.Menu)
     */
    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        super.onPrepareOptionsMenu(menu);
        
        // disable save for read only files
        menu.findItem(R.id.action_save).setEnabled(!mEditor.isReadOnly());
        
        // disable preview for non previewable files (duh...)
        menu.findItem(R.id.action_preview_in_browser).setEnabled(
                getAxelApplication().canBePreviewed());
        
        // add templates as submenus
        MenuItem newTemplate = menu.findItem(R.id.action_new_template);
        SubMenu templates = newTemplate.getSubMenu();
        templates.clear();
        
        List<File> templateFiles = TemplateFiles.getTemplateFiles(this);
        for (File template : templateFiles) {
            templates.add(R.id.action_group_template, 0, Menu.NONE,
                    template.getName());
        }
        
        return true;
    }
    
    /**
     * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
     */
    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        boolean result;
        
        if (item.getGroupId() == R.id.action_group_template) {
            String template = item.getTitle().toString();
            String templatePath = TemplateFiles.getOuputPath(this, template);
            final File file = new File(templatePath);
            
            mAfterSave = new Runnable() {
                
                @Override
                public void run() {
                    mEditor.doOpenFile(file, true);
                }
            };
            
            promptSaveDirty();
            
        }
        
        result = true;
        switch (item.getItemId()) {
            case R.id.action_new_empty:
                newContent();
                break;
            case R.id.action_open_file:
                openFile();
                break;
            case R.id.action_open_recent:
                openRecentFile();
                break;
            case R.id.action_new_template:
                // openTemplateFile();
                // Now we use fast access
                break;
            case R.id.action_preview_in_browser:
                previewFile();
                break;
            case R.id.action_save:
                saveContent();
                break;
            case R.id.action_save_as:
                saveContentAs();
                break;
            case R.id.action_save_as_template:
                promptTemplateName();
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
    
    // @Override
    @Deprecated
    public void onItemClick(final Object source, final int pos,
            final int actionId) {
        
        switch (actionId) {
            case R.id.action_delete:
                promptDeleteNode();
                break;
            case R.id.action_add_child:
//                promptNodeAddChild();
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
     * @see fr.xgouchet.xmleditor.ui.adapter.NodeTreeAdapter.TreeNodeEventListener#onNodeLongPressed(fr.xgouchet.xmleditor.data.tree.TreeNode,
     *      android.view.View, int)
     */
    @Override
    public void onNodeLongPressed(final TreeNode<XmlData> node,
            final View view, final int position) {
        performQuickAction(node, view, Settings.sLongPressQA);
    }
    
    /**
     * @see fr.xgouchet.xmleditor.ui.adapter.NodeTreeAdapter.TreeNodeEventListener#onNodeTapped(fr.xgouchet.xmleditor.data.tree.TreeNode,
     *      android.view.View, int)
     */
    @Override
    public void onNodeTapped(final TreeNode<XmlData> node, final View view,
            final int position) {
        performQuickAction(node, view, Settings.sSingleTapQA);
    }
    
    /**
     * @see fr.xgouchet.xmleditor.ui.adapter.NodeTreeAdapter.TreeNodeEventListener#onNodeDoubleTapped(fr.xgouchet.xmleditor.data.tree.TreeNode,
     *      android.view.View, int)
     */
    @Override
    public void onNodeDoubleTapped(final TreeNode<XmlData> node,
            final View view, final int position) {
        performQuickAction(node, view, Settings.sDoubleTapQA);
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
        
        mAdapter = new NodeTreeAdapter<XmlData>(this, root);
        mAdapter.setNodeStyler(new XmlNodeStyler());
        mAdapter.setListener(this);
        mListView.setAdapter(mAdapter);
        mAdapter.setHighlighter(null);
    }
    
    /**
     * @see fr.xgouchet.xmleditor.data.XmlEditor.XmlEditorListener#onXmlDocumentSaved()
     */
    @Override
    public void onXmlDocumentSaved() {
        runAfterSave();
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
    
    @Override
    public void onXmlParseError(final Uri uri, final String message) {
        
    }
    
    @Override
    public void onHtmlParseError(final Uri uri, final String message) {
        // TODO Auto-generated method stub
        
    }
    
    /**
     * @see fr.xgouchet.xmleditor.data.XmlEditor.XmlEditorListener#onConfirmNotification(java.lang.String)
     */
    @Override
    public void onConfirmNotification(final String message) {
        Crouton.makeText(this, message, Style.CONFIRM).show();
    }
    
    /**
     * @see fr.xgouchet.xmleditor.data.XmlEditor.XmlEditorListener#onInfoNotification(String)
     */
    @Override
    public void onInfoNotification(final String message) {
        Crouton.makeText(this, message, Style.INFO).show();
    }
    
    /**
     * @see fr.xgouchet.xmleditor.data.XmlEditor.XmlEditorListener#onErrorNotification(java.lang.String)
     */
    @Override
    public void onErrorNotification(final String message) {
        Crouton.makeText(this, message, Style.ALERT).show();
    }
    
    /**
     * Performs a quick action on a node element
     */
    private void performQuickAction(final TreeNode<XmlData> node,
            final View view, final String action) {
        
        if (mActionMode != null) {
            mActionMode.finish();
            mActionMode = null;
            return;
        }
        
        if (Constants.QUICK_ACTION_NONE.equals(action)) {
            // do nothing, yeah !!!
            
        } else if (Constants.QUICK_ACTION_DISPLAY_MENU.equals(action)) {
            // display the menu using Android's Action mode
            startNodeActionMode((XmlNode) node);
        } else if (!mEditor.isReadOnly()) {
            
            // some quick actions that modify the node
            XmlNode xmlNode = (XmlNode) node;
            mEditor.setSelection(xmlNode);
            
            if (Constants.QUICK_ACTION_ADD_CHILD.equals(action)) {
//                if (xmlNode.isElement() || xmlNode.isDocument()) {
//                    promptNodeAddChild();
//                }
            } else if (Constants.QUICK_ACTION_COMMENT_TOGGLE.equals(action)) {
                if ((!xmlNode.isDocument())
                        && (!xmlNode.isDocumentDeclaration())) {
                    mEditor.doCommentUncommentNode();
                }
            } else if (Constants.QUICK_ACTION_DELETE.equals(action)) {
                if ((!xmlNode.isDocument())
                        && (!xmlNode.isDocumentDeclaration())) {
                    promptDeleteNode();
                }
            } else if (Constants.QUICK_ACTION_EDIT.equals(action)) {
                if (!xmlNode.isDocument()) {
                    doEditNode();
                }
            } else if (Constants.QUICK_ACTION_ORDER_CHILDREN.equals(action)) {
                if (xmlNode.hasChildren()) {
                    doSortChildren();
                }
            }
        }
    }
    
    private void startNodeActionMode(final XmlNode node) {
        mEditor.setSelection(node);
        mActionMode = startActionMode(mActionModeCallback);
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
            }
            catch (URISyntaxException e) {
                Crouton.makeText(this, R.string.toast_intent_invalid_uri,
                        Style.ALERT).show();
            }
            catch (IllegalArgumentException e) {
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
                
//                open = new Intent(getApplicationContext(),
//                        AxelOpenActivity.class);
                
//                startActivityForResult(open, Constants.REQUEST_OPEN);
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
                    
//                    open = new Intent(getApplicationContext(),
//                            AxelOpenRecentActivity.class);
                    
//                    startActivityForResult(open, Constants.REQUEST_OPEN);
                    
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
                if (mEditor.hasPath()) {
                    doPreviewFile();
                } else {
                    Crouton.showText(AxelActivity.this,
                            R.string.toast_preview_file_not_saved, Style.ALERT);
                }
            }
        };
        
        promptSaveDirty();
    }
    
    /**
     * General save command : check if a path exist for the current content,
     * then save it , else invoke the {@link AxelActivity#saveContentAs()} method
     */
    private void saveContent() {
        if (mEditor.hasPath()) {
            mEditor.doSaveFile();
        } else {
            saveContentAs();
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
        
        builder.setNeutralButton(R.string.action_save_as,
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
                        mAfterSave = null;
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
        dlg.setNode(mEditor.getSelection());
        dlg.setSiblingsAttribute(mEditor.getSelection().getContent()
                .getAttributes());
        
        dlg.setOnDismissListener(new OnDismissListener() {
            
            @Override
            public void onDismiss(final DialogInterface dialog) {
                mEditor.doAddAttribute(dlg.getAttribute());
            }
        });
        dlg.show();
    }
    
    
    /**
     * Opens a Web view to preview the current file
     */
    private void doPreviewFile() {
        Intent intent = new Intent();
        intent.setClass(getBaseContext(), AxelPreviewActivity.class);
        startActivity(intent);
    }
    
    /**
     * TODO add an option to validate from action bar
     * Validates a file with the W3C validator API
     * 
     * @param file
     *            the file to validate
     */
    private void doValidateFile(final File file) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.fromFile(file));
        intent.setClass(getBaseContext(), AxelValidatorActivity.class);
        startActivity(intent);
    }
    
    /**
     * @param fileName
     *            saves the template
     */
    private void doSaveTemplate(final String fileName) {
        String path = TemplateFiles.getOuputPath(this, fileName);
        
        mEditor.doSaveFile(path, false);
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
        intent.putExtra(Intent.EXTRA_EMAIL, new String[] {
                getResources()
                        .getString(R.string.ui_mail)
        });
        intent.putExtra(Intent.EXTRA_SUBJECT, "Axel - File open error");
        startActivity(Intent.createChooser(intent,
                getString(R.string.ui_choose_mail)));
    }
    
    /**
     * Opens an editor for the selected node
     */
    private void doEditNode() {
        getAxelApplication().setCurrentSelection(mEditor.getSelection());
        
        Intent edit = new Intent(getApplicationContext(),
                AxelNodeEditorActivity.class);
        edit.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivityForResult(edit, Constants.REQUEST_EDIT_NODE);
    }
    
    /**
     * Opens an editor to sort an element's children;
     */
    private void doSortChildren() {
        getAxelApplication().setCurrentSelection(mEditor.getSelection());
        
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
        
        name = mEditor.getCurrentFileName();
        if (TextUtils.isEmpty(name)) {
            name = "?";
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
    private final List<View> mCurrentSelectedViews;
    
    /** */
    private NodeTreeAdapter<XmlData> mAdapter;
    
    private AxelApplication mAxelApplication;
    
    /** The current action mode */
    private ActionMode mActionMode;
    
    /** The action mode callbacks */
    private final Callback mActionModeCallback = new Callback() {
        
        private XmlNode mActiveNode;
        
        @Override
        public boolean onCreateActionMode(final ActionMode mode, final Menu menu) {
            mActiveNode = mEditor.getSelection();
            if ((mActiveNode == null) || (mActiveNode.getContent() == null)) {
                return false;
            }
            
            MenuInflater inflater = new MenuInflater(AxelActivity.this);
            inflater.inflate(R.menu.editor_actions, menu);
            
            if (!mActiveNode.canHaveChildren()) {
                menu.removeItem(R.id.action_add_child);
                menu.removeItem(R.id.action_paste);
            }
            
            if (!mActiveNode.canSortChildren()) {
                menu.removeItem(R.id.action_sort_children);
            }
            
            if (!mActiveNode.isElement()) {
                menu.removeItem(R.id.action_add_attr);
            }
            
            if (!mActiveNode.canBeRemovedFromParent()) {
                menu.removeItem(R.id.action_cut);
                menu.removeItem(R.id.action_delete);
            }
            
            if (mActiveNode.isDocumentDeclaration()) {
                menu.removeItem(R.id.action_copy);
            }
            
            if (mActiveNode.isDocument()) {
                menu.removeItem(R.id.action_edit);
                menu.removeItem(R.id.action_comment);
            } else if (!mActiveNode.isDocumentDeclaration()) {
                if (mActiveNode.isComment()) {
                    menu.findItem(R.id.action_comment).setTitle(
                            R.string.action_uncomment);
                    // TODO also change the icon?
                }
            }
            
            // only start action mode when there is something to do!
            return (menu.size() >= 0);
        }
        
        @Override
        public boolean onPrepareActionMode(final ActionMode mode,
                final Menu menu) {
            return false;
        }
        
        @Override
        public boolean onActionItemClicked(final ActionMode mode,
                final MenuItem item) {
            
            mEditor.setSelection(mActiveNode);
            switch (item.getItemId()) {
                case R.id.action_delete:
                    promptDeleteNode();
                    break;
                case R.id.action_add_child:
//                    promptNodeAddChild();
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
            
            mActionMode.finish();
            return true;
        }
        
        @Override
        public void onDestroyActionMode(final ActionMode mode) {
        }
    };
}
