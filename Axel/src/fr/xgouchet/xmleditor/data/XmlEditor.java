package fr.xgouchet.xmleditor.data;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;

import android.content.Context;
import fr.xgouchet.xmleditor.R;
import fr.xgouchet.xmleditor.common.AxelUtils;
import fr.xgouchet.xmleditor.data.tree.TreeNode;
import fr.xgouchet.xmleditor.data.xml.XmlData;
import fr.xgouchet.xmleditor.data.xml.XmlNode;
import fr.xgouchet.xmleditor.parser.xml.XmlTreeParserException;
import fr.xgouchet.xmleditor.parser.xml.XmlTreePullParser;
import fr.xgouchet.xmleditor.tasks.AsyncHtmlFileLoader;
import fr.xgouchet.xmleditor.tasks.AsyncXmlFileLoader;
import fr.xgouchet.xmleditor.tasks.AsyncXmlFileLoader.XmlFileLoaderListener;
import fr.xgouchet.xmleditor.tasks.AsyncXmlFileWriter;

/**
 * This class handles all
 */
public class XmlEditor implements XmlFileLoaderListener {

	/** The root of the underlying document */
	private XmlNode mRoot;
	/** The currently selected node */
	private XmlNode mSelection;

	/** is dirty ? */
	private boolean mDirty;
	/** is read only */
	private boolean mReadOnly;

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

	/**
	 * Callback for events on the document being edited
	 */
	public static interface XmlEditorListener {
		/**
		 * Called when the document being edited changed (clear / load)
		 * 
		 * @param root
		 *            the root element of the document
		 * @param name
		 *            the file name
		 * @param path
		 *            the file path
		 */
		void onXmlDocumentChanged(XmlNode root, String name, String path);

		/**
		 * Call when something in the xml tree changed
		 */
		void onXmlContentChanged();

		/**
		 * @param message
		 *            an error message to display
		 */
		void onXmlErrorNotification(String message);

		/**
		 * @param message
		 *            an confirmation message to display
		 */
		void onXmlConfirmNotification(String message);

	}

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

	/**
	 * @return if the current document has a name and path, and can be saved as
	 *         is
	 */
	public boolean hasPath() {
		return ((mCurrentFilePath == null) || (mCurrentFilePath.length() == 0));
	}

	/**
	 * Called when the app leaves foreground, and async operation must be
	 * canceled
	 */
	public void doCancelOperations() {
		if (mLoader != null) {
			mLoader.cancel(true);
		}
	}

	/**
	 * Clears the content of the editor. Assumes that user was prompted and
	 * previous data was saved
	 */
	public void doClearContents() {
		mCurrentFilePath = null;
		mCurrentFileName = null;
		mCurrentEncoding = null;

		mDirty = false;
		mReadOnly = false;

		mRoot = XmlNode.createDocument();
		mRoot.addChildNode(XmlNode.createDocumentDeclaration("1.0", "UTF-8",
				null));
		mRoot.setExpanded(true, true);
		mRoot.updateChildViewCount(true);

		mListener.onXmlDocumentChanged(mRoot, mCurrentFileName,
				mCurrentFilePath);
	}

	/**
	 * Send the order to open a file into the app
	 * 
	 * @param file
	 *            the source file
	 * @param ignore
	 *            ignore the file link
	 */
	public void doOpenFile(final File file, final boolean ignore) {
		if (mLoader == null) {
			int flags = 0;
			if (ignore) {
				flags |= XmlFileLoaderResult.FLAG_IGNORE_FILE;
			}

			mLoader = new AsyncXmlFileLoader(mContext, this, flags);
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
	public void doOpenFileAsHtml(final File file, final boolean forceReadOnly) {
		if (mLoader == null) {

			mLoader = new AsyncHtmlFileLoader(mContext, this,
					XmlFileLoaderResult.FLAG_HTML_SOUP);
			mLoader.execute(file);
		}
	}

	/**
	 * Reloads the current file
	 */
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
		if (path == null) {
			mListener.onXmlErrorNotification(mContext
					.getString(R.string.toast_save_null));
			return;
		}

		if (mWriter == null) {
			// TODO mWriter = new AsyncXmlFileWriter(this, mRoot,
			// mCurrentEncoding);
			// mWriter.execute(path);
		}
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
		// FIXME mClipboard.setText(text, label);

		crouton = mContext.getString(R.string.ui_copy_clipboard,
				AxelUtils.ellipsize(text, 64));
		mListener.onXmlConfirmNotification(crouton);
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
		// FIXME mClipboard.setText(text, label);

		// remove selected node
		XmlNode parent = (XmlNode) mSelection.getParent();
		parent.removeChildNode(mSelection);
		mSelection = null;

		setDirty();

		crouton = mContext.getString(R.string.ui_copy_clipboard,
				AxelUtils.ellipsize(text, 64));
		mListener.onXmlConfirmNotification(crouton);
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

		String[] clipboardText = null;// FIXME mClipboard.getText();

		for (String clip : clipboardText) {
			node = contentAsXml(clip);
			if (node != null) {
				parent = mSelection;
				if (parent.canHasChild(node)) {
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
			if (parent.canHasChild(node)) {
				node.setExpanded(true, true);
				node.updateChildViewCount(true);

				index = parent.getChildPosition(mSelection);
				parent.removeChildNode(mSelection);
				parent.addChildNode(node, index);

				parent.updateParentViewCount();

				setDirty();
			} else {
				mListener.onXmlErrorNotification(mContext
						.getString(R.string.toast_xml_uncomment));
			}
		}
	}

	/**
	 * @return the content of the current selected node (comment) as an XML node
	 */
	private XmlNode getCommentContent() {
		return contentAsXml(mSelection.getContent().getText());
	}

	/**
	 * @param content
	 * @return the string content as an XML Node (or null
	 */
	private XmlNode contentAsXml(final String content) {
		XmlNode node = null, doc = null;
		InputStream input;

		if (content.indexOf('<') < 0) {
			node = XmlNode.createText(content);
		} else if (content
				.matches("^\\s*<\\?\\s*([a-zA-Z:_])([a-zA-Z0-9:_]*)\\s(.(?!>))*\\?>\\s*$")) {
			int start = content.indexOf("<?") + 2;
			int end = content.indexOf("?>");
			node = XmlNode.createProcessingInstruction(content.substring(start,
					end));
		} else if (content.matches("^\\s*<!\\[CDATA\\[([\\w\\W\\s]*)]]>\\s*$")) {
			int start = content.indexOf("<![CDATA[") + 9;
			int end = content.indexOf("]]>");
			node = XmlNode.createCDataSection(content.substring(start, end));
		} else {
			input = new ByteArrayInputStream(content.getBytes());
			try {
				doc = XmlTreePullParser.parseXmlTree(input, false, null);
			} catch (XmlTreeParserException e) {
				mListener.onXmlErrorNotification(e.getMessage(mContext));
			}

			if ((doc != null) && (doc.getChildrenCount() > 0)) {
				node = (XmlNode) doc.getChildAtPos(0);
			}
		}

		return node;
	}

	private void setDirty() {
		mDirty = true;
		mListener.onXmlContentChanged();
	}

	@Override
	public void onXmlFileError(final Throwable throwable, final String message) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onXmlFileLoaded(final XmlNode root, final File file, final String hash,
			final String encoding, final boolean readOnly) {
		// TODO Auto-generated method stub

	}
}
