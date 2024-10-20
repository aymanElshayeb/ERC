package com.app.smartpos.orders;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;

import com.app.smartpos.Constant;
import com.app.smartpos.database.DatabaseAccess;
import com.app.smartpos.utils.BaseActivity;
import com.app.smartpos.utils.qrandbrcodegeneration.ZatcaQRCodeGenerationService;
import com.urovo.sdk.print.PrinterProviderImpl;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;

public class UrovoPrinter extends BaseActivity {
    String name, price, qty;
    double productTotalPrice;
    List<HashMap<String, String>> orderDetailsList;
    HashMap<String, String> orderList;
    HashMap<String, String> configuration;
    String merchantTaxNumber, merchantId, productCode;
    PrinterProviderImpl mPrintManager = null;
    SimpleDateFormat sdf1 = new SimpleDateFormat(Constant.REPORT_DATETIME_FORMAT);
    ZatcaQRCodeGenerationService zatcaQRCodeGenerationService = new ZatcaQRCodeGenerationService();
    List<Bitmap> bitmaps;
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("a");


    public UrovoPrinter() {
        mPrintManager = PrinterProviderImpl.getInstance(UrovoPrinter.this);
    }

    public boolean printReceipt(Bitmap bitmap) {
        DatabaseAccess databaseAccess = DatabaseAccess.getInstance(UrovoPrinter.this);
        databaseAccess.open();
        configuration = databaseAccess.getConfiguration();
        merchantTaxNumber = configuration.isEmpty() ? "" : configuration.get("merchant_tax_number");
        merchantId = configuration.isEmpty() ? "" : configuration.get("merchant_id");
        try {
            mPrintManager.initPrint();
            mPrintManager.addBitmap(bitmap, 0);
            mPrintManager.startPrint();
        } catch (Exception e) {
            e.printStackTrace();
        }
        mPrintManager.close();
        return true;

    }

    @SuppressLint("NewApi")
    public boolean printZReport(Bitmap bitmap) {
        try {
            mPrintManager.initPrint();
            mPrintManager.addBitmap(bitmap, 0);
            mPrintManager.startPrint();
        } catch (Exception e) {
            e.printStackTrace();
        }
        mPrintManager.close();
        return true;
    }


}
