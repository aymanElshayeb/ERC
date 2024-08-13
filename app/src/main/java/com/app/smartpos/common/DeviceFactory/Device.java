package com.app.smartpos.common.DeviceFactory;

import android.content.Intent;

public class Device implements DeviceInterface{
    @Override
    public Intent pay(double total) {
        return null;
    }

    @Override
    public String resultHeader() {
        return null;
    }

    @Override
    public String jsonActivityResult() {
        return "";
    }
}
