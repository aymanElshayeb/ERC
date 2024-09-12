package com.app.smartpos.orders;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;

import com.app.smartpos.Constant;
import com.app.smartpos.database.DatabaseAccess;
import com.app.smartpos.settings.end_shift.EndShiftModel;
import com.app.smartpos.settings.end_shift.ShiftDifferences;
import com.app.smartpos.utils.printing.PrintingHelper;
import com.app.smartpos.utils.qrandbrcodegeneration.BarcodeEncoder;
import com.app.smartpos.utils.qrandbrcodegeneration.ZatcaQRCodeGeneration;
import com.app.smartpos.utils.qrandbrcodegeneration.ZatcaQRCodeGenerationService;
import com.app.smartpos.utils.BaseActivity;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.urovo.sdk.print.PrinterProviderImpl;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("a");


    public UrovoPrinter() {
       mPrintManager = PrinterProviderImpl.getInstance(UrovoPrinter.this);
    }


    public boolean printReceipt(String invoiceId,String orderDate, String orderTime, double priceBeforeTax, double priceAfterTax, String tax, String discount, String currency,String printType){
        DatabaseAccess databaseAccess = DatabaseAccess.getInstance(UrovoPrinter.this);
        databaseAccess.open();
        configuration = databaseAccess.getConfiguration();
        merchantTaxNumber = configuration.isEmpty() ? "" : configuration.get("merchant_tax_number");
        merchantId = configuration.isEmpty() ? "" : configuration.get("merchant_id");
        databaseAccess.open();
        orderDetailsList = databaseAccess.getOrderDetailsList(invoiceId);
        databaseAccess.open();
        orderList = databaseAccess.getOrderListByOrderId(invoiceId);
        f = new DecimalFormat("#.00");
        try {
            mPrintManager.initPrint();
            if(!configuration.get("merchant_logo").isEmpty())
                mPrintManager.addImage(PrintingHelper.getImageBundle(), PrintingHelper.base64ToByteArray(configuration.isEmpty() ? "" : configuration.get("merchant_logo")));
            printMerchantId(merchantId);
            printMerchantTaxNumber(merchantTaxNumber);
            mPrintManager.addTextLeft_Right(PrintingHelper.getTextBundle(Constant.CENTER_ALIGNED,true), orderDate, orderTime);
            printReceiptNo(invoiceId);
            printType(printType);
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
            printZatcaQrCode(databaseAccess);
            printLine();
            mPrintManager.startPrint();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        mPrintManager.close();
        return true;

    }

    @SuppressLint("NewApi")
    public boolean printZReport(EndShiftModel endShiftModel){
        f = new DecimalFormat("#.00");
        try {
            mPrintManager.initPrint();
            mPrintManager.addText(PrintingHelper.getTextBundle(Constant.CENTER_ALIGNED,true), "Z Report");
            mPrintManager.addText(PrintingHelper.getTextBundle(Constant.CENTER_ALIGNED,true), LocalDateTime.now().toString().replace("T","  ") + " " + LocalDateTime.now().format(formatter));
            printLine();
            mPrintManager.addText(PrintingHelper.getTextBundle(Constant.CENTER_ALIGNED,true), "Shift no  " + endShiftModel.getSequence());
            mPrintManager.addText(PrintingHelper.getTextBundle(Constant.LEFT_ALIGNED,true), "Username   " + endShiftModel.getUserName());
            printStartAndEndShiftTime(endShiftModel.getStartDateTime(), endShiftModel.getEndDateTime());
            printLine();
            printStartAndLeaveCash(endShiftModel.getStartCash(), endShiftModel.getLeaveCash());
            printNumOfTransactions(endShiftModel.getNum_successful_transaction(), endShiftModel.getNum_returned_transaction());
            printTransactionsAmount(endShiftModel.getTotal_amount() - endShiftModel.getTotalRefundsAmount(),endShiftModel.getTotalRefundsAmount() * -1);
            printCashDiscrepancies(endShiftModel.getTotal_amount() - endShiftModel.getTotalCardsAmount(), Objects.requireNonNull(endShiftModel.getShiftDifferences().get("CASH")).getInput());
            printPaymentDetails(endShiftModel.getTotal_amount() - endShiftModel.getTotalCardsAmount(), endShiftModel.getTotalCardsAmount());
            printCardTypesBreakdown(endShiftModel.getShiftDifferences());
            printLine();
            mPrintManager.startPrint();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        mPrintManager.close();
        return true;
    }

    private void printTransactionsAmount(double totalAmount, double totalRefundsAmount) {
        mPrintManager.addText(PrintingHelper.getTextBundle(Constant.CENTER_ALIGNED,true),"Transactions amount breakdown");
        printLine();
        bitmaps = new ArrayList<>();
        bitmaps.add(PrintingHelper.createBitmapFromText("Sales amount"));
        bitmaps.add(PrintingHelper.createBitmapFromText(String.valueOf(totalAmount)));
        mPrintManager.addBitmap(PrintingHelper.combineMultipleBitmapsHorizontally(bitmaps,140),0);
        bitmaps = new ArrayList<>();
        bitmaps.add(PrintingHelper.createBitmapFromText("Refunded amount"));
        bitmaps.add(PrintingHelper.createBitmapFromText(String.valueOf(totalRefundsAmount)));
        mPrintManager.addBitmap(PrintingHelper.combineMultipleBitmapsHorizontally(bitmaps,100),0);
        printLine();
        bitmaps = new ArrayList<>();
        bitmaps.add(PrintingHelper.createBitmapFromText("Total"));
        bitmaps.add(PrintingHelper.createBitmapFromText(zeroChecker(String.valueOf(totalAmount - totalRefundsAmount))));
        mPrintManager.addBitmap(PrintingHelper.combineMultipleBitmapsHorizontally(bitmaps,200),0);
        printLine();
    }

    private void printCashDiscrepancies(double totalCash, double inputCash) {
        mPrintManager.addText(PrintingHelper.getTextBundle(Constant.CENTER_ALIGNED,true),"Cash discrepancies");
        printLine();
        mPrintManager.addText(PrintingHelper.getTextBundle(Constant.LEFT_ALIGNED,true), "Actual total cash   " + zeroChecker(f.format(totalCash)));
        mPrintManager.addText(PrintingHelper.getTextBundle(Constant.LEFT_ALIGNED,true), "Input total cash      " + zeroChecker(f.format(inputCash)));
        printLine();
    }

    private void printNumOfTransactions(int numSuccessfulTransaction, int numReturnedTransaction) {
        mPrintManager.addText(PrintingHelper.getTextBundle(Constant.CENTER_ALIGNED,true),"Transactions number breakdown");
        printLine();
        bitmaps = new ArrayList<>();
        bitmaps.add(PrintingHelper.createBitmapFromText("Successful transactions"));
        bitmaps.add(PrintingHelper.createBitmapFromText(String.valueOf(numSuccessfulTransaction)));
        mPrintManager.addBitmap(PrintingHelper.combineMultipleBitmapsHorizontally(bitmaps,85),0);
        bitmaps = new ArrayList<>();
        bitmaps.add(PrintingHelper.createBitmapFromText("Refunded transactions"));
        bitmaps.add(PrintingHelper.createBitmapFromText(String.valueOf(numReturnedTransaction)));
        mPrintManager.addBitmap(PrintingHelper.combineMultipleBitmapsHorizontally(bitmaps,100),0);
        printLine();
    }

    private void printCardTypesBreakdown(HashMap<String, ShiftDifferences> shiftsCardTypesCalculations) {
        mPrintManager.addText(PrintingHelper.getTextBundle(Constant.CENTER_ALIGNED,true),"Card types breakdown");
        printLine();
        double totalCard = 0.0;
        for (Map.Entry<String, ShiftDifferences> shiftsCardTypeCalculations : shiftsCardTypesCalculations.entrySet()){
            if(!shiftsCardTypeCalculations.getKey().equalsIgnoreCase("cash")){
                mPrintManager.addTextLeft_Right(PrintingHelper.getTextBundle(Constant.LEFT_ALIGNED,true), shiftsCardTypeCalculations.getKey(), zeroChecker(f.format(shiftsCardTypeCalculations.getValue().getReal())));
                totalCard += Double.parseDouble(f.format(shiftsCardTypeCalculations.getValue().getReal()));
            }
        }
        printLine();
        mPrintManager.addTextLeft_Right(PrintingHelper.getTextBundle(Constant.LEFT_ALIGNED,true), "Total", zeroChecker(String.valueOf(totalCard)));
    }

    private void printPaymentDetails(double totalCash, double totalCard) {
        mPrintManager.addText(PrintingHelper.getTextBundle(Constant.CENTER_ALIGNED,true),"Payment details");
        printLine();
        double totalPayments = 0.0;
        bitmaps = new ArrayList<>();
        bitmaps.add(PrintingHelper.createBitmapFromText("Total cash"));
        bitmaps.add(PrintingHelper.createBitmapFromText(zeroChecker(f.format(totalCash))));
        mPrintManager.addBitmap(PrintingHelper.combineMultipleBitmapsHorizontally(bitmaps,100),0);
        totalPayments += Double.parseDouble(f.format(totalCash));
        totalPayments += printTotalCard(totalCard);
        printLine();
        bitmaps = new ArrayList<>();
        bitmaps.add(PrintingHelper.createBitmapFromText("Total"));
        bitmaps.add(PrintingHelper.createBitmapFromText(zeroChecker(String.valueOf(totalPayments))));
        mPrintManager.addBitmap(PrintingHelper.combineMultipleBitmapsHorizontally(bitmaps,150),0);
        printLine();
    }

    private void printStartAndLeaveCash(double startCash, double leaveCash) {

        bitmaps = new ArrayList<>();
        bitmaps.add(PrintingHelper.createBitmapFromText("Start cash"));
        bitmaps.add(PrintingHelper.createBitmapFromText(zeroChecker(f.format(startCash))));
        mPrintManager.addBitmap(PrintingHelper.combineMultipleBitmapsHorizontally(bitmaps,110),0);
        bitmaps = new ArrayList<>();
        bitmaps.add(PrintingHelper.createBitmapFromText("Leave cash"));
        bitmaps.add(PrintingHelper.createBitmapFromText(zeroChecker(f.format(leaveCash))));
        mPrintManager.addBitmap(PrintingHelper.combineMultipleBitmapsHorizontally(bitmaps,100),0);
        printLine();
    }

    private void printStartAndEndShiftTime(long startDateTime, long endDateTime) {
        mPrintManager.addTextOnlyLeft(PrintingHelper.getTextBundle(Constant.LEFT_ALIGNED,true), "Shift start date time");
        mPrintManager.addText(PrintingHelper.getTextBundle(Constant.CENTER_ALIGNED,true),printReportDateTime(getDateTime(startDateTime)));
        mPrintManager.addTextOnlyLeft(PrintingHelper.getTextBundle(Constant.LEFT_ALIGNED,true), "Shift end date time");
        mPrintManager.addText(PrintingHelper.getTextBundle(Constant.CENTER_ALIGNED,true),printReportDateTime(getDateTime(endDateTime)));
    }

    @SuppressLint("NewApi")
    private String printReportDateTime(LocalDateTime dateTime) {
        return dateTime.toString().replace("T","  ") + " " + dateTime.format(formatter);
    }

    private double printTotalCard(double totalCard) {
        bitmaps = new ArrayList<>();
        bitmaps.add(PrintingHelper.createBitmapFromText("Total card"));
        bitmaps.add(PrintingHelper.createBitmapFromText(zeroChecker(String.valueOf(totalCard))));
        mPrintManager.addBitmap(PrintingHelper.combineMultipleBitmapsHorizontally(bitmaps,105),0);
        return totalCard;
    }

    private static LocalDateTime getDateTime(Long dateTimeMillisecond) {
        Instant instant = Instant.ofEpochMilli(dateTimeMillisecond);
        LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        return localDateTime;
    }

    private void printZatcaQrCode(DatabaseAccess databaseAccess) {
        ZatcaQRCodeGeneration zatcaQRCodeGeneration = new ZatcaQRCodeGeneration();
        mPrintManager.addBitmap(zatcaQRCodeGeneration.getQrCodeBitmap(orderList,databaseAccess,orderDetailsList,configuration),70);
    }

    private void printTotalIncludingTax(double priceAfterTax) {
        bitmaps = new ArrayList<>();
        bitmaps.add(PrintingHelper.createBitmapFromText(priceAfterTax != 0 ? f.format(priceAfterTax) : "0.0"));
        bitmaps.add(PrintingHelper.createBitmapFromText("الإجمالى النهائى"));
        mPrintManager.addBitmap(PrintingHelper.combineMultipleBitmapsHorizontally(bitmaps,40),0);
        printLine();
    }

    private void printTax(String tax) {
        bitmaps = new ArrayList<>();
        bitmaps.add(PrintingHelper.createBitmapFromText(Double.parseDouble(tax) != 0 ?f.format(Double.parseDouble(tax)) : "0.0"));
        bitmaps.add(PrintingHelper.createBitmapFromText("ضريبة القيمة المضافة"));
        mPrintManager.addBitmap(PrintingHelper.combineMultipleBitmapsHorizontally(bitmaps,40),0);
        printLine();
    }

    private void printDiscount(String discount) {
        bitmaps = new ArrayList<>();
        bitmaps.add(PrintingHelper.createBitmapFromText(Double.parseDouble(discount) != 0 ?f.format(Double.parseDouble(discount)) : "0.0"));
        bitmaps.add(PrintingHelper.createBitmapFromText("الخصم"));
        mPrintManager.addBitmap(PrintingHelper.combineMultipleBitmapsHorizontally(bitmaps,70),0);
        printLine();
    }

    private void printTotalExcludingTax(double priceBeforeTax) {
        bitmaps = new ArrayList<>();
        bitmaps.add(PrintingHelper.createBitmapFromText(priceBeforeTax != 0 ? f.format(priceBeforeTax) : "0.0"));
        bitmaps.add(PrintingHelper.createBitmapFromText("الإجمالى قبل الضريبة"));
        mPrintManager.addBitmap(PrintingHelper.combineMultipleBitmapsHorizontally(bitmaps,40),0);
        printLine();
    }

    private void printProducts(List<HashMap<String, String>> orderDetailsList) {
        bitmaps = new ArrayList<>();
        bitmaps.add(PrintingHelper.createBitmapFromText("الإجمالى"));
        bitmaps.add(PrintingHelper.createBitmapFromText("السعر"));
        bitmaps.add(PrintingHelper.createBitmapFromText("الكمية"));
//        bitmaps.add(PrintingHelper.createBitmapFromText("بيان الصنف"));
        mPrintManager.addBitmap(PrintingHelper.combineMultipleBitmapsHorizontally(bitmaps,40),0);
        printLine();
        bitmaps = new ArrayList<>();
        for (int i = 0; i < orderDetailsList.size(); i++) {
            productCode = orderDetailsList.get(i).get("product_uuid");
            name = orderDetailsList.get(i).get("product_name_ar");
            price = f.format(Double.parseDouble(orderDetailsList.get(i).get("product_price")));
            qty = orderDetailsList.get(i).get("product_qty");
            productTotalPrice = Double.parseDouble(price) * Integer.parseInt(qty);
            bitmaps = new ArrayList<>();
            String total=productTotalPrice==0?"0.0":f.format(productTotalPrice);
            bitmaps.add(PrintingHelper.createBitmapFromText(total));
            bitmaps.add(PrintingHelper.createBitmapFromText(price));
            bitmaps.add(PrintingHelper.createBitmapFromText(qty));
            mPrintManager.addBitmap(PrintingHelper.combineMultipleBitmapsHorizontally(bitmaps,50),0);
            mPrintManager.addBitmap(PrintingHelper.createBitmapFromText(name),150);
            printLine();
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

    private void printType(String type) {
        if (!type.equals("invoice")&&!type.isEmpty())
            mPrintManager.addText(PrintingHelper.getTextBundle(Constant.CENTER_ALIGNED, true),type);
    }

    private void printMerchantTaxNumber(String merchantTaxNumber) {
        bitmaps = new ArrayList<>();
        bitmaps.add(PrintingHelper.createBitmapFromText(merchantTaxNumber));
        bitmaps.add(PrintingHelper.createBitmapFromText("الرقم الضريبى :"));
        mPrintManager.addBitmap(PrintingHelper.combineMultipleBitmapsHorizontally(bitmaps,15),70);
    }

    private void printMerchantId(String merchantId) {
        bitmaps = new ArrayList<>();
        bitmaps.add(PrintingHelper.createBitmapFromText(merchantId));
        bitmaps.add(PrintingHelper.createBitmapFromText("رقم السجل التجارى :"));
        mPrintManager.addBitmap(PrintingHelper.combineMultipleBitmapsHorizontally(bitmaps,45),70);
    }

    private void printLine(){
        mPrintManager.addText(PrintingHelper.getTextBundle(Constant.LEFT_ALIGNED,true),"----------------------------------------");
    }

    private String zeroChecker(String valueToBePrinted){
        return valueToBePrinted.equalsIgnoreCase(".00") || valueToBePrinted.equalsIgnoreCase("0.0")? "0" : valueToBePrinted;
    }
}
