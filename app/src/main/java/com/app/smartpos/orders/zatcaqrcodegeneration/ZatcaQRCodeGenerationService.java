package com.app.smartpos.orders.zatcaqrcodegeneration;

import android.graphics.Bitmap;

import com.google.zxing.BarcodeFormat;

public class ZatcaQRCodeGenerationService {

    public Bitmap createZatcaQrCode(ZatcaQRCodeDto zatcaQRCodeDto) {
        ZatcaQRCodeGeneration zatcaQRCodeGeneration = new ZatcaQRCodeGeneration();
        BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
        Bitmap base64Image = null;

        try {
            String qrCode = zatcaQRCodeGeneration.getBase64(zatcaQRCodeDto);
            base64Image = barcodeEncoder.encodeQr(qrCode, BarcodeFormat.QR_CODE, 600, 600);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return base64Image;
    }
}
