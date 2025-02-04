package com.app.smartpos.devices.urovo;

import static com.app.smartpos.common.CrashReport.CustomExceptionHandler.addToDatabase;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.widget.Toast;

import com.app.smartpos.Constant;
import com.app.smartpos.R;
import com.app.smartpos.database.DatabaseAccess;
import com.app.smartpos.devices.PrinterHandler;
import com.app.smartpos.utils.BaseActivity;
import com.app.smartpos.utils.MultiLanguageApp;
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
    PrinterProviderImpl mPrintManager = null;
    SimpleDateFormat sdf1 = new SimpleDateFormat(Constant.REPORT_DATETIME_FORMAT);
    ZatcaQRCodeGenerationService zatcaQRCodeGenerationService = new ZatcaQRCodeGenerationService();
    List<Bitmap> bitmaps;
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("a");


    public UrovoPrinter() {
        mPrintManager = PrinterProviderImpl.getInstance(UrovoPrinter.this);
    }

    public void printReceipt(Bitmap bitmap, PrinterHandler handler) {
        DatabaseAccess databaseAccess = DatabaseAccess.getInstance(UrovoPrinter.this);
        databaseAccess.open();
        configuration = databaseAccess.getConfiguration();
        boolean success = false;
        try {
            mPrintManager.initPrint();
            mPrintManager.addBitmap(bitmap, 0);
            int status = mPrintManager.startPrint();
            handler.printStatus(handlePrintStatus(status));
        } catch (Exception e) {
            addToDatabase(e,"error-in-printInvoice-urovoPrinter");
            e.printStackTrace();
        }
        mPrintManager.close();

    }

    private boolean handlePrintStatus(int status) {
        boolean success = false;
        if(status == UrovoPrinterStatus.OK.getKey()) {
            Toast.makeText(MultiLanguageApp.getApp(), MultiLanguageApp.getApp().getString(R.string.print_successful), Toast.LENGTH_SHORT).show();
            success = true;
        }
        else if (status == UrovoPrinterStatus.OUT_OF_PAPER.getKey())
            Toast.makeText(MultiLanguageApp.getApp(), MultiLanguageApp.getApp().getString(R.string.printer_out_of_paper), Toast.LENGTH_SHORT).show();
        else if (status == UrovoPrinterStatus.OVER_HEAT.getKey())
            Toast.makeText(MultiLanguageApp.getApp(), MultiLanguageApp.getApp().getString(R.string.printer_over_heat), Toast.LENGTH_SHORT).show();
        else if (status == UrovoPrinterStatus.BUSY.getKey())
            Toast.makeText(MultiLanguageApp.getApp(), MultiLanguageApp.getApp().getString(R.string.printer_busy), Toast.LENGTH_SHORT).show();
        else if (status == UrovoPrinterStatus.UNDER_VOLTAGE.getKey())
            Toast.makeText(MultiLanguageApp.getApp(), MultiLanguageApp.getApp().getString(R.string.device_under_voltage), Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(MultiLanguageApp.getApp(), MultiLanguageApp.getApp().getString(R.string.printer_error), Toast.LENGTH_SHORT).show();
        return success;
    }

    @SuppressLint("NewApi")
    public boolean printZReport(Bitmap bitmap) {
        try {
            mPrintManager.initPrint();
            mPrintManager.addBitmap(bitmap, 0);
            mPrintManager.startPrint();
        } catch (Exception e) {
            addToDatabase(e,"error-in-printZReport-UrovoPrinter");
            e.printStackTrace();
        }
        mPrintManager.close();
        return true;
    }


}
