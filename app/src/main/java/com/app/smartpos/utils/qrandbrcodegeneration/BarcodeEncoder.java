package com.app.smartpos.utils.qrandbrcodegeneration;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;

import org.apache.commons.codec.binary.Base64;

import java.io.ByteArrayOutputStream;

public class BarcodeEncoder {
    public Bitmap encodeQrOrBc(String contents, BarcodeFormat format, int width, int height) {
        Bitmap encodedQR = null;
        try {
            encodedQR = toBitmap(encode(contents, format, width, height));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return encodedQR;
    }

    private String createMatrixImage(BitMatrix matrix) {
        String matrixImage = null;
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", bos);
            matrixImage = new String(Base64.encodeBase64(bos.toByteArray()), "utf-8");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return matrixImage;
    }

    public static Bitmap toBitmap(BitMatrix matrix) {
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                bmp.setPixel(x, y, matrix.get(x, y) ? Color.BLACK : Color.WHITE);
            }
        }
        return bmp;
    }

    public Bitmap base64ToBitmap(String base64){
        Bitmap bitmap = null;
        try {
            // Decode Base64 string to byte array
            byte[] decodedBytes = android.util.Base64.decode(base64, android.util.Base64.DEFAULT);
            // Convert byte array to Bitmap
            bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        }catch (Exception e){
            e.printStackTrace();
        }
        return bitmap;

    }

    private BitMatrix encode(String contents, BarcodeFormat format, int width, int height)
            throws WriterException {
        BitMatrix bitMatrix = null;
        try {
            bitMatrix = new MultiFormatWriter().encode(contents, format, width, height);
        } catch (WriterException e) {
            throw e;
        } catch (Exception e) {
            throw new WriterException(e);
        }
        return bitMatrix;
    }
}
