package fr.xgouchet.xmleditor.data;

import java.io.File;
import java.io.IOException;

import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore.MediaColumns;
import fr.xgouchet.androidlib.data.ClipboardUtils;
import fr.xgouchet.androidlib.data.ClipboardUtils.ClipboardProxy;
import fr.xgouchet.androidlib.data.FileUtils;
import fr.xgouchet.xmleditor.R;
import fr.xgouchet.xmleditor.common.AxelUtils;
import fr.xgouchet.xmleditor.common.Constants;
import fr.xgouchet.xmleditor.common.RecentFiles;
import fr.xgouchet.xmleditor.common.Settings;
import fr.xgouchet.xmleditor.data.tree.TreeNode;
import fr.xgouchet.xmleditor.data.xml.UnknownFileFormatException;
import fr.xgouchet.xmleditor.data.xml.XmlAttribute;
import fr.xgouchet.xmleditor.data.xml.XmlData;
import fr.xgouchet.xmleditor.data.xml.XmlNode;
import fr.xgouchet.xmleditor.tasks.AsyncHtmlLoader;
import fr.xgouchet.xmleditor.tasks.AsyncXmlFileWriter;
import fr.xgouchet.xmleditor.tasks.AsyncXmlFileWriter.XmlFileWriterListener;
import fr.xgouchet.xmleditor.tasks.AsyncXmlLoader;
import fr.xgouchet.xmleditor.tasks.XmlLoaderListener;


/**
 * This class handles all
 */
public class XmlEditor {
    
    /** The root of the underlying document */
    private XmlNode mRoot;
    /** The currently selected node */
    private XmlNode mSelection;
    
    /** is dirty ? */
    private boolean mDirty;
    /** is read only */
    private boolean mReadOnly;
    
    @Deprecated
    private String mCurrentFilePath;
    @Deprecated
    private String mCurrentFileHash;
    @Deprecated
    private String mCurrentFileName;
    
    private Uri mCurrentDocumentUri;
    private String mCurrentDocumentHash;
    private String mCurrentDocumentName;
    private String mCurrentEncoding;
    
    /** the loader for async load */
    private AsyncXmlLoader mLoader;
    /** the writer for async write */
    private AsyncXmlFileWriter mWriter;
    
    /** the clipboard manager proxy */
    private ClipboardProxy mClipboardManager;
    
    
    
    /** The listener for events on this editor */
    private final XmlEditorListener mListener;
    /** the current application context */
    private final Context mContext;
    
    
    
    /**
     * @param listener
     *            the listener for events on this editor
     */
    public XmlEditor(final XmlEditorListener listener, final Context context) {
        if (listener == null) {
            throw new IllegalArgumentException(new NullPointerException());
        }
        mListener = listener;
        mContext = context;
    }
    
    //////////////////////////////////////////////////////////////////////////////////////
    // EDITOR LIFECYCLE
    //////////////////////////////////////////////////////////////////////////////////////
    
    /**
     * Performs initialisations when the context is ready
     */
    public void onStart() {
        mClipboardManager = ClipboardUtils.getClipboardProxy(mContext);
    }
    
    /**
     * Called when the app leaves foreground, and async operation must be
     * canceled
     */
    public void onPause() {
        if (mLoader != null) {
            mLoader.cancel(true);
        }
    }
    
    
    //////////////////////////////////////////////////////////////////////////////////////
    // EDITOR INPUT/OUTPUT
    //////////////////////////////////////////////////////////////////////////////////////
    
    
    
    /**
     * Loads a document located at the given Uri
     * 
     * @param uri
     *            the Uri of a source document to read
     * @param ignore
     *            should the source uri be ignored in the loaded document
     */
    public void loadDocument(final Uri uri, final boolean ignore) {
        if (mLoader == null) {
            int flags = 0;
            if (ignore) {
                flags |= AsyncXmlLoader.FLAG_IGNORE_FILE;
            }
            
            mLoader = new AsyncXmlLoader(mContext, uri, mXmlLoaderListener, flags);
            mLoader.execute();
        }
    }
    
    /**
     * Loads a HTML document with a tag soup library
     * 
     * 
     * @param uri
     *            the Uri of a source document to read
     * @param ignore
     *            should the source uri be ignored in the loaded document
     */
    public void loadHtmlDocument(Uri uri) {
        if (mLoader == null) {
            int flags = AsyncXmlLoader.FLAG_HTML_SOUP | AsyncXmlLoader.FLAG_FORCE_READ_ONLY;
            
            mLoader = new AsyncHtmlLoader(mContext, uri, mXmlLoaderListener, flags);
            mLoader.execute();
        }
    }
    
    
    //////////////////////////////////////////////////////////////////////////////////////
    // MISC
    //////////////////////////////////////////////////////////////////////////////////////
    
    
    
    /**
     * @return if the current edited document is read only
     */
    public boolean isReadOnly() {
        return mReadOnly;
    }
    
    /**
     * @return if the current document has unsaved modifications
     */
    public boolean isDirty() {
        return mDirty;
    }
    
    public boolean hasRoot() {
        return (mRoot != null);
    }
    
    /**
     * @return if the current document has a name and path, and can be saved as
     *         is
     */
    public boolean hasPath() {
        return ((mCurrentFilePath != null) && (mCurrentFilePath.length() >= 0));
    }
    
    /**
     * @return if the current document's file exists in the file system
     */
    public boolean fileExists() {
        File file = new File(mCurrentFilePath);
        return file.exists();
    }
    
    /**
     * @return true if the file's hash has changed since last access (save /
     *         load), or if the file doesn't exist anymore.
     */
    public boolean fileChanged() {
        File file = new File(mCurrentFilePath);
        
        String hash = FileUtils.getFileHash(file);
        if (!hash.equals(mCurrentFileHash)) {
            return true;
        }
        
        return false;
    }
    
    public void setDirty() {
        onXmlContentChanged();
    }
    
    /**
     * @param selection
     *            the currently selected node
     */
    public void setSelection(final XmlNode selection) {
        mSelection = selection;
    }
    
    /**
     * @return the current selected node
     */
    public XmlNode getSelection() {
        return mSelection;
    }
    
    /**
     * @return the current document's file name
     */
    @Deprecated
    public String getCurrentFileName() {
        return mCurrentFileName;
    }
    
    /**
     * @return the current document's name
     */
    public String getCurrentDocumentName() {
        return mCurrentDocumentName;
    }
    
    
    /**
     * Clears the content of the editor. Assumes that user was prompted and
     * previous data was saved
     */
    public void doClearContents() {
        mCurrentDocumentUri = null;
        mCurrentDocumentName = null;
        mCurrentEncoding = null;
        
        mDirty = false;
        mReadOnly = false;
        
        mRoot = XmlNode.createDocument();
        mRoot.addChildNode(XmlNode.createDocumentDeclaration("1.0", "UTF-8",
                null));
        mRoot.setExpanded(true, true);
        mRoot.updateChildViewCount(true);
        
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
    @Deprecated
    public void doOpenFile(final File file, final boolean ignore) {
//        if (mLoader == null) {
//            int flags = 0;
//            if (ignore) {
//                flags |= AsyncXmlFileLoader.FLAG_IGNORE_FILE;
//            }
//            
//            mLoader = new AsyncXmlFileLoader(mContext, mXmlFileLoaderListener,
//                    flags);
//            mLoader.execute(file);
//        }
    }
    
    
    
    /**
     * Reloads the current file
     */
    @Deprecated
    public void doReloadCurrentFile() {
        final File file = new File(mCurrentFilePath);
        doOpenFile(file, false);
    }
    
    /**
     * Saves the text editor's content into a file at the current path. If an
     * after save {@link Runnable} exists, run it
     */
    public void doSaveFile() {
        doSaveFile(mCurrentFilePath);
    }
    
    /**
     * Saves the text editor's content into a file at the given path. If an
     * after save {@link Runnable} exists, run it
     * 
     * @param path
     *            the path to the file (must be a valid path and not null)
     */
    public void doSaveFile(final String path) {
        doSaveFile(path, true);
    }
    
    /**
     * Saves the text editor's content into a file at the given path. If an
     * after save {@link Runnable} exists, run it
     * 
     * @param path
     *            the path to the file (must be a valid path and not null)
     * @param keepPath
     *            if true, then this document will assume the given path is the
     *            document's path. If false, the document will keep a link to
     *            any previous path
     */
    public void doSaveFile(final String path, final boolean keepPath) {
        if (path == null) {
            mListener.onErrorNotification(mContext
                    .getString(R.string.toast_save_null));
            return;
        }
        
        if (mWriter == null) {
            mWriter = new AsyncXmlFileWriter(mContext, mXmlFileWriterListener,
                    mRoot, mCurrentEncoding);
            mWriter.setKeepPath(false);
            mWriter.execute(path);
        } else {
            // TODO throw error : current file being written
        }
    }
    
    /**
     * Adds an attribute to the selected node
     * 
     * @param attribute
     *            the attribute to add
     */
    public void doAddAttribute(final XmlAttribute attribute) {
        if (attribute == null) {
            return;
        }
        
        if ((mSelection != null) && (mSelection.isElement())) {
            mSelection.getContent().addAttribute(attribute);
            setDirty();
        }
    }
    
    
    public void addChildToNode(XmlNode node, XmlNode child, boolean edit) {
        if (node.addChildNode(child)) {
            if (node.isDocument()) {
                node.reorderDocumentChildren();
            }
            
            child.updateChildViewCount(false);
            node.updateChildViewCount(false);
            node.setExpanded(true, false);
            
            if (edit && Settings.sEditOnCreate) {
//                mSelection = child;
//             TODO   doEditNode();
            } else {
//                mSelection = null;
            }
            
            setDirty();
        }
    }
    
    /**
     * Adds a new child to the selected node
     * 
     * @param node
     *            the node to add
     * @param edit
     *            if the node editor should be opened once the node is added
     */
    public void doAddChildToNode(final XmlNode node, final boolean edit) {
        if (mSelection.addChildNode(node)) {
            if (mSelection.isDocument()) {
                mSelection.reorderDocumentChildren();
            }
            
            node.updateChildViewCount(false);
            mSelection.updateChildViewCount(false);
            mSelection.setExpanded(true, false);
            
            if (edit && Settings.sEditOnCreate) {
                mSelection = node;
                doEditNode();
            } else {
                mSelection = null;
            }
            
            setDirty();
        }
    }
    
    public void doEditNode() {
        
    }
    
    /**
     * Copy a node's text value into the system clipboard
     */
    public void doCopyNode() {
        StringBuilder builder = new StringBuilder();
        mSelection.buildXmlString(builder);
        
        String text, label, crouton;
        
        text = builder.toString().trim();
        label = mSelection.getContent().toString();
        mClipboardManager.setText(text, label);
        
        crouton = mContext.getString(R.string.ui_copy_clipboard,
                AxelUtils.ellipsize(text, 64));
        mListener.onConfirmNotification(crouton);
    }
    
    /**
     * Cut a node's text value into the system clipboard
     */
    public void doCutNode() {
        StringBuilder builder = new StringBuilder();
        mSelection.buildXmlString(builder);
        
        String text, label, crouton;
        
        // copy to clipboard
        text = builder.toString().trim();
        label = mSelection.getContent().toString();
        mClipboardManager.setText(text, label);
        
        // remove selected node
        XmlNode parent = (XmlNode) mSelection.getParent();
        parent.removeChildNode(mSelection);
        mSelection = null;
        
        setDirty();
        
        crouton = mContext.getString(R.string.ui_copy_clipboard,
                AxelUtils.ellipsize(text, 64));
        mListener.onConfirmNotification(crouton);
    }
    
    /**
     * Comment or uncomment a node
     */
    public void doCommentUncommentNode() {
        if (mSelection.isComment()) {
            doUncommentNode();
        } else {
            doCommentNode();
        }
    }
    
    /**
     * Deletes a node from its parent
     */
    public void doDeleteNode() {
        if (mSelection != null) {
            if (mSelection.removeFromParent()) {
                mSelection = null;
                setDirty();
            }
        }
    }
    
    /**
     * Paste the clipboard's content as child of the selected node
     */
    public void doPasteContentInNode() {
        XmlNode node, parent;
        
        String[] clipboardText = mClipboardManager.getText();
        
        for (String clip : clipboardText) {
            try {
                node = AxelUtils.contentAsXml(clip);
            }
            catch (Exception e) {
                mListener.onErrorNotification(e.getMessage());
                node = null;
            }
            
            if (node != null) {
                parent = mSelection;
                if (parent.canHaveChild(node)) {
                    node.setExpanded(true, true);
                    node.updateChildViewCount(true);
                    
                    parent.addChildNode(node);
                    parent.setExpanded(true);
                    parent.updateParentViewCount();
                    
                    setDirty();
                }
            }
        }
    }
    
    /**
     * Comment a node
     */
    private void doCommentNode() {
        
        TreeNode<XmlData> parent = mSelection.getParent();
        int index = parent.getChildPosition(mSelection);
        parent.removeChildNode(mSelection);
        
        StringBuilder builder = new StringBuilder();
        mSelection.buildXmlString(builder);
        String content = builder.toString().trim();
        
        XmlNode comment = XmlNode.createComment(content);
        parent.addChildNode(comment, index);
        comment.updateChildViewCount(true);
        parent.updateParentViewCount();
        
        setDirty();
    }
    
    /**
     * Uncomment a comment node
     */
    private void doUncommentNode() {
        XmlNode node = null, parent;
        int index;
        
        node = getCommentContent();
        
        if (node != null) {
            parent = (XmlNode) mSelection.getParent();
            if (parent.canHaveChild(node)) {
                node.setExpanded(true, true);
                node.updateChildViewCount(true);
                
                index = parent.getChildPosition(mSelection);
                parent.removeChildNode(mSelection);
                parent.addChildNode(node, index);
                
                parent.updateParentViewCount();
                
                setDirty();
            } else {
                mListener.onErrorNotification(mContext
                        .getString(R.string.toast_xml_uncomment));
            }
        }
    }
    
    /**
     * @return the content of the current selected node (comment) as an XML node
     */
    private XmlNode getCommentContent() {
        
        try {
            return AxelUtils.contentAsXml(mSelection.getContent().getText());
        }
        catch (Exception e) {
            mListener.onErrorNotification(e.getMessage());
            return null;
        }
        
    }
    
    
    /**
     * Fires the {@link XmlEditorListener#onXmlDocumentChanged(XmlNode, String, String)} methode on
     * this editor's listener
     */
    private void onXmlDocumentChanged() {
        mListener.onXmlDocumentChanged(mRoot, mCurrentDocumentName,
                mCurrentFilePath);
    }
    
    /**
     * Fires the {@link XmlEditorListener#onXmlContentChanged()} methode on this
     * editor's listener
     */
    private void onXmlContentChanged() {
        mDirty = true;
        mListener.onXmlContentChanged();
    }
    
    
    /**
     * The listener for the AsyncXmlLoader events
     */
    private XmlLoaderListener mXmlLoaderListener = new XmlLoaderListener() {
        
        @Override
        public void onXmlFileLoaded(final XmlNode root, final Uri uri, final String hash,
                final String encoding, final boolean readOnly) {
            
            mRoot = root;
            
            if (uri == null) {
                mCurrentDocumentUri = null;
                mCurrentDocumentHash = null;
                mCurrentDocumentName = null;
                mReadOnly = false;
            } else {
                mCurrentDocumentUri = uri;
                mCurrentDocumentHash = hash;
                mCurrentEncoding = encoding;
                mReadOnly = readOnly; // TODO check if file is writeable
                
                // TODO update recent list
                
                
                // Exctract name from provider
                String[] proj = {
                        MediaColumns.TITLE
                };
                Cursor cursor = mContext.getContentResolver().query(mCurrentDocumentUri, proj,
                        null, null, null);
                if ((cursor != null) && (cursor.getCount() != 0)) {
                    int columnIndex = cursor.getColumnIndexOrThrow(MediaColumns.TITLE);
                    cursor.moveToFirst();
                    mCurrentDocumentName = cursor.getString(columnIndex);
                }
            }
            
            if (mReadOnly) {
                mListener.onInfoNotification(mContext.getString(R.string.toast_open_read_only));
            }
            
            onXmlDocumentChanged();
            
            mLoader = null;
            
        }
        
        @Override
        public void onXmlFileLoadError(final Uri uri, final Throwable throwable,
                final String message) {
            
            mLoader = null;
            
            // DEBUG
            throwable.printStackTrace();
            
            // Check for parsing error 
            if (throwable instanceof XmlPullParserException) {
                if (AxelUtils.isHtmlDocument(uri)) {
                    mListener.onHtmlParseError(uri, message);
                } else {
                    mListener.onXmlParseError(uri, message);
                }
                return;
            }
            
            // Check for Memory error
            if (throwable instanceof OutOfMemoryError) {
                mListener.onErrorNotification(mContext
                        .getString(R.string.toast_open_memory_error));
                return;
            }
            
            // Check for file format error
            if (throwable instanceof UnknownFileFormatException) {
                mListener.onErrorNotification(mContext
                        .getString(R.string.toast_xml_unknown_format));
                return;
            }
            
            // Check for plain old IOException
            if (throwable instanceof IOException) {
                mListener.onErrorNotification(mContext.getString(R.string.toast_xml_io_exception));
                return;
            }
            
            // TODO handle other errors
            
            // Default, lets just print the error and hope for the best
            mListener.onErrorNotification(throwable.getMessage());
            
        }
        
        
    };
    
    /** the fiel writer listener for this editor */
    @Deprecated
    private XmlFileWriterListener mXmlFileWriterListener = new XmlFileWriterListener() {
        
        @Override
        public void onXmlFileWritten(final String filePath,
                final boolean keepPath) {
            if (keepPath) {
                File file = new File(filePath);
                mCurrentFilePath = FileUtils.getCanonizePath(file);
                mCurrentFileName = file.getName();
                mCurrentFileHash = FileUtils.getFileHash(file);
                
                RecentFiles.updateRecentList(filePath);
                RecentFiles.saveRecentList(mContext.getSharedPreferences(
                        Constants.PREFERENCES_NAME, Context.MODE_PRIVATE));
                
                mDirty = false;
                
                onXmlDocumentChanged();
            }
            mListener.onConfirmNotification(mContext
                    .getString(R.string.toast_save_success));
            mListener.onXmlDocumentSaved();
            
            mWriter = null;
        }
        
        @Override
        public void onXmlFileWriteError(final Throwable throwable,
                final String message) {
            // TODO handle each possible exception
            throwable.printStackTrace();
            
            mWriter = null;
        }
    };
}
