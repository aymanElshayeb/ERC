package com.app.smartpos.utils.qrandbrcodegeneration;


import static com.app.smartpos.common.CrashReport.CustomExceptionHandler.addToDatabase;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;

import com.app.smartpos.Constant;
import com.app.smartpos.devices.DeviceFactory.Device;
import com.app.smartpos.devices.DeviceFactory.DeviceFactory;
import com.app.smartpos.database.DatabaseAccess;
import com.app.smartpos.utils.printing.PrintingHelper;
import com.google.zxing.BarcodeFormat;

import java.io.ByteArrayOutputStream;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;

import kotlin.text.Charsets;

public class ZatcaQRCodeGeneration {
    String SELLER_NAME_TAG = "1";
    String TAX_NUMBER_TAG = "2";
    String INVOICE_DATE_TAG = "3";
    String TOTAL_AMOUNT_TAG = "4";
    String TAX_AMOUNT_TAG = "5";
    SimpleDateFormat sdf1 = new SimpleDateFormat(Constant.REPORT_DATETIME_FORMAT);

    private byte[] convertTagsAndLengthToHexValues(String tag, String value) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            byte[] var4 = value.getBytes(Charsets.UTF_8);
            byte[] var3 = new byte[]{Byte.parseByte(tag), Byte.parseByte(String.valueOf(var4.length))};
            outputStream.write(var3);
            outputStream.write(var4);

        } catch (Exception ex) {
            ex.printStackTrace();
            addToDatabase(ex,"convertTagsAndLengthToHexValues-function-error-zatcaQRCodeGeneration");

        }

        return outputStream.toByteArray();
    }

    @SuppressLint("NewApi")
    public String getBase64(ZatcaQRCodeDto zatcaQRCodeDto) {
        ByteArrayOutputStream outputStream1 = new ByteArrayOutputStream();
        String base64QRCode = null;
        try {

            byte[] tlSellerName = zatcaQRCodeDto.getSellerName() != null
                    ? convertTagsAndLengthToHexValues(SELLER_NAME_TAG, zatcaQRCodeDto.getSellerName())
                    : null;

            byte[] tlTaxNumber = zatcaQRCodeDto.getTaxNumber() != null
                    ? convertTagsAndLengthToHexValues(TAX_NUMBER_TAG, zatcaQRCodeDto.getTaxNumber())
                    : null;
            byte[] tlInvoiceDate = zatcaQRCodeDto.getInvoiceDate() != null
                    ? convertTagsAndLengthToHexValues(INVOICE_DATE_TAG, zatcaQRCodeDto.getInvoiceDate())
                    : null;

            byte[] tlTotalAmountWithTax = zatcaQRCodeDto.getTotalAmountWithTax() != null
                    ? convertTagsAndLengthToHexValues(TOTAL_AMOUNT_TAG, zatcaQRCodeDto.getTotalAmountWithTax())
                    : null;

            byte[] tlTaxAmount = zatcaQRCodeDto.getTaxAmount() != null
                    ? convertTagsAndLengthToHexValues(TAX_AMOUNT_TAG, zatcaQRCodeDto.getTaxAmount())
                    : null;

            outputStream1.write(tlSellerName);
            outputStream1.write(tlTaxNumber);
            outputStream1.write(tlInvoiceDate);
            outputStream1.write(tlTotalAmountWithTax);
            outputStream1.write(tlTaxAmount);
            Device device = DeviceFactory.getDevice();
            base64QRCode = device.zatcaQrCodeGeneration(outputStream1.toByteArray());
        } catch (Exception ex) {
            addToDatabase(ex,"getBase64-function-error-ZatcaQRCodeGeneration");
            ex.printStackTrace();
        }
        return base64QRCode;
    }

    public Bitmap getQrCodeBitmap(HashMap<String, String> orderList, DatabaseAccess databaseAccess, List<HashMap<String, String>> orderDetailsList, HashMap<String, String> configuration, boolean print) {
        Bitmap qrCodeBitmap;
        StringBuilder qrCode = new StringBuilder(orderList.get("qr_code"));
        if (qrCode.toString().isEmpty()) {
            ZatcaQRCodeDto zatcaQRCodeDto = new ZatcaQRCodeDto();
            zatcaQRCodeDto.setInvoiceDate(sdf1.format(new Timestamp(Long.parseLong(orderList.get("order_timestamp")))));
            zatcaQRCodeDto.setTaxAmount(orderDetailsList.get(0).get("tax_amount"));
            zatcaQRCodeDto.setSellerName(configuration.isEmpty() ? "" : configuration.get("invoice_merchant_id").replace("cr",""));
            zatcaQRCodeDto.setTaxNumber(configuration.isEmpty() ? "" : configuration.get("merchant_tax_number"));
            zatcaQRCodeDto.setTotalAmountWithTax(orderList.get("in_tax_total"));
            ZatcaQRCodeGenerationService zatcaQRCodeGenerationService = new ZatcaQRCodeGenerationService();
            qrCodeBitmap = PrintingHelper.resizeBitmap(zatcaQRCodeGenerationService.createZatcaQrCode(zatcaQRCodeDto, qrCode), 200, 200);
            if (print) {
                databaseAccess.open();
                databaseAccess.addQrCodeToOrder(orderList.get("invoice_id"), qrCode.toString());
            }
        } else {
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            qrCodeBitmap = PrintingHelper.resizeBitmap(barcodeEncoder.encodeQrOrBc(qrCode.toString(), BarcodeFormat.QR_CODE, 600, 600), 200, 200);
        }
        return qrCodeBitmap;
    }

    public String getQrCodeString(HashMap<String, String> orderList, DatabaseAccess databaseAccess, List<HashMap<String, String>> orderDetailsList, HashMap<String, String> configuration) {
        String data;
        StringBuilder qrCode = new StringBuilder(orderList.get("qr_code"));
        if (qrCode.toString().isEmpty()) {
            ZatcaQRCodeDto zatcaQRCodeDto = new ZatcaQRCodeDto();
            zatcaQRCodeDto.setInvoiceDate(sdf1.format(new Timestamp(Long.parseLong(orderList.get("order_timestamp")))));
            zatcaQRCodeDto.setTaxAmount(orderDetailsList.get(0).get("tax_amount"));
            zatcaQRCodeDto.setSellerName(configuration.isEmpty() ? "" : configuration.get("invoice_merchant_id"));
            zatcaQRCodeDto.setTaxNumber(configuration.isEmpty() ? "" : configuration.get("merchant_tax_number"));
            zatcaQRCodeDto.setTotalAmountWithTax(orderList.get("in_tax_total"));
            ZatcaQRCodeGenerationService zatcaQRCodeGenerationService = new ZatcaQRCodeGenerationService();
            data = zatcaQRCodeGenerationService.createZatcaQrCodeString(zatcaQRCodeDto, qrCode);
            databaseAccess.open();
            databaseAccess.addQrCodeToOrder(orderList.get("invoice_id"), qrCode.toString());
        } else {
            data = qrCode.toString();
        }
        return data;
    }

}
