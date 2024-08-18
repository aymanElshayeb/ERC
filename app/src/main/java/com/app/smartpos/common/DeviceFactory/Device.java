package com.app.smartpos.common.DeviceFactory;

import android.content.Intent;

public interface Device{
    Intent pay(long total);
    String resultHeader();
    String jsonActivityResult();
    String amountString();
    boolean print(String invoiceId,String orderDate, String orderTime, double priceBeforeTax, double priceAfterTax, String tax, String discount, String currency);
    String zatcaQrCodeGeneration(byte[] byteArray);
}
