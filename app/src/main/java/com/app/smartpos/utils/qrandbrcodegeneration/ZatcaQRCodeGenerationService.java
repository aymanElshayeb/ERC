package com.app.smartpos.utils.qrandbrcodegeneration;

import static com.app.smartpos.common.CrashReport.CustomExceptionHandler.addToDatabase;

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
            addToDatabase(e,"createZatcaQrCode-function-error-ZatcaQRCodeGenerationService");
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
            data = qrCodeBase64.toString();

        } catch (Exception e) {
            addToDatabase(e,"createZatcaQrCodeString-function-error-ZatcaQRCodeGenerationService");
            e.printStackTrace();
        }
        return data;
    }
}
