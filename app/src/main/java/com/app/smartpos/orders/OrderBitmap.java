package com.app.smartpos.orders;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.app.smartpos.Constant;
import com.app.smartpos.R;
import com.app.smartpos.common.Utils;
import com.app.smartpos.database.DatabaseAccess;
import com.app.smartpos.settings.end_shift.EndShiftModel;
import com.app.smartpos.settings.end_shift.ShiftDifferences;
import com.app.smartpos.utils.BaseActivity;
import com.app.smartpos.utils.LocaleManager;
import com.app.smartpos.utils.MultiLanguageApp;
import com.app.smartpos.utils.printing.PrintingHelper;
import com.app.smartpos.utils.qrandbrcodegeneration.BarcodeEncoder;
import com.app.smartpos.utils.qrandbrcodegeneration.ZatcaQRCodeGeneration;
import com.app.smartpos.utils.qrandbrcodegeneration.ZatcaQRCodeGenerationService;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.newland.sdk.me.module.printer.PrinterModule;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class OrderBitmap extends BaseActivity {
    String name, price, qty;
    double productTotalPrice;
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
    MultiLanguageApp activity;
    public OrderBitmap(Activity activity) {
        this.activity=MultiLanguageApp.getApp();
    }


    public Bitmap orderBitmap(String invoiceId, String orderDate, String orderTime, double priceBeforeTax, double priceAfterTax, double tax, String discount, String currency, String printType) {
        DatabaseAccess databaseAccess = DatabaseAccess.getInstance(OrderBitmap.this);
        databaseAccess.open();
        configuration = databaseAccess.getConfiguration();
        merchantTaxNumber = configuration.isEmpty() ? "" : configuration.get("merchant_tax_number");
        merchantId = configuration.isEmpty() ? "" : configuration.get("merchant_id");
        databaseAccess.open();
        orderDetailsList = databaseAccess.getOrderDetailsList(invoiceId);
        databaseAccess.open();
        orderList = databaseAccess.getOrderListByOrderId(invoiceId);
        try {
            if (!configuration.get("merchant_logo").isEmpty()) {
                byte[] decodedString = PrintingHelper.base64ToByteArray(configuration.isEmpty() ? "" : configuration.get("merchant_logo"));
                Bitmap logo = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                bitmaps.add(new PrinterModel(0, logo));
            }
            printMerchantId(merchantId);
            printMerchantTaxNumber(merchantTaxNumber);
            bitmaps.add(new PrinterModel(PrintingHelper.createBitmapFromText(orderDate), PrintingHelper.createBitmapFromText(orderTime)));
            //mPrintManager.addTextLeft_Right(PrintingHelper.getTextBundle(Constant.CENTER_ALIGNED,true), orderDate, orderTime);
            printReceiptNo(invoiceId);
            printType(printType);
            //bitmaps.add(new PrinterModel(0, PrintingHelper.createBitmapFromText("فاتورة ضريبية مبسطة")));
            printInvoiceBarcode(invoiceId);
            //Todo products ( id, name, price including tax, qty, total including tax
            printProducts(orderDetailsList);
            printTotalExcludingTax(priceBeforeTax);
            printDiscount(discount);
            printTax(tax);
            printTotalIncludingTax(priceAfterTax);
            printPaidAndChangeAmount(orderList.get("paid_amount"), priceAfterTax, orderList.get("change_amount"), orderList.get("order_payment_method"));
            //Todo total paid
//        mPrintManager.addTextLeft_Center_Right(PrintingHelper.getTextBundle(), Utils.trimLongDouble(), handleArabicText("إجمالى المدفوع").toString(), "");
            //Todo needs to be paid
//        mPrintManager.addTextLeft_Center_Right(PrintingHelper.getTextBundle(), Utils.trimLongDouble(), handleArabicText("الصافى").toString(), "");
            //Todo remaining
//        mPrintManager.addTextLeft_Center_Right(PrintingHelper.getTextBundle(), Utils.trimLongDouble(), handleArabicText("الباقى").toString(), "");
            printZatcaQrCode(databaseAccess);
            printLine();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return creatGeneralBitmap();

    }

    @SuppressLint("NewApi")
    public Bitmap shiftZReport(EndShiftModel endShiftModel) {
        bitmaps.add(new PrinterModel(0, PrintingHelper.createBitmapFromText("Z Report")));
        bitmaps.add(new PrinterModel(0, PrintingHelper.createBitmapFromText(getDateTime(new Date().getTime()))));
        printLine();
        bitmaps.add(new PrinterModel(0, PrintingHelper.createBitmapFromText(activity.getString(R.string.shift_no) +" " + endShiftModel.getSequence())));
        bitmaps.add(new PrinterModel(-1, PrintingHelper.createBitmapFromText(activity.getString(R.string.username) +" " + endShiftModel.getUserName())));

        printStartAndEndShiftTime(endShiftModel.getStartDateTime(), endShiftModel.getEndDateTime());
        printLine();
        printStartAndLeaveCash(endShiftModel.getStartCash(), endShiftModel.getLeaveCash());
        printNumOfTransactions(endShiftModel.getNum_successful_transaction(), endShiftModel.getNum_returned_transaction());
        printTransactionsAmount(endShiftModel.getTotal_amount() - endShiftModel.getTotalRefundsAmount(), endShiftModel.getTotalRefundsAmount() * -1);
        printCashDiscrepancies(endShiftModel.getTotal_amount() - endShiftModel.getTotalCardsAmount(), Objects.requireNonNull(endShiftModel.getShiftDifferences().get("CASH")).getInput());
        printPaymentDetails(endShiftModel.getTotal_amount() - endShiftModel.getTotalCardsAmount(), endShiftModel.getTotalCardsAmount());
        printCardTypesBreakdown(endShiftModel.getShiftDifferences());
        printLine();


        return creatGeneralBitmap();
    }

    private void printTransactionsAmount(double totalAmount, double totalRefundsAmount) {
        bitmaps.add(new PrinterModel(0, PrintingHelper.createBitmapFromText(activity.getString(R.string.transactions_amount_breakdown))));
        printLine();
        ArrayList<Bitmap> combinedBitmaps1 = new ArrayList<>();
        combinedBitmaps1.add(PrintingHelper.createBitmapFromText(activity.getString(R.string.sales_amount)));
        combinedBitmaps1.add(PrintingHelper.createBitmapFromText(String.valueOf(totalAmount)));
        bitmaps.add(new PrinterModel(-1, PrintingHelper.combineMultipleBitmapsHorizontally(combinedBitmaps1, 140)));

        ArrayList<Bitmap> combinedBitmaps2 = new ArrayList<>();
        combinedBitmaps2.add(PrintingHelper.createBitmapFromText(activity.getString(R.string.refunded_amount)));
        combinedBitmaps2.add(PrintingHelper.createBitmapFromText(String.valueOf(totalRefundsAmount)));
        bitmaps.add(new PrinterModel(-1, PrintingHelper.combineMultipleBitmapsHorizontally(combinedBitmaps2, 100)));
        printLine();
        ArrayList<Bitmap> combinedBitmaps3 = new ArrayList<>();
        combinedBitmaps3.add(PrintingHelper.createBitmapFromText(activity.getString(R.string.total)));
        combinedBitmaps3.add(PrintingHelper.createBitmapFromText(zeroChecker(String.valueOf(totalAmount - totalRefundsAmount))));
        bitmaps.add(new PrinterModel(-1, PrintingHelper.combineMultipleBitmapsHorizontally(combinedBitmaps3, 200)));
        printLine();
    }

    private void printCashDiscrepancies(double totalCash, double inputCash) {
        bitmaps.add(new PrinterModel(0, PrintingHelper.createBitmapFromText(activity.getString(R.string.cash_discrepancies))));
        printLine();
        bitmaps.add(new PrinterModel(-1, PrintingHelper.createBitmapFromText(activity.getString(R.string.actual_total_cash)+"   " + zeroChecker(Utils.trimLongDouble(totalCash)))));
        bitmaps.add(new PrinterModel(-1, PrintingHelper.createBitmapFromText(activity.getString(R.string.input_total_cash)+"      " + zeroChecker(Utils.trimLongDouble(inputCash)))));

        printLine();
    }

    private void printNumOfTransactions(int numSuccessfulTransaction, int numReturnedTransaction) {
        bitmaps.add(new PrinterModel(0, PrintingHelper.createBitmapFromText(activity.getString(R.string.transactions_number_breakdown))));

        printLine();
        ArrayList<Bitmap> combinedBitmaps1 = new ArrayList<>();
        combinedBitmaps1.add(PrintingHelper.createBitmapFromText(activity.getString(R.string.successful_transactions)));
        combinedBitmaps1.add(PrintingHelper.createBitmapFromText(String.valueOf(numSuccessfulTransaction)));
        bitmaps.add(new PrinterModel(-1, PrintingHelper.combineMultipleBitmapsHorizontally(combinedBitmaps1, 85)));

        ArrayList<Bitmap> combinedBitmaps2 = new ArrayList<>();
        combinedBitmaps2.add(PrintingHelper.createBitmapFromText(activity.getString(R.string.refunded_transactions)));
        combinedBitmaps2.add(PrintingHelper.createBitmapFromText(String.valueOf(numReturnedTransaction)));
        bitmaps.add(new PrinterModel(-1, PrintingHelper.combineMultipleBitmapsHorizontally(combinedBitmaps2, 100)));

        printLine();
    }

    private void printCardTypesBreakdown(HashMap<String, ShiftDifferences> shiftsCardTypesCalculations) {
        bitmaps.add(new PrinterModel(0, PrintingHelper.createBitmapFromText(activity.getString(R.string.card_types_breakdown))));

        printLine();
        double totalCard = 0.00;
        for (Map.Entry<String, ShiftDifferences> shiftsCardTypeCalculations : shiftsCardTypesCalculations.entrySet()) {
            if (!shiftsCardTypeCalculations.getKey().equalsIgnoreCase(activity.getString(R.string.cash))) {
                ArrayList<Bitmap> combinedBitmaps1 = new ArrayList<>();
                combinedBitmaps1.add(PrintingHelper.createBitmapFromText(shiftsCardTypeCalculations.getKey()));
                combinedBitmaps1.add(PrintingHelper.createBitmapFromText(zeroChecker(Utils.trimLongDouble(shiftsCardTypeCalculations.getValue().getReal()))));
                //bitmaps.add(new PrinterModel(-1, PrintingHelper.combineMultipleBitmapsHorizontally(combinedBitmaps1, 100)));
                bitmaps.add(new PrinterModel(combinedBitmaps1.get(0), combinedBitmaps1.get(1)));
                totalCard += Double.parseDouble(Utils.trimLongDouble(shiftsCardTypeCalculations.getValue().getReal()));
            }
        }
        printLine();
        ArrayList<Bitmap> combinedBitmaps1 = new ArrayList<>();
        combinedBitmaps1.add(PrintingHelper.createBitmapFromText(activity.getString(R.string.total)));
        combinedBitmaps1.add(PrintingHelper.createBitmapFromText(zeroChecker(String.valueOf(totalCard))));
        //bitmaps.add(new PrinterModel(-1, PrintingHelper.combineMultipleBitmapsHorizontally(combinedBitmaps1, 100)));
        bitmaps.add(new PrinterModel(combinedBitmaps1.get(0), combinedBitmaps1.get(1)));
    }

    private void printPaymentDetails(double totalCash, double totalCard) {
        bitmaps.add(new PrinterModel(0, PrintingHelper.createBitmapFromText(activity.getString(R.string.payment_details))));

        printLine();
        double totalPayments = 0.00;
        ArrayList<Bitmap> combinedBitmaps1 = new ArrayList<>();
        combinedBitmaps1.add(PrintingHelper.createBitmapFromText(activity.getString(R.string.total_cash)));
        combinedBitmaps1.add(PrintingHelper.createBitmapFromText(zeroChecker(Utils.trimLongDouble(totalCash))));
        bitmaps.add(new PrinterModel(-1, PrintingHelper.combineMultipleBitmapsHorizontally(combinedBitmaps1, 100)));

        totalPayments += Double.parseDouble(Utils.trimLongDouble(totalCash));
        totalPayments += printTotalCard(totalCard);
        printLine();
        ArrayList<Bitmap> combinedBitmaps2 = new ArrayList<>();
        combinedBitmaps2.add(PrintingHelper.createBitmapFromText(activity.getString(R.string.total)));
        combinedBitmaps2.add(PrintingHelper.createBitmapFromText(zeroChecker(Utils.trimLongDouble(totalPayments))));
        bitmaps.add(new PrinterModel(-1, PrintingHelper.combineMultipleBitmapsHorizontally(combinedBitmaps2, 150)));

        printLine();
    }

    private void printStartAndLeaveCash(double startCash, double leaveCash) {

        ArrayList<Bitmap> combinedBitmaps1 = new ArrayList<>();
        combinedBitmaps1.add(PrintingHelper.createBitmapFromText(activity.getString(R.string.start_cash)));
        combinedBitmaps1.add(PrintingHelper.createBitmapFromText(zeroChecker(Utils.trimLongDouble(startCash))));
        bitmaps.add(new PrinterModel(combinedBitmaps1.get(0), combinedBitmaps1.get(1)));

        ArrayList<Bitmap> combinedBitmaps2 = new ArrayList<>();
        combinedBitmaps2.add(PrintingHelper.createBitmapFromText(activity.getString(R.string.leave_cash)));
        combinedBitmaps2.add(PrintingHelper.createBitmapFromText(zeroChecker(Utils.trimLongDouble(leaveCash))));
        bitmaps.add(new PrinterModel(combinedBitmaps2.get(0), combinedBitmaps2.get(1)));

        printLine();
    }

    private void printStartAndEndShiftTime(long startDateTime, long endDateTime) {
        bitmaps.add(new PrinterModel(-1, PrintingHelper.createBitmapFromText(activity.getString(R.string.shift_start_date_time))));
        bitmaps.add(new PrinterModel(0, PrintingHelper.createBitmapFromText(getDateTime(startDateTime))));

        bitmaps.add(new PrinterModel(-1, PrintingHelper.createBitmapFromText(activity.getString(R.string.shift_end_date_time))));
        bitmaps.add(new PrinterModel(0, PrintingHelper.createBitmapFromText(getDateTime(endDateTime))));

    }

    private double printTotalCard(double totalCard) {
        ArrayList<Bitmap> combinedBitmaps = new ArrayList<>();
        combinedBitmaps.add(PrintingHelper.createBitmapFromText(activity.getString(R.string.total_card)));
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
        int mirror=1;
        String language = LocaleManager.getLanguage(activity);
        if(language.equals("ar")){
            mirror=-1;
        }
        for (int i = 0; i < size; i++) {
            Bitmap bitmap = bitmaps.get(i).getBitmap();
            if (bitmaps.get(i).type == 1) {
                int startX = 0;
                int side=bitmaps.get(i).getSide() * mirror;
                if (side == 0) {
                    startX = width / 2 - bitmap.getWidth() / 2;
                } else if (side == 1) {
                    startX = width - bitmap.getWidth();
                }
                canvas.drawBitmap(bitmaps.get(i).getBitmap(), startX, lastY, new Paint());
            } else if (bitmaps.get(i).type == 2) {
                Bitmap bitmap2 = bitmaps.get(i).bitmap2;
                if(language.equals("ar")){
                    canvas.drawBitmap(bitmaps.get(i).bitmap2, 0, lastY, new Paint());
                    canvas.drawBitmap(bitmaps.get(i).getBitmap(), width - bitmaps.get(i).getBitmap().getWidth(), lastY, new Paint());
                }else {
                    canvas.drawBitmap(bitmaps.get(i).getBitmap(), 0, lastY, new Paint());
                    canvas.drawBitmap(bitmaps.get(i).bitmap2, width - bitmap2.getWidth(), lastY, new Paint());
                }
            } else if (bitmaps.get(i).type == 3) {
                Bitmap bitmap2 = bitmaps.get(i).bitmap2;
                Bitmap bitmap3 = bitmaps.get(i).bitmap3;
                if(language.equals("ar")){
                    canvas.drawBitmap(bitmaps.get(i).bitmap3, 0, lastY, new Paint());
                    canvas.drawBitmap(bitmaps.get(i).bitmap2, (int) ((double) width / 2.0 - (double) bitmap2.getWidth() / 2.0), lastY, new Paint());
                    canvas.drawBitmap(bitmaps.get(i).getBitmap(), width - bitmaps.get(i).getBitmap().getWidth(), lastY, new Paint());
                }else {
                    canvas.drawBitmap(bitmaps.get(i).getBitmap(), 0, lastY, new Paint());
                    canvas.drawBitmap(bitmaps.get(i).bitmap2, (int) ((double) width / 2.0 - (double) bitmap2.getWidth() / 2.0), lastY, new Paint());
                    canvas.drawBitmap(bitmaps.get(i).bitmap3, width - bitmap3.getWidth(), lastY, new Paint());
                }

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
        newBitmaps.add(PrintingHelper.createBitmapFromText(activity.getString(R.string.final_total)));
        newBitmaps.add(PrintingHelper.createBitmapFromText(priceAfterTax != 0 ? Utils.trimLongDouble(priceAfterTax) : "0.00"));
        bitmaps.add(new PrinterModel(newBitmaps.get(0), newBitmaps.get(1)));
        bitmaps.add(new PrinterModel(-1, PrintingHelper.createBitmapFromText(line)));
    }

    private void printPaidAndChangeAmount(String paidAmount, double priceAfterTax, String changeAmount, String orderPaymentMethod) {
        if (orderPaymentMethod.equalsIgnoreCase("cash")) {
            List<Bitmap> newBitmaps1 = new ArrayList<>();
            newBitmaps1.add(PrintingHelper.createBitmapFromText(activity.getString(R.string.total_paid)));
            newBitmaps1.add(PrintingHelper.createBitmapFromText(Utils.trimLongDouble(Double.parseDouble(paidAmount) - Double.parseDouble(changeAmount))));
            bitmaps.add(new PrinterModel(newBitmaps1.get(0), newBitmaps1.get(1)));
            printLine();
//            List<Bitmap> newBitmaps2 = new ArrayList<>();
//            newBitmaps2.add(PrintingHelper.createBitmapFromText(activity.getString(R.string.net_amount)));
//            newBitmaps2.add(PrintingHelper.createBitmapFromText(Utils.trimLongDouble(priceAfterTax)));
//            bitmaps.add(new PrinterModel(newBitmaps2.get(0), newBitmaps2.get(1)));
//            printLine();
            List<Bitmap> newBitmaps3 = new ArrayList<>();
            newBitmaps3.add(PrintingHelper.createBitmapFromText(activity.getString(R.string.receipt_change)));
            newBitmaps3.add(PrintingHelper.createBitmapFromText(Utils.trimLongDouble(-1 * Double.parseDouble(changeAmount))));
            bitmaps.add(new PrinterModel(newBitmaps3.get(0), newBitmaps3.get(1)));
            printLine();
        }
    }

    private void printTax(double tax) {
        List<Bitmap> newBitmaps = new ArrayList<>();
        newBitmaps.add(PrintingHelper.createBitmapFromText(activity.getString(R.string.value_added_tax)));
        newBitmaps.add(PrintingHelper.createBitmapFromText(tax != 0 ? Utils.trimLongDouble(tax) : "0.00"));
        bitmaps.add(new PrinterModel(newBitmaps.get(0), newBitmaps.get(1)));
        bitmaps.add(new PrinterModel(-1, PrintingHelper.createBitmapFromText(line)));
    }

    private void printDiscount(String discount) {
        List<Bitmap> newBitmaps = new ArrayList<>();
        newBitmaps.add(PrintingHelper.createBitmapFromText(activity.getString(R.string.discount)));
        newBitmaps.add(PrintingHelper.createBitmapFromText(Double.parseDouble(discount) != 0 ? Utils.trimLongDouble(discount) : "0.00"));
        bitmaps.add(new PrinterModel(newBitmaps.get(0), newBitmaps.get(1)));
        bitmaps.add(new PrinterModel(-1, PrintingHelper.createBitmapFromText(line)));
    }

    private void printTotalExcludingTax(double priceBeforeTax) {
        List<Bitmap> newBitmaps = new ArrayList<>();
        newBitmaps.add(PrintingHelper.createBitmapFromText(activity.getString(R.string.total_before_tax)));
        newBitmaps.add(PrintingHelper.createBitmapFromText(priceBeforeTax != 0 ? Utils.trimLongDouble(priceBeforeTax) : "0.00"));
        bitmaps.add(new PrinterModel(newBitmaps.get(0), newBitmaps.get(1)));
        bitmaps.add(new PrinterModel(-1, PrintingHelper.createBitmapFromText(line)));
    }

    private void printProducts(List<HashMap<String, String>> orderDetailsList) {
        bitmaps.add(new PrinterModel(-1, PrintingHelper.createBitmapFromText(line)));
        List<Bitmap> newBitmaps = new ArrayList<>();
        newBitmaps.add(PrintingHelper.createBitmapFromText(activity.getString(R.string.quantity)));
        newBitmaps.add(PrintingHelper.createBitmapFromText(activity.getString(R.string.price)));
        newBitmaps.add(PrintingHelper.createBitmapFromText(activity.getString(R.string.total)));
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
            ProductBitmap.add(PrintingHelper.createBitmapFromText(qty));
            ProductBitmap.add(PrintingHelper.createBitmapFromText(Utils.trimLongDouble(Double.parseDouble(price))));
            ProductBitmap.add(PrintingHelper.createBitmapFromText(Utils.trimLongDouble(productTotalPrice)));
            //ProductBitmap.add(PrintingHelper.createBitmapFromText("    "));
            bitmaps.add(new PrinterModel(ProductBitmap.get(0), ProductBitmap.get(1), ProductBitmap.get(2)));
            bitmaps.add(new PrinterModel(-1, PrintingHelper.createBitmapFromText(name)));
            bitmaps.add(new PrinterModel(-1, PrintingHelper.createBitmapFromText(line)));
        }
    }

    private void printInvoiceBarcode(String invoiceId) throws WriterException {
        BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
        bitmaps.add(new PrinterModel(0, barcodeEncoder.encodeQrOrBc(invoiceId, BarcodeFormat.CODE_128, 400, 100)));
    }

    private void printReceiptNo(String invoiceId) {
        List<Bitmap> newBitmaps = new ArrayList<>();
        newBitmaps.add(PrintingHelper.createBitmapFromText(activity.getString(R.string.receipt_no)));
        newBitmaps.add(PrintingHelper.createBitmapFromText(invoiceId));
        bitmaps.add(new PrinterModel(newBitmaps.get(0), newBitmaps.get(1)));
    }

    private void printType(String type) {
        if (!type.equals("invoice") && !type.isEmpty())
            bitmaps.add(new PrinterModel(0, PrintingHelper.createBitmapFromText(type)));
        else {
            bitmaps.add(new PrinterModel(0, PrintingHelper.createBitmapFromText(activity.getString(R.string.simplified_tax_invoice))));
        }
    }

    private void printMerchantTaxNumber(String merchantTaxNumber) {
        List<Bitmap> newBitmaps = new ArrayList<>();
        newBitmaps.add(PrintingHelper.createBitmapFromText(activity.getString(R.string.commercial_registration_number_)));
        newBitmaps.add(PrintingHelper.createBitmapFromText(merchantTaxNumber));
        bitmaps.add(new PrinterModel(newBitmaps.get(0), newBitmaps.get(1)));
    }

    private void printMerchantId(String merchantId) {
        List<Bitmap> newBitmaps = new ArrayList<>();
        newBitmaps.add(PrintingHelper.createBitmapFromText(activity.getString(R.string.tax_number_)));
        newBitmaps.add(PrintingHelper.createBitmapFromText(merchantId));
        bitmaps.add(new PrinterModel(newBitmaps.get(0), newBitmaps.get(1)));
    }

    private String zeroChecker(String valueToBePrinted) {
        return valueToBePrinted.equalsIgnoreCase(".00") || valueToBePrinted.equalsIgnoreCase("0.00") ? "0" : valueToBePrinted;
    }

    private void printLine() {
        bitmaps.add(new PrinterModel(-1, PrintingHelper.createBitmapFromText("----------------------------------------")));
    }

    private String getDateTime(Long dateTimeMillisecond) {
        Date date = new Date(dateTimeMillisecond);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ", Locale.UK);
        String formattedDate = sdf.format(date);
        return formattedDate;
    }

}
