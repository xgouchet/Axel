package fr.xgouchet.xmleditor.common;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.preference.PreferenceManager;
import android.util.Log;
import fr.xgouchet.xmleditor.R;

/**
 * 
 */
public class AxelChangeLog {

    private static final String PREF_PREV_VERSION = "previous_version";
    private int mVersion = -1;

    /**
     * Warning, this automatically saves the current version, so you only get
     * one shot. Then again, I guess that's the idea...
     * 
     * @param context
     *            the current application context
     * @return if this is the first launch since last update
     */
    public boolean isFirstLaunchEver(final Context context) {
        SharedPreferences prefs = getPreferences(context);

        int previous;

        previous = getPreviousVersion(context, prefs);

        return (previous < 0);
    }

    /**
     * Warning, this automatically saves the current version, so you only get
     * one shot. Then again, I guess that's the idea...
     * 
     * @param context
     *            the current application context
     * @return if this is the first launch since last update
     */
    public boolean isFirstLaunchAfterUpdate(final Context context) {

        SharedPreferences prefs = getPreferences(context);

        int previous, current;

        previous = getPreviousVersion(context, prefs);
        current = getCurrentVersion(context);

        return (previous < current);
    }

    /**
     * 
     * @param context
     * @return if an update was detected
     */
    public boolean displayChangeLog(final Context context) {

        SharedPreferences prefs = getPreferences(context);

        boolean updateLaunch;
        updateLaunch = isFirstLaunchAfterUpdate(context);

        if (updateLaunch) {
            displayUpdateDialog(context);
        }

        saveCurrentVersion(context, prefs);

        return updateLaunch;
    }

    /**
     * Saves the current version for next launch
     * 
     * @param context
     *            the current application context
     * @param prefs
     *            the shared preferences for this app
     */
    public void saveCurrentVersion(final Context context, final SharedPreferences prefs) {
        Editor editor;

        editor = prefs.edit();
        editor.putInt(PREF_PREV_VERSION, getCurrentVersion(context));
        editor.commit();
    }

    /**
     * Displays an alert dialog with the update info
     * 
     * @param context
     *            the current application context
     */
    private void displayUpdateDialog(final Context context) {
        Builder builder;

        builder = new Builder(context);

        builder.setTitle(R.string.ui_whats_new);
        builder.setMessage(getChangelogMessage(context));
        builder.setCancelable(true);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int which) {
                dialog.dismiss();
            }
        });

        builder.create().show();
    }

    /**
     * @param context
     *            the current application context
     * @return the message for the changelog
     */
    private String getChangelogMessage(final Context context) {
        StringBuilder builder = new StringBuilder();

        builder.append(context.getString(R.string.quick_help));
        builder.append("\n\n");

        builder.append(context.getString(getTitleResource(context)));
        builder.append("\n\n");
        builder.append(context.getString(getChangeLogResource(context)));

        return builder.toString();
    }

    /**
     * @param context
     *            the current application context
     * @return the string resource for the title to display in the change log
     */
    private int getTitleResource(final Context context) {
        return getTitleResourceForVersion(getCurrentVersion(context));
    }

    /**
     * @param version
     *            the version code of the application
     * @return the string resource for the title to display in the change log
     */
    private int getTitleResourceForVersion(final int version) {
        int title;
        switch (version) {
            case 1:
                title = R.string.release1;
                break;
            case 2:
                title = R.string.release2;
                break;
            case 3:
                title = R.string.release3;
                break;
            case 4:
                title = R.string.release4;
                break;
            case 5:
                title = R.string.release5;
                break;
            case 6:
                title = R.string.release6;
                break;
            case 7:
            case 8:
            case 9:
            case 10:
                title = R.string.release7;
                break;
            case 11:
                title = R.string.release11;
                break;
            case 12:
                title = R.string.release12;
                break;
            case 13:
            default:
                title = R.string.release13;
                break;
        }
        return title;
    }

    /**
     * @param context
     *            the current application context
     * @return the string resource for the changelog to display in the change
     *         log
     */
    private int getChangeLogResource(final Context context) {
        return getChangeLogResourceForVersion(getCurrentVersion(context));
    }

    /**
     * @param version
     *            the version code of the application
     * @return the string resource for the changelog to display in the change
     *         log
     */
    private int getChangeLogResourceForVersion(final int version) {
        int log;
        switch (version) {
            case 1:
                log = R.string.release1_log;
                break;
            case 2:
                log = R.string.release2_log;
                break;
            case 3:
                log = R.string.release3_log;
                break;
            case 4:
                log = R.string.release4_log;
                break;
            case 5:
                log = R.string.release5_log;
                break;
            case 6:
                log = R.string.release6_log;
                break;
            case 7:
            case 8:
            case 9:
            case 10:
                log = R.string.release7_log;
                break;
            case 11:
                log = R.string.release11_log;
                break;
            case 12:
                log = R.string.release12_log;
                break;
            case 13:
            default:
                log = R.string.release13_log;
                break;
        }
        return log;
    }

    /**
     * @param context
     *            the current application context
     * @param prefs
     *            the shared preferences to use
     * @return the previously opened version of the application
     */
    private int getPreviousVersion(final Context context, final SharedPreferences prefs) {
        return prefs.getInt(PREF_PREV_VERSION, -1);
    }

    /**
     * @param context
     *            the current application context
     * @return the current installed application version
     */
    private int getCurrentVersion(final Context context) {
        int version;

        if (mVersion < 0) {
            try {
                version = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
            } catch (NameNotFoundException e) {
                Log.e("Felix",
                        "Unable to get package info for package name " + context.getPackageName());
                version = -1;
            }

            if (version >= 0) {
                mVersion = version;
            }
        } else {
            version = mVersion;
        }

        return version;
    }

    private SharedPreferences getPreferences(final Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

}
