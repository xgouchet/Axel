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
			case XmlPullParser.END_TAG:

				break;
			case XmlPullParser.TEXT:

				break;
			case XmlPullParser.CDSECT:

				break;
			case XmlPullParser.ENTITY_REF:
				break;
			case XmlPullParser.IGNORABLE_WHITESPACE:
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
	private static final String TAG_VALIDITY = "validity";

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
		} else {
			Log.d("PARSER", "TAG : " + tag + " @(" + ns + ")");
		}
	}

	private ValidatorParser() {
	}
}
