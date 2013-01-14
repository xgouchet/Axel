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
