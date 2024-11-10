package com.app.smartpos.devices.DeviceFactory;

import com.app.smartpos.common.Consts;
import com.app.smartpos.devices.newland.NewLandDevice;
import com.app.smartpos.devices.urovo.UrovoDevice;

public class DeviceFactory {

    public static Device getDevice() {
        String manufacturer = Consts.MANUFACTURER;
        return manufacturer.equalsIgnoreCase("newland") ? new NewLandDevice() : new UrovoDevice();
    }
}
