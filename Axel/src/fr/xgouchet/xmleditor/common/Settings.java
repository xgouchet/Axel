package fr.xgouchet.xmleditor.common;

import android.content.SharedPreferences;

/**
 * The settings fields
 */
public final class Settings {

	/** */
	public static int sMaxRecentFiles = 10;
	/** */
	public static boolean sKeepTextExact = true;
	/** */
	public static boolean sShowAttrInline = false;
	/** */
	public static boolean sEditOnCreate = true;
	/** */
	public static String sSingleTapQA = Constants.QUICK_ACTION_EDIT;
	/** */
	public static String sDoubleTapQA = Constants.QUICK_ACTION_ADD_CHILD;
	/** */
	public static String sLongPressQA = Constants.QUICK_ACTION_DISPLAY_MENU;
	/** */
	public static boolean sEscapeTextContent = true;
	/** */
	public static boolean sEscapeAttributeValues = true;
	/** */
	public static String sIndentationSize = Constants.INDENT_MEDIUM;

	/**
	 * Updates the settings from the preference
	 * 
	 * @param preferences
	 *            the preference file to use
	 */
	public static void updateFromPreferences(final SharedPreferences preferences) {
		RecentFiles.loadRecentFiles(preferences.getString(
				Constants.PREFERENCE_RECENTS, ""));

		sMaxRecentFiles = getStringPreferenceAsInteger(preferences,
				Constants.PREFERENCE_MAX_RECENTS, "10");

		sKeepTextExact = preferences.getBoolean(
				Constants.PREFERENCE_KEEP_TEXT_EXACT, true);

		sShowAttrInline = preferences.getBoolean(
				Constants.PREFERENCE_DISPLAY_ATTRIBUTES_INLINE, false);

		sEditOnCreate = preferences.getBoolean(
				Constants.PREFERENCE_EDIT_ON_CREATE, true);

		sSingleTapQA = preferences
				.getString(Constants.PREFERENCE_SINGLE_TAP_QA,
						Constants.QUICK_ACTION_EDIT);
		sDoubleTapQA = preferences.getString(
				Constants.PREFERENCE_DOUBLE_TAP_QA,
				Constants.QUICK_ACTION_ADD_CHILD);

		sLongPressQA = preferences.getString(
				Constants.PREFERENCE_LONG_PRESS_QA,
				Constants.QUICK_ACTION_DISPLAY_MENU);

		sEscapeAttributeValues = preferences.getBoolean(
				Constants.PREFERENCE_ESCAPE_ATTR_VALUE, true);
		sEscapeTextContent = preferences.getBoolean(
				Constants.PREFERENCE_ESCAPE_TEXT_CONTENT, true);

		sIndentationSize = preferences.getString(
				Constants.PREFERENCE_INDENTATION_SIZE, Constants.INDENT_MEDIUM);
	}

	/**
	 * Reads a preference stored as a string and returns the numeric value
	 * 
	 * @param prefs
	 *            the prefernce to read from
	 * @param key
	 *            the key
	 * @param def
	 *            the default value
	 * @return the value as an int
	 */
	protected static int getStringPreferenceAsInteger(
			final SharedPreferences prefs, final String key, final String def) {
		String strVal = null;
		int intVal;

		try {
			strVal = prefs.getString(key, def);
		} catch (Exception e) {
			strVal = def;
		}

		try {
			intVal = Integer.parseInt(strVal);
		} catch (NumberFormatException e) {
			intVal = 0;
		}

		return intVal;
	}

	private Settings() {

	}
}
