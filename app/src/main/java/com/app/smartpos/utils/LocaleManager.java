package com.app.smartpos.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

import java.util.Locale;

public class LocaleManager {

    private static final String SELECTED_LANGUAGE = "Locale.Helper.Selected.Language";

    public static void onCreate(Context context) {

        String lang;
        if (getLanguage(context).isEmpty()) {
            lang = getPersistedData(context, Locale.getDefault().getLanguage());
        } else {
            lang = getLanguage(context);
        }

        setLocale(context, lang);
    }

    public static void onCreate(Context context, String defaultLanguage) {
        String lang = getPersistedData(context, defaultLanguage);
        setLocale(context, lang);
    }

    public static String getLanguage(Context context) {
        return getPersistedData(context, Locale.getDefault().getLanguage());
    }

    public static void setLocale(Context context, String language) {
        persist(context, language);
        updateResources(context, language);
    }

    private static String getPersistedData(Context context, String defaultLanguage) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(SELECTED_LANGUAGE, defaultLanguage);
    }

    private static void persist(Context context, String language) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString(SELECTED_LANGUAGE, language);
        editor.apply();
    }

    private static void updateResources(Context context, String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        Resources res = context.getResources();
        Configuration config = new Configuration(res.getConfiguration());
        if (Build.VERSION.SDK_INT >= 19) {
            config.setLocale(locale);
            context = context.createConfigurationContext(config);

        } else {
            config.locale = locale;
            res.updateConfiguration(config, res.getDisplayMetrics());
        }
        //resources.updateConfiguration(configuration, resources.getDisplayMetrics());


    }

    public static void changeLang() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            Locale locale = new Locale(getLanguage(MultiLanguageApp.getApp()));
            Locale.setDefault(locale);
            Configuration configuration = MultiLanguageApp.getApp().getResources().getConfiguration();
            configuration.setLocale(locale);
            MultiLanguageApp.getApp().getResources().updateConfiguration(configuration, MultiLanguageApp.getApp().getResources().getDisplayMetrics());
        }
    }

    public static void updateLocale(Context context, String lang) {
        persist(context, lang);

        LocaleListCompat localeListCompat = LocaleListCompat.forLanguageTags(lang);
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> AppCompatDelegate.setApplicationLocales(localeListCompat));


    }

    public static void resetApp(Activity activity) {
        PackageManager pm = activity.getPackageManager();
        Intent intent = pm.getLaunchIntentForPackage(activity.getPackageName());
        activity.finishAffinity(); // Finishes all activities.
        activity.startActivity(intent);    // Start the launch activity
        activity.overridePendingTransition(0, 0);
    }
}