package com.app.smartpos.utils;

import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.MotionEvent;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

import static android.content.pm.PackageManager.GET_META_DATA;
import static com.app.smartpos.utils.LocaleManager.changeLang;

import com.app.smartpos.BuildConfig;
import com.app.smartpos.Registration.Registration;
import com.app.smartpos.SplashActivity;
import com.app.smartpos.auth.AuthActivity;
import com.app.smartpos.common.RootUtil;

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
        if(getCurrentFocus()!=null){
            InputMethodManager imm= (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),0);
        }
        return super.dispatchTouchEvent(ev);
    }

    private ConnectivityManager.NetworkCallback connectivityCallback
            = new ConnectivityManager.NetworkCallback() {
        @Override
        public void onAvailable(Network network) {
            isConnected = true;
            connectionChanged(isConnected);
        }

        @Override
        public void onLost(Network network) {
            isConnected = false;
            activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            isConnected = activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
            connectionChanged(isConnected);
            //Utils.addLog("datadata", isConnected ? "INTERNET CONNECTED" : "INTERNET LOST");
        }

    };

    public void connectionChanged(boolean state){
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
        boolean access=(BuildConfig.BUILD_TYPE.equals("debug") || (Settings.Global.getInt(getContentResolver(),
                Settings.Global.ADB_ENABLED, 0)==0 && !RootUtil.isDeviceRooted()));

        if(this instanceof AuthActivity || this instanceof Registration || this instanceof SplashActivity){

        }else{
            access = access && SharedPrefUtils.getIsLoggedIn(this);
        }
        if(!access){
            finishAffinity();
        }
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
