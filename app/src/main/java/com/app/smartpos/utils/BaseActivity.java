package com.app.smartpos.utils;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

import static android.content.pm.PackageManager.GET_META_DATA;
import static com.app.smartpos.utils.LocaleManager.changeLang;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        LocaleManager.onCreate(this);
        changeLang();
        super.onCreate(savedInstanceState);
        resetTitles();
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


}
