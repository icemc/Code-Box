package com.wordpress.icemc.gsmcodes.application;

import android.app.Activity;
import android.app.Application;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.wordpress.icemc.gsmcodes.utilities.AppStart;
import com.wordpress.icemc.gsmcodes.utilities.LocaleHelper;


public class GSMCodes extends Application{
    private static GSMCodes ourInstance = new GSMCodes();
    public static Configuration phoneConfiguration;

    public static GSMCodes getInstance() {
        return ourInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        phoneConfiguration = getResources().getConfiguration();
        switch (AppStart.checkAppStartStatus(this, PreferenceManager.getDefaultSharedPreferences(this))) {
            case NORMAL:
                new LocaleHelper().updateLocale(this);
                break;
            case FIRST_TIME:
                AppStart.setAppStartStatusToFirstTime(PreferenceManager.getDefaultSharedPreferences(this));
                break;
            case FIRST_TIME_VERSION:
                AppStart.setAppStartStatusToFirstTimeVersion(PreferenceManager.getDefaultSharedPreferences(this));
                break;
        }

        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
            }

            @Override
            public void onActivityStarted(Activity activity) {

            }

            @Override
            public void onActivityResumed(Activity activity) {

            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {

            }
        });
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //new LocaleHelper().updateLocale(this);
    }
}
