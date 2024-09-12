package com.app.smartpos.orders;

import android.graphics.Bitmap;

public class PrinterModel {
    int side;
    Bitmap bitmap;
    Bitmap bitmap2;
    Bitmap bitmap3;
    int type=1;

    public PrinterModel(int side, Bitmap bitmap) {
        this.side = side;
        this.bitmap = bitmap;
    }

    public PrinterModel(Bitmap bitmap1,Bitmap bitmap2) {
        this.bitmap = bitmap1;
        this.bitmap2 = bitmap2;
        type = 2;
    }

    public PrinterModel(Bitmap bitmap1,Bitmap bitmap2,Bitmap bitmap3) {
        this.bitmap = bitmap1;
        this.bitmap2 = bitmap2;
        this.bitmap3 = bitmap3;
        type = 3;
    }


    public int getSide() {
        return side;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }
}
