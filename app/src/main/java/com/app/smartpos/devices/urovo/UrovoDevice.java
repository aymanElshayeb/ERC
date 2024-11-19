package com.app.smartpos.devices.urovo;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;

import com.app.smartpos.common.Consts;
import com.app.smartpos.common.ThirdTag;
import com.app.smartpos.devices.DeviceFactory.Device;
import com.app.smartpos.devices.PrinterHandler;

import java.util.Base64;

public class UrovoDevice implements Device {
    @Override
    public Intent pay(double total) {
        Intent intent = new Intent();
        intent.setPackage(Consts.PACKAGE_UROVO);
        intent.setAction(Consts.CARD_ACTION_UROVO_PURCHASE);
        intent.putExtra(ThirdTag.TRANS_TYPE, "2");
        intent.putExtra(ThirdTag.AMOUNT, String.valueOf(Double.valueOf(total)));
        intent.putExtra(ThirdTag.IS_APP_2_APP, true);
        return intent;
    }

    @Override
    public String resultHeader() {
        return "TransactionResult";
    }

    @Override
    public String jsonActivityResult() {
        return "result";
    }

    @Override
    public String amountString() {
        return "Amount";
    }

    @Override
    public void printReceipt(Bitmap bitmap, PrinterHandler printerHandler) {
        UrovoPrinter urovoPrinter = new UrovoPrinter();
        urovoPrinter.printReceipt(bitmap,printerHandler);
    }

    @Override
    public boolean printZReport(Bitmap bitmap) {
        UrovoPrinter urovoPrinter = new UrovoPrinter();
        return urovoPrinter.printZReport(bitmap);
    }

    @SuppressLint("NewApi")
    @Override
    public String zatcaQrCodeGeneration(byte[] byteArray) {
        return Base64.getEncoder().encodeToString(byteArray);
    }

    @Override
    public String getPrintLine() {
        return "------------------------------------------";
    }
}
