package com.app.smartpos.orders;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Toast;

import com.app.smartpos.R;
import com.app.smartpos.database.DatabaseAccess;
import com.app.smartpos.pdf_report.BarCodeEncoder;
import com.app.smartpos.utils.IPrintToPrinter;
import com.app.smartpos.utils.WoosimPrnMng;
import com.app.smartpos.utils.printerFactory;
import com.app.smartpos.utils.printerWordMng;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.woosim.printer.WoosimCmd;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;

public class TestPrinter implements IPrintToPrinter {


    String name, price, qty, weight;
    double cost_total, subTotal, totalPrice;
    Bitmap bm;
    DecimalFormat f;
    private final Context context;
    List<HashMap<String, String>> orderDetailsList;
    String currency, shopName, shopAddress, shopEmail, shopContact, invoiceId, orderDate, orderTime, customerName, footer, tax, discount;

    public TestPrinter(Context context, String shopName, String shopAddress, String shopEmail, String shopContact, String invoiceId, String orderDate, String orderTime, String customerName, String footer, double subTotal, double totalPrice, String tax, String discount, String currency) {
        this.context = context;
        this.shopName = shopName;
        this.shopAddress = shopAddress;
        this.shopEmail = shopEmail;
        this.shopContact = shopContact;
        this.invoiceId = invoiceId;
        this.orderDate = orderDate;
        this.orderTime = orderTime;
        this.customerName = customerName;
        this.footer = footer;
        this.subTotal = subTotal;
        this.totalPrice = totalPrice;
        this.tax = tax;
        this.discount = discount;
        this.currency = currency;
        f = new DecimalFormat("#0.00");


        DatabaseAccess databaseAccess = DatabaseAccess.getInstance(context);
        databaseAccess.open();


        //get data from local database

        orderDetailsList = databaseAccess.getOrderDetailsList(invoiceId);
    }

    @Override
    public void printContent(WoosimPrnMng prnMng) {


        double getDiscount = Double.parseDouble(discount);
        double getTax = Double.parseDouble(tax);

        //Generate barcode
        BarCodeEncoder qrCodeEncoder = new BarCodeEncoder();
        bm = null;

        try {
            bm = qrCodeEncoder.encodeAsBitmap(invoiceId, BarcodeFormat.CODE_128, 400, 48);
        } catch (WriterException e) {
            e.printStackTrace();
        }


        printerWordMng wordMng = printerFactory.createPaperMng(context);
        Bundle format = getTextBundle();
        prnMng.printStr(shopName, 2, WoosimCmd.ALIGN_CENTER);
        //prnMng.printStr(shopAddress, 1, WoosimCmd.ALIGN_CENTER);
        //prnMng.printStr("Email: " + shopEmail, 1, WoosimCmd.ALIGN_CENTER);
        //prnMng.printStr("Contact: " + shopContact, 1, WoosimCmd.ALIGN_CENTER);
        //prnMng.printStr("Order Time: " + orderTime + " " + orderDate, 1, WoosimCmd.ALIGN_CENTER);
        //Todo print الرقم الضريبى "tax number"
        prnMng.printStr(orderDate + "             " + orderTime, 1, WoosimCmd.ALIGN_CENTER);
        prnMng.printStr("Receipt ID: " + invoiceId, 1, WoosimCmd.ALIGN_CENTER);
        //prnMng.printStr(customerName, 1, WoosimCmd.ALIGN_CENTER);
        //prnMng.printStr("Email: " + shopEmail, 1, WoosimCmd.ALIGN_CENTER);
        prnMng.printStr("فاتورة ضريبية مبسطة", true, false, 1, WoosimCmd.ALIGN_CENTER);
        //Todo print barcode of the receipt id
//        prnMng.printStr("--------------------------------");
//
        prnMng.printStr("  Items      Price   Qty   Total", 1, WoosimCmd.ALIGN_CENTER);
        prnMng.printStr("--------------------------------");


        for (int i = 0; i < orderDetailsList.size(); i++) {
            name = orderDetailsList.get(i).get("product_name_en");
            price = orderDetailsList.get(i).get("product_price");
            qty = orderDetailsList.get(i).get("product_qty");
            weight = orderDetailsList.get(i).get("product_weight");


            cost_total = Integer.parseInt(qty) * Double.parseDouble(price);
            prnMng.printStr(name, 1, WoosimCmd.ALIGN_LEFT);
            prnMng.printStr("             " + price + "       " + qty + "      " + cost_total, 1, WoosimCmd.ALIGN_CENTER);
            prnMng.printStr("--------------------------------");
//            prnMng.leftRightAlign(name.trim(), " " + price + " x" + qty + " " + f.format(cost_total));

//            prnMng.printTable();

        }

//        prnMng.printPhoto(createBitmapFromText(f.format(subTotal) + "   " + "الإجمالى قبل الضريبة"));
//        prnMng.printStr(f.format(getDiscount) +  "   " + "الخصم", 1, WoosimCmd.ALIGN_LEFT);
//        prnMng.printStr(f.format(getTax) +  "   " + "ضريبة القيمة المضافة", 1, WoosimCmd.ALIGN_LEFT);
//        prnMng.printStr(f.format(totalPrice) +  "   " + "الإجمالى النهائى", 1, WoosimCmd.CT_ARABIC_FORMS_B);
//        prnMng.printStr("--------------------------------");
//        prnMng.printStr("Total paid" + "   " + "اجمالى المدفوع", 1, WoosimCmd.ALIGN_LEFT);
//        prnMng.printStr("needs to be paid" + "   " + "الصافى", 1, WoosimCmd.ALIGN_LEFT);
//        prnMng.printStr("Remaining" + "   " + "الباقى", 1, WoosimCmd.ALIGN_LEFT);
//        prnMng.printStr("--------------------------------");


        prnMng.printStr("Sub Total: " + currency + f.format(subTotal), 1, WoosimCmd.ALIGN_LEFT);
        prnMng.printStr("Total Tax (+): " + currency + f.format(getTax), 1, WoosimCmd.ALIGN_LEFT);
        prnMng.printStr("Discount (-): " + currency + f.format(getDiscount), 1, WoosimCmd.ALIGN_LEFT);
        prnMng.printStr("--------------------------------");
        prnMng.printStr("Total Price: " + currency + f.format(totalPrice), 1, WoosimCmd.ALIGN_LEFT);
        prnMng.printStr(footer, 1, WoosimCmd.ALIGN_CENTER);

        prnMng.printNewLine();

        //print barcode
        prnMng.printPhoto(bm);

        prnMng.printNewLine();
        prnMng.printNewLine();
        //Any finalization, you can call it here or any where in your running activity.
        printEnded(prnMng);
    }

    private Bundle getTextBundle() {
        Bundle format = new Bundle();
        String fontPath = Environment.getExternalStorageDirectory() + "/alipuhuiti.ttf";
        format.putInt("font", 1);
        format.putInt("align", 1);
        format.putBoolean("fontBold", true);
        format.putString("fontName", fontPath);
        format.putInt("lineHeight", 0);
        return format;
    }

    public static Bitmap createBitmapFromText(String text) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.BLACK);
        paint.setTextSize(50);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

        float baseline = -paint.ascent(); // ascent() is negative
        int width = (int) (paint.measureText(text) + 0.5f); // round
        int height = (int) (baseline + paint.descent() + 0.5f);

        Bitmap image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(image);
        canvas.drawText(text, 0, baseline, paint);

        return image;
    }

    @Override
    public void printEnded(WoosimPrnMng prnMng) {
        //Do any finalization you like after print ended.
        if (prnMng.printSucc()) {
            Toast.makeText(context, R.string.print_succ, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(context, R.string.print_error, Toast.LENGTH_LONG).show();
        }
    }
}
