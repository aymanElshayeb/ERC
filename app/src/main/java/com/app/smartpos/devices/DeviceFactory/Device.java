package com.app.smartpos.devices.DeviceFactory;

import android.content.Intent;
import android.graphics.Bitmap;

public interface Device {
    Intent pay(double total);

    String resultHeader();

    String jsonActivityResult();

    String amountString();

    boolean printReceipt(Bitmap bitmap);

    boolean printZReport(Bitmap bitmap);

    String zatcaQrCodeGeneration(byte[] byteArray);

    String getPrintLine();
}
