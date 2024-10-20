package com.app.smartpos.utils;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

import com.app.smartpos.R;

import java.util.Date;

import kotlin.text.Charsets;

public class SharedPrefUtils {

    public static boolean isRegistered(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.app_name), MODE_PRIVATE);
        return sharedPreferences.getBoolean("isRegistered", false);
    }

    public static void setIsRegistered(Context context, Boolean flag) {
        SharedPreferences.Editor editor = context.getSharedPreferences(context.getString(R.string.app_name), MODE_PRIVATE).edit();
        editor.putBoolean("isRegistered", flag).commit();
    }

    public static boolean isDemoPressed(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.app_name), MODE_PRIVATE);
        return sharedPreferences.getBoolean("isDemoPressed", false);
    }

    public static void setDemoPressed(Context context, Boolean flag) {
        SharedPreferences.Editor editor = context.getSharedPreferences(context.getString(R.string.app_name), MODE_PRIVATE).edit();
        editor.putBoolean("isDemoPressed", flag).commit();
    }

    public static long getStartDateTime(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.app_name), MODE_PRIVATE);
        Date date = new Date();
        return sharedPreferences.getLong("start_date_time", date.getTime());
    }

    public static void setStartDateTime(Context context) {
        SharedPreferences.Editor editor = context.getSharedPreferences(context.getString(R.string.app_name), MODE_PRIVATE).edit();
        Date date = new Date();
        editor.putLong("start_date_time", date.getTime()).commit();
    }

    public static String getName(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.app_name), MODE_PRIVATE);
        return sharedPreferences.getString("user_name", "");
    }

    public static String getEmail(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.app_name), MODE_PRIVATE);
        return sharedPreferences.getString("user_email", "");
    }

    public static void setEmail(Context context, String email) {
        SharedPreferences.Editor editor = context.getSharedPreferences(context.getString(R.string.app_name), MODE_PRIVATE).edit();
        editor.putString("user_email", email).commit();
    }

    public static String getUserId(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.app_name), MODE_PRIVATE);
        return sharedPreferences.getString("user_id", "");
    }

    public static void setName(Context context, String userName) {
        SharedPreferences.Editor editor = context.getSharedPreferences(context.getString(R.string.app_name), MODE_PRIVATE).edit();
        editor.putString("user_name", userName).commit();
    }

    public static byte[] getAuthData(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.app_name), MODE_PRIVATE);
        return sharedPreferences.getString("auth_data", "").getBytes(Charsets.ISO_8859_1);
    }

    public static void setAuthData(Context context, byte[] data) {
        SharedPreferences.Editor editor = context.getSharedPreferences(context.getString(R.string.app_name), MODE_PRIVATE).edit();
        editor.putString("auth_data", new String(data, Charsets.ISO_8859_1)).commit();
    }

    public static void setUserId(Context context, String userID) {
        SharedPreferences.Editor editor = context.getSharedPreferences(context.getString(R.string.app_name), MODE_PRIVATE).edit();
        editor.putString("user_id", userID).commit();
    }

    public static void setMerchantId(Context context, String tenantId) {
        SharedPreferences.Editor editor = context.getSharedPreferences(context.getString(R.string.app_name), MODE_PRIVATE).edit();
        editor.putString("merchant_id", tenantId).commit();
    }

    public static String getMerchantId(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.app_name), MODE_PRIVATE);
        return sharedPreferences.getString("merchant_id", "");
    }

    public static void setEcrCode(Context context, String ecrCode) {
        SharedPreferences.Editor editor = context.getSharedPreferences(context.getString(R.string.app_name), MODE_PRIVATE).edit();
        editor.putString("ecr_code", ecrCode).commit();
    }

    public static String getEcrCode(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.app_name), MODE_PRIVATE);
        return sharedPreferences.getString("ecr_code", "");
    }

    public static void setMobileNumber(Context context, String ecrCode) {
        SharedPreferences.Editor editor = context.getSharedPreferences(context.getString(R.string.app_name), MODE_PRIVATE).edit();
        editor.putString("mobile", ecrCode).commit();
    }

    public static String getMobileNumber(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.app_name), MODE_PRIVATE);
        return sharedPreferences.getString("mobile", "");
    }


    public static void setUserName(Context context, String ecrCode) {
        SharedPreferences.Editor editor = context.getSharedPreferences(context.getString(R.string.app_name), MODE_PRIVATE).edit();
        editor.putString("username", ecrCode).commit();
    }

    public static String getUserName(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.app_name), MODE_PRIVATE);
        return sharedPreferences.getString("username", "");
    }

    public static void setIsLoggedIn(Context context, Boolean flag) {
        SharedPreferences.Editor editor = context.getSharedPreferences(context.getString(R.string.app_name), MODE_PRIVATE).edit();
        editor.putBoolean("loggedIn", flag).commit();
    }

    public static boolean getIsLoggedIn(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.app_name), MODE_PRIVATE);
        return sharedPreferences.getBoolean("loggedIn", false);
    }


    public static void setAuthorization(String auth) {
        SharedPreferences.Editor editor = MultiLanguageApp.getApp().getSharedPreferences(MultiLanguageApp.getApp().getString(R.string.app_name), MODE_PRIVATE).edit();
        editor.putString("ecr_auth", auth).commit();
    }

    public static String getAuthorization() {
        SharedPreferences sharedPreferences = MultiLanguageApp.getApp().getSharedPreferences(MultiLanguageApp.getApp().getString(R.string.app_name), MODE_PRIVATE);
        return sharedPreferences.getString("ecr_auth", "");
    }

    public static void resetAuthorization() {
        SharedPreferences.Editor editor = MultiLanguageApp.getApp().getSharedPreferences(MultiLanguageApp.getApp().getString(R.string.app_name), MODE_PRIVATE).edit();
        editor.putString("ecr_auth", "").commit();
    }

    public static void setProductLastUpdatedTimeStamp(String lastUpdatedTimeStamp) {
        SharedPreferences.Editor editor = MultiLanguageApp.getApp().getSharedPreferences(MultiLanguageApp.getApp().getString(R.string.app_name), MODE_PRIVATE).edit();
        editor.putString("last_updated_time_stamp", lastUpdatedTimeStamp).commit();
    }

    public static String getProductLastUpdatedTimeStamp() {
        SharedPreferences sharedPreferences = MultiLanguageApp.getApp().getSharedPreferences(MultiLanguageApp.getApp().getString(R.string.app_name), MODE_PRIVATE);
        String data = sharedPreferences.getString("last_updated_time_stamp", "");
        return data.isEmpty() ? "" : "?lastUpdateTimestamp=" + data;
    }
}
