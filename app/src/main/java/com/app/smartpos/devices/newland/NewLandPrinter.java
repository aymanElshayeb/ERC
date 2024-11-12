package com.app.smartpos.devices.newland;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;

import com.app.smartpos.common.Utils;
import com.app.smartpos.database.DatabaseAccess;
import com.app.smartpos.utils.BaseActivity;
import com.app.smartpos.utils.printing.PrintingHelper;
import com.app.smartpos.utils.qrandbrcodegeneration.BarcodeEncoder;
import com.app.smartpos.utils.qrandbrcodegeneration.ZatcaQRCodeGeneration;
import com.google.zxing.WriterException;
import com.newland.sdk.me.module.printer.ErrorCode;
import com.newland.sdk.me.module.printer.ModuleManage;
import com.newland.sdk.me.module.printer.PrintListener;
import com.newland.sdk.me.module.printer.PrinterModule;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NewLandPrinter extends BaseActivity {
    String name, price, qty;
    double productTotalPrice;
    DecimalFormat f;
    List<HashMap<String, String>> orderDetailsList;
    HashMap<String, String> orderList;
    HashMap<String, String> configuration;
    String merchantTaxNumber, merchantId, productCode;
    PrinterModule mPrinterModule;
    String currency = "";
    StringBuffer printDara;


    public NewLandPrinter() {
        ModuleManage.getInstance().init();
        mPrinterModule = ModuleManage.getInstance().getPrinterModule();
    }

    public Bitmap loadBitmapFromView(Bitmap logo, Bitmap bitmap) {
        Bitmap.Config conf = Bitmap.Config.ARGB_4444; // see other conf types
        Bitmap bmp = Bitmap.createBitmap(293, 200, conf); // this creates a MUTABLE bitmap
        Canvas canvas = new Canvas(bmp);
        //canvas.drawBi
        canvas.drawBitmap(logo, 0, 0, new Paint());
        canvas.drawBitmap(bitmap, 0, logo.getHeight() + 40, new Paint());
        return bmp;
    }

    private void printZatcaQrCode(DatabaseAccess databaseAccess) {
        ZatcaQRCodeGeneration zatcaQRCodeGeneration = new ZatcaQRCodeGeneration();
        String data = zatcaQRCodeGeneration.getQrCodeString(orderList, databaseAccess, orderDetailsList, configuration);
        printDara.append("!QRCODE 200 0 3\n*QRCODE c " + data + "\n");
        printDara.append("*feedline 16\n");
    }

    private void printTotalIncludingTax(double priceAfterTax) {

        String text = "Total:";
        String printString = String.valueOf(priceAfterTax);
        printDara.append("!NLFONT 15 15 3\n*TEXT l " + text + "\n!NLFONT 15 15 3\n*text r " + currency + printString + "\n");
        //printDara.append("!NLFONT 15 15 3\n*text l "+printString+"\n");
        printDara.append("!NLFONT 15 15 3\n*text l ----------------------------------------\n");

    }

    private void printTax(String tax) {

        String text = "Total Tax:";
        String value = currency + tax;
        printDara.append("!NLFONT 15 15 3\n*TEXT l " + text + "\n!NLFONT 15 15 3\n*text r " + value + "\n");
        printDara.append("!NLFONT 15 15 3\n*text l ----------------------------------------\n");

    }

    private void printDiscount(String discount) {
        String text = "Discount:";
        String value = currency + discount;
        printDara.append("!NLFONT 15 15 3\n*TEXT l " + text + "\n!NLFONT 15 15 3\n*text r " + value + "\n");
//        printDara.append("!NLFONT 15 15 3\n*text l "+printString+"\n");
    }

    private void printTotalExcludingTax(double priceBeforeTax) {

        String text = "Sub Total:";
        String value = currency + priceBeforeTax;
        printDara.append("!NLFONT 15 15 3\n*TEXT l " + text + "\n!NLFONT 15 15 3\n*text r " + value + "\n");

    }

    private void printProducts(List<HashMap<String, String>> orderDetailsList) {
        printDara.append("!NLFONT 15 15 3\n*text l ----------------------------------------\n");
        printDara.append("!NLFONT 15 15 3\n*TEXT l Description \n!NLFONT 15 15 3\n*text r Price \n\n");
        for (int i = 0; i < orderDetailsList.size(); i++) {
            productCode = orderDetailsList.get(i).get("product_uuid");
            name = orderDetailsList.get(i).get("product_name_en");
            price = orderDetailsList.get(i).get("product_price");
            qty = orderDetailsList.get(i).get("product_qty");
            productTotalPrice = Double.parseDouble(price) * Integer.parseInt(qty);

            printDara.append("!NLFONT 15 15 3\n*TEXT l " + name + " \n!NLFONT 15 15 3\n*text r " + currency + price + "\n");
            printDara.append("!NLFONT 15 15 3\n*text l (" + qty + "*" + currency + price + ") \n");
            printDara.append("!NLFONT 15 15 3\n*text l ----------------------------------------\n");
        }
    }

    private void printInvoiceBarcode(String invoiceId) throws WriterException {
        BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
        printDara.append("!BARCODE 8 80 1 3\n*BARCODE c " + invoiceId + "\n");
        //mPrintManager.addBitmap(barcodeEncoder.encodeQrOrBc(invoiceId,BarcodeFormat.CODE_128,400,100),0);
    }

    private void printReceiptNo(String invoiceId) {
        String printString = "Receipt No " + invoiceId + "\n";
        printDara.append("!NLFONT 15 15 3\n*text l " + printString + "\n");
    }

    private void printMerchantTaxNumber(String merchantTaxNumber) {
        String text = "Merchant tax number:";
        String printString = text + " " + merchantTaxNumber;
        printDara.append("!NLFONT 15 15 3\n*text c " + printString + "\n");
    }

    private Bitmap printMerchantId(String merchantId) {
//        String text = "Merchant ID:";
//        String printString = text +" "+merchantId;
//        printDara.append("!NLFONT 15 15 3\n*text c " + printString + "\n");

        ArrayList<Bitmap> bitmaps = new ArrayList<>();
        bitmaps.add(PrintingHelper.createBitmapFromText(merchantId));
        bitmaps.add(PrintingHelper.createBitmapFromText("الرقم الضريبى :"));
        return PrintingHelper.combineMultipleBitmapsHorizontally(bitmaps, 45);

    }


}
