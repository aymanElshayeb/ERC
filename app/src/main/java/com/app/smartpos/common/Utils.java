package com.app.smartpos.common;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;

import com.app.smartpos.BuildConfig;
import com.app.smartpos.database.DatabaseAccess;
import com.app.smartpos.utils.MultiLanguageApp;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

public class Utils {

    public static String trimLongDouble(double value) {
//        String stringValue=value+"";
//        String partOne=stringValue.split("\\.")[0];
//        String partTwo=stringValue.split("\\.")[1];
//        if(partTwo.length()<=2){
//            return stringValue;
//        }else{
//            return partOne+"."+partTwo.charAt(0)+partTwo.charAt(1);
//        }
        String pattern = "#.00"; //your pattern as per need
        Locale locale = new Locale("en", "US");
        DecimalFormat f = (DecimalFormat) NumberFormat.getNumberInstance(locale);
        f.setRoundingMode(RoundingMode.HALF_UP);
        f.applyPattern(pattern);
        String result = value == 0 ? "0.00" : f.format(value);
        if (result.equals(".00") || result.equals("-.00") || result.equals("0.00") || result.equals("-0.00")) {
            result = "0.00";
        }
        if (result.startsWith(".")) {
            result = "0" + result;
        }
        if (result.startsWith("-.")) {
            result = result.replace("-", "-0");
        }
        return result;
    }

    public static String trimLongDouble(String value) {
        double doubleValue = Double.parseDouble(value);
        Utils.addLog("datadata_amount", value + " " + doubleValue);
        String pattern = "#.00"; //your pattern as per need
        Locale locale = new Locale("en", "US");
        DecimalFormat f = (DecimalFormat) NumberFormat.getNumberInstance(locale);
        f.setRoundingMode(RoundingMode.HALF_UP);
        f.applyPattern(pattern);
        String result = value.equals("0") ? "0.00" : f.format(doubleValue);
        if (result.equals(".00") || result.equals("-.00") || result.equals("0.00") || result.equals("-0.00")) {
            result = "0.00";
        }
        if (result.startsWith(".")) {
            result = "0" + result;
        }
        if (result.startsWith("-.")) {
            result = result.replace("-", "-0");
        }

        return result;
    }

    public static String getDeviceId(Context context) {
        String id = Build.SERIAL;
        String oldId=Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        return id.equals("unknown") ? oldId : id;
    }

    public static boolean isValidEmail(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }

    public static void addLog(String key, String value) {
        if (Objects.equals(BuildConfig.BUILD_TYPE, "debug")) {
            Log.i(key, value);
        }
    }

    public static void addRequestTracking(String url, String apiName, String header, String body, String response){
        DatabaseAccess databaseAccess=DatabaseAccess.getInstance(MultiLanguageApp.getApp());
        databaseAccess.open();
        HashMap<String,String> configuration = databaseAccess.getConfiguration();
        databaseAccess.open();
        databaseAccess.addRequestTracking(configuration.get("ecr_code"),getDeviceId(MultiLanguageApp.getApp()),
                apiName,url,header,body,response);
    }
}
