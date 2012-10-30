package fr.xgouchet.xmleditor.data.xml;

import android.content.Context;
import android.text.TextUtils;
import fr.xgouchet.xmleditor.R;

/**
 * 
 */
public final class XmlTreeParserException extends RuntimeException {

	/** */
	private static final long serialVersionUID = -3842092917936610127L;

	/**  */
	public enum XmlError {
		/** */
		noParser,
		/** */
		featureUnavailable,
		/** */
		ioException,
		/** */
		parseException
	}

	/**
	 * @param error
	 *            the error to set
	 */
	public XmlTreeParserException(XmlError error) {
		super();
		mError = error;
	}

	/**
	 * @param error
	 *            the error to set
	 * @param cause
	 *            the cause of this exception
	 */
	public XmlTreeParserException(XmlError error, Throwable cause) {
		super(cause);
		mError = error;
	}

	/**
	 * @param xmlContext
	 *            some more info on the parse error
	 */
	public void setXmlContext(String xmlContext) {
		mXmlContext = xmlContext;
	}

	/**
	 * @return the error type
	 */
	public XmlError getError() {
		return mError;
	}

	/**
	 * @param context
	 *            the current application context
	 * @return the message to toast
	 */
	public CharSequence getMessage(Context context) {
		String message;

		switch (mError) {
		case noParser:
			message = context.getString(R.string.toast_xml_no_parser_found);
			break;
		case featureUnavailable:
			message = context.getString(R.string.toast_xml_unsupported_feature);
			break;
		case parseException:
			if (TextUtils.isEmpty(mXmlContext)) {
				message = context.getString(R.string.toast_xml_parse_error);
			} else {
				message = context.getString(
						R.string.toast_xml_parse_error_verbose, mXmlContext);
			}
			break;
		case ioException:
		default:
			message = context.getString(R.string.toast_xml_io_exception);
		}

		return message;
	}

	private final XmlError mError;
	private String mXmlContext;

}
