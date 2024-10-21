package com.app.smartpos.utils;

import android.app.Application;

import net.sqlcipher.database.SQLiteDatabase;

public class MultiLanguageApp extends Application {

    public static MultiLanguageApp app;
//    @Override
//    protected void attachBaseContext(Context base) {
//        super.attachBaseContext(LocaleManager.setLocale(base));
//    }
//
//    @Override
//    public void onConfigurationChanged(Configuration newConfig) {
//        super.onConfigurationChanged(newConfig);
//        LocaleManager.setLocale(this);
//    }


    @Override
    public void onCreate() {
        super.onCreate();
        SQLiteDatabase.loadLibs(this);
        app = this;
    }

    public static MultiLanguageApp getApp() {
        return app;
    }
}
