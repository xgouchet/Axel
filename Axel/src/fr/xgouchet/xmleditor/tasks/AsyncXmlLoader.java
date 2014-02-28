package fr.xgouchet.xmleditor.tasks;

import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.NotSerializableException;

import org.xmlpull.v1.XmlPullParserException;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.TextUtils;
import fr.xgouchet.axml.CompressedXmlUtils;
import fr.xgouchet.plist.PlistUtils;
import fr.xgouchet.xmleditor.R;
import fr.xgouchet.xmleditor.common.FileUtils;
import fr.xgouchet.xmleditor.data.xml.UnknownFileFormatException;
import fr.xgouchet.xmleditor.data.xml.XmlNode;
import fr.xgouchet.xmleditor.parser.plist.XMLPlistParser;
import fr.xgouchet.xmleditor.parser.xml.XmlCompressedTreeParser;
import fr.xgouchet.xmleditor.parser.xml.XmlTreePullParser;
import fr.xgouchet.xmleditor.parser.xml.XmlTreePullParser.XmlPullParserInstantiationException;
import fr.xgouchet.xmleditor.parser.xml.XmlTreePullParser.XmlPullParserUnavailableFeatureException;


/**
 * Loads asynchronously an XML document based on a document's URI
 * 
 * @author Xavier Gouchet
 * 
 */
public class AsyncXmlLoader extends AsyncTask<Void, String, Void> {
    
    /** Asks the loader to ignore the original file */
    public static final int FLAG_IGNORE_FILE = 0x01;
    /** forces the result to be read only */
    public static final int FLAG_FORCE_READ_ONLY = 0x02;
    /** treat the document as HTML soup */
    public static final int FLAG_HTML_SOUP = 0x04;
    
    /** The current application context */
    protected final Context mContext;
    /** The progress dialog */
    private ProgressDialog mDialog;
    
    /** The loaded uri */
    protected final Uri mUri;
    /** The loaded document's Hash */
    protected String mHash;
    /** The loaded document's encoding */
    protected String mEncoding;
    /** the loaded document's XML root */
    protected XmlNode mRoot;
    
    /** Ignores the source file */
    private boolean mIgnoreFile;
    /** Force document as read only ? */
    private boolean mForceReadOnly;
    
    /** the listener for this loader's events */
    protected final XmlLoaderListener mListener;
    
    /** Throwable thrown while loading */
    protected Throwable mThrowable;
    
    
    /**
     * 
     * @param context
     * @param listener
     * @param flags
     */
    public AsyncXmlLoader(final Context context, final Uri uri,
            final XmlLoaderListener listener, final int flags) {
        mUri = uri;
        mContext = context;
        mListener = listener;
        mForceReadOnly = ((flags & FLAG_FORCE_READ_ONLY) == FLAG_FORCE_READ_ONLY);
        mIgnoreFile = ((flags & FLAG_IGNORE_FILE) == FLAG_IGNORE_FILE);
    }
    
    
    //////////////////////////////////////////////////////////////////////////////////////
    // ASYNC TASK LIFECYCLE
    //////////////////////////////////////////////////////////////////////////////////////
    
    @Override
    protected void onCancelled(final Void result) {
        
        if (mDialog != null) {
            mDialog.dismiss();
        }
    }
    
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        
        // create the progress dialog
        if (mDialog == null) {
            mDialog = new ProgressDialog(mContext);
            mDialog.setTitle(R.string.ui_loading);
            mDialog.setMessage(mContext.getString(R.string.ui_wait));
        }
        
        // show the progress dialog
        mDialog.show();
        mDialog.setCancelable(false);
    }
    
    @Override
    protected Void doInBackground(final Void... params) {
        
        
        try {
            doParseDocument();
        }
        catch (Exception e) {
            mThrowable = e;
        }
        
        return null;
    }
    
    @Override
    protected void onProgressUpdate(final String... values) {
        
        // update dialog title
        mDialog.setTitle(TextUtils.concat(values));
        
        super.onProgressUpdate(values);
    }
    
    @Override
    protected void onPostExecute(final Void result) {
        super.onPostExecute(result);
        
        // dismiss the dialog
        mDialog.dismiss();
        mDialog = null;
        
        // callback uri
        Uri uri = (mIgnoreFile ? null : mUri);
        
        if (mThrowable == null) {
            mListener.onXmlFileLoaded(mRoot, uri, mHash, mEncoding,
                    mForceReadOnly);
        } else {
            mListener.onXmlFileLoadError(uri, mThrowable, null);
        }
        
    }
    
    //////////////////////////////////////////////////////////////////////////////////////
    // XML LOADING
    //////////////////////////////////////////////////////////////////////////////////////
    
    /**
     * Try and parse the document in any known format
     * 
     * @throws IOException
     * @throws XmlPullParserException
     * @throws XmlPullParserInstantiationException
     * @throws XmlPullParserUnavailableFeatureException
     * @throws StringIndexOutOfBoundsException
     */
    protected void doParseDocument()
            throws StringIndexOutOfBoundsException, XmlPullParserUnavailableFeatureException,
            XmlPullParserInstantiationException, XmlPullParserException, IOException {
        
        mEncoding = null;
        
        // Get file's original hash
        publishProgress(mContext.getString(R.string.ui_hashing));
        mHash = FileUtils.getHash(getInputStream());
        
        // Check if file is compresed
        if (FileUtils.isValidXmlFile(getInputStream())) {
            mEncoding = FileUtils.getFileEncoding(getInputStream());
            doParseXmlDocument();
        } else if (CompressedXmlUtils.isCompressedXml(getInputStream())) {
            doParseCompressedDocument();
        } else if (PlistUtils.isBinaryPlist(getInputStream())) {
            doParseBinaryPlistDocument();
        } else {
            mThrowable = new UnknownFileFormatException();
        }
    }
    
    /**
     * Try and parse the document as a standard XML
     * 
     * @throws IOException
     * @throws XmlPullParserException
     * @throws XmlPullParserInstantiationException
     * @throws XmlPullParserUnavailableFeatureException
     * @throws StringIndexOutOfBoundsException
     */
    protected void doParseXmlDocument()
            throws StringIndexOutOfBoundsException, XmlPullParserUnavailableFeatureException,
            XmlPullParserInstantiationException, XmlPullParserException, IOException {
        InputStream input = getInputStream();
        
        // 
        publishProgress(mContext.getString(R.string.ui_parsing));
        mRoot = XmlTreePullParser.parseXmlTree(input, true, mEncoding);
        
        if (input != null) {
            input.close();
        }
    }
    
    /**
     * Try and parse the document as a compressed Android XML
     * 
     * @throws IOException
     */
    protected void doParseCompressedDocument()
            throws IOException {
        InputStream input = getInputStream();
        
        // 
        publishProgress(mContext.getString(R.string.ui_parsing));
        mRoot = XmlCompressedTreeParser.parseXmlTree(input);
        mForceReadOnly = true;
        
        if (input != null) {
            input.close();
        }
    }
    
    /**
     * Try and parse the document as a Binary PList
     * 
     * @throws IOException
     * @throws FileNotFoundException
     * @throws EOFException
     * @throws NotSerializableException
     */
    protected void doParseBinaryPlistDocument()
            throws NotSerializableException, EOFException, FileNotFoundException, IOException {
        InputStream input = getInputStream();
        
        // 
        publishProgress(mContext.getString(R.string.ui_parsing));
        mRoot = XMLPlistParser.parseXmlTree(input);
        mForceReadOnly = true;
        
        if (input != null) {
            input.close();
        }
    }
    
    
    
    //////////////////////////////////////////////////////////////////////////////////////
    // UTILS
    //////////////////////////////////////////////////////////////////////////////////////
    
    /**
     * 
     * @return the input stream to load
     * @throws FileNotFoundException
     */
    protected InputStream getInputStream()
            throws FileNotFoundException {
        return mContext.getContentResolver().openInputStream(mUri);
    }
    
}
