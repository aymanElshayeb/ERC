package com.app.smartpos.utils.printing;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.util.Base64;

import java.util.List;

public class PrintingHelper {
    private static String fontPath = Environment.getExternalStorageDirectory() + "/alipuhuiti.ttf";

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
