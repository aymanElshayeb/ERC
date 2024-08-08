package com.app.smartpos.orders;

import android.graphics.Bitmap;

import com.app.smartpos.Constant;
import com.app.smartpos.database.DatabaseAccess;
import com.app.smartpos.utils.printing.PrintingHelper;
import com.app.smartpos.utils.qrandbrcodegeneration.BarcodeEncoder;
import com.app.smartpos.utils.qrandbrcodegeneration.ZatcaQRCodeDto;
import com.app.smartpos.utils.qrandbrcodegeneration.ZatcaQRCodeGenerationService;
import com.app.smartpos.utils.BaseActivity;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.urovo.sdk.print.PrinterProviderImpl;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class UrovoPrinter extends BaseActivity {
    String name, price, qty;
    double productTotalPrice;
    DecimalFormat f;
    List<HashMap<String, String>> orderDetailsList;
    HashMap<String, String> orderList;
    HashMap<String, String> configuration;
    String merchantTaxNumber, merchantId, productCode;
    PrinterProviderImpl mPrintManager = null;
    SimpleDateFormat sdf1 = new SimpleDateFormat(Constant.REPORT_DATETIME_FORMAT);
    ZatcaQRCodeGenerationService zatcaQRCodeGenerationService = new ZatcaQRCodeGenerationService();
    List<Bitmap> bitmaps;


    public UrovoPrinter() {
       mPrintManager = PrinterProviderImpl.getInstance(UrovoPrinter.this);
    }


    public boolean printReceipt(String invoiceId,String orderDate, String orderTime, double priceBeforeTax, double priceAfterTax, String tax, String discount, String currency){
        DatabaseAccess databaseAccess = DatabaseAccess.getInstance(UrovoPrinter.this);
        databaseAccess.open();
        configuration = databaseAccess.getConfiguration();
        merchantTaxNumber = configuration.get("merchant_tax_number");
        merchantId = configuration.get("merchant_id");
        databaseAccess.open();
        orderDetailsList = databaseAccess.getOrderDetailsList(invoiceId);
        databaseAccess.open();
        orderList = databaseAccess.getOrderListByOrderId(invoiceId);
        f = new DecimalFormat();
        try {
            mPrintManager.initPrint();
            mPrintManager.addImage(PrintingHelper.getImageBundle(), PrintingHelper.base64ToByteArray(configuration.get("merchant_logo")));
            printMerchantId(merchantId);
            printMerchantTaxNumber(merchantTaxNumber);
            mPrintManager.addTextLeft_Right(PrintingHelper.getTextBundle(Constant.CENTER_ALIGNED,true), orderDate, orderTime);
            printReceiptNo(invoiceId);
            mPrintManager.addBitmap(PrintingHelper.createBitmapFromText("فاتورة ضريبية مبسطة"), 100);
            printInvoiceBarcode(invoiceId);
            //Todo products ( id, name, price including tax, qty, total including tax
            printProducts(orderDetailsList);
            printTotalExcludingTax(priceBeforeTax);
            printDiscount(discount);
            printTax(tax);
            printTotalIncludingTax(priceAfterTax);
        //Todo total paid
//        mPrintManager.addTextLeft_Center_Right(PrintingHelper.getTextBundle(), f.format(), handleArabicText("إجمالى المدفوع").toString(), "");
        //Todo needs to be paid
//        mPrintManager.addTextLeft_Center_Right(PrintingHelper.getTextBundle(), f.format(), handleArabicText("الصافى").toString(), "");
        //Todo remaining
//        mPrintManager.addTextLeft_Center_Right(PrintingHelper.getTextBundle(), f.format(), handleArabicText("الباقى").toString(), "");
            printZatcaQrCode(orderDetailsList,merchantId,merchantTaxNumber,orderList,databaseAccess);
            mPrintManager.startPrint();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        mPrintManager.close();
        return true;

    }

    private void printZatcaQrCode(List<HashMap<String, String>> orderDetailsList, String merchantId, String merchantTaxNumber, HashMap<String, String> orderList, DatabaseAccess databaseAccess) {
        ZatcaQRCodeDto zatcaQRCodeDto = new ZatcaQRCodeDto();
        zatcaQRCodeDto.setInvoiceDate(sdf1.format(new Timestamp(Long.parseLong(orderList.get("order_timestamp")))));
        zatcaQRCodeDto.setTaxAmount(orderDetailsList.get(0).get("tax_amount"));
        zatcaQRCodeDto.setSellerName(merchantId);
        zatcaQRCodeDto.setTaxNumber(merchantTaxNumber);
        zatcaQRCodeDto.setTotalAmountWithTax(orderList.get("in_tax_total"));
        String qrCodeBase64 = "";
        mPrintManager.addBitmap(PrintingHelper.resizeBitmap(zatcaQRCodeGenerationService.createZatcaQrCode(zatcaQRCodeDto,qrCodeBase64),200,200),70);
        databaseAccess.open();
        databaseAccess.addQrCodeToOrder(orderList.get("order_id"),qrCodeBase64);
    }

    private void printTotalIncludingTax(double priceAfterTax) {
        bitmaps = new ArrayList<>();
        bitmaps.add(PrintingHelper.createBitmapFromText(f.format(priceAfterTax)));
        bitmaps.add(PrintingHelper.createBitmapFromText("الإجمالى النهائى"));
        mPrintManager.addBitmap(PrintingHelper.combineMultipleBitmapsHorizontally(bitmaps,40),0);
        mPrintManager.addText(PrintingHelper.getTextBundle(Constant.LEFT_ALIGNED,true),"----------------------------------------");
    }

    private void printTax(String tax) {
        bitmaps = new ArrayList<>();
        bitmaps.add(PrintingHelper.createBitmapFromText(f.format(Double.parseDouble(tax))));
        bitmaps.add(PrintingHelper.createBitmapFromText("ضريبة القيمة المضافة"));
        mPrintManager.addBitmap(PrintingHelper.combineMultipleBitmapsHorizontally(bitmaps,40),0);
        mPrintManager.addText(PrintingHelper.getTextBundle(Constant.LEFT_ALIGNED,true),"----------------------------------------");
    }

    private void printDiscount(String discount) {
        bitmaps = new ArrayList<>();
        bitmaps.add(PrintingHelper.createBitmapFromText(f.format(Double.parseDouble(discount))));
        bitmaps.add(PrintingHelper.createBitmapFromText("الخصم"));
        mPrintManager.addBitmap(PrintingHelper.combineMultipleBitmapsHorizontally(bitmaps,70),0);
        mPrintManager.addText(PrintingHelper.getTextBundle(Constant.LEFT_ALIGNED,true),"----------------------------------------");
    }

    private void printTotalExcludingTax(double priceBeforeTax) {
        bitmaps = new ArrayList<>();
        bitmaps.add(PrintingHelper.createBitmapFromText(f.format(priceBeforeTax)));
        bitmaps.add(PrintingHelper.createBitmapFromText("الإجمالى قبل الضريبة"));
        mPrintManager.addBitmap(PrintingHelper.combineMultipleBitmapsHorizontally(bitmaps,40),0);
        mPrintManager.addText(PrintingHelper.getTextBundle(Constant.LEFT_ALIGNED,true),"----------------------------------------");
    }

    private void printProducts(List<HashMap<String, String>> orderDetailsList) {
        mPrintManager.addText(PrintingHelper.getTextBundle(Constant.LEFT_ALIGNED,true),"----------------------------------------");
        bitmaps = new ArrayList<>();
        bitmaps.add(PrintingHelper.createBitmapFromText("الإجمالى"));
        bitmaps.add(PrintingHelper.createBitmapFromText("السعر"));
        bitmaps.add(PrintingHelper.createBitmapFromText("الكمية"));
        bitmaps.add(PrintingHelper.createBitmapFromText("بيان الصنف"));
        mPrintManager.addBitmap(PrintingHelper.combineMultipleBitmapsHorizontally(bitmaps,40),0);
        mPrintManager.addText(PrintingHelper.getTextBundle(Constant.LEFT_ALIGNED,true),"----------------------------------------");
        bitmaps = new ArrayList<>();
        for (int i = 0; i < orderDetailsList.size(); i++) {
            productCode = orderDetailsList.get(i).get("product_uuid");
            name = orderDetailsList.get(i).get("product_name_ar");
            price = orderDetailsList.get(i).get("product_price");
            qty = orderDetailsList.get(i).get("product_qty");
            productTotalPrice = Double.parseDouble(price) * Integer.parseInt(qty);
            bitmaps = new ArrayList<>();
            bitmaps.add(PrintingHelper.createBitmapFromText(f.format(productTotalPrice)));
            bitmaps.add(PrintingHelper.createBitmapFromText(price));
            bitmaps.add(PrintingHelper.createBitmapFromText(qty));
            bitmaps.add(PrintingHelper.createBitmapFromText(productCode));
            mPrintManager.addBitmap(PrintingHelper.combineMultipleBitmapsHorizontally(bitmaps,60),0);
            mPrintManager.addBitmap(PrintingHelper.createBitmapFromText(name),250);
            mPrintManager.addText(PrintingHelper.getTextBundle(Constant.LEFT_ALIGNED,true),"----------------------------------------");
        }
    }

    private void printInvoiceBarcode(String invoiceId) throws WriterException {
        BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
        mPrintManager.addBitmap(barcodeEncoder.encodeQrOrBc(invoiceId,BarcodeFormat.CODE_128,400,100),0);
    }

    private void printReceiptNo(String invoiceId) {
        mPrintManager.addText(PrintingHelper.getTextBundle(Constant.LEFT_ALIGNED, true),"Receipt No ");
        mPrintManager.addText(PrintingHelper.getTextBundle(Constant.CENTER_ALIGNED, true),invoiceId);
    }

    private void printMerchantTaxNumber(String merchantTaxNumber) {
        bitmaps = new ArrayList<>();
        bitmaps.add(PrintingHelper.createBitmapFromText(merchantTaxNumber));
        bitmaps.add(PrintingHelper.createBitmapFromText("رقم السجل التجارى :"));
        mPrintManager.addBitmap(PrintingHelper.combineMultipleBitmapsHorizontally(bitmaps,45),70);
    }

    private void printMerchantId(String merchantId) {
        bitmaps = new ArrayList<>();
        bitmaps.add(PrintingHelper.createBitmapFromText(merchantId));
        bitmaps.add(PrintingHelper.createBitmapFromText("الرقم الضريبى :"));
        mPrintManager.addBitmap(PrintingHelper.combineMultipleBitmapsHorizontally(bitmaps,45),70);
    }


}
