package com.app.smartpos.common.DeviceFactory;

import android.content.Intent;

import com.app.smartpos.settings.end_shift.EndShiftModel;

public interface Device{
    Intent pay(long total);
    String resultHeader();
    String jsonActivityResult();
    String amountString();
    boolean printReciept(String invoiceId, String orderDate, String orderTime, double priceBeforeTax, double priceAfterTax, String tax, String discount, String currency,String printType);
    boolean printZReport(EndShiftModel endShiftModel);
    String zatcaQrCodeGeneration(byte[] byteArray);
}
