package fr.xgouchet.xmleditor.parser.xml;

import android.content.Context;
import fr.xgouchet.xmleditor.R;

/**
 * 
 */
@Deprecated
public final class XmlTreeParserException extends RuntimeException {

	/** */
	private static final long serialVersionUID = -3842092917936610127L;

	/**  */
	public enum XmlError {
		/** */
		noError,
		/** */
		noParser,
		/** */
		featureUnavailable,
		/** */
		ioException,
		/** */
		parseException,
		/** */
		noInput,
		/** */
		fileNotFound,
		/** */
		outOfMemory,
		/** */
		write,
		/** */
		rename,
		/** */
		delete,
		/** */
		unknown,

	}

	/**
	 * @param error
	 *            the error to set
	 * @param cause
	 *            the cause of this exception
	 */
	public XmlTreeParserException(final XmlError error, final Throwable cause) {
		super(cause);
		mError = error;
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
	public String getMessage(final Context context) {
		String message;

		switch (mError) {
		case noParser:
			message = context.getString(R.string.toast_xml_no_parser_found);
			break;
		case featureUnavailable:
			message = context.getString(R.string.toast_xml_unsupported_feature);
			break;
		case parseException:
			message = context.getString(R.string.toast_xml_parse_error);
			break;
		case ioException:
		default:
			message = context.getString(R.string.toast_xml_io_exception);
		}

		return message;
	}

	/**
	 * @see java.lang.Throwable#getMessage()
	 */
	@Override
	public String getMessage() {
		return getCause().getMessage();
	}

	private final XmlError mError;

}
