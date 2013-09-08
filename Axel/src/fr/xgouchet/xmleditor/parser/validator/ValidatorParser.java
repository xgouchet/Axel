package fr.xgouchet.xmleditor.parser.validator;

import java.io.IOException;
import java.io.InputStream;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.util.Log;
import fr.xgouchet.xmleditor.parser.xml.XmlTreePullParser.XmlPullParserInstantiationException;
import fr.xgouchet.xmleditor.parser.xml.XmlTreePullParser.XmlPullParserUnavailableFeatureException;

/**
 * 
 */
public class ValidatorParser {

	public static ValidatorResult parseValidatorResponse(final InputStream input)
			throws XmlPullParserInstantiationException,
			XmlPullParserUnavailableFeatureException, XmlPullParserException,
			IOException {
		XmlPullParserFactory factory;
		XmlPullParser xpp;
		ValidatorParser parser;

		parser = new ValidatorParser();

		try {
			factory = XmlPullParserFactory.newInstance();
			factory.setNamespaceAware(true);
		} catch (XmlPullParserException e) {
			throw new XmlPullParserInstantiationException(
					"Factory couldn't create new parser instance", null, e);
		}

		try {
			xpp = factory.newPullParser();
		} catch (XmlPullParserException e) {
			throw new XmlPullParserUnavailableFeatureException(
					"Some required features are unavailable", null, e);
		}

		xpp.setInput(input, null);
		parser.parse(xpp);

		return parser.getResult();
	}

	// /////////////////////////////////////////////////////////

	private ValidatorResult mResult;
	private ValidatorEntry mCurrentEntry;

	/**
	 * @return the parsed result or null
	 */
	public ValidatorResult getResult() {
		return mResult;
	}

	private void parse(final XmlPullParser xpp) throws XmlPullParserException,
			IOException {

		mResult = new ValidatorResult();

		int eventType = xpp.getEventType();

		while (eventType != XmlPullParser.END_DOCUMENT) {
			switch (eventType) {
			case XmlPullParser.START_TAG:
				pullElement(xpp);
				break;
			default:
				break;
			}
			eventType = xpp.next();
		}

	}

	private static final String NS_VALIDATOR = "http://www.w3.org/2005/10/markup-validator";

	private static final String TAG_DOCTYPE = "doctype";
	private static final String TAG_CHARSET = "charset";
	// we wont check validity as no doctype means invalide
	// private static final String TAG_VALIDITY = "validity";
	private static final String TAG_ERROR = "error";
	private static final String TAG_WARNING = "warning";

	private static final String TAG_LINE = "line";
	private static final String TAG_COL = "col";
	private static final String TAG_MESSAGE = "message";
	private static final String TAG_MESSAGE_ID = "messageid";
	private static final String TAG_EXPLANATION = "explanation";

	private void pullElement(final XmlPullParser xpp)
			throws XmlPullParserException, IOException {
		String tag = xpp.getName();
		String ns = xpp.getNamespace();

		// ignore data from other namespace
		if (!NS_VALIDATOR.equals(ns)) {
			return;
		}

		if (TAG_DOCTYPE.equals(tag)) {
			if (xpp.next() == XmlPullParser.TEXT) {
				String text = xpp.getText();
				mResult.setDocType(text);
				Log.i("PARSER", "Detected doctype : " + text);
			}
		} else if (TAG_CHARSET.equals(tag)) {
			if (xpp.next() == XmlPullParser.TEXT) {
				String text = xpp.getText();
				mResult.setCharset(text);
				Log.i("PARSER", "Detected charset : " + text);
			}
		} else if (TAG_ERROR.equals(tag)) {
			mCurrentEntry = ValidatorEntry.error();
			mResult.addEntry(mCurrentEntry);
		} else if (TAG_WARNING.equals(tag)) {
			mCurrentEntry = ValidatorEntry.warning();
			mResult.addEntry(mCurrentEntry);
		} else if (TAG_LINE.equals(tag)) {
			if (xpp.next() == XmlPullParser.TEXT) {
				String text = xpp.getText();
				mCurrentEntry.setLine(Integer.parseInt(text));
			}
		} else if (TAG_COL.equals(tag)) {
			if (xpp.next() == XmlPullParser.TEXT) {
				String text = xpp.getText();
				mCurrentEntry.setColumn(Integer.parseInt(text));
			}
		} else if (TAG_MESSAGE_ID.equals(tag)) {
			if (xpp.next() == XmlPullParser.TEXT) {
				String text = xpp.getText();
				if (mCurrentEntry != null) {
					mCurrentEntry.setMessageId(text);
				}
			}
		} else if (TAG_MESSAGE.equals(tag)) {
			if (xpp.next() == XmlPullParser.TEXT) {
				String text = xpp.getText();
				mCurrentEntry.setMessage(text);
			}
		} else {
			Log.d("PARSER", "TAG : " + tag + " @(" + ns + ")");
		}
	}

	private ValidatorParser() {
	}
}
