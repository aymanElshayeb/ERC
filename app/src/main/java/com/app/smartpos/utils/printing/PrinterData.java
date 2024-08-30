package com.app.smartpos.utils.printing;

import android.graphics.Bitmap;

public class PrinterData {
    Bitmap bitmap;
    String invoice_id;
    String customer_name;
    String order_date;
    String order_time;
    double tax;
    double price_after_tax;
    double price_before_tax;
    String discount;
    String currency;

    public PrinterData(Bitmap bitmap, String invoice_id, String customer_name, String order_date, String order_time, double tax, double price_after_tax, double price_before_tax,String discount,String currency) {
        this.bitmap = bitmap;
        this.invoice_id = invoice_id;
        this.customer_name = customer_name;
        this.order_date = order_date;
        this.order_time = order_time;
        this.tax = tax;
        this.price_after_tax = price_after_tax;
        this.price_before_tax = price_before_tax;
        this.discount=discount;
        this.currency=currency;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public String getInvoice_id() {
        return invoice_id;
    }

    public String getCustomer_name() {
        return customer_name;
    }

    public String getOrder_date() {
        return order_date;
    }

    public String getOrder_time() {
        return order_time;
    }

    public double getTax() {
        return tax;
    }

    public double getPrice_after_tax() {
        return price_after_tax;
    }

    public double getPrice_before_tax() {
        return price_before_tax;
    }

    public String getDiscount() {
        return discount;
    }

    public String getCurrency() {
        return currency;
    }

    @Override
    public String toString() {
        return "PrinterData{" +
                "bitmap=" + bitmap +
                ", invoice_id='" + invoice_id + '\'' +
                ", customer_name='" + customer_name + '\'' +
                ", order_date='" + order_date + '\'' +
                ", order_time='" + order_time + '\'' +
                ", tax=" + tax +
                ", price_after_tax=" + price_after_tax +
                ", price_before_tax=" + price_before_tax +
                ", discount='" + discount + '\'' +
                ", currency='" + currency + '\'' +
                '}';
    }
}
