package com.app.smartpos.devices.DeviceFactory;

import android.content.Intent;
import android.graphics.Bitmap;

import com.app.smartpos.devices.PrinterHandler;

public interface Device {
    Intent pay(double total);

    String resultHeader();

    String jsonActivityResult();

    String amountString();

    void printReceipt(Bitmap bitmap, PrinterHandler printerHandler);

    boolean printZReport(Bitmap bitmap);

    String zatcaQrCodeGeneration(byte[] byteArray);

    String getPrintLine();
}
