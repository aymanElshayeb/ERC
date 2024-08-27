package com.app.smartpos.orders;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

import com.app.smartpos.Constant;
import com.app.smartpos.database.DatabaseAccess;
import com.app.smartpos.utils.BaseActivity;
import com.app.smartpos.utils.printing.PrintingHelper;
import com.app.smartpos.utils.qrandbrcodegeneration.BarcodeEncoder;
import com.app.smartpos.utils.qrandbrcodegeneration.ZatcaQRCodeGeneration;
import com.app.smartpos.utils.qrandbrcodegeneration.ZatcaQRCodeGenerationService;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.newland.sdk.me.module.printer.ErrorCode;
import com.newland.sdk.me.module.printer.ModuleManage;
import com.newland.sdk.me.module.printer.PrintListener;
import com.newland.sdk.me.module.printer.PrinterModule;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class OrderBitmap extends BaseActivity {
    String name, price, qty;
    double productTotalPrice;
    DecimalFormat f;
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
    String line="--------------------------------------------";

    public OrderBitmap() {

    }



    public Bitmap orderBitmap(String invoiceId, String orderDate, String orderTime, double priceBeforeTax, double priceAfterTax, double tax, String discount, String currency) {
        DatabaseAccess databaseAccess = DatabaseAccess.getInstance(OrderBitmap.this);
        databaseAccess.open();
        configuration = databaseAccess.getConfiguration();
        merchantTaxNumber = configuration.isEmpty() ? "" : configuration.get("merchant_tax_number");
        merchantId = configuration.isEmpty() ? "" : configuration.get("merchant_id");
        databaseAccess.open();
        orderDetailsList = databaseAccess.getOrderDetailsList(invoiceId);
        databaseAccess.open();
        orderList = databaseAccess.getOrderListByOrderId(invoiceId);
        f = new DecimalFormat("#.00");
        try {
            byte[] decodedString = PrintingHelper.base64ToByteArray(configuration.isEmpty() ? "" : configuration.get("merchant_logo"));
            Bitmap logo = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            bitmaps.add(new PrinterModel(0,logo));
            printMerchantId(merchantId);
            printMerchantTaxNumber(merchantTaxNumber);
            bitmaps.add(new PrinterModel(0,PrintingHelper.createBitmapFromText(orderDate + "      " + orderTime)));
            //mPrintManager.addTextLeft_Right(PrintingHelper.getTextBundle(Constant.CENTER_ALIGNED,true), orderDate, orderTime);
            printReceiptNo(invoiceId);
            bitmaps.add(new PrinterModel(0,PrintingHelper.createBitmapFromText("فاتورة ضريبية مبسطة")));
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

        } catch (Exception e) {
            e.printStackTrace();
        }
        return creatGeneralBitmap();

    }

    public Bitmap creatGeneralBitmap() {
        totalHeight = 0;
        int size = bitmaps.size();
        for (int i = 0; i < size; i++) {
            Bitmap bitmap = bitmaps.get(i).getBitmap();
            totalHeight += bitmap.getHeight();
            if (bitmap.getWidth() > width) {
                width = bitmap.getWidth();
            }
        }
        Log.i("datadata", totalHeight + "");
        Bitmap.Config conf = Bitmap.Config.ARGB_4444; // see other conf types
        Bitmap bmp = Bitmap.createBitmap(width, totalHeight, conf); // this creates a MUTABLE bitmap
        Canvas canvas = new Canvas(bmp);
        canvas.drawColor(Color.WHITE);
        int lastY = 0;
        for (int i = 0; i < size; i++) {
            Bitmap bitmap = bitmaps.get(i).getBitmap();
            int startX=0;
            if(bitmaps.get(i).getSide()==0){
                startX=width/2 - bitmap.getWidth()/2;
            }else if(bitmaps.get(i).getSide()==1){
                startX = width-bitmap.getWidth();
            }
            canvas.drawBitmap(bitmaps.get(i).getBitmap(), startX, lastY, new Paint());
            lastY += bitmap.getHeight();
        }
        return bmp;
    }

    private void printZatcaQrCode(DatabaseAccess databaseAccess) {
        ZatcaQRCodeGeneration zatcaQRCodeGeneration = new ZatcaQRCodeGeneration();
        bitmaps.add(new PrinterModel(0,zatcaQRCodeGeneration.getQrCodeBitmap(orderList, databaseAccess, orderDetailsList, configuration)));
    }

    private void printTotalIncludingTax(double priceAfterTax) {
        List<Bitmap> newBitmaps = new ArrayList<>();
        newBitmaps.add(PrintingHelper.createBitmapFromText(f.format(priceAfterTax)));
        newBitmaps.add(PrintingHelper.createBitmapFromText("الإجمالى النهائى"));
        bitmaps.add(new PrinterModel(-1,PrintingHelper.combineMultipleBitmapsHorizontally(newBitmaps, 70)));
        bitmaps.add(new PrinterModel(-1,PrintingHelper.createBitmapFromText(line)));
    }

    private void printTax(double tax) {
        List<Bitmap> newBitmaps = new ArrayList<>();
        newBitmaps.add(PrintingHelper.createBitmapFromText(f.format(tax)));
        newBitmaps.add(PrintingHelper.createBitmapFromText("ضريبة القيمة المضافة"));
        bitmaps.add(new PrinterModel(-1,PrintingHelper.combineMultipleBitmapsHorizontally(newBitmaps, 40)));
        bitmaps.add(new PrinterModel(-1,PrintingHelper.createBitmapFromText(line)));
    }

    private void printDiscount(String discount) {
        List<Bitmap> newBitmaps = new ArrayList<>();
        newBitmaps.add(PrintingHelper.createBitmapFromText(Double.parseDouble(discount) != 0 ? f.format(discount) : String.valueOf(0)));
        newBitmaps.add(PrintingHelper.createBitmapFromText("الخصم"));
        bitmaps.add(new PrinterModel(-1,PrintingHelper.combineMultipleBitmapsHorizontally(newBitmaps, 70)));
        bitmaps.add(new PrinterModel(-1,PrintingHelper.createBitmapFromText(line)));
    }

    private void printTotalExcludingTax(double priceBeforeTax) {
        List<Bitmap> newBitmaps = new ArrayList<>();
        newBitmaps.add(PrintingHelper.createBitmapFromText(f.format(priceBeforeTax)));
        newBitmaps.add(PrintingHelper.createBitmapFromText("الإجمالى قبل الضريبة"));
        bitmaps.add(new PrinterModel(-1,PrintingHelper.combineMultipleBitmapsHorizontally(newBitmaps, 40)));
        bitmaps.add(new PrinterModel(-1,PrintingHelper.createBitmapFromText(line)));
    }

    private void printProducts(List<HashMap<String, String>> orderDetailsList) {
        bitmaps.add(new PrinterModel(-1,PrintingHelper.createBitmapFromText(line)));
        List<Bitmap> newBitmaps = new ArrayList<>();
        newBitmaps.add(PrintingHelper.createBitmapFromText("الإجمالى"));
        newBitmaps.add(PrintingHelper.createBitmapFromText("السعر"));
        newBitmaps.add(PrintingHelper.createBitmapFromText("الكمية"));
        newBitmaps.add(PrintingHelper.createBitmapFromText("    "));
        bitmaps.add(new PrinterModel(1,PrintingHelper.combineMultipleBitmapsHorizontally(newBitmaps, 40)));
        bitmaps.add(new PrinterModel(-1,PrintingHelper.createBitmapFromText(line)));

        for (int i = 0; i < orderDetailsList.size(); i++) {
            productCode = orderDetailsList.get(i).get("product_uuid");
            name = orderDetailsList.get(i).get("product_name_ar");
            price = orderDetailsList.get(i).get("product_price");
            qty = orderDetailsList.get(i).get("product_qty");
            productTotalPrice = Double.parseDouble(price) * Integer.parseInt(qty);
            List<Bitmap> ProductBitmap = new ArrayList<>();
            ProductBitmap.add(PrintingHelper.createBitmapFromText(f.format(productTotalPrice)));
            ProductBitmap.add(PrintingHelper.createBitmapFromText(f.format(Double.parseDouble(price))));
            ProductBitmap.add(PrintingHelper.createBitmapFromText(qty));
            ProductBitmap.add(PrintingHelper.createBitmapFromText("    "));
            bitmaps.add(new PrinterModel(1,PrintingHelper.combineMultipleBitmapsHorizontally(ProductBitmap, 50)));
            bitmaps.add(new PrinterModel(1,PrintingHelper.createBitmapFromText(name)));
            bitmaps.add(new PrinterModel(-1,PrintingHelper.createBitmapFromText(line)));
        }
    }

    private void printInvoiceBarcode(String invoiceId) throws WriterException {
        BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
        bitmaps.add(new PrinterModel(0,barcodeEncoder.encodeQrOrBc(invoiceId, BarcodeFormat.CODE_128, 400, 100)));
    }

    private void printReceiptNo(String invoiceId) {
        bitmaps.add(new PrinterModel(-1,PrintingHelper.createBitmapFromText("Receipt No ")));
        bitmaps.add(new PrinterModel(0,PrintingHelper.createBitmapFromText(invoiceId)));

    }

    private void printMerchantTaxNumber(String merchantTaxNumber) {
        List<Bitmap> newBitmaps = new ArrayList<>();
        newBitmaps.add(PrintingHelper.createBitmapFromText(merchantTaxNumber));
        newBitmaps.add(PrintingHelper.createBitmapFromText("رقم السجل التجارى :"));
        bitmaps.add(new PrinterModel(1,PrintingHelper.combineMultipleBitmapsHorizontally(newBitmaps, 45)));
    }

    private void printMerchantId(String merchantId) {
        List<Bitmap> newBitmaps = new ArrayList<>();
        newBitmaps.add(PrintingHelper.createBitmapFromText(merchantId));
        newBitmaps.add(PrintingHelper.createBitmapFromText("الرقم الضريبى :"));
        bitmaps.add(new PrinterModel(1,PrintingHelper.combineMultipleBitmapsHorizontally(newBitmaps, 45)));
    }


}
