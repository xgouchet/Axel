package fr.xgouchet.xmleditor.common;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * Storage for a recent files list
 */
public final class RecentFiles {

	/**
	 * loads the recent files from shared preferences
	 * 
	 * @param saved
	 *            the previously saved string
	 */
	public static void loadRecentFiles(final String saved) {
		sPaths = new LinkedList<String>();
		String[] paths;
		paths = saved.split(File.pathSeparator);

		for (String path : paths) {
			if (path.length() > 0) {
				sPaths.add(path);
			}

			if (sPaths.size() == Settings.sMaxRecentFiles) {
				break;
			}
		}
	}

	/**
	 * Saves the preferences when they have been edited
	 * 
	 * @param prefs
	 *            the preferences to save to
	 */
	public static void saveRecentList(final SharedPreferences prefs) {
		String str = "";
		Editor editor;

		for (String path : sPaths) {
			str += path;
			str += File.pathSeparator;
		}

		editor = prefs.edit();
		editor.putString(Constants.PREFERENCE_RECENTS, str);
		editor.commit();
	}

	/**
	 * @return the list of most recent files
	 */
	public static List<String> getRecentFiles() {
		return sPaths;
	}

	/**
	 * Updates the recent list with a path. If the path is already in the list,
	 * bring it back to top, else add it.
	 * 
	 * @param path
	 *            the path to insert
	 */
	public static void updateRecentList(final String path) {
		if (sPaths.contains(path)) {
			sPaths.remove(path);
		}

		sPaths.add(0, path);
		while (sPaths.size() > Settings.sMaxRecentFiles) {
			sPaths.remove(Settings.sMaxRecentFiles);
		}

	}

	/**
	 * Removes a path from the recent files list
	 * 
	 * @param path
	 *            the path to remove
	 */
	public static void removePath(final String path) {
		if (sPaths.contains(path)) {
			sPaths.remove(path);
		}
	}

	/** the list of paths in the recent list */
	private static List<String> sPaths;

	private RecentFiles() {
	}
}
