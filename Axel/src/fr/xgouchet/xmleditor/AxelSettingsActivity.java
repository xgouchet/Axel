package fr.xgouchet.xmleditor;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.view.MenuItem;
import fr.xgouchet.xmleditor.common.Constants;
import fr.xgouchet.xmleditor.common.Settings;

@SuppressWarnings("deprecation")
public class AxelSettingsActivity extends PreferenceActivity implements
		OnSharedPreferenceChangeListener {

	/**
	 * @see android.preference.PreferenceActivity#onCreate(android.os.Bundle)
	 */

	@Override
	protected void onCreate(final Bundle icicle) {
		super.onCreate(icicle);

		getPreferenceManager().setSharedPreferencesName(
				Constants.PREFERENCES_NAME);

		addPreferencesFromResource(R.xml.axel_prefs);

		getPreferenceManager().getSharedPreferences()
				.registerOnSharedPreferenceChangeListener(this);

		updateSummaries();

		getActionBar().setDisplayHomeAsUpEnabled(true);
	}

	public boolean onOptionsItemSelected(MenuItem item) {

		if (item.getItemId() == android.R.id.home) {
			finish();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	/**
	 * @see android.content.SharedPreferences.OnSharedPreferenceChangeListener#onSharedPreferenceChanged(android.content.SharedPreferences,
	 *      java.lang.String)
	 */
	@Override
	public void onSharedPreferenceChanged(
			final SharedPreferences sharedPreferences, final String key) {
		Settings.updateFromPreferences(sharedPreferences);
		updateSummaries();
	}

	/**
	 * Updates the summaries for every list
	 */
	protected void updateSummaries() {
		ListPreference listPref;

		listPref = (ListPreference) findPreference(Constants.PREFERENCE_MAX_RECENTS);
		listPref.setSummary(listPref.getEntry());
		listPref = (ListPreference) findPreference(Constants.PREFERENCE_SINGLE_TAP_QA);
		listPref.setSummary(listPref.getEntry());
		listPref = (ListPreference) findPreference(Constants.PREFERENCE_DOUBLE_TAP_QA);
		listPref.setSummary(listPref.getEntry());
		listPref = (ListPreference) findPreference(Constants.PREFERENCE_LONG_PRESS_QA);
		listPref.setSummary(listPref.getEntry());

		listPref = (ListPreference) findPreference(Constants.PREFERENCE_INDENTATION_SIZE);
		listPref.setSummary(listPref.getEntry());
	}

}
