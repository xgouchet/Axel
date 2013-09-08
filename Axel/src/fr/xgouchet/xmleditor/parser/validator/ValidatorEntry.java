package fr.xgouchet.xmleditor.parser.validator;

/**
 * 
 */
public class ValidatorEntry {

	/**
	 * The type of entry
	 */
	public enum Type {
		ERROR, WARNING;
	}

	public static ValidatorEntry error() {
		return new ValidatorEntry(Type.ERROR);
	}

	public static ValidatorEntry warning() {
		return new ValidatorEntry(Type.WARNING);
	}

	private int mLine, mColumn;

	String mMessageId;

	private String mMessage, mExplanation;

	private final Type mType;

	/**
	 * @param type
	 *            the type
	 */
	public ValidatorEntry(final Type type) {
		mType = type;
	}

	public int getColumn() {
		return mColumn;
	}

	public String getExplanation() {
		return mExplanation;
	}

	public int getLine() {
		return mLine;
	}

	public String getMessage() {
		return mMessage;
	}

	public String getMessageId() {
		return mMessageId;
	}

	public Type getType() {
		return mType;
	}

	public void setColumn(final int column) {
		mColumn = column;
	}

	public void setExplanation(final String explanation) {
		mExplanation = explanation;
	}

	public void setLine(final int line) {
		mLine = line;
	}

	public void setMessage(final String message) {
		mMessage = message;
	}

	public void setMessageId(final String messageId) {
		mMessageId = messageId;
	}

	@Override
	public String toString() {

		return "#" + mMessageId + " : " + mMessage;
	}
}
