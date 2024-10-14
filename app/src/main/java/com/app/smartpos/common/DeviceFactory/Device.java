package com.app.smartpos.common.DeviceFactory;

import android.content.Intent;
import android.graphics.Bitmap;

import com.app.smartpos.settings.end_shift.EndShiftModel;

public interface Device{
    Intent pay(double total);
    String resultHeader();
    String jsonActivityResult();
    String amountString();
    boolean printReceipt(Bitmap bitmap);
    boolean printZReport(Bitmap bitmap);
    String zatcaQrCodeGeneration(byte[] byteArray);
    String getPrintLine();
}
