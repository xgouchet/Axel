package fr.xgouchet.xmleditor;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;

/**
 * 
 */
public class AxelAboutActivity extends Activity implements OnClickListener {

    /**
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_about);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        findViewById(R.id.button_mail).setOnClickListener(this);
        findViewById(R.id.button_play_store).setOnClickListener(this);
        findViewById(R.id.button_rate).setOnClickListener(this);
        findViewById(R.id.button_donate).setOnClickListener(this);

    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * @see android.view.View.OnClickListener#onClick(android.view.View)
     */
    @Override
    public void onClick(final View view) {

        if (view.getId() == R.id.button_mail) {
            PackageManager pm = getPackageManager();
            ApplicationInfo appInfo = getApplicationInfo();
            PackageInfo pkgInfo;
            try {
                pkgInfo = pm.getPackageInfo(appInfo.packageName, 0);
            } catch (NameNotFoundException e) {
                pkgInfo = new PackageInfo();
                pkgInfo.versionName = "?";
            }

            CharSequence appName;
            appName = pm.getApplicationLabel(appInfo) + " (version " + pkgInfo.versionName + ")";

            sendEmail(this, appName);
        } else if (view.getId() == R.id.button_play_store) {
            openMarket(this);
        } else if (view.getId() == R.id.button_rate) {
            CharSequence appPackage;
            appPackage = getApplicationInfo().packageName;
            openMarketApp(this, appPackage);
        } else if (view.getId() == R.id.button_donate) {
            openDonate(this);
        }
    }

    /**
     * Start an email composer to send an email
     * 
     * @param ctx
     *            the current context
     * @param object
     *            the title of the mail to compose
     */
    public static void sendEmail(final Context ctx, final CharSequence object) {

        String uriText = "mailto:" + ctx.getResources().getString(R.string.ui_mail) + "?subject="
                + Uri.encode(object.toString());

        Intent email = new Intent(Intent.ACTION_SENDTO);
        email.setData(Uri.parse(uriText));

        ctx.startActivity(Intent.createChooser(email, ctx.getString(R.string.ui_choose_mail)));
    }

    /**
     * Open the market on my apps
     * 
     * @param activity
     *            the calling activity
     */
    public static void openMarket(final Activity activity) {
        String url;
        Intent market;
        market = new Intent(Intent.ACTION_VIEW);
        url = activity.getString(R.string.ui_market_url);
        market.setData(Uri.parse(url));
        try {
            activity.startActivity(market);
        } catch (ActivityNotFoundException e) {
            //            TODO Crouton.showText(activity, R.string.toast_no_market, Style.ALERT);
        }
    }

    /**
     * Open the market on this app
     * 
     * @param activity
     *            the calling activity
     * @param appPackage
     *            the application package name
     */
    public static void openMarketApp(final Activity activity, final CharSequence appPackage) {
        String url;
        Intent market;
        market = new Intent(Intent.ACTION_VIEW);
        url = activity.getString(R.string.ui_market_app_url, appPackage);
        market.setData(Uri.parse(url));
        try {
            activity.startActivity(market);
        } catch (ActivityNotFoundException e) {
            //            TODO Crouton.showText(activity, R.string.toast_no_market, Style.ALERT);
        }
    }

    /**
     * Open the market on my apps
     * 
     * @param activity
     *            the calling activity
     */
    public static void openDonate(final Activity activity) {
        String url;
        Intent donate;
        donate = new Intent(Intent.ACTION_VIEW);
        url = activity.getString(R.string.ui_donate_url);
        donate.setData(Uri.parse(url));
        activity.startActivity(donate);
    }
}
