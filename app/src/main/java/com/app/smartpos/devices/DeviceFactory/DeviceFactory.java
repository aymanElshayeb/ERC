package com.app.smartpos.devices.DeviceFactory;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import com.app.smartpos.common.Consts;
import com.app.smartpos.devices.newland.NewLandDevice;
import com.app.smartpos.devices.newleapgeneraldevice.NewLeapGeneralDevice;
import com.app.smartpos.devices.urovo.UrovoDevice;
import com.app.smartpos.utils.MultiLanguageApp;

import java.util.List;

public class DeviceFactory {

    public static Device getDevice() {
        Boolean isNewLand = queryForAction(MultiLanguageApp.getApp().getPackageManager(),Consts.CARD_ACTION,Consts.PACKAGE);
        Boolean isUrovo = queryForAction(MultiLanguageApp.getApp().getPackageManager(),Consts.CARD_ACTION_UROVO_PURCHASE,Consts.PACKAGE_UROVO);
        //Boolean isGeneralNeoleap = queryForAction(MultiLanguageApp.getApp().getPackageManager(),Consts.CARD_ACTION_GENERAL_NEWLEAP_PURCHASE);
        if(isNewLand){
            return new NewLandDevice();
        }else if(isUrovo){
            return new UrovoDevice();
        }else {
            return new NewLeapGeneralDevice();
        }
    }

    public static Boolean queryForAction(PackageManager packageManager, String action,String packageName) {
        Intent intent = new Intent(action);
        intent.setPackage(packageName);
        return !packageManager.queryIntentActivities(intent, 0).isEmpty();
    }
}
