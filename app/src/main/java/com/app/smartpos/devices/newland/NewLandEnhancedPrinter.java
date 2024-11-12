package com.app.smartpos.devices.newland;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.app.smartpos.Constant;
import com.app.smartpos.R;
import com.app.smartpos.common.Utils;
import com.app.smartpos.devices.urovo.UrovoPrinterStatus;
import com.app.smartpos.orders.PrinterModel;
import com.app.smartpos.utils.BaseActivity;
import com.app.smartpos.utils.MultiLanguageApp;
import com.app.smartpos.utils.qrandbrcodegeneration.ZatcaQRCodeGenerationService;
import com.newland.sdk.me.module.printer.ErrorCode;
import com.newland.sdk.me.module.printer.ModuleManage;
import com.newland.sdk.me.module.printer.PrintListener;
import com.newland.sdk.me.module.printer.PrinterModule;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class NewLandEnhancedPrinter extends BaseActivity {
    String name, price, qty;
    double productTotalPrice;
    List<HashMap<String, String>> orderDetailsList;
    HashMap<String, String> orderList;
    HashMap<String, String> configuration;
    String merchantTaxNumber, merchantId, productCode;
    PrinterModule mPrintManager = null;
    SimpleDateFormat sdf1 = new SimpleDateFormat(Constant.REPORT_DATETIME_FORMAT);
    ZatcaQRCodeGenerationService zatcaQRCodeGenerationService = new ZatcaQRCodeGenerationService();
    List<PrinterModel> bitmaps = new LinkedList<>();
    int totalHeight = 0;
    int width = 0;
    String line = "--------------------------------------------";
    //DateTimeFormatter formatter = DateTimeFormatter.ofPattern("a");
    MultiLanguageApp activity;

    public NewLandEnhancedPrinter() {
        ModuleManage.getInstance().init();
        mPrintManager = ModuleManage.getInstance().getPrinterModule();
        this.activity = MultiLanguageApp.getApp();
    }


    public Boolean printReceipt(Bitmap bitmap) {
        Map<String, Bitmap> bitmapResult = new HashMap<>();
        String bitmapName1 = "logo";
        bitmapResult.put(bitmapName1, bitmap);
        String printDara = "*image c " + bitmap.getWidth() + "*" + (bitmap.getHeight() + 20) + " path:" + bitmapName1 + "\n" +
                "!hz s\n!asc s\n";
        final boolean[] error = {false};
        mPrintManager.print(printDara, bitmapResult, new PrintListener() {
            @Override
            public void onSuccess() {
                Utils.addLog("datadata", "success");
                error[0] =false;
            }

            @Override
            public void onError(ErrorCode errorCode, String s) {
                NewLandPrinterStatus printerStatus = NewLandPrinterStatus.getStatus(s);
                assert printerStatus != null;
                handlePrintStatus(printerStatus.getKey());
                Utils.addLog("datadata_error", "error " + errorCode + " " + s);
                error[0] =true;
            }
        });
        return error[0];
    }

    private void handlePrintStatus(String status) {
        if (status.equalsIgnoreCase(NewLandPrinterStatus.OUT_OF_PAPER.getKey()))
            status = MultiLanguageApp.getApp().getString(R.string.printer_out_of_paper);
        else if (status.equalsIgnoreCase(NewLandPrinterStatus.OVER_HEAT.getKey()))
            status = MultiLanguageApp.getApp().getString(R.string.printer_over_heat);
        else if (status.equalsIgnoreCase(NewLandPrinterStatus.BUSY.getKey()))
            status = MultiLanguageApp.getApp().getString(R.string.printer_busy);
        else if (status.equalsIgnoreCase(NewLandPrinterStatus.UNDER_VOLTAGE.getKey()))
            status = MultiLanguageApp.getApp().getString(R.string.device_under_voltage);
        else
            status = MultiLanguageApp.getApp().getString(R.string.printer_error);
        String finalStatus = status;
        new Handler(Looper.getMainLooper()).post(() ->Toast.makeText(MultiLanguageApp.getApp(), finalStatus, Toast.LENGTH_SHORT).show());
    }

    @SuppressLint("NewApi")
    public boolean printZReport(Bitmap bitmap) {
        try {

            Map<String, Bitmap> bitmapResult = new HashMap<>();
            String bitmapName1 = "logo";
            bitmapResult.put(bitmapName1, bitmap);

            String printDara = "*image c " + bitmap.getWidth() + "*" + (bitmap.getHeight() + 20) + " path:" + bitmapName1 + "\n" +
                    "!hz s\n!asc s\n";
            mPrintManager.print(printDara, bitmapResult, new PrintListener() {
                @Override
                public void onSuccess() {
                    Utils.addLog("datadata", "success");
                }

                @Override
                public void onError(ErrorCode errorCode, String s) {
                    Utils.addLog("datadata_error", "error " + errorCode + " " + s);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }
}
