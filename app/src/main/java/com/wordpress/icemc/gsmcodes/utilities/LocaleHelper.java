package com.wordpress.icemc.gsmcodes.utilities;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;


import com.wordpress.icemc.gsmcodes.application.GSMCodes;

import java.util.Locale;
import java.util.TreeMap;

/**
 * Changes the locale of the app and keeps the changes persistent
 *
 * @author abdulwd and Ice Mc
 */

public class LocaleHelper {

    public void updateLocale(Context context, String localeCode) {
        Locale locale = getLocale(localeCode);
        Locale.setDefault(locale);
        Configuration configuration = new Configuration(GSMCodes.phoneConfiguration);
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.setLocale(locale);
        } else {
            configuration.locale = locale;
        }
        context.getResources().updateConfiguration(configuration, displayMetrics);
        context.getApplicationContext().getResources().updateConfiguration(configuration, displayMetrics);
    }

    public void updateLocale(Context context) {
        String localeCode = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(ApplicationConstants.APP_LANGUAGE, "en");

        updateLocale(context, localeCode);
    }

    public TreeMap<String, String> getEntryListValues() {
        //Holds language as key and language code as value
        TreeMap<String, String> languageList = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (String language : ApplicationConstants.TRANSLATIONS_AVAILABLE) {
            Locale locale = getLocale(language);
            languageList.put(locale.getDisplayName(locale), language);
        }
        return languageList;
    }

    private Locale getLocale(String splitLocaleCode) {
        if (splitLocaleCode.contains("_")) {
            String[] arg = splitLocaleCode.split("_");
            return new Locale(arg[0], arg[1]);
        } else {
            return new Locale(splitLocaleCode);
        }
    }

    public String getLocaleString() {
       return Locale.getDefault().getLanguage();
    }
}
