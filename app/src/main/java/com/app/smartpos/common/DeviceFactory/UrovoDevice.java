package com.app.smartpos.common.DeviceFactory;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;

import com.app.smartpos.common.Consts;
import com.app.smartpos.common.ThirdTag;
import com.app.smartpos.orders.UrovoPrinter;
import com.app.smartpos.settings.end_shift.EndShiftModel;

import java.util.Base64;

public class UrovoDevice implements Device{
    @Override
    public Intent pay(long total) {
        Intent intent = new Intent();
        intent.setPackage(Consts.PACKAGE_UROVO);
        intent.setAction(Consts.CARD_ACTION_UROVO_PURCHASE);
        intent.putExtra(ThirdTag.TRANS_TYPE, "2");
        intent.putExtra(ThirdTag.AMOUNT, String.valueOf(Long.valueOf(total)));
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
    public boolean printReceipt(Bitmap bitmap) {
        UrovoPrinter urovoPrinter = new UrovoPrinter();
        return urovoPrinter.printReceipt(bitmap);
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
    public int getSpacingToBeDecreased() {
        return 20;
    }
}
