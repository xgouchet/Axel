package fr.xgouchet.xmleditor.tasks;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.xmlpull.v1.XmlPullParserException;

import fr.xgouchet.xmleditor.R;
import fr.xgouchet.xmleditor.common.FileUtils;
import fr.xgouchet.xmleditor.parser.html.HtmlCleanerParser;
import fr.xgouchet.xmleditor.parser.xml.XmlTreePullParser.XmlPullParserInstantiationException;
import fr.xgouchet.xmleditor.parser.xml.XmlTreePullParser.XmlPullParserUnavailableFeatureException;
import android.content.Context;
import android.net.Uri;


public class AsyncHtmlLoader extends AsyncXmlLoader {
    
    
    public AsyncHtmlLoader(Context context, Uri uri, XmlLoaderListener listener, int flags) {
        super(context, uri, listener, flags);
    }
    
    @Override
    protected void doParseDocument()
            throws StringIndexOutOfBoundsException, XmlPullParserUnavailableFeatureException,
            XmlPullParserInstantiationException, XmlPullParserException, IOException {
        
        mEncoding = null;
        
        // Get file's original hash
        publishProgress(mContext.getString(R.string.ui_hashing));
        mHash = FileUtils.getHash(getInputStream());
        
        // Get file encoding
        mEncoding = FileUtils.getFileEncoding(getInputStream());
        
        // Actually parse the html soup
        InputStream input = getInputStream();
        publishProgress(mContext.getString(R.string.ui_parsing));
        mRoot = HtmlCleanerParser.parseHtmlTree(new InputStreamReader(input));
        
        // close the stream
        if (input != null) {
            input.close();
        }
    }
}
