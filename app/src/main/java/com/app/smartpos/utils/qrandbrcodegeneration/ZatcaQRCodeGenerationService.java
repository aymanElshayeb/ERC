package com.app.smartpos.utils.qrandbrcodegeneration;

import android.graphics.Bitmap;

import com.google.zxing.BarcodeFormat;

public class ZatcaQRCodeGenerationService {

    public Bitmap createZatcaQrCode(ZatcaQRCodeDto zatcaQRCodeDto, StringBuilder qrCodeBase64) {
        ZatcaQRCodeGeneration zatcaQRCodeGeneration = new ZatcaQRCodeGeneration();
        BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
        Bitmap bitmap = null;

        try {
            qrCodeBase64.append(zatcaQRCodeGeneration.getBase64(zatcaQRCodeDto));
            bitmap = barcodeEncoder.encodeQrOrBc(qrCodeBase64.toString(), BarcodeFormat.QR_CODE, 600, 600);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    public String createZatcaQrCodeString(ZatcaQRCodeDto zatcaQRCodeDto, StringBuilder qrCodeBase64) {
        ZatcaQRCodeGeneration zatcaQRCodeGeneration = new ZatcaQRCodeGeneration();
        BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
        String data = null;

        try {
            qrCodeBase64.append(zatcaQRCodeGeneration.getBase64(zatcaQRCodeDto));
            data=qrCodeBase64.toString();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }
}
