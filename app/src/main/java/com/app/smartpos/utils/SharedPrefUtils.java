package com.app.smartpos.utils;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import com.app.smartpos.R;
import com.app.smartpos.common.Keystore.DecryptionHelper;
import com.app.smartpos.common.Keystore.KeyStoreHelper;
import com.app.smartpos.common.Utils;

import java.nio.charset.StandardCharsets;
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

    public static String getAuthorization() {
        SharedPreferences sharedPreferences = MultiLanguageApp.getApp().getSharedPreferences(MultiLanguageApp.getApp().getString(R.string.app_name), MODE_PRIVATE);
        return sharedPreferences.getString("ecr_auth", "");
    }

    public static void setAuthorization(String auth) {
        SharedPreferences.Editor editor = MultiLanguageApp.getApp().getSharedPreferences(MultiLanguageApp.getApp().getString(R.string.app_name), MODE_PRIVATE).edit();
        editor.putString("ecr_auth", auth).commit();
    }

    public static void resetAuthorization() {
        SharedPreferences.Editor editor = MultiLanguageApp.getApp().getSharedPreferences(MultiLanguageApp.getApp().getString(R.string.app_name), MODE_PRIVATE).edit();
        editor.putString("ecr_auth", "").commit();
    }

    public static String getProductLastUpdatedTimeStamp() {
        SharedPreferences sharedPreferences = MultiLanguageApp.getApp().getSharedPreferences(MultiLanguageApp.getApp().getString(R.string.app_name), MODE_PRIVATE);
        String data = sharedPreferences.getString("ecr_last_updated_time_stamp", "");
        return data.isEmpty() ? "" : "?lastUpdateTimestamp=" + data;
    }

    public static void setProductLastUpdatedTimeStamp(String lastUpdatedTimeStamp) {
        SharedPreferences.Editor editor = MultiLanguageApp.getApp().getSharedPreferences(MultiLanguageApp.getApp().getString(R.string.app_name), MODE_PRIVATE).edit();
        editor.putString("ecr_last_updated_time_stamp", lastUpdatedTimeStamp).commit();
    }

    public static String getApiKey() {
        SharedPreferences sharedPreferences = MultiLanguageApp.getApp().getSharedPreferences(MultiLanguageApp.getApp().getString(R.string.app_name), MODE_PRIVATE);
        byte[] encryptedVector = Base64.decode(getApiVector(), Base64.DEFAULT);
        byte[] encryptedKey = Base64.decode(sharedPreferences.getString("ecr_apikey", ""), Base64.DEFAULT);
        String apiKey = "";
        try {
            apiKey = new DecryptionHelper().decrypt(encryptedKey, encryptedVector, new KeyStoreHelper().getOrCreateSecretKey());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return apiKey;
    }

    public static void setApiKey(String apiKey) {
        SharedPreferences.Editor editor = MultiLanguageApp.getApp().getSharedPreferences(MultiLanguageApp.getApp().getString(R.string.app_name), MODE_PRIVATE).edit();
        editor.putString("ecr_apikey", apiKey).commit();
    }

    public static String getApiVector() {
        SharedPreferences sharedPreferences = MultiLanguageApp.getApp().getSharedPreferences(MultiLanguageApp.getApp().getString(R.string.app_name), MODE_PRIVATE);
        return sharedPreferences.getString("ecr_vector", "");
    }

    public static void setApiVector(String vector) {
        SharedPreferences.Editor editor = MultiLanguageApp.getApp().getSharedPreferences(MultiLanguageApp.getApp().getString(R.string.app_name), MODE_PRIVATE).edit();
        editor.putString("ecr_vector", vector).commit();
    }

    public static String getDatabasePassword() {
        SharedPreferences sharedPreferences = MultiLanguageApp.getApp().getSharedPreferences(MultiLanguageApp.getApp().getString(R.string.app_name), MODE_PRIVATE);
        byte[] encryptedVector = Base64.decode(getDatabasePasswordKey(), Base64.DEFAULT);
        byte[] encryptedKey = Base64.decode(sharedPreferences.getString("ecr_database_password", ""), Base64.DEFAULT);
        String databasePassword = "";
        try {
            databasePassword = new DecryptionHelper().decrypt(encryptedKey, encryptedVector, new KeyStoreHelper().getOrCreateSecretKey());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return databasePassword;
    }

    public static void setDatabasePassword(String apiKey) {
        SharedPreferences.Editor editor = MultiLanguageApp.getApp().getSharedPreferences(MultiLanguageApp.getApp().getString(R.string.app_name), MODE_PRIVATE).edit();
        editor.putString("ecr_database_password", apiKey).commit();
    }

    public static String getDatabasePasswordKey() {
        SharedPreferences sharedPreferences = MultiLanguageApp.getApp().getSharedPreferences(MultiLanguageApp.getApp().getString(R.string.app_name), MODE_PRIVATE);
        return sharedPreferences.getString("ecr_database_password_key", "");
    }

    public static void setDatabasePasswordKey(String apiKey) {
        SharedPreferences.Editor editor = MultiLanguageApp.getApp().getSharedPreferences(MultiLanguageApp.getApp().getString(R.string.app_name), MODE_PRIVATE).edit();
        editor.putString("ecr_database_password_key", apiKey).commit();
    }
}
