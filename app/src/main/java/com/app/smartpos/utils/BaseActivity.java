package com.app.smartpos.utils;

import static android.content.pm.PackageManager.GET_META_DATA;
import static com.app.smartpos.common.CrashReport.CustomExceptionHandler.addToDatabase;
import static com.app.smartpos.utils.LocaleManager.changeLang;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.MotionEvent;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.app.smartpos.BuildConfig;
import com.app.smartpos.common.RootUtil;
import com.app.smartpos.common.Utils;

import java.util.Locale;

public abstract class BaseActivity extends AppCompatActivity {

    boolean isConnected = false;
    ConnectivityManager connectivityManager;
    NetworkInfo activeNetworkInfo;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        LocaleManager.onCreate(this);
        changeLang();
        super.onCreate(savedInstanceState);
        resetTitles();
        connectivityManager = (ConnectivityManager) getSystemService(
                Context.CONNECTIVITY_SERVICE);
    }


    protected void resetTitles() {
        try {
            ActivityInfo info = getPackageManager().getActivityInfo(getComponentName(), GET_META_DATA);
            if (info.labelRes != 0) {
                setTitle(info.labelRes);
            }
        } catch (PackageManager.NameNotFoundException e) {
            addToDatabase(e,"resetTitles-baseActivity");
            e.printStackTrace();
        }
    }


    //for Android Android N
    @Override
    public void applyOverrideConfiguration(Configuration overrideConfiguration) {
        Locale locale = new Locale(LocaleManager.getLanguage(this));
        Locale.setDefault(locale);
        overrideConfiguration.setLocale(locale);
        super.applyOverrideConfiguration(overrideConfiguration);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
        return super.dispatchTouchEvent(ev);
    }

    private final ConnectivityManager.NetworkCallback connectivityCallback
            = new ConnectivityManager.NetworkCallback() {
        @Override
        public void onAvailable(Network network) {
            isConnected = true;
            Utils.addLog("datadata_error","true");
            connectionChanged(isConnected);
        }

        @Override
        public void onLost(Network network) {
            isConnected = false;
            activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            Utils.addLog("datadata_error","false");
            isConnected = activeNetworkInfo != null && activeNetworkInfo.isAvailable();
            Utils.addLog("datadata_error",isConnected+"");
            connectionChanged(isConnected);

            //Utils.addLog("datadata", isConnected ? "INTERNET CONNECTED" : "INTERNET LOST");
        }

    };

    public void connectionChanged(boolean state) {
    }

    private void checkConnectivity() {
        connectivityManager.registerNetworkCallback(
                new NetworkRequest.Builder()
                        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                        .build(), connectivityCallback);

    }

    @Override
    protected void onResume() {
        super.onResume();
        checkConnectivity();
        boolean access = ((Settings.Global.getInt(getContentResolver(), Settings.Global.ADB_ENABLED, 0) == 0 && !RootUtil.isDeviceRooted()));
//        if (!access) {
//            finishAffinity();
//        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        connectivityManager.unregisterNetworkCallback(connectivityCallback);
    }

    public boolean isConnected() {
        return isConnected;
    }
}
