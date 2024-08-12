package com.app.smartpos.orders;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

import com.app.smartpos.Constant;
import com.app.smartpos.database.DatabaseAccess;
import com.app.smartpos.utils.BaseActivity;
import com.app.smartpos.utils.printing.PrintingHelper;
import com.app.smartpos.utils.qrandbrcodegeneration.BarcodeEncoder;
import com.app.smartpos.utils.qrandbrcodegeneration.ZatcaQRCodeGeneration;
import com.app.smartpos.utils.qrandbrcodegeneration.ZatcaQRCodeGenerationService;
import com.google.zxing.WriterException;
import com.newland.sdk.me.module.printer.ErrorCode;
import com.newland.sdk.me.module.printer.ModuleManage;
import com.newland.sdk.me.module.printer.PrintListener;
import com.newland.sdk.me.module.printer.PrinterModule;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
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
    String currency="";
    StringBuffer printDara;


    public NewLandPrinter() {
        ModuleManage.getInstance().init();
        mPrinterModule = ModuleManage.getInstance().getPrinterModule();
    }


    public boolean printReceipt(String invoiceId, String orderDate, String orderTime, double priceBeforeTax, double priceAfterTax, String tax, String discount, String currency) {
        DatabaseAccess databaseAccess = DatabaseAccess.getInstance(NewLandPrinter.this);
        databaseAccess.open();
        this.currency=currency;

        configuration = databaseAccess.getConfiguration();
        merchantTaxNumber = configuration.isEmpty() ? "" : configuration.get("merchant_tax_number");
        merchantId = configuration.isEmpty() ? "" : configuration.get("merchant_id");
        databaseAccess.open();
        orderDetailsList = databaseAccess.getOrderDetailsList(invoiceId);
        databaseAccess.open();
        orderList = databaseAccess.getOrderListByOrderId(invoiceId);
        f = new DecimalFormat();
        try {
            printDara = new StringBuffer();
            //String fontsPath = mPrinterModule.setFont(this, "simsun.ttc");
            Map<String, Bitmap> bitmaps = new HashMap<>();
            byte[] decodedString = PrintingHelper.base64ToByteArray(configuration.isEmpty() ? "" : configuration.get("merchant_logo"));
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            String bitmapName1 = "logo";
            bitmaps.put(bitmapName1, decodedByte);

            printDara.append("*image c 370*120 path:" + bitmapName1 + "\n");
            //Set Font Size,small
            printDara.append("!hz s\n!asc s\n");

            printMerchantId(merchantId);
            printMerchantTaxNumber(merchantTaxNumber);
            printDara.append("!NLFONT 15 15 3\n*text c Order Date: " + orderDate + " " + orderTime + "\n");
            printReceiptNo(invoiceId);
            //mPrintManager.addBitmap(PrintingHelper.createBitmapFromText("فاتورة ضريبية مبسطة"), 100);
            printInvoiceBarcode(invoiceId);
            //Todo products ( id, name, price including tax, qty, total including tax
            printProducts(orderDetailsList);
            printTotalExcludingTax(priceBeforeTax);
            printDiscount(discount);
            printTax(tax);
            printTotalIncludingTax(priceAfterTax);
            //Todo total paid
//        mPrintManager.addTextLeft_Center_Right(PrintingHelper.getTextBundle(), f.format(), handleArabicText("إجمالى المدفوع").toString(), "");
            //Todo needs to be paid
//        mPrintManager.addTextLeft_Center_Right(PrintingHelper.getTextBundle(), f.format(), handleArabicText("الصافى").toString(), "");
            //Todo remaining
//        mPrintManager.addTextLeft_Center_Right(PrintingHelper.getTextBundle(), f.format(), handleArabicText("الباقى").toString(), "");
            printZatcaQrCode(databaseAccess);
            mPrinterModule.print(printDara.toString(), bitmaps, new PrintListener() {
                @Override
                public void onSuccess() {
                    Log.i("datadata","done");
                }

                @Override
                public void onError(ErrorCode errorCode, String s) {
                    Log.i("datadata_error","error "+errorCode+" "+s);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;

    }

    private void printZatcaQrCode(DatabaseAccess databaseAccess) {
        ZatcaQRCodeGeneration zatcaQRCodeGeneration = new ZatcaQRCodeGeneration();
        String data = zatcaQRCodeGeneration.getQrCodeString(orderList, databaseAccess, orderDetailsList, configuration);
        printDara.append("!QRCODE 200 0 3\n*QRCODE c "+data+"\n");
        printDara.append("*feedline 16\n");
    }

    private void printTotalIncludingTax(double priceAfterTax) {

        String text = "Total:";
        String printString = priceAfterTax+"";
        printDara.append("!NLFONT 15 15 3\n*TEXT l "+text+"\n!NLFONT 15 15 3\n*text r "+currency+printString+"\n");
        //printDara.append("!NLFONT 15 15 3\n*text l "+printString+"\n");
        printDara.append("!NLFONT 15 15 3\n*text l ----------------------------------------\n");

    }

    private void printTax(String tax) {

        String text = "Total Tax:";
        String value = currency+tax;
        printDara.append("!NLFONT 15 15 3\n*TEXT l "+text+"\n!NLFONT 15 15 3\n*text r "+value+"\n");
        printDara.append("!NLFONT 15 15 3\n*text l ----------------------------------------\n");

    }

    private void printDiscount(String discount) {
        String text = "Discount:";
        String value = currency+discount;
        printDara.append("!NLFONT 15 15 3\n*TEXT l "+text+"\n!NLFONT 15 15 3\n*text r "+value+"\n");
//        printDara.append("!NLFONT 15 15 3\n*text l "+printString+"\n");
    }

    private void printTotalExcludingTax(double priceBeforeTax) {

        String text = "Sub Total:";
        String value = currency+priceBeforeTax;
        printDara.append("!NLFONT 15 15 3\n*TEXT l "+text+"\n!NLFONT 15 15 3\n*text r "+value+"\n");

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

            printDara.append("!NLFONT 15 15 3\n*TEXT l "+name+" \n!NLFONT 15 15 3\n*text r "+currency+price+"\n");
            printDara.append("!NLFONT 15 15 3\n*text l ("+qty+"*"+currency+price+") \n");
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
        String printString = text+" "+merchantTaxNumber;
        printDara.append("!NLFONT 15 15 3\n*text c " + printString + "\n");
    }

    private void printMerchantId(String merchantId) {
        String text = "Merchant ID:";
        String printString = text +" "+merchantId;
        printDara.append("!NLFONT 15 15 3\n*text c " + printString + "\n");


    }


}
