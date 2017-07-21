package com.wordpress.icemc.gsmcodes.utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

public class AppStart {

    private AppStart() {
    }

    // The app version code that was used during the last startup
    private static final String LAST_APP_VERSION = "1";

    //caches the result of {@link #checkAppStartStatus(Context context, SharedPreferences sharedPreferences)}
    private static AppStartStatus status = null;

    /**
     * Finds out started for the first time (ever or in the current version)
     * @return the type of app start status
     */
    public static AppStartStatus checkAppStartStatus(Context context, SharedPreferences sharedPreferences) {
        PackageInfo packageInfo;

        try {
            packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.COMPONENT_ENABLED_STATE_DEFAULT);

            int lastVersionCode = sharedPreferences.getInt(LAST_APP_VERSION, -1);
            int currentVersionCode = packageInfo.versionCode;
            status = checkAppStartStatus(currentVersionCode, lastVersionCode);

            //update version in preferences
            sharedPreferences.edit()
                    .putInt(LAST_APP_VERSION, currentVersionCode)
                    .commit();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return status;
    }

    public static AppStartStatus checkAppStartStatus(int currentVersionCode, int lastVersionCode) {
        if (lastVersionCode == -1) {
            return AppStartStatus.FIRST_TIME;
        } else if (lastVersionCode < currentVersionCode) {
            return AppStartStatus.FIRST_TIME_VERSION;
        } else if (lastVersionCode > currentVersionCode) {
            //TODO log a warning msg here
            return AppStartStatus.NORMAL;
        } else {
            return AppStartStatus.NORMAL;
        }
    }
}

