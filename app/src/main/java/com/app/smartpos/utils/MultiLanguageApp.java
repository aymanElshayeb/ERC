package com.app.smartpos.utils;

import android.app.Application;

import com.app.smartpos.database.DatabaseOpenHelper;

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

    public static MultiLanguageApp getApp() {
        return app;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if(SharedPrefUtils.isRegistered(this)){
            DatabaseOpenHelper.DATABASE_PASSWORD = SharedPrefUtils.getDatabasePassword();
        }
        app = this;
    }
}
