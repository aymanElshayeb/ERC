package com.app.smartpos.orders;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.util.Base64;

import com.app.smartpos.database.DatabaseAccess;
import com.app.smartpos.database.DatabaseOpenHelper;
import com.app.smartpos.utils.BaseActivity;
import com.urovo.sdk.print.PrinterProviderImpl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;

public class UrovoPrinter extends BaseActivity {
    private static final Log log = LogFactory.getLog(UrovoPrinter.class);
    String name, price, qty, weight;
    double productTotalPrice, totalExcludingTax, totalIncludingTax;
    Bitmap bm;
    DecimalFormat f;
    private Context context;
    List<HashMap<String, String>> orderDetailsList;
    HashMap<String, String> configuration;
    String merchantTaxNumber, productCode;
    PrinterProviderImpl mPrintManager = null;
    boolean isPrinting = false;
    String fontPath = Environment.getExternalStorageDirectory() + "/alipuhuiti.ttf";



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

        //Todo merchant logo
        mPrintManager.addImage(getImageBundle(), base64ToByteArray(configuration.get("merchant_logo")));
        //Todo merchant name
        mPrintManager.addText(getTextBundle(), configuration.get("merchant_id"));
        //Todo merchant tax number
        mPrintManager.addText(getTextBundle(), merchantTaxNumber);
        //Todo date and time
        mPrintManager.addTextLeft_Right(getTextBundle(), orderDate, orderTime);
        //Todo Receipt No after generating it
        mPrintManager.addText(getTextBundle(),"Receipt No " + invoiceId);
        //Todo فاتورة ضريبية مبسطة
        mPrintManager.addText(getTextBundle(), "فاتورة ضريبية مبسطة");
        //Todo barcode from receipt no
        mPrintManager.addBarCode(getBarCodeBundle(),invoiceId);
        //Todo products ( id, name, price including tax, qty, total including tax
        mPrintManager.addBlackLine();
        mPrintManager.addTextLeft_Center_Right(getTextBundle(), handleArabicText("الإجمالى").toString(), handleArabicText("الكمية  السعر").toString(), handleArabicText("بيان الصنف").toString() );
        mPrintManager.addBlackLine();
        for (int i = 0; i < orderDetailsList.size(); i++) {
            productCode = orderDetailsList.get(i).get("product_code");
            name = orderDetailsList.get(i).get("product_name_en");
            price = orderDetailsList.get(i).get("product_sell_price");
            qty = orderDetailsList.get(i).get("product_qty");
            productTotalPrice = Double.parseDouble(price) * Integer.parseInt(qty);
            mPrintManager.addTextLeft_Center_Right(getTextBundle(), f.format(productTotalPrice), price + "  " + qty, productCode);
            mPrintManager.addTextLeft_Right(getTextBundle(), "", handleArabicText(name).toString());
        }
        mPrintManager.addBlackLine();
        //Todo total excluding tax
        mPrintManager.addTextLeft_Center_Right(getTextBundle(), f.format(priceBeforeTax), handleArabicText("الإجمالى قبل الضريبة").toString(), "");
        mPrintManager.addBlackLine();
        //Todo discount
        mPrintManager.addTextLeft_Center_Right(getTextBundle(), f.format(discount), handleArabicText("الخصم").toString(), "");
        //Todo tax
        mPrintManager.addTextLeft_Center_Right(getTextBundle(), f.format(tax), handleArabicText("ضريبة القيمة المضافة").toString(), "");
        //Todo total including tax
        mPrintManager.addTextLeft_Center_Right(getTextBundle(), f.format(priceAfterTax), handleArabicText("الإجمالى النهائى").toString(), "");
        mPrintManager.addBlackLine();
//        //Todo total paid
        mPrintManager.addTextLeft_Center_Right(getTextBundle(), "", handleArabicText("إجمالى المدفوع").toString(), "");
//        //Todo needs to be paid
        mPrintManager.addTextLeft_Center_Right(getTextBundle(), "", handleArabicText("الصافى").toString(), "");
//        //Todo remaining
        mPrintManager.addTextLeft_Center_Right(getTextBundle(), "", handleArabicText("الباقى").toString(), "");
        mPrintManager.addBlackLine();
        //Todo zatca qr code
        mPrintManager.addQrCode(getQrCodeBundle(), merchantTaxNumber+invoiceId);

        return true;

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
        return format;
    }

    private byte[] base64ToByteArray(String base64String) {
        return Base64.decode(base64String, Base64.DEFAULT);
    }

    private Bundle getImageBundle() {
        Bundle format = new Bundle();
        format.putInt("align", 1);
        format.putInt("offset", 190);
        format.putInt("width", 100);
        format.putInt("height", 100);
        format.putString("text", "ACHAT");
        format.putInt("YAlign", 1);
        format.putInt("font", 2);
        format.putBoolean("fontBold", true);
        format.putString("fontName", fontPath);
        return format;
    }

    private Bundle getTextBundle() {
        Bundle format = new Bundle();
        format.putInt("font", 1);
        format.putInt("align", 1);
        format.putBoolean("fontBold", true);
        format.putString("fontName", fontPath);
        format.putInt("lineHeight", 0);
        return format;
    }

    public boolean test(){
        mPrintManager.initPrint();
        int status = mPrintManager.getStatus();
        if (status != 0) {
            isPrinting = false;
            return false;
        }
        isPrinting = true;
//        mPrintManager.addText(getTextBundle(),handleArabicText("كتفم الجافا"));
        mPrintManager.addBitmap(handleArabicText("كتفم الجافا"), 0);
//        mPrintManager.addImageWithText(getTextBundle(),handleArabicText("كتفم الجافا"));
        int iRet = mPrintManager.startPrint();
        mPrintManager.close();
        isPrinting = false;
        return true;
    }

    private Bitmap handleArabicText(String arabicText) {
        return createBitmapFromText(arabicText);
    }

    public static Bitmap createBitmapFromText(String text) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.BLACK);
        paint.setTextSize(50);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

        float baseline = -paint.ascent(); // ascent() is negative
        int width = (int) (paint.measureText(text) + 0.5f); // round
        int height = (int) (baseline + paint.descent() + 0.5f);

        Bitmap image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(image);
        canvas.drawColor(Color.WHITE);
        canvas.drawText(text, 0, baseline, paint);

        return image;
    }



}
