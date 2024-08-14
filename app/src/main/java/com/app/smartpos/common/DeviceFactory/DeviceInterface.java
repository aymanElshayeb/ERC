package com.app.smartpos.common.DeviceFactory;

import android.content.Intent;

public interface DeviceInterface {

    Intent pay(long total);

    String resultHeader();
    String jsonActivityResult();
    String amountString();
}
