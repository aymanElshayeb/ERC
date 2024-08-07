package com.app.smartpos.orders;

import android.content.Context;
import android.device.PrinterManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.util.Base64;

import com.app.smartpos.Constant;
import com.app.smartpos.database.DatabaseAccess;
import com.app.smartpos.orders.zatcaqrcodegeneration.ZatcaQRCodeDto;
import com.app.smartpos.orders.zatcaqrcodegeneration.ZatcaQRCodeGenerationService;
import com.app.smartpos.utils.BaseActivity;
import com.google.zxing.BarcodeFormat;
import com.urovo.sdk.print.PrintFormat;
import com.urovo.sdk.print.PrinterProviderImpl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class UrovoPrinter extends BaseActivity {
    private static final Log log = LogFactory.getLog(UrovoPrinter.class);
    final int CONFIGURATION_ID = 1;
    String name, price, qty, weight;
    double productTotalPrice, totalExcludingTax, totalIncludingTax;
    Bitmap bm;
    DecimalFormat f;
    private Context context;
    List<HashMap<String, String>> orderDetailsList;
    ArrayList<HashMap<String, String>> orderList;
    HashMap<String, String> configuration;
    String merchantTaxNumber, productCode;
    PrinterProviderImpl mPrintManager = null;
    boolean isPrinting = false;
    String fontPath = Environment.getExternalStorageDirectory() + "/alipuhuiti.ttf";
    SimpleDateFormat sdf1 = new SimpleDateFormat(Constant.REPORT_DATETIME_FORMAT);
    ZatcaQRCodeGenerationService zatcaQRCodeGenerationService = new ZatcaQRCodeGenerationService();

    public UrovoPrinter() {
       mPrintManager = PrinterProviderImpl.getInstance(UrovoPrinter.this);
    }


    public boolean printReceipt(String invoiceId,String orderDate, String orderTime, double priceBeforeTax, double priceAfterTax, String tax, String discount, String currency){
        DatabaseAccess databaseAccess = DatabaseAccess.getInstance(UrovoPrinter.this);

        databaseAccess.open();
        configuration = databaseAccess.getConfiguration();
        merchantTaxNumber = configuration.get("merchant_tax_number");

        databaseAccess.open();
        orderDetailsList = databaseAccess.getOrderDetailsList(invoiceId);

        databaseAccess.open();
        orderList = databaseAccess.getOrderList();

        f = new DecimalFormat();
        try {
            mPrintManager.initPrint();
//            mPrintManager.addBitmap(handleArabicText("مينا"),0);
//            mPrintManager.addBitmap(handleArabicText("النجم"),50);
            List<Bitmap> bitmaps = new ArrayList<>();

//            mPrintManager.addTextLeft_Center_Right(" 6.00","6.00     1"," PR0001",1,false);


            //Todo merchant logo
            mPrintManager.addImage(getImageBundle(), base64ToByteArray(configuration.get("merchant_logo")));
//            //Todo merchant name
            bitmaps = new ArrayList<>();
            bitmaps.add(handleText(configuration.get("merchant_id"),false));
            bitmaps.add(handleText("الرقم الضريبى :",false));
            mPrintManager.addBitmap(combineMultipleBitmapsHorizontally(bitmaps,45),70);
//            mPrintManager.addText(getTextBundle(Constant.CENTER_ALIGNED,true), configuration.get("merchant_id"));
//            //Todo merchant tax number
            bitmaps = new ArrayList<>();
            bitmaps.add(handleText(merchantTaxNumber,false));
            bitmaps.add(handleText("رقم السجل التجارى :",false));
            mPrintManager.addBitmap(combineMultipleBitmapsHorizontally(bitmaps,45),70);
//            mPrintManager.addText(getTextBundle(Constant.CENTER_ALIGNED), merchantTaxNumber);
//            //Todo date and time
            mPrintManager.addTextLeft_Right(getTextBundle(Constant.CENTER_ALIGNED,true), orderDate, orderTime);
//            //Todo Receipt No after generating it
            mPrintManager.addText(getTextBundle(Constant.LEFT_ALIGNED, true),"Receipt No " + invoiceId.replaceAll(" ",""));
//            //Todo فاتورة ضريبية مبسطة
            mPrintManager.addBitmap(handleText("فاتورة ضريبية مبسطة",false), 100);
//            //Todo barcode from receipt no
            mPrintManager.addBarCode(getBarCodeBundle(),"A"+"001-001-0000000001"+"B");
//            //Todo products ( id, name, price including tax, qty, total including tax
            bitmaps = new ArrayList<>();
            bitmaps.add(handleText("الإجمالى", false));
            bitmaps.add(handleText("السعر", false));
            bitmaps.add(handleText("الكمية", false));
            bitmaps.add(handleText("بيان الصنف", false));
            mPrintManager.addBitmap(combineMultipleBitmapsHorizontally(bitmaps,40),0);
            mPrintManager.addText(getTextBundle(Constant.LEFT_ALIGNED,true),"----------------------------------------");            bitmaps = new ArrayList<>();
            ////            mPrintManager.addBlackLine();
//            mPrintManager.addTextLeft_Center_Right(getTextBundle(Constant.CENTER_ALIGNED), handleArabicText("الإجمالى").toString(), handleArabicText("الكمية  السعر").toString(), handleArabicText("بيان الصنف").toString() );
////            mPrintManager.addBlackLine();
            for (int i = 0; i < orderDetailsList.size(); i++) {
                productCode = orderDetailsList.get(i).get("product_uid");
                name = orderDetailsList.get(i).get("product_name_ar");
                price = orderDetailsList.get(i).get("product_price");
                qty = orderDetailsList.get(i).get("product_qty");
                productTotalPrice = Double.parseDouble(price) * Integer.parseInt(qty);
                bitmaps = new ArrayList<>();
                bitmaps.add(handleText(f.format(productTotalPrice),false));
                bitmaps.add(handleText(price,false));
                bitmaps.add(handleText(qty,false));
                bitmaps.add(handleText(productCode,false));
                mPrintManager.addBitmap(combineMultipleBitmapsHorizontally(bitmaps,60),0);
                mPrintManager.addBitmap(handleText(orderDetailsList.get(i).get("product_name_ar"),false),250);
                mPrintManager.addText(getTextBundle(Constant.LEFT_ALIGNED,true),"----------------------------------------");
            }
////            mPrintManager.addBlackLine();
//            //Todo total excluding tax
            bitmaps = new ArrayList<>();
            bitmaps.add(handleText(f.format(priceBeforeTax),false));
            bitmaps.add(handleText("الإجمالى قبل الضريبة",false));
            mPrintManager.addBitmap(combineMultipleBitmapsHorizontally(bitmaps,40),0);
            mPrintManager.addText(getTextBundle(Constant.LEFT_ALIGNED,true),"----------------------------------------");
            //            mPrintManager.addTextLeft_Center_Right(getTextBundle(Constant.CENTER_ALIGNED), f.format(priceBeforeTax), convertBitmapToBase64(handleArabicText("الإجمالى قبل الضريبة")), "");
////            mPrintManager.addBlackLine();
//            //Todo discount
            bitmaps = new ArrayList<>();
            bitmaps.add(handleText(f.format(Double.parseDouble(discount)),false));
            bitmaps.add(handleText("الخصم",false));
            mPrintManager.addBitmap(combineMultipleBitmapsHorizontally(bitmaps,70),0);
            mPrintManager.addText(getTextBundle(Constant.LEFT_ALIGNED,true),"----------------------------------------");//            mPrintManager.addTextLeft_Center_Right(getTextBundle(Constant.CENTER_ALIGNED), f.format(Double.parseDouble(discount)), handleArabicText("الخصم").toString(), "");
//            //Todo tax
            bitmaps = new ArrayList<>();
            bitmaps.add(handleText(f.format(Double.parseDouble(tax)),false));
            bitmaps.add(handleText("ضريبة القيمة المضافة",false));
            mPrintManager.addBitmap(combineMultipleBitmapsHorizontally(bitmaps,40),0);
            mPrintManager.addText(getTextBundle(Constant.LEFT_ALIGNED,true),"----------------------------------------");//            mPrintManager.addTextLeft_Center_Right(getTextBundle(Constant.CENTER_ALIGNED), f.format(Double.parseDouble(tax)), handleArabicText("ضريبة القيمة المضافة").toString(), "");
//            //Todo total including tax
            bitmaps = new ArrayList<>();
            bitmaps.add(handleText(f.format(priceAfterTax),false));
            bitmaps.add(handleText("الإجمالى النهائى",false));
            mPrintManager.addBitmap(combineMultipleBitmapsHorizontally(bitmaps,40),0);
            mPrintManager.addText(getTextBundle(Constant.LEFT_ALIGNED,true),"----------------------------------------");//            mPrintManager.addTextLeft_Center_Right(getTextBundle(Constant.CENTER_ALIGNED), f.format(priceAfterTax), handleArabicText("الإجمالى النهائى").toString(), "");
//            mPrintManager.addBlackLine();
//        //Todo total paid
//        mPrintManager.addTextLeft_Center_Right(getTextBundle(), f.format(), handleArabicText("إجمالى المدفوع").toString(), "");
//        //Todo needs to be paid
//        mPrintManager.addTextLeft_Center_Right(getTextBundle(), f.format(), handleArabicText("الصافى").toString(), "");
//        //Todo remaining
//        mPrintManager.addTextLeft_Center_Right(getTextBundle(), f.format(), handleArabicText("الباقى").toString(), "");
//            mPrintManager.addBlackLine();
            //Todo zatca qr code
            ZatcaQRCodeDto zatcaQRCodeDto = new ZatcaQRCodeDto();
            zatcaQRCodeDto.setInvoiceDate(sdf1.format(new Timestamp(System.currentTimeMillis())));
            zatcaQRCodeDto.setTaxAmount(orderDetailsList.get(0).get("tax_amount"));
            zatcaQRCodeDto.setSellerName(configuration.get("merchant_id"));
            zatcaQRCodeDto.setTaxNumber(merchantTaxNumber);
            zatcaQRCodeDto.setTotalAmountWithTax(orderList.get(0).get("in_tax_total"));
            mPrintManager.addBitmap(resizeBitmap(zatcaQRCodeGenerationService.createZatcaQrCode(zatcaQRCodeDto),200,200),50);

        }
        catch (Exception e){
            e.printStackTrace();
        }
        try {
            int iRet = mPrintManager.startPrint();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        mPrintManager.close();
        return true;

    }

    public static Bitmap resizeBitmap(Bitmap bitmap, int newWidth, int newHeight) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, false);
    }

    private Bundle getQrCodeBundle() {
        Bundle format = new Bundle();
        format.putInt("align", 1);
        format.putInt("offset", 20);
        format.putInt("expectedHeight", 50);
        return format;
    }

    private Bundle getBarCodeBundle() {
        Bundle format = new Bundle();
        format.putInt("align", 0);
        format.putInt("width", 300);
        format.putInt("height", 100);
        format.putInt("offset", 50);
        format.putSerializable(PrintFormat.BARCODE_TYPE, BarcodeFormat.CODABAR);
        return format;
    }

    private byte[] base64ToByteArray(String base64String) {
        return Base64.decode(base64String, Base64.DEFAULT);
    }

    private Bundle getImageBundle() {
        Bundle format = new Bundle();
        format.putInt("align", 1);
        format.putInt("offset", 50);
        format.putInt("width", 250);
        format.putInt("height", 250);
        format.putString("text", "ACHAT");
        format.putInt("YAlign", 1);
        format.putInt("font", 2);
        format.putBoolean("fontBold", true);
        format.putString("fontName", fontPath);
        return format;
    }

    private Bundle getTextBundle(int align, boolean newLine) {
        Bundle format = new Bundle();
        format.putInt("font", 1);
        format.putInt("align", align);
        format.putBoolean("fontBold", true);
        format.putString("fontName", fontPath);
        format.putInt("lineHeight", 0);
        format.putBoolean("newline", newLine);
        return format;
    }

    private Bundle getArabicTextBundle(int align) {
        Bundle format = new Bundle();
        format.putInt("font", 0);
        format.putString("fontName",fontPath);
        format.putInt("align", align);
        format.putBoolean("fontBold", true);
        //format.putString("fontName", fontPath);
        format.putInt("lineHeight", 0);
        return format;
    }

    private Bitmap handleText(String text, boolean rightAligned) {
        return createBitmapFromText(text,rightAligned);
    }

    private static Bitmap createBitmapFromText(String text, boolean rightAligned) {
        float x = 0;
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.BLACK);
        paint.setTextSize(22);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

        float baseline = -paint.ascent(); // ascent() is negative
        int width = (int) (paint.measureText(text) + 0.5f); // round
        int height = (int) (baseline + paint.descent() + 0.5f);

        Bitmap image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(image);
        canvas.drawColor(Color.WHITE);
        if(rightAligned){
//            int printerDPI = 203;
//            int printerWidthInPixels = (int) (1.575 * printerDPI);
//            x = printerWidthInPixels - width;
            x =100;
        }
        canvas.drawText(text, x, baseline, paint);

        return image;
    }

    public static Bitmap combineBitmapsHorizontally(Bitmap bitmap1, Bitmap bitmap2, Bitmap bitmap3, Bitmap bitmap4, int smallSpacing, int bigSpacing) {
        int width = bitmap1.getWidth() + bitmap2.getWidth() + smallSpacing;
        int height = Math.max(bitmap1.getHeight(), bitmap2.getHeight());

        Bitmap combinedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(combinedBitmap);
        canvas.drawColor(Color.TRANSPARENT);
        canvas.drawColor(Color.WHITE);


        canvas.drawBitmap(bitmap1, 0, 0, null);
        canvas.drawBitmap(bitmap2, bitmap1.getWidth() + smallSpacing, 0, null);

        return combinedBitmap;
    }

    public static Bitmap combineMultipleBitmapsHorizontally(List<Bitmap> bitmaps, int spacing) {
        if (bitmaps == null || bitmaps.isEmpty()) {
            return null;
        }

        int totalWidth = 0;
        int maxHeight = 0;

        // Calculate the total width and maximum height
        for (Bitmap bitmap:bitmaps) {
            if (bitmap != null) {
                totalWidth += bitmap.getWidth() + spacing;
                maxHeight = Math.max(maxHeight, bitmap.getHeight());
            }
        }

        // Subtract the extra spacing added after the last bitmap
        totalWidth -= spacing;

        Bitmap combinedBitmap = Bitmap.createBitmap(totalWidth, maxHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(combinedBitmap);
        canvas.drawColor(Color.TRANSPARENT);
        canvas.drawColor(Color.WHITE);

        int currentXPoint = 0;
        for (Bitmap bitmap : bitmaps) {
            if (bitmap != null) {
                canvas.drawBitmap(bitmap, currentXPoint, 0, null);
                currentXPoint += bitmap.getWidth() + spacing;
            }
        }

        return combinedBitmap;
    }



}
