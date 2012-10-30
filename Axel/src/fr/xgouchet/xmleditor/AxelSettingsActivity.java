package fr.xgouchet.xmleditor;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import fr.xgouchet.xmleditor.common.Constants;
import fr.xgouchet.xmleditor.common.Settings;

@SuppressWarnings("deprecation")
public class AxelSettingsActivity extends PreferenceActivity implements
		OnSharedPreferenceChangeListener {

	/**
	 * @see android.preference.PreferenceActivity#onCreate(android.os.Bundle)
	 */

	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		getPreferenceManager().setSharedPreferencesName(
				Constants.PREFERENCES_NAME);

		addPreferencesFromResource(R.xml.axel_prefs);

		getPreferenceManager().getSharedPreferences()
				.registerOnSharedPreferenceChangeListener(this);

		updateSummaries();
	}

	/**
	 * @see android.content.SharedPreferences.OnSharedPreferenceChangeListener#onSharedPreferenceChanged(android.content.SharedPreferences,
	 *      java.lang.String)
	 */
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		Settings.updateFromPreferences(sharedPreferences);
	}

	/**
	 * Updates the summaries for every list
	 */
	protected void updateSummaries() {
		ListPreference listPref;

		listPref = (ListPreference) findPreference(Constants.PREFERENCE_MAX_RECENTS);
		listPref.setSummary(listPref.getEntry());
	}

}
