package com.app.smartpos.orders.zatcaqrcodegeneration;


import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.logging.Logger;

import kotlin.text.Charsets;

public class ZatcaQRCodeGeneration {
    String SELLER_NAME_TAG = "1";
    String TAX_NUMBER_TAG = "2";
    String INVOICE_DATE_TAG = "3";
    String TOTAL_AMOUNT_TAG = "4";
    String TAX_AMOUNT_TAG = "5";

    private byte[] convertTagsAndLengthToHexValues(String tag, String value) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            byte[] var4 = value.getBytes(Charsets.UTF_8);
            byte[] var3 = new byte[] { Byte.parseByte(tag),  Byte.parseByte(String.valueOf(var4.length)) };
            outputStream.write(var3);
            outputStream.write(var4);

        } catch (Exception ex) {
        }

        return outputStream.toByteArray();
    }

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

            base64QRCode = Base64.getEncoder().encodeToString(outputStream1.toByteArray());
        } catch (Exception ex) {
        }
        return base64QRCode;
    }

}
