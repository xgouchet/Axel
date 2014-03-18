package fr.xgouchet.xmleditor.common;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

public final class RecentUtils {

	private static final String PREF_RECENT = "recent";

	private static SortedSet<RecentEntry> sRecentEntries = new TreeSet<RecentUtils.RecentEntry>();

	private static int MAX = 2;

	/**
	 * Represents a Recent document entry
	 * 
	 */
	public static class RecentEntry implements Comparable<RecentEntry> {

		private final Uri mUri;
		private final long mTimestamp;
		private final String mName;

		public RecentEntry(final JSONObject json) {
			mUri = Uri.parse(json.optString("uri"));
			mTimestamp = json.optLong("timestamp");
			mName = json.optString("name");
		}

		public RecentEntry(final Uri uri, final String name,
				final long timestamp) {
			mUri = uri;
			mTimestamp = timestamp;
			mName = name;
		}

		public long getTimestamp() {
			return mTimestamp;
		}

		public Uri getUri() {
			return mUri;
		}

		public String getName() {
			return mName;
		}

		public String toJson() throws JSONException {
			JSONObject json = new JSONObject();
			json.put("uri", mUri.toString());
			json.put("timestamp", mTimestamp);
			json.put("name", mName);

			return json.toString();
		}

		@Override
		public int compareTo(final RecentEntry another) {
			if (another == null) {
				return 1;
			}

			if (another.getTimestamp() > mTimestamp) {
				return -1;
			} else if (another.getTimestamp() < mTimestamp) {
				return 1;
			} else {
				return 0;
			}

		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = (prime * result) + ((mUri == null) ? 0 : mUri.hashCode());
			return result;
		}

		@Override
		public boolean equals(final Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}

			RecentEntry other = (RecentEntry) obj;
			if (mUri == null) {
				if (other.mUri != null) {
					return false;
				}
			} else if (!mUri.equals(other.mUri)) {
				return false;
			}

			return true;
		}

	}

	/**
	 * 
	 * @param context
	 */
	public static void onResume(final Context context) {

		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);

		// Get set of json strings
		Set<String> recents = Collections.emptySet();
		recents = prefs.getStringSet(PREF_RECENT, recents);

		// fill in the data
		sRecentEntries.clear();
		for (String data : recents) {
			try {
				Log.i("RecentUtils", data);
				RecentEntry entry = new RecentEntry(new JSONObject(data));
				sRecentEntries.add(entry);
			} catch (Exception e) {
				// Ignore
				Log.e("RecentUtils", "Error in json parsing", e);
			}
		}
	}

	/**
	 * @return an immutable set of recently used URIs
	 */
	public static SortedSet<RecentEntry> getRecentEntries() {
		return sRecentEntries;
	}

	/**
	 * Updates and save the recent uris list
	 * 
	 * @param context
	 *            the current application's context
	 * @param uri
	 *            the current uri
	 */
	public static void updateRecentUris(final Context context, final Uri uri,
			final String name) {

		// Add the entry
		RecentEntry newEntry, existingEntry = null;
		newEntry = new RecentEntry(uri, name, System.currentTimeMillis());

		// check for an existing entry
		for (RecentEntry entry : sRecentEntries) {
			if (entry.mUri.equals(uri)) {
				existingEntry = entry;
				break;
			}
		}
		
		// remove the existing entry (update the timestamp)
		if (existingEntry != null) {
			sRecentEntries.remove(existingEntry);
		}
		
		// save the new entry
		sRecentEntries.add(newEntry);

		// check for max size
		if (sRecentEntries.size() > 5) {
			sRecentEntries.remove(sRecentEntries.first());
		}

		saveRecentEntries(context);
	}

	/**
	 * Saves the current recent entries state
	 * 
	 * @param context
	 */
	public static void saveRecentEntries(final Context context) {

		// Convert all entries to JSON
		Set<String> stringSet = new HashSet<String>();
		for (RecentEntry entry : sRecentEntries) {
			try {
				stringSet.add(entry.toJson());
			} catch (JSONException e) {
				// Ignore
				Log.e("RecentUtils", "Error in json generating", e);
			}
		}

		// save string set
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		prefs.edit().putStringSet(PREF_RECENT, stringSet).apply();

	}
}
