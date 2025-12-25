package com.app.smartpos.utils;

import android.app.Application;
import android.widget.Toast;

import com.aheaditec.talsec_security.security.api.SuspiciousAppInfo;
import com.aheaditec.talsec_security.security.api.Talsec;
import com.aheaditec.talsec_security.security.api.TalsecConfig;
import com.aheaditec.talsec_security.security.api.ThreatListener;

import java.util.List;

public class MultiLanguageApp extends Application implements ThreatListener.ThreatDetected {

    public static MultiLanguageApp app;

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;

        TalsecConfig config = new TalsecConfig.Builder(
                "com.app.smartpos",  // your app package name
                new String[]{"BgfBw1udrMLpBvxucoI4hqRk9Wkp/EpugcM21w/BLVQ="})  // your release cert hash (SHA256 Base64)
                //.watcherMail("your.monitoring@email.com") // alert email
                .prod(true)  // set false for dev mode
                .build();

        // Register listener and start freeRASP
        ThreatListener threatListener = new ThreatListener(this);
        threatListener.registerListener(this);

        Talsec.start(this, config);
    }

    public static MultiLanguageApp getApp() {
        return app;
    }

    @Override
    public void onRootDetected() {
        Toast.makeText(app, "root detected", Toast.LENGTH_SHORT).show();
        System.exit(1);
    }

    @Override
    public void onDebuggerDetected() {
        //Toast.makeText(app, "debug detected", Toast.LENGTH_SHORT).show();
        //System.exit(1);
    }

    @Override
    public void onEmulatorDetected() {
       // System.exit(1);
    }

    @Override
    public void onTamperDetected() {
        //System.exit(1);
    }

    @Override
    public void onUntrustedInstallationSourceDetected() {

    }

    @Override
    public void onHookDetected() {

    }

    @Override
    public void onDeviceBindingDetected() {

    }

    @Override
    public void onObfuscationIssuesDetected() {

    }

    @Override
    public void onMalwareDetected(List<SuspiciousAppInfo> list) {

    }

    @Override
    public void onScreenshotDetected() {

    }

    @Override
    public void onScreenRecordingDetected() {

    }

    @Override
    public void onMultiInstanceDetected() {

    }
}
