package com.app.smartpos.utils.qrandbrcodegeneration;

import android.graphics.Bitmap;

import com.google.zxing.BarcodeFormat;

public class ZatcaQRCodeGenerationService {

    public Bitmap createZatcaQrCode(ZatcaQRCodeDto zatcaQRCodeDto, String qrCodeBase64) {
        ZatcaQRCodeGeneration zatcaQRCodeGeneration = new ZatcaQRCodeGeneration();
        BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
        Bitmap bitmap = null;

        try {
            qrCodeBase64 = zatcaQRCodeGeneration.getBase64(zatcaQRCodeDto);
            bitmap = barcodeEncoder.encodeQrOrBc(qrCodeBase64, BarcodeFormat.QR_CODE, 600, 600);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }
}
