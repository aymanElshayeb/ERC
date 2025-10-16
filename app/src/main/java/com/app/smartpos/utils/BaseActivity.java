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
import android.provider.Settings;
import android.view.MotionEvent;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.app.smartpos.common.RootUtil;
import com.app.smartpos.common.Utils;
import com.scottyab.rootbeer.RootBeer;

import java.util.LinkedList;
import java.util.Locale;

public abstract class BaseActivity extends AppCompatActivity {

    boolean isConnected = false;
    ConnectivityManager connectivityManager;
    NetworkInfo activeNetworkInfo;
    LinkedList<Network>networks = new LinkedList<>();
    private final ConnectivityManager.NetworkCallback connectivityCallback
            = new ConnectivityManager.NetworkCallback() {
        @Override
        public void onAvailable(Network network) {
            isConnected = true;
            Utils.addLog("datadata_network", "onAvailable "+network.toString());
            networks.add(network);
            connectionChanged(true);
        }

        @Override
        public void onLost(Network network) {

            activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            LinkedList<Network>tempNetworks = new LinkedList<>();
            tempNetworks.addAll(networks);
            for(int i=0;i<tempNetworks.size();i++){

                if(tempNetworks.get(i).equals(network)) {
                    Utils.addLog("datadata_network_remove", networks.remove(network) + "");
                }
            }
            tempNetworks.clear();
            Utils.addLog("datadata_network", "onlost "+network.toString());

            isConnected = !networks.isEmpty();
            connectionChanged(isConnected);
            Utils.addLog("datadata_network", isConnected+" "+networks.size());
            //Utils.addLog("datadata", isConnected ? "INTERNET CONNECTED" : "INTERNET LOST");
        }

    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        LocaleManager.onCreate(this);
        changeLang();
        super.onCreate(savedInstanceState);
        resetTitles();
        connectivityManager = (ConnectivityManager) getSystemService(
                Context.CONNECTIVITY_SERVICE);
        checkConnectivity();
    }

    protected void resetTitles() {
        try {
            ActivityInfo info = getPackageManager().getActivityInfo(getComponentName(), GET_META_DATA);
            if (info.labelRes != 0) {
                setTitle(info.labelRes);
            }
        } catch (PackageManager.NameNotFoundException e) {
            addToDatabase(e, "resetTitles-baseActivity");
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
        RootBeer rootBeer = new RootBeer(this);
        boolean access = (Settings.Global.getInt(getContentResolver(), Settings.Global.ADB_ENABLED, 0) == 0 && !RootUtil.isDeviceRooted() && !rootBeer.isRooted() && !rootBeer.isRootedWithBusyBoxCheck());
       if (!access) {
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
