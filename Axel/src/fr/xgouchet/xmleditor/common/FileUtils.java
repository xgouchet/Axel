package fr.xgouchet.xmleditor.common;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.mozilla.universalchardet.UniversalDetector;

import android.util.Log;
import fr.xgouchet.androidlib.data.TextFileUtils;


public class FileUtils {
    
    private static final String XML_HEADER_REGEX = "^\\s*<(\\?|!(--)?)?\\s*\\w+.*";
    private static final int XML_HEADER_LENGTH = 64;
    
    
    public static boolean isValidXmlFile(final InputStream input) {
        boolean result;
        
        try {
            final byte[] header = new byte[XML_HEADER_LENGTH];
            input.read(header, 0, XML_HEADER_LENGTH);
            
            String headerStr = new String(header, "ASCII");
            result = headerStr.matches(XML_HEADER_REGEX);
            
        }
        catch (Exception e) {
            result = false;
        }
        finally {
            try {
                input.close();
            }
            catch (Exception e) {
                // ignore this exception
            }
        }
        
        return result;
        
    }
    
    /**
     * 
     * @param inputStream
     * @return
     */
    public static String getHash(final InputStream input) {
        
        byte[] buffer = new byte[1024];
        int length;
        
        StringBuffer sb = new StringBuffer("");
        
        try {
            // generate MD5 digest
            MessageDigest md = MessageDigest.getInstance("MD5");
            
            // read and update MD5
            do {
                length = input.read(buffer, 0, 1024);
                if (length > 0) {
                    md.update(buffer, 0, length);
                }
            }
            while (length > 0);
            
            
            // convert the byte to hex format
            byte[] mdbytes = md.digest();
            for (int i = 0; i < mdbytes.length; i++) {
                sb.append(Integer.toHexString((mdbytes[i] & 0xff)));
            }
        }
        catch (NoSuchAlgorithmException e) {
            Log.w("FileUtils", "MD5 algorithm not available");
        }
        catch (FileNotFoundException e) {
            Log.w("FileUtils", "Input file not found, can't compute checksum");
        }
        catch (IOException e) {
            Log.w("FileUtils", "IO error, can't compute checksum");
        }
        finally {
            try {
                input.close();
            }
            catch (Exception e) {
                // ignore this exception
            }
        }
        
        // System.out.println("Digest(in hex format):: " + sb.toString());
        
        return sb.toString();
        
    }
    
    /**
     * @param file
     *            a text file
     * @return the encoding used by the file
     */
    public static String getFileEncoding(final InputStream input) {
        String encoding = null;
        final UniversalDetector detector = new UniversalDetector(null);
        final byte[] buf = new byte[1024];
        
        try {
            int len;
            do {
                len = input.read(buf);
                if (len > 0) {
                    detector.handleData(buf, 0, len);
                }
            }
            while ((len > 0) && !detector.isDone());
            detector.dataEnd();
            
            encoding = detector.getDetectedCharset();
            
            detector.reset();
            
        }
        catch (IOException e) {
            Log.w(TextFileUtils.class.getName(),
                    "IO exception while detecting encoding");
        }
        finally {
            try {
                input.close();
            }
            catch (Exception e) {
                // ignore this exception
            }
        }
        
        return encoding;
    }
    
}
