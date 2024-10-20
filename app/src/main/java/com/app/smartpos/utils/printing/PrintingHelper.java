package com.app.smartpos.utils.printing;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.util.Base64;

import com.app.smartpos.common.Utils;
import com.app.smartpos.database.DatabaseAccess;
import com.app.smartpos.orders.OrderBitmap;

import java.util.HashMap;
import java.util.List;

public class PrintingHelper {
    private static final String fontPath = Environment.getExternalStorageDirectory() + "/alipuhuiti.ttf";

    public static Bitmap resizeBitmap(Bitmap bitmap, int newWidth, int newHeight) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, false);
    }

    public static byte[] base64ToByteArray(String base64String) {
        return Base64.decode(base64String, Base64.DEFAULT);
    }

    public static Bundle getImageBundle() {
        Bundle format = new Bundle();
        format.putInt("align", 1);
        format.putInt("offset", 50);
        format.putInt("width", 300);
        format.putInt("height", 300);
        format.putString("text", "ACHAT");
        format.putInt("YAlign", 1);
        format.putInt("font", 2);
        format.putBoolean("fontBold", true);
        format.putString("fontName", fontPath);
        return format;
    }

    public static Bundle getImageBundle(Bitmap bitmap) {
        Bundle format = new Bundle();
        format.putInt("align", 1);
        format.putInt("offset", 50);
        format.putInt("width", bitmap.getWidth());
        format.putInt("height", bitmap.getHeight());
        format.putString("text", "ACHAT");
        format.putInt("YAlign", 1);
        format.putInt("font", 2);
        format.putBoolean("fontBold", true);
        format.putString("fontName", fontPath);
        return format;
    }

    public static Bundle getTextBundle(int align, boolean newLine) {
        Bundle format = new Bundle();
        format.putInt("font", 1);
        format.putInt("align", align);
        format.putBoolean("fontBold", true);
        format.putString("fontName", fontPath);
        format.putInt("lineHeight", 0);
        format.putBoolean("newline", newLine);
        return format;
    }

    public static Bitmap createBitmapFromText(String text) {
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
        canvas.drawText(text, x, baseline, paint);

        return image;
    }

    public static Bitmap combineMultipleBitmapsHorizontally(List<Bitmap> bitmaps, int spacing) {
        if (bitmaps == null || bitmaps.isEmpty()) {
            return null;
        }

        int totalWidth = 0;
        int maxHeight = 0;

        // Calculate the total width and maximum height
        for (Bitmap bitmap : bitmaps) {
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

    public static PrinterData createBitmap(DatabaseAccess databaseAccess, Activity activity, String id, String printType) {
        databaseAccess.open();
        String currency = databaseAccess.getCurrency();
        databaseAccess.open();
        //HashMap<String, String> orderDetails = databaseAccess.getOrderDetailsList(getIntent().getStringExtra("id")).get(0);
        HashMap<String, String> orderLitItem = databaseAccess.getOrderListByOrderId(id);
        //Utils.addLog("datadata",map.toString());
        String invoice_id = orderLitItem.get("invoice_id");
        String customer_name = orderLitItem.get("customer_name");
        String order_date = orderLitItem.get("order_date");
        String order_time = orderLitItem.get("order_time");
        databaseAccess.open();
        double tax = databaseAccess.totalOrderTax(invoice_id);
        String discount = orderLitItem.get("discount");
        databaseAccess.open();
        double price_after_tax = databaseAccess.totalOrderPrice(invoice_id);
        Utils.addLog("datadata_total_2", String.valueOf(price_after_tax));
        double price_before_tax = price_after_tax - tax;

        OrderBitmap orderBitmap = new OrderBitmap(activity);
        Bitmap bitmap = orderBitmap.orderBitmap(invoice_id, order_date, order_time, price_before_tax, price_after_tax, tax, discount, currency, printType);
        return new PrinterData(bitmap, invoice_id, customer_name, order_date, order_time, tax, price_after_tax, price_before_tax, discount, currency);

    }


}
