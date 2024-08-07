package com.app.smartpos.utils;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

import com.app.smartpos.R;

import java.util.Date;

public class SharedPrefUtils {

    public static boolean isFirstOpen(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.app_name),MODE_PRIVATE);
        return sharedPreferences.getBoolean("first_open",true);
    }

    public static void setFirstOpen(Context context,Boolean flag){
        SharedPreferences.Editor editor = context.getSharedPreferences(context.getString(R.string.app_name),MODE_PRIVATE).edit();
        editor.putBoolean("first_open", flag).commit();
    }

    public static long getStartDateTime(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.app_name),MODE_PRIVATE);
        Date date = new Date();
        return sharedPreferences.getLong("start_date_time", date.getTime());
    }

    public static void setStartDateTime(Context context){
        SharedPreferences.Editor editor = context.getSharedPreferences(context.getString(R.string.app_name),MODE_PRIVATE).edit();
        Date date = new Date();
        editor.putLong("start_date_time", date.getTime()).commit();
    }

    public static String getUsername(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.app_name),MODE_PRIVATE);
        return sharedPreferences.getString("user_name","");
    }

    public static String getUserId(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.app_name),MODE_PRIVATE);
        return sharedPreferences.getString("user_id","");
    }

    public static void setUsername(Context context,String userName){
        SharedPreferences.Editor editor = context.getSharedPreferences(context.getString(R.string.app_name),MODE_PRIVATE).edit();
        editor.putString("user_name", userName).commit();
    }

    public static void setUserId(Context context,String userID){
        SharedPreferences.Editor editor = context.getSharedPreferences(context.getString(R.string.app_name),MODE_PRIVATE).edit();
        editor.putString("user_id", userID).commit();
    }



}
