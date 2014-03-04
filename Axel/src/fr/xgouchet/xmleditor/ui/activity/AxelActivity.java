package fr.xgouchet.xmleditor.ui.activity;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import de.neofonie.mobile.app.android.widget.crouton.Crouton;
import de.neofonie.mobile.app.android.widget.crouton.Style;
import fr.xgouchet.xmleditor.AxelAboutActivity;
import fr.xgouchet.xmleditor.AxelHelpActivity;
import fr.xgouchet.xmleditor.AxelSettingsActivity;
import fr.xgouchet.xmleditor.R;
import fr.xgouchet.xmleditor.common.AxelChangeLog;
import fr.xgouchet.xmleditor.common.AxelUtils;
import fr.xgouchet.xmleditor.common.Constants;
import fr.xgouchet.xmleditor.common.Settings;
import fr.xgouchet.xmleditor.common.TemplateFiles;
import fr.xgouchet.xmleditor.data.XmlEditor;
import fr.xgouchet.xmleditor.data.XmlEditorListener;
import fr.xgouchet.xmleditor.data.xml.XmlNode;
import fr.xgouchet.xmleditor.ui.fragment.AEditorFragment;
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
    private AEditorFragment mEditorFragment;
    
    /** the runable to run after a save */
    private Runnable mAfterSave; // Mennen ? Axe ?
    
    public AxelActivity() {
        mXmlEditor = new XmlEditor(this, this);
    }
    
    // ////////////////////////////////////////////////////////////////////////////////////
    // Activity Lifecycle
    // ////////////////////////////////////////////////////////////////////////////////////
    
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Set content view
        setContentView(R.layout.activity_editor);
        
        // Set fragment (TODO read from prefs)
        mEditorFragment = new SimpleEditorFragment();
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
        
        // Reload Preferences
        SharedPreferences prefs;
        prefs = getSharedPreferences(Constants.PREFERENCES_NAME,
                Context.MODE_PRIVATE);
        Settings.updateFromPreferences(prefs);
        
        // Display change log if needed
        AxelChangeLog changeLog;
        changeLog = new AxelChangeLog();
        
        if (changeLog.displayChangeLog(this, prefs)) {
            // copy templates from the assets
            TemplateFiles.copyTemplatesFromAssets(this);
        }
        
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        
        if (mXmlEditor.hasRoot()) {
            if (mXmlEditor.hasPath()) {
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
        
        Settings.updateFromPreferences(getSharedPreferences(
                Constants.PREFERENCES_NAME, MODE_PRIVATE));
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
        }
        
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
        // menu.findItem(R.id.action_preview_in_browser).setEnabled(
        // getAxelApplication().canBePreviewed());
        
        // add templates as submenus
        MenuItem newTemplate = menu.findItem(R.id.action_new_template);
        SubMenu templates = newTemplate.getSubMenu();
        templates.clear();
        
        List<File> templateFiles = TemplateFiles.getTemplateFiles(this);
        for (File template : templateFiles) {
            templates.add(R.id.action_group_template, 0, Menu.NONE,
                    template.getName());
        }
        
        // TODO add recent documents as submenu
        
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        
        // Check for template
        if (item.getGroupId() == R.id.action_group_template) {
            openTemplate(item.getTitle().toString());
            return true;
        }
        
        // TODO check for recent file
        
        boolean result = true;
        switch (item.getItemId()) {
            case R.id.action_new_empty:
                newContent();
                break;
            case R.id.action_open_file:
                openDocument();
                break;
            case R.id.action_open_recent:
                // TODO openRecentFile();
                break;
            case R.id.action_preview_in_browser:
                // TODO previewFile();
                break;
            case R.id.action_save:
                // TODO saveContent();
                break;
            case R.id.action_save_as_file:
                // TODO saveContentAs();
                break;
            case R.id.action_save_as_template:
                // TODO promptTemplateName();
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
        File file;
        
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
            try {
                file = new File(new URI(intent.getData().toString()));
                mXmlEditor.doOpenFile(file, false);
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
            mXmlEditor.doClearContents();
        }
    }
    
    /**
     * Clears the current content to make a new file
     */
    private void newContent() {
        mAfterSave = new Runnable() {
            
            @Override
            public void run() {
                mXmlEditor.doClearContents();
            }
        };
        
        // TODO promptSaveDirty();
    }
    
    /**
     * Starts an activity to choose a file to open
     */
    @TargetApi(VERSION_CODES.KITKAT)
    private void openDocument() {
        mAfterSave = new Runnable() {
            
            @Override
            public void run() {
                Intent open;
                int requestCode;
                boolean createChooser;
                
                if (VERSION.SDK_INT > VERSION_CODES.KITKAT) {
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
                    startActivityForResult(
                            Intent.createChooser(open, getString(R.string.ui_open_with)),
                            requestCode);
                } else {
                    startActivityForResult(open, requestCode);
                }
            }
        };
        
        runAfterSave();
        // TODO promptSaveDirty();
    }
    
    /**
     * Loads a template document
     * 
     * @param template
     */
    private void openTemplate(final String template) {
        String templatePath = TemplateFiles.getOuputPath(this, template);
        final File file = new File(templatePath);
        
        mAfterSave = new Runnable() {
            
            @Override
            public void run() {
                mXmlEditor.doOpenFile(file, true);
            }
        };
        
        // TODO promptSaveDirty();
    }
    
    // ////////////////////////////////////////////////////////////////////////////////////
    // XmlEditorListener Implementation
    // ////////////////////////////////////////////////////////////////////////////////////
    
    @Override
    public void onXmlDocumentChanged(final XmlNode root, final String name,
            final String path) {
        
        // notify fragment
        mEditorFragment.onXmlDocumentChanged(root);
    }
    
    @Override
    public void onXmlContentChanged() {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void onXmlDocumentSaved() {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void onXmlParseError(final Uri uri, final String message) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void onHtmlParseError() {
        // TODO Auto-generated method stub
        
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
