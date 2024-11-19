package com.app.smartpos.devices.newleapgeneraldevice;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;

import com.app.smartpos.common.Consts;
import com.app.smartpos.common.ThirdTag;
import com.app.smartpos.devices.DeviceFactory.Device;
import com.app.smartpos.devices.PrinterHandler;
import com.app.smartpos.devices.newland.NewLandEnhancedPrinter;
import com.app.smartpos.devices.urovo.UrovoPrinter;

import java.util.Base64;

public class NewLeapGeneralDevice implements Device {
    @Override
    public Intent pay(double total) {
        Intent intent = new Intent();
        intent.setAction(Consts.CARD_ACTION_GENERAL_NEWLEAP_PURCHASE);
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
        NewLandEnhancedPrinter newLandEnhancedPrinter = new NewLandEnhancedPrinter();
        newLandEnhancedPrinter.printReceipt(bitmap,printerHandler);
    }

    @Override
    public boolean printZReport(Bitmap bitmap) {
        NewLandEnhancedPrinter newLandEnhancedPrinter = new NewLandEnhancedPrinter();
        return newLandEnhancedPrinter.printZReport(bitmap);
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
