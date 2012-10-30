package fr.xgouchet.xmleditor.data.xml;

import android.text.TextUtils;

/**
 * 
 */
public class XmlAttribute {

	/**
	 * Represents an xml attribute without namespace ( name="value" )
	 * 
	 * @param name
	 * @param value
	 */
	public XmlAttribute(String name, String value) {
		this(null, name, value);
	}

	/**
	 * Represents an xml attribute ( prefix:name="value" )
	 * 
	 * @param prefix
	 *            the namespace (or null for attributes without namespace)
	 * @param name
	 *            the name
	 * @param value
	 *            the value
	 */
	public XmlAttribute(String prefix, String name, String value) {
		mName = name;
		mValue = value;
		mPrefix = (prefix == null) ? "" : prefix;
	}

	/**
	 * @param attr
	 *            the attribute to copy
	 */
	public XmlAttribute(XmlAttribute attr) {
		mName = new String(attr.getName());
		mValue = new String(attr.getValue());
		mPrefix = new String(attr.getPrefix());
	}

	/**
	 * @param prefix
	 *            the namespace
	 */
	public void setPrefix(String prefix) {
		mPrefix = (prefix == null) ? "" : prefix;
	}

	/**
	 * @param name
	 *            the name
	 */
	public void setName(String name) {
		mName = name;
	}

	/**
	 * @param value
	 *            the value to set
	 */
	public void setValue(String value) {
		mValue = value;
	}

	/**
	 * @return the prefix (ie namespace)
	 */
	public String getPrefix() {
		return mPrefix;
	}

	/**
	 * @return the attribute's name (without it's namespace)
	 */
	public String getName() {
		return mName;
	}

	/**
	 * @return the attribute's string value
	 */
	public String getValue() {
		return mValue;
	}

	/**
	 * @return the full attribute name (ie "prefix:name")
	 */
	public String getFullName() {
		String fullName = "";
		if (!TextUtils.isEmpty(mPrefix)) {
			fullName = mPrefix + ":";
		}

		fullName += mName;
		return fullName;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return (getFullName() + "=\"" + getValue() + "\"");
	}

	/** */
	protected String mPrefix;
	/** */
	protected String mName;
	/** */
	protected String mValue;

}
