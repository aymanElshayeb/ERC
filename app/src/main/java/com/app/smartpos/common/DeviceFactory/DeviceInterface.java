package com.app.smartpos.common.DeviceFactory;

import android.content.Intent;

public interface DeviceInterface {

    Intent pay(double total);

    String resultHeader();
    String jsonActivityResult();
}
