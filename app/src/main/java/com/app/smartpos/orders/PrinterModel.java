package com.app.smartpos.orders;

import android.graphics.Bitmap;

public class PrinterModel {
    int side;
    Bitmap bitmap;

    public PrinterModel(int side, Bitmap bitmap) {
        this.side = side;
        this.bitmap = bitmap;
    }

    public int getSide() {
        return side;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }
}
