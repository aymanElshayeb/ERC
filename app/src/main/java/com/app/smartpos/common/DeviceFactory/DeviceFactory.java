package com.app.smartpos.common.DeviceFactory;

import com.app.smartpos.common.Consts;

public class DeviceFactory {

    public static Device getDevice(){
        String manufacturer = Consts.MANUFACTURER;
        return manufacturer.equalsIgnoreCase("newland") ? new NewLandDevice() : new UrovoDevice();
    }
}
