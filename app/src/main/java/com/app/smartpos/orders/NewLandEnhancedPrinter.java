package com.app.smartpos.orders;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

import com.app.smartpos.Constant;
import com.app.smartpos.database.DatabaseAccess;
import com.app.smartpos.settings.end_shift.EndShiftModel;
import com.app.smartpos.settings.end_shift.ShiftDifferences;
import com.app.smartpos.utils.BaseActivity;
import com.app.smartpos.utils.printing.PrintingHelper;
import com.app.smartpos.utils.qrandbrcodegeneration.BarcodeEncoder;
import com.app.smartpos.utils.qrandbrcodegeneration.ZatcaQRCodeGeneration;
import com.app.smartpos.utils.qrandbrcodegeneration.ZatcaQRCodeGenerationService;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.newland.sdk.me.module.printer.ErrorCode;
import com.newland.sdk.me.module.printer.ModuleManage;
import com.newland.sdk.me.module.printer.PrintListener;
import com.newland.sdk.me.module.printer.PrinterModule;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class NewLandEnhancedPrinter extends BaseActivity {
    String name, price, qty;
    double productTotalPrice;
    DecimalFormat f;
    List<HashMap<String, String>> orderDetailsList;
    HashMap<String, String> orderList;
    HashMap<String, String> configuration;
    String merchantTaxNumber, merchantId, productCode;
    PrinterModule mPrintManager = null;
    SimpleDateFormat sdf1 = new SimpleDateFormat(Constant.REPORT_DATETIME_FORMAT);
    ZatcaQRCodeGenerationService zatcaQRCodeGenerationService = new ZatcaQRCodeGenerationService();
    List<PrinterModel> bitmaps = new LinkedList<>();
    int totalHeight = 0;
    int width = 0;
    String line = "--------------------------------------------";
    //DateTimeFormatter formatter = DateTimeFormatter.ofPattern("a");

    public NewLandEnhancedPrinter() {
        ModuleManage.getInstance().init();
        mPrintManager = ModuleManage.getInstance().getPrinterModule();
    }

    private String getDateTime(Long dateTimeMillisecond) {
        Date date = new Date(dateTimeMillisecond);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ", Locale.UK);
        String formattedDate = sdf.format(date);
        return formattedDate;
    }

    public boolean printReceipt(String invoiceId, String orderDate, String orderTime, double priceBeforeTax, double priceAfterTax, String tax, String discount, String currency, String printType) {
        DatabaseAccess databaseAccess = DatabaseAccess.getInstance(NewLandEnhancedPrinter.this);
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
            byte[] decodedString = PrintingHelper.base64ToByteArray(configuration.isEmpty() ? "" : configuration.get("merchant_logo"));
            Bitmap logo = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            bitmaps.add(new PrinterModel(0, logo));
            printMerchantId(merchantId);
            printMerchantTaxNumber(merchantTaxNumber);
            bitmaps.add(new PrinterModel(PrintingHelper.createBitmapFromText(orderDate), PrintingHelper.createBitmapFromText(orderTime)));
            //mPrintManager.addTextLeft_Right(PrintingHelper.getTextBundle(Constant.CENTER_ALIGNED,true), orderDate, orderTime);
            printReceiptNo(invoiceId);
            printType(printType);
            printInvoiceBarcode(invoiceId);
            //Todo products ( id, name, price including tax, qty, total including tax
            printProducts(orderDetailsList);
            printTotalExcludingTax(priceBeforeTax);
            printDiscount(discount);
            printTax(Double.parseDouble(tax));
            printTotalIncludingTax(priceAfterTax);
            printPaidAndChangeAmount(orderList.get("paid_amount"), priceAfterTax, orderList.get("change_amount"), orderList.get("order_payment_method"));
            //Todo total paid
//        mPrintManager.addTextLeft_Center_Right(PrintingHelper.getTextBundle(), f.format(), handleArabicText("إجمالى المدفوع").toString(), "");
            //Todo needs to be paid
//        mPrintManager.addTextLeft_Center_Right(PrintingHelper.getTextBundle(), f.format(), handleArabicText("الصافى").toString(), "");
            //Todo remaining
//        mPrintManager.addTextLeft_Center_Right(PrintingHelper.getTextBundle(), f.format(), handleArabicText("الباقى").toString(), "");
            printZatcaQrCode(databaseAccess);
            printLine();
            Map<String, Bitmap> bitmapResult = new HashMap<>();
            String bitmapName1 = "logo";
            bitmapResult.put(bitmapName1, creatGeneralBitmap());

            StringBuffer printDara = new StringBuffer();
            printDara.append("*image c " + width + "*" + totalHeight + " path:" + bitmapName1 + "\n");
            printDara.append("!hz s\n!asc s\n");
            mPrintManager.print(printDara.toString(), bitmapResult, new PrintListener() {
                @Override
                public void onSuccess() {
                    Log.i("datadata", "success");
                }

                @Override
                public void onError(ErrorCode errorCode, String s) {
                    Log.i("datadata_error", "error " + errorCode + " " + s);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;

    }

    @SuppressLint("NewApi")
    public boolean printZReport(EndShiftModel endShiftModel) {
        f = new DecimalFormat("#.00");
        try {

            bitmaps.add(new PrinterModel(0, PrintingHelper.createBitmapFromText("Z Report")));
            bitmaps.add(new PrinterModel(0, PrintingHelper.createBitmapFromText(getDateTime(new Date().getTime()))));
            printLine();
            bitmaps.add(new PrinterModel(0, PrintingHelper.createBitmapFromText("Shift no  " + endShiftModel.getSequence())));
            bitmaps.add(new PrinterModel(-1, PrintingHelper.createBitmapFromText("Username   " + endShiftModel.getUserName())));

            printStartAndEndShiftTime(endShiftModel.getStartDateTime(), endShiftModel.getEndDateTime());
            printLine();
            printStartAndLeaveCash(endShiftModel.getStartCash(), endShiftModel.getLeaveCash());
            printNumOfTransactions(endShiftModel.getNum_successful_transaction(), endShiftModel.getNum_returned_transaction());
            printTransactionsAmount(endShiftModel.getTotal_amount() - endShiftModel.getTotalRefundsAmount(), endShiftModel.getTotalRefundsAmount() * -1);
            printCashDiscrepancies(endShiftModel.getTotal_amount() - endShiftModel.getTotalCardsAmount(), Objects.requireNonNull(endShiftModel.getShiftDifferences().get("CASH")).getInput());
            printPaymentDetails(endShiftModel.getTotal_amount() - endShiftModel.getTotalCardsAmount(), endShiftModel.getTotalCardsAmount());
            printCardTypesBreakdown(endShiftModel.getShiftDifferences());
            printLine();

            Map<String, Bitmap> bitmapResult = new HashMap<>();
            String bitmapName1 = "logo";
            bitmapResult.put(bitmapName1, creatGeneralBitmap());

            StringBuffer printDara = new StringBuffer();
            printDara.append("*image c " + width + "*" + totalHeight + " path:" + bitmapName1 + "\n");
            printDara.append("!hz s\n!asc s\n");
            mPrintManager.print(printDara.toString(), bitmapResult, new PrintListener() {
                @Override
                public void onSuccess() {
                    Log.i("datadata", "success");
                }

                @Override
                public void onError(ErrorCode errorCode, String s) {
                    Log.i("datadata_error", "error " + errorCode + " " + s);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    private void printTransactionsAmount(double totalAmount, double totalRefundsAmount) {
        bitmaps.add(new PrinterModel(0, PrintingHelper.createBitmapFromText("Transactions amount breakdown")));
        printLine();
        ArrayList<Bitmap> combinedBitmaps1 = new ArrayList<>();
        combinedBitmaps1.add(PrintingHelper.createBitmapFromText("Sales amount"));
        combinedBitmaps1.add(PrintingHelper.createBitmapFromText(String.valueOf(totalAmount)));
        bitmaps.add(new PrinterModel(-1, PrintingHelper.combineMultipleBitmapsHorizontally(combinedBitmaps1, 140)));

        ArrayList<Bitmap> combinedBitmaps2 = new ArrayList<>();
        combinedBitmaps2.add(PrintingHelper.createBitmapFromText("Refunded amount"));
        combinedBitmaps2.add(PrintingHelper.createBitmapFromText(String.valueOf(totalRefundsAmount)));
        bitmaps.add(new PrinterModel(-1, PrintingHelper.combineMultipleBitmapsHorizontally(combinedBitmaps2, 100)));
        printLine();
        ArrayList<Bitmap> combinedBitmaps3 = new ArrayList<>();
        combinedBitmaps3.add(PrintingHelper.createBitmapFromText("Total"));
        combinedBitmaps3.add(PrintingHelper.createBitmapFromText(zeroChecker(String.valueOf(totalAmount - totalRefundsAmount))));
        bitmaps.add(new PrinterModel(-1, PrintingHelper.combineMultipleBitmapsHorizontally(combinedBitmaps3, 200)));
        printLine();
    }

    private void printCashDiscrepancies(double totalCash, double inputCash) {
        bitmaps.add(new PrinterModel(0, PrintingHelper.createBitmapFromText("Cash discrepancies")));
        printLine();
        bitmaps.add(new PrinterModel(-1, PrintingHelper.createBitmapFromText("Actual total cash   " + zeroChecker(f.format(totalCash)))));
        bitmaps.add(new PrinterModel(-1, PrintingHelper.createBitmapFromText("Input total cash      " + zeroChecker(f.format(inputCash)))));

        printLine();
    }

    private void printNumOfTransactions(int numSuccessfulTransaction, int numReturnedTransaction) {
        bitmaps.add(new PrinterModel(0, PrintingHelper.createBitmapFromText("Transactions number breakdown")));

        printLine();
        ArrayList<Bitmap> combinedBitmaps1 = new ArrayList<>();
        combinedBitmaps1.add(PrintingHelper.createBitmapFromText("Successful transactions"));
        combinedBitmaps1.add(PrintingHelper.createBitmapFromText(String.valueOf(numSuccessfulTransaction)));
        bitmaps.add(new PrinterModel(-1, PrintingHelper.combineMultipleBitmapsHorizontally(combinedBitmaps1, 85)));

        ArrayList<Bitmap> combinedBitmaps2 = new ArrayList<>();
        combinedBitmaps2.add(PrintingHelper.createBitmapFromText("Refunded transactions"));
        combinedBitmaps2.add(PrintingHelper.createBitmapFromText(String.valueOf(numReturnedTransaction)));
        bitmaps.add(new PrinterModel(-1, PrintingHelper.combineMultipleBitmapsHorizontally(combinedBitmaps2, 100)));

        printLine();
    }

    private void printCardTypesBreakdown(HashMap<String, ShiftDifferences> shiftsCardTypesCalculations) {
        bitmaps.add(new PrinterModel(0, PrintingHelper.createBitmapFromText("Card types breakdown")));

        printLine();
        double totalCard = 0.0;
        for (Map.Entry<String, ShiftDifferences> shiftsCardTypeCalculations : shiftsCardTypesCalculations.entrySet()) {
            if (!shiftsCardTypeCalculations.getKey().equalsIgnoreCase("cash")) {
                ArrayList<Bitmap> combinedBitmaps1 = new ArrayList<>();
                combinedBitmaps1.add(PrintingHelper.createBitmapFromText(shiftsCardTypeCalculations.getKey()));
                combinedBitmaps1.add(PrintingHelper.createBitmapFromText(zeroChecker(f.format(shiftsCardTypeCalculations.getValue().getReal()))));
                //bitmaps.add(new PrinterModel(-1, PrintingHelper.combineMultipleBitmapsHorizontally(combinedBitmaps1, 100)));
                bitmaps.add(new PrinterModel(combinedBitmaps1.get(0), combinedBitmaps1.get(1)));
                totalCard += Double.parseDouble(f.format(shiftsCardTypeCalculations.getValue().getReal()));
            }
        }
        printLine();
        ArrayList<Bitmap> combinedBitmaps1 = new ArrayList<>();
        combinedBitmaps1.add(PrintingHelper.createBitmapFromText("Total"));
        combinedBitmaps1.add(PrintingHelper.createBitmapFromText(zeroChecker(String.valueOf(totalCard))));
        //bitmaps.add(new PrinterModel(-1, PrintingHelper.combineMultipleBitmapsHorizontally(combinedBitmaps1, 100)));
        bitmaps.add(new PrinterModel(combinedBitmaps1.get(0), combinedBitmaps1.get(1)));
    }

    private void printPaymentDetails(double totalCash, double totalCard) {
        bitmaps.add(new PrinterModel(0, PrintingHelper.createBitmapFromText("Payment details")));

        printLine();
        double totalPayments = 0.0;
        ArrayList<Bitmap> combinedBitmaps1 = new ArrayList<>();
        combinedBitmaps1.add(PrintingHelper.createBitmapFromText("Total cash"));
        combinedBitmaps1.add(PrintingHelper.createBitmapFromText(zeroChecker(f.format(totalCash))));
        bitmaps.add(new PrinterModel(-1, PrintingHelper.combineMultipleBitmapsHorizontally(combinedBitmaps1, 100)));

        totalPayments += Double.parseDouble(f.format(totalCash));
        totalPayments += printTotalCard(totalCard);
        printLine();
        ArrayList<Bitmap> combinedBitmaps2 = new ArrayList<>();
        combinedBitmaps2.add(PrintingHelper.createBitmapFromText("Total"));
        combinedBitmaps2.add(PrintingHelper.createBitmapFromText(zeroChecker(f.format(totalPayments))));
        bitmaps.add(new PrinterModel(-1, PrintingHelper.combineMultipleBitmapsHorizontally(combinedBitmaps2, 150)));

        printLine();
    }

    private void printStartAndLeaveCash(double startCash, double leaveCash) {

        ArrayList<Bitmap> combinedBitmaps1 = new ArrayList<>();
        combinedBitmaps1.add(PrintingHelper.createBitmapFromText("Start cash"));
        combinedBitmaps1.add(PrintingHelper.createBitmapFromText(zeroChecker(f.format(startCash))));
        bitmaps.add(new PrinterModel(combinedBitmaps1.get(0), combinedBitmaps1.get(1)));

        ArrayList<Bitmap> combinedBitmaps2 = new ArrayList<>();
        combinedBitmaps2.add(PrintingHelper.createBitmapFromText("Leave cash"));
        combinedBitmaps2.add(PrintingHelper.createBitmapFromText(zeroChecker(f.format(leaveCash))));
        bitmaps.add(new PrinterModel(combinedBitmaps2.get(0), combinedBitmaps2.get(1)));

        printLine();
    }

    private void printStartAndEndShiftTime(long startDateTime, long endDateTime) {
        bitmaps.add(new PrinterModel(-1, PrintingHelper.createBitmapFromText("Shift start date time")));
        bitmaps.add(new PrinterModel(0, PrintingHelper.createBitmapFromText(getDateTime(startDateTime))));

        bitmaps.add(new PrinterModel(-1, PrintingHelper.createBitmapFromText("Shift end date time")));
        bitmaps.add(new PrinterModel(0, PrintingHelper.createBitmapFromText(getDateTime(endDateTime))));

    }

    private double printTotalCard(double totalCard) {
        ArrayList<Bitmap> combinedBitmaps = new ArrayList<>();
        combinedBitmaps.add(PrintingHelper.createBitmapFromText("Total card"));
        combinedBitmaps.add(PrintingHelper.createBitmapFromText(zeroChecker(String.valueOf(totalCard))));
        bitmaps.add(new PrinterModel(-1, PrintingHelper.combineMultipleBitmapsHorizontally(combinedBitmaps, 105)));
        return totalCard;
    }

    public Bitmap creatGeneralBitmap() {
        totalHeight = 0;
        int size = bitmaps.size();
        for (int i = 0; i < size; i++) {
            Bitmap bitmap = bitmaps.get(i).getBitmap();
            totalHeight += bitmap.getHeight();
            if (bitmap.getWidth() > width) {
                width = bitmap.getWidth();
            }
        }
        //Log.i("datadata", totalHeight + "");
        Bitmap.Config conf = Bitmap.Config.ARGB_4444; // see other conf types
        Bitmap bmp = Bitmap.createBitmap(width, totalHeight, conf); // this creates a MUTABLE bitmap
        Canvas canvas = new Canvas(bmp);
        canvas.drawColor(Color.WHITE);
        int lastY = 0;
        for (int i = 0; i < size; i++) {
            Bitmap bitmap = bitmaps.get(i).getBitmap();
            if (bitmaps.get(i).type == 1) {
                int startX = 0;
                if (bitmaps.get(i).getSide() == 0) {
                    startX = width / 2 - bitmap.getWidth() / 2;
                } else if (bitmaps.get(i).getSide() == 1) {
                    startX = width - bitmap.getWidth();
                }
                canvas.drawBitmap(bitmaps.get(i).getBitmap(), startX, lastY, new Paint());
            } else if (bitmaps.get(i).type == 2) {
                Bitmap bitmap2 = bitmaps.get(i).bitmap2;
                canvas.drawBitmap(bitmaps.get(i).getBitmap(), 0, lastY, new Paint());
                canvas.drawBitmap(bitmaps.get(i).bitmap2, width - bitmap2.getWidth(), lastY, new Paint());
            } else if (bitmaps.get(i).type == 3) {
                Bitmap bitmap2 = bitmaps.get(i).bitmap2;
                Bitmap bitmap3 = bitmaps.get(i).bitmap3;
                canvas.drawBitmap(bitmaps.get(i).getBitmap(), 0, lastY, new Paint());
                canvas.drawBitmap(bitmaps.get(i).bitmap2, (int) ((double) width / 2.0 - (double) bitmap2.getWidth() / 2.0), lastY, new Paint());
                canvas.drawBitmap(bitmaps.get(i).bitmap3, width - bitmap3.getWidth(), lastY, new Paint());

            }
            lastY += bitmap.getHeight();
        }
        return bmp;
    }

    private void printZatcaQrCode(DatabaseAccess databaseAccess) {
        ZatcaQRCodeGeneration zatcaQRCodeGeneration = new ZatcaQRCodeGeneration();
        bitmaps.add(new PrinterModel(0, zatcaQRCodeGeneration.getQrCodeBitmap(orderList, databaseAccess, orderDetailsList, configuration, true)));
    }

    private void printTotalIncludingTax(double priceAfterTax) {
        List<Bitmap> newBitmaps = new ArrayList<>();
        newBitmaps.add(PrintingHelper.createBitmapFromText(priceAfterTax != 0 ? f.format(priceAfterTax) : "0.0"));
        newBitmaps.add(PrintingHelper.createBitmapFromText("الإجمالى النهائى"));
        bitmaps.add(new PrinterModel(newBitmaps.get(0), newBitmaps.get(1)));
        bitmaps.add(new PrinterModel(-1, PrintingHelper.createBitmapFromText(line)));
    }

    private void printPaidAndChangeAmount(String paidAmount, double priceAfterTax, String changeAmount, String orderPaymentMethod) {
        if (orderPaymentMethod.equalsIgnoreCase("cash")) {
            List<Bitmap> newBitmaps1 = new ArrayList<>();
            newBitmaps1.add(PrintingHelper.createBitmapFromText(f.format(Double.parseDouble(paidAmount) - Double.parseDouble(changeAmount))));
            newBitmaps1.add(PrintingHelper.createBitmapFromText("إجمالى المدفوع"));
            bitmaps.add(new PrinterModel(newBitmaps1.get(0), newBitmaps1.get(1)));
            printLine();
            List<Bitmap> newBitmaps2 = new ArrayList<>();
            newBitmaps2.add(PrintingHelper.createBitmapFromText(f.format(priceAfterTax)));
            newBitmaps2.add(PrintingHelper.createBitmapFromText("الصافى"));
            bitmaps.add(new PrinterModel(newBitmaps2.get(0), newBitmaps2.get(1)));
            printLine();
            List<Bitmap> newBitmaps3 = new ArrayList<>();
            newBitmaps3.add(PrintingHelper.createBitmapFromText(f.format(-1 * Double.parseDouble(changeAmount))));
            newBitmaps3.add(PrintingHelper.createBitmapFromText("الباقى"));
            bitmaps.add(new PrinterModel(newBitmaps3.get(0), newBitmaps3.get(1)));
            printLine();
        }
    }

    private void printTax(double tax) {
        List<Bitmap> newBitmaps = new ArrayList<>();
        newBitmaps.add(PrintingHelper.createBitmapFromText(tax != 0 ? f.format(tax) : "0.0"));
        newBitmaps.add(PrintingHelper.createBitmapFromText("ضريبة القيمة المضافة"));
        bitmaps.add(new PrinterModel(newBitmaps.get(0), newBitmaps.get(1)));
        bitmaps.add(new PrinterModel(-1, PrintingHelper.createBitmapFromText(line)));
    }

    private void printDiscount(String discount) {
        List<Bitmap> newBitmaps = new ArrayList<>();
        newBitmaps.add(PrintingHelper.createBitmapFromText(Double.parseDouble(discount) != 0 ? f.format(discount) : "0.0"));
        newBitmaps.add(PrintingHelper.createBitmapFromText("الخصم"));
        bitmaps.add(new PrinterModel(newBitmaps.get(0), newBitmaps.get(1)));
        bitmaps.add(new PrinterModel(-1, PrintingHelper.createBitmapFromText(line)));
    }

    private void printTotalExcludingTax(double priceBeforeTax) {
        List<Bitmap> newBitmaps = new ArrayList<>();
        newBitmaps.add(PrintingHelper.createBitmapFromText(priceBeforeTax != 0 ? f.format(priceBeforeTax) : "0.0"));
        newBitmaps.add(PrintingHelper.createBitmapFromText("الإجمالى قبل الضريبة"));
        bitmaps.add(new PrinterModel(newBitmaps.get(0), newBitmaps.get(1)));
        bitmaps.add(new PrinterModel(-1, PrintingHelper.createBitmapFromText(line)));
    }

    private void printProducts(List<HashMap<String, String>> orderDetailsList) {
        bitmaps.add(new PrinterModel(-1, PrintingHelper.createBitmapFromText(line)));
        List<Bitmap> newBitmaps = new ArrayList<>();
        newBitmaps.add(PrintingHelper.createBitmapFromText("الإجمالى"));
        newBitmaps.add(PrintingHelper.createBitmapFromText("السعر"));
        newBitmaps.add(PrintingHelper.createBitmapFromText("الكمية"));
        //newBitmaps.add(PrintingHelper.createBitmapFromText("    "));
        bitmaps.add(new PrinterModel(newBitmaps.get(0), newBitmaps.get(1), newBitmaps.get(2)));
        bitmaps.add(new PrinterModel(-1, PrintingHelper.createBitmapFromText(line)));

        for (int i = 0; i < orderDetailsList.size(); i++) {
            //productCode = orderDetailsList.get(i).get("product_uuid");
            name = orderDetailsList.get(i).get("product_name_ar");
            price = orderDetailsList.get(i).get("product_price");
            qty = orderDetailsList.get(i).get("product_qty");
            productTotalPrice = Double.parseDouble(price) * Integer.parseInt(qty);
            List<Bitmap> ProductBitmap = new ArrayList<>();
            ProductBitmap.add(PrintingHelper.createBitmapFromText(f.format(productTotalPrice)));
            ProductBitmap.add(PrintingHelper.createBitmapFromText(f.format(Double.parseDouble(price))));
            ProductBitmap.add(PrintingHelper.createBitmapFromText(qty));
            //ProductBitmap.add(PrintingHelper.createBitmapFromText("    "));
            bitmaps.add(new PrinterModel(ProductBitmap.get(0), ProductBitmap.get(1), ProductBitmap.get(2)));
            bitmaps.add(new PrinterModel(1, PrintingHelper.createBitmapFromText(name)));
            bitmaps.add(new PrinterModel(-1, PrintingHelper.createBitmapFromText(line)));
        }
    }

    private void printInvoiceBarcode(String invoiceId) throws WriterException {
        BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
        bitmaps.add(new PrinterModel(0, barcodeEncoder.encodeQrOrBc(invoiceId, BarcodeFormat.CODE_128, 400, 100)));
    }

    private void printReceiptNo(String invoiceId) {
        List<Bitmap> newBitmaps = new ArrayList<>();
        newBitmaps.add(PrintingHelper.createBitmapFromText("Receipt No "));
        newBitmaps.add(PrintingHelper.createBitmapFromText(invoiceId));
        bitmaps.add(new PrinterModel(newBitmaps.get(0), newBitmaps.get(1)));
    }

    private void printType(String type) {
        if (!type.equals("invoice") && !type.isEmpty())
            bitmaps.add(new PrinterModel(0, PrintingHelper.createBitmapFromText(type)));
        else {
            bitmaps.add(new PrinterModel(0, PrintingHelper.createBitmapFromText("فاتورة ضريبية مبسطة")));
        }
    }

    private void printMerchantTaxNumber(String merchantTaxNumber) {
        List<Bitmap> newBitmaps = new ArrayList<>();
        newBitmaps.add(PrintingHelper.createBitmapFromText(merchantTaxNumber));
        newBitmaps.add(PrintingHelper.createBitmapFromText("رقم السجل التجارى :"));
        bitmaps.add(new PrinterModel(newBitmaps.get(0), newBitmaps.get(1)));
    }

    private void printMerchantId(String merchantId) {
        List<Bitmap> newBitmaps = new ArrayList<>();
        newBitmaps.add(PrintingHelper.createBitmapFromText(merchantId));
        newBitmaps.add(PrintingHelper.createBitmapFromText("الرقم الضريبى :"));
        bitmaps.add(new PrinterModel(newBitmaps.get(0), newBitmaps.get(1)));
    }


    private String zeroChecker(String valueToBePrinted) {
        return valueToBePrinted.equalsIgnoreCase(".00") || valueToBePrinted.equalsIgnoreCase("0.0") ? "0" : valueToBePrinted;
    }

    private void printLine() {
        bitmaps.add(new PrinterModel(-1, PrintingHelper.createBitmapFromText("----------------------------------------")));
    }
}
