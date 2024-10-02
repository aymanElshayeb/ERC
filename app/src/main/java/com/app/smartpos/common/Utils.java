package com.app.smartpos.common;

import android.app.Activity;
import android.provider.Settings;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class Utils {

    public static String trimLongDouble(double value){
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
        String result=value == 0 ? "0.00" : f.format(value);
        if(result.equals(".00") || result.equals("-.00") || result.equals("0.00") || result.equals("-0.00")){
            result = "0.00";
        }
        if(result.startsWith(".")){
            result="0"+result;
        }
        return result;
    }

    public static String trimLongDouble(String value){
        double doubleValue=Double.parseDouble(value);
        String pattern = "#.00"; //your pattern as per need
        Locale locale = new Locale("en", "US");
        DecimalFormat f = (DecimalFormat) NumberFormat.getNumberInstance(locale);
        f.setRoundingMode(RoundingMode.HALF_UP);
        f.applyPattern(pattern);
        String result=value.equals("0") ? "0.00" : f.format(doubleValue);
        if(result.equals(".00") || result.equals("-.00") || result.equals("0.00") || result.equals("-0.00")){
            result = "0.00";
        }
        if(result.startsWith(".")){
            result="0"+result;
        }
        return result;
    }

    public static String getDeviceId(Activity activity){
        return Settings.Secure.getString(activity.getContentResolver(),
                Settings.Secure.ANDROID_ID);
    }
}
