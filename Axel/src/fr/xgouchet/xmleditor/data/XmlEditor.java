package fr.xgouchet.xmleditor.data;

import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import fr.xgouchet.xmleditor.R;
import fr.xgouchet.xmleditor.common.AxelUtils;
import fr.xgouchet.xmleditor.common.ClipboardUtils;
import fr.xgouchet.xmleditor.common.RecentUtils;
import fr.xgouchet.xmleditor.common.Settings;
import fr.xgouchet.xmleditor.data.tree.TreeNode;
import fr.xgouchet.xmleditor.data.xml.UnknownFileFormatException;
import fr.xgouchet.xmleditor.data.xml.XmlAttribute;
import fr.xgouchet.xmleditor.data.xml.XmlData;
import fr.xgouchet.xmleditor.data.xml.XmlNode;
import fr.xgouchet.xmleditor.tasks.AsyncHtmlLoader;
import fr.xgouchet.xmleditor.tasks.AsyncXmlLoader;
import fr.xgouchet.xmleditor.tasks.AsyncXmlWriter;
import fr.xgouchet.xmleditor.tasks.XmlLoaderListener;
import fr.xgouchet.xmleditor.tasks.XmlWriterListener;

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

    private Uri mCurrentDocumentUri;
    private String mCurrentDocumentHash;
    private String mCurrentDocumentName;
    private String mCurrentEncoding;

    /** the loader for async load */
    private AsyncXmlLoader mLoader;
    /** the writer for async write */
    private AsyncXmlWriter mWriter;

    /** the listeners for events on this editor */
    private final List<SoftReference<XmlEditorListener>> mListeners;

    /** the current application context */
    private final Context mContext;

    /**
     * @param listener
     *            the listener for events on this editor
     */
    public XmlEditor(final Context context) {
        mListeners = new ArrayList<SoftReference<XmlEditorListener>>();
        mContext = context;
    }

    /**
     * Adds a listener for xml events
     * 
     * @param listener
     */
    public void addListener(final XmlEditorListener listener) {
        // prevent adding the same listener twice
        for (SoftReference<XmlEditorListener> ref : mListeners) {
            if (listener.equals(ref.get())) {
                return;
            }
        }
        mListeners.add(new SoftReference<XmlEditorListener>(listener));
    }

    /**
     * Removes a listener previously added
     * 
     * @param listener
     */
    public void removeListener(final XmlEditorListener listener) {
        SoftReference<XmlEditorListener> refToRemove = null;

        for (SoftReference<XmlEditorListener> ref : mListeners) {
            if (listener.equals(ref.get())) {
                refToRemove = ref;
                break;
            }
        }

        if (refToRemove != null) {
            mListeners.remove(refToRemove);
        }
    }

    // ////////////////////////////////////////////////////////////////////////////////////
    // EDITOR LIFECYCLE
    // ////////////////////////////////////////////////////////////////////////////////////

    /**
     * Performs initialisations when the context is ready
     */
    public void onStart() {
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

    // ////////////////////////////////////////////////////////////////////////////////////
    // EDITOR INPUT/OUTPUT
    // ////////////////////////////////////////////////////////////////////////////////////

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
    public void loadHtmlDocument(final Uri uri) {
        if (mLoader == null) {
            int flags = AsyncXmlLoader.FLAG_HTML_SOUP | AsyncXmlLoader.FLAG_FORCE_READ_ONLY;

            mLoader = new AsyncHtmlLoader(mContext, uri, mXmlLoaderListener, flags);
            mLoader.execute();
        }
    }

    /**
     * Saves the current document at the current location
     */
    public void saveDocument() {

        saveDocument(mCurrentDocumentUri, true);
    }

    /**
     * Saves an XML document at the given Uri *
     * 
     * @param uri
     *            the Uri to write the document to
     */
    public void saveDocument(final Uri uri, final boolean keepUri) {
        if (uri == null) {
            fireOnErrorNotification(mContext.getString(R.string.notif_doc_save_null_uri));
            return;
        }

        if (mWriter == null) {
            mWriter = new AsyncXmlWriter(mContext, uri, mRoot, mXmlWriterListener,
                    mCurrentEncoding, keepUri);
            mWriter.execute();
        } else {
            fireOnInfoNotification(mContext.getString(R.string.notif_doc_already_saving));
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////
    // NODE EDITING ACTIONS
    //////////////////////////////////////////////////////////////////////////////////////

    /**
     * Comment or uncomment the active selected node
     */
    public void performCommentUncomment() {
        if (mSelection.isComment()) {
            performUncomment();
        } else {
            performComment();
        }
    }

    /**
     * Comment a node
     */
    private void performComment() {

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
    private void performUncomment() {
        XmlNode node = null, parent;
        int index;

        try {
            node = AxelUtils.contentAsXml(mSelection.getContent().getText());
        } catch (Exception e) {
            // TODO handle all errors
            fireOnErrorNotification(e.getMessage());
            return;
        }

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
                fireOnErrorNotification(mContext.getString(R.string.toast_xml_uncomment));
            }
        }
    }

    /**
     * Copy the active selected node's text value into the system clipboard
     */
    public void performClipboardCopy() {
        StringBuilder builder = new StringBuilder();
        mSelection.buildXmlString(builder);

        String text, label, message;

        text = builder.toString().trim();
        label = mSelection.getContent().toString();
        ClipboardUtils.setText(text, label, mContext);

        message = mContext.getString(R.string.notif_clipboard_copy, AxelUtils.ellipsize(text, 64));
        fireOnConfirmNotification(message, null);
    }

    /**
     * Cut the active selected node's text value into the system clipboard
     */
    public void performClipboardCut() {
        StringBuilder builder = new StringBuilder();
        mSelection.buildXmlString(builder);

        String text, label, message;

        // copy to clipboard
        text = builder.toString().trim();
        label = mSelection.getContent().toString();
        ClipboardUtils.setText(text, label, mContext);

        // remove selected node
        XmlNode parent = (XmlNode) mSelection.getParent();
        parent.removeChildNode(mSelection);
        mSelection = null;

        setDirty();

        message = mContext.getString(R.string.notif_clipboard_cut, AxelUtils.ellipsize(text, 64));
        fireOnConfirmNotification(message, null);
    }

    /**
     * Paste the clipboard's content as child of the active selected node
     */
    public void performClipboardPaste() {
        XmlNode node, parent;

        String[] clipboardText = ClipboardUtils.getText(mContext);

        for (String clip : clipboardText) {
            try {
                node = AxelUtils.contentAsXml(clip);
            } catch (Exception e) {
                // TODO handle all possible errors
                fireOnErrorNotification(e.getMessage());
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
     * Deletes the active selected node, allowing the user to undo the action
     */
    public void performDelete() {

        // save the current state
        final XmlNode parent = (XmlNode) mSelection.getParent();
        final XmlNode selection = mSelection;
        final int position = parent.getChildPosition(selection);

        // prepare the undo action
        Runnable undoAction = new Runnable() {

            @Override
            public void run() {
                parent.addChildNode(selection, position);
                fireOnXmlContentChanged();
            }
        };

        // try and remove from parent
        if (mSelection.removeFromParent()) {
            fireOnConfirmNotification(
                    mContext.getString(R.string.notif_node_deleted, selection.toString()),
                    undoAction);
            mSelection = null;
            setDirty();
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
     * @return if the current document has a valid Uri
     */
    public boolean canSave() {
        return (mCurrentDocumentUri != null);
    }

    /**
     * @return if the current document's file exists in the file system
     */
    @Deprecated
    public boolean fileExists() {
        // File file = new File(mCurrentFilePath);
        // return file.exists();
        return false;
    }

    /**
     * TODO make this an async task, because big files will make this
     * reaaaaaally long
     * 
     * @return true if the file's hash has changed since last access (save /
     *         load), or if the file doesn't exist anymore.
     */
    @Deprecated
    public boolean fileChanged() {

        // TODO compute HASH
        //        String hash = FileUtils.getHash(input);
        //        if (!hash.equals(mCurrentDocumentHash)) {
        //            return true;
        //        }

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
        return "";
    }

    /**
     * @return the current document's node
     */
    public XmlNode getCurrentDocumentNode() {
        return mRoot;
    }

    /**
     * @return the current document's Uri
     */
    public Uri getCurrentDocumentUri() {
        return mCurrentDocumentUri;
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
        mRoot.addChildNode(XmlNode.createDocumentDeclaration("1.0", "UTF-8", null));
        mRoot.setExpanded(true, true);
        mRoot.updateChildViewCount(true);

        onXmlDocumentChanged();
    }

    /**
     * Reloads the current file
     */
    @Deprecated
    public void doReloadCurrentFile() {
        // final File file = new File(mCurrentFilePath);
        // TODO doOpenFile(file, false);
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

    public void addChildToNode(final XmlNode node, final XmlNode child, final boolean edit) {
        if (node.addChildNode(child)) {
            if (node.isDocument()) {
                node.reorderDocumentChildren();
            }

            child.updateChildViewCount(false);
            node.updateChildViewCount(false);
            node.setExpanded(true, false);

            if (edit && Settings.sEditOnCreate) {
                // mSelection = child;
                // TODO doEditNode();
            } else {
                // mSelection = null;
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
     * Fires the {@link XmlEditorListener#onXmlDocumentChanged(XmlNode, String, String)} methode on
     * this editor's listener
     */
    private void onXmlDocumentChanged() {
        fireOnXmlDocumentChanged(mRoot, mCurrentDocumentName, mCurrentDocumentUri);
    }

    /**
     * Fires the {@link XmlEditorListener#onXmlContentChanged()} methode on this
     * editor's listener
     */
    private void onXmlContentChanged() {
        mDirty = true;
        fireOnXmlContentChanged();
    }

    /**
     * The listener for the AsyncXmlLoader events
     */
    private XmlLoaderListener mXmlLoaderListener = new XmlLoaderListener() {

        @Override
        public void onXmlDocumentLoaded(final XmlNode root, final Uri uri, final String hash,
                final String encoding, final boolean readOnly) {

            mRoot = root;
            mDirty = false;

            if (uri == null) {
                mCurrentDocumentUri = null;
                mCurrentDocumentHash = null;
                mCurrentDocumentName = null;
                mReadOnly = false;
            } else {
                mCurrentDocumentUri = uri;
                mCurrentDocumentHash = hash;
                mCurrentDocumentName = AxelUtils.getUriFileName(mCurrentDocumentUri, mContext);
                mCurrentEncoding = encoding;
                mReadOnly = readOnly; // TODO check if file is writeable

                RecentUtils.updateRecentUris(mContext, mCurrentDocumentUri, mCurrentDocumentName);
            }

            if (mReadOnly) {
                fireOnInfoNotification(mContext.getString(R.string.notif_doc_open_read_only));
            }

            onXmlDocumentChanged();

            mLoader = null;

        }

        @Override
        public void onXmlDocumentLoadError(final Uri uri, final Throwable throwable,
                final String message) {

            mLoader = null;

            // Check for parsing error
            if (throwable instanceof XmlPullParserException) {
                if (AxelUtils.isHtmlDocument(uri)) {
                    fireOnHtmlParseError(uri, message);
                } else {
                    fireOnXmlParseError(uri, message);
                }
                return;
            }

            // Check for Memory error
            if (throwable instanceof OutOfMemoryError) {
                fireOnErrorNotification(mContext.getString(R.string.notif_doc_open_error_memory));
                return;
            }

            // Check for file format error
            if (throwable instanceof UnknownFileFormatException) {
                fireOnErrorNotification(mContext.getString(R.string.notif_doc_open_error_format));
                return;
            }

            // Check for plain old IOException
            if (throwable instanceof IOException) {
                fireOnErrorNotification(mContext.getString(R.string.notif_doc_open_error_io));
                return;
            }

            // TODO handle other errors

            // Default, lets just print the error and hope for the best
            Log.e("XmlEditor", "Unknown load error for Uri " + uri, throwable);
            fireOnErrorNotification(mContext.getString(R.string.notif_doc_open_error_unknown));

        }

    };

    /**
     * The listener for the AsyncXmlWriter events
     */
    private XmlWriterListener mXmlWriterListener = new XmlWriterListener() {

        @Override
        public void onXmlDocumentWritten(final Uri uri, final String hash) {

            mDirty = false;

            if (uri != null) {
                mCurrentDocumentUri = uri;
                mCurrentDocumentName = AxelUtils.getUriFileName(mCurrentDocumentUri, mContext);
                mCurrentDocumentHash = hash;

                RecentUtils.updateRecentUris(mContext, mCurrentDocumentUri, mCurrentDocumentName);
            }

            fireOnConfirmNotification(mContext.getString(R.string.notif_doc_saved), null);
            fireOnXmlDocumentSaved();

            mWriter = null;
        }

        @Override
        public void onXmlDocumentWriteError(final Uri uri, final Throwable throwable,
                final String message) {

            // TODO handle all possible errors for user friendly toasts
            fireOnErrorNotification(throwable.getMessage());

            throwable.printStackTrace();

            mWriter = null;
        }
    };

    // ////////////////////////////////////////////////////////////////////////////////////
    // FIRE EVENTS
    // ////////////////////////////////////////////////////////////////////////////////////

    private void fireOnXmlDocumentChanged(final XmlNode root, final String name, final Uri uri) {
        for (SoftReference<XmlEditorListener> ref : mListeners) {
            if (ref.get() == null) {
                continue;
            }

            ref.get().onXmlDocumentChanged(root, name, uri);
        }
    }

    private void fireOnXmlContentChanged() {
        for (SoftReference<XmlEditorListener> ref : mListeners) {
            if (ref.get() == null) {
                continue;
            }

            ref.get().onXmlContentChanged();
        }
    }

    private void fireOnXmlDocumentSaved() {
        for (SoftReference<XmlEditorListener> ref : mListeners) {
            if (ref.get() == null) {
                continue;
            }

            ref.get().onXmlDocumentSaved();
        }
    }

    private void fireOnHtmlParseError(final Uri uri, final String message) {
        for (SoftReference<XmlEditorListener> ref : mListeners) {
            if (ref.get() == null) {
                continue;
            }

            ref.get().onHtmlParseError(uri, message);
        }
    }

    private void fireOnXmlParseError(final Uri uri, final String message) {
        for (SoftReference<XmlEditorListener> ref : mListeners) {
            if (ref.get() == null) {
                continue;
            }

            ref.get().onXmlParseError(uri, message);
        }
    }

    private void fireOnErrorNotification(final String message) {
        for (SoftReference<XmlEditorListener> ref : mListeners) {
            if (ref.get() == null) {
                continue;
            }

            ref.get().onErrorNotification(message);
        }
    }

    private void fireOnInfoNotification(final String message) {
        for (SoftReference<XmlEditorListener> ref : mListeners) {
            if (ref.get() == null) {
                continue;
            }

            ref.get().onInfoNotification(message);
        }
    }

    private void fireOnConfirmNotification(final String message, final Runnable undo) {
        for (SoftReference<XmlEditorListener> ref : mListeners) {
            if (ref.get() == null) {
                continue;
            }
            ref.get().onConfirmNotification(message, undo);
        }
    }
}
