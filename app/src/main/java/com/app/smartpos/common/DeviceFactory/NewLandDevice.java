package com.app.smartpos.common.DeviceFactory;

import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Base64;

import com.app.smartpos.common.Consts;
import com.app.smartpos.common.ThirdTag;
import com.app.smartpos.orders.NewLandEnhancedPrinter;
import com.app.smartpos.settings.end_shift.EndShiftModel;

public class NewLandDevice implements Device{
    @Override
    public Intent pay(double total) {
        Intent intent = new Intent();
        intent.setPackage(Consts.PACKAGE);
        intent.setAction(Consts.CARD_ACTION);
        intent.putExtra(ThirdTag.CHANNEL_ID, "acquire");
        intent.putExtra(ThirdTag.TRANS_TYPE, 2);
        intent.putExtra(ThirdTag.OUT_ORDERNO, "12345");
        intent.putExtra(ThirdTag.AMOUNT, total*100);
        intent.putExtra(ThirdTag.INSERT_SALE, true);
        intent.putExtra(ThirdTag.RF_FORCE_PSW, true);
        return intent;
    }

    @Override
    public String resultHeader() {
        return "madaTransactionResult";
    }

    @Override
    public String jsonActivityResult() {
        return ThirdTag.JSON_DATA;
    }

    @Override
    public String amountString() {
        return "Amounts";
    }

    @Override
    public boolean printReceipt(Bitmap bitmap) {
        NewLandEnhancedPrinter newLandPrinter = new NewLandEnhancedPrinter();
        return newLandPrinter.printReceipt(bitmap);
    }

    @Override
    public boolean printZReport(Bitmap bitmap) {
        NewLandEnhancedPrinter newLandPrinter = new NewLandEnhancedPrinter();
        return newLandPrinter.printZReport(bitmap);
    }

    @Override
    public String zatcaQrCodeGeneration(byte[] byteArray) {
        return Base64.encodeToString(byteArray,Base64.DEFAULT);
    }

    @Override
    public String getPrintLine() {
        return "-------------------------------------------";
    }
}
