package com.app.smartpos.database;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.app.smartpos.Constant;
import com.app.smartpos.auth.LoginUser;
import com.app.smartpos.settings.end_shift.EndShiftModel;
import com.app.smartpos.settings.end_shift.ShiftDifferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Objects;


public class DatabaseAccess {
    private static DatabaseAccess instance;
    private SQLiteOpenHelper openHelper;
    private SQLiteDatabase database;

    /**
     * Private constructor to avoid object creation from outside classes.
     *
     * @param context
     */
    private DatabaseAccess(Context context) {
        this.openHelper = new DatabaseOpenHelper(context);
    }

    /**
     * Return a singleton instance of DatabaseAccess.
     *
     * @param context the Context
     * @return the instance of DabaseAccess
     */
    public static DatabaseAccess getInstance(Context context) {


        if (instance == null) {
            instance = new DatabaseAccess(context);
        }
        return instance;
    }

    /**
     * Open the database connection.
     */
    public void open() {
        this.database = openHelper.getWritableDatabase();
    }

    /**
     * Close the database connection.
     */
    public void close() {
        if (database != null) {
            this.database.close();
        }
    }

    //insert customer
    public boolean addCustomer(String customer_name, String customer_cell,
                               String customer_email, String customer_address,
                               Boolean customer_active) {

        ContentValues values = new ContentValues();


        values.put("customer_name", customer_name);
        values.put("customer_cell", customer_cell);
        values.put("customer_email", customer_email);
        values.put("customer_address", customer_address);
        values.put("customer_active",customer_active);

        long check = database.insert("customers", null, values);
        database.close();

        //if data insert success, its return 1, if failed return -1
        if (check == -1) {
            return false;
        } else {
            return true;
        }
    }


    //insert category
    public boolean addCategory(String category_name) {

        ContentValues values = new ContentValues();


        values.put("category_name", category_name);


        long check = database.insert("product_category", null, values);
        database.close();

        //if data insert success, its return 1, if failed return -1
        if (check == -1) {
            return false;
        } else {
            return true;
        }
    }


    //insert payment method
    public boolean addPaymentMethod(String payment_method_name,boolean payment_method_active) {

        ContentValues values = new ContentValues();


        values.put("payment_method_name", payment_method_name);
        values.put("payment_method_active", payment_method_active);

        long check = database.insert("payment_method", null, values);
        database.close();

        //if data insert success, its return 1, if failed return -1
        if (check == -1) {
            return false;
        } else {
            return true;
        }
    }


    //insert unit
    public boolean addUnit(String unitName) {

        ContentValues values = new ContentValues();


        values.put("weight_unit", unitName);


        long check = database.insert("product_weight", null, values);
        database.close();

        //if data insert success, its return 1, if failed return -1
        if (check == -1) {
            return false;
        } else {
            return true;
        }
    }


    //insert order type
    public boolean addOrderType(String orderType) {

        ContentValues values = new ContentValues();


        values.put("order_type_name", orderType);


        long check = database.insert("order_type", null, values);
        database.close();

        //if data insert success, its return 1, if failed return -1
        if (check == -1) {
            return false;
        } else {
            return true;
        }
    }

    public String getLastShift(String key){
        String result="";
        Cursor cursor = database.rawQuery("SELECT * FROM shift ORDER BY id DESC LIMIT 1", null);
        if (cursor.moveToFirst()) {
            result = cursor.getString(cursor.getColumnIndex(key));
        }
        database.close();
        return result;
    }
    public int addShift(EndShiftModel endShiftModel) {

        ContentValues values = new ContentValues();

        values.put("sequence", endShiftModel.getSequence());
        values.put("device_id", endShiftModel.getDeviceID());
        values.put("username", endShiftModel.getUserName());
        values.put("start_date_time", endShiftModel.getStartDateTime());
        values.put("end_date_time", endShiftModel.getEndDateTime());

        double total_cash=0;
        double diff_cash=0;
        if(endShiftModel.getShiftDifferences().containsKey("CASH")){
            ShiftDifferences shiftDifferences=endShiftModel.getShiftDifferences().get("CASH");
            total_cash = shiftDifferences.getReal();
            diff_cash = shiftDifferences.getDiff();
        }
        values.put("total_cash",total_cash);
        values.put("difference_cash",diff_cash);

        values.put("start_cash",endShiftModel.getStartCash());
        values.put("leave_cash",endShiftModel.getLeaveCash());

        values.put("num_successful_transaction",endShiftModel.getNum_successful_transaction());
        values.put("num_canceled_transaction",endShiftModel.getNum_canceled_transaction());
        values.put("num_returned_transaction",endShiftModel.getNum_returned_transaction());

        long check = 0;
        try {
            check = database.insertOrThrow("shift", null, values);
        } catch (Exception e) {
            Log.i("datadata", e.getMessage() + "");
        }


        //if data insert success, its return 1, if failed return -1
        if (check == -1) {
            database.close();
            return -1;
        } else {
            return getShiftWithTimestamp(endShiftModel.getEndDateTime());
        }

    }

    public boolean addShiftCreditCalculations(int id, ShiftDifferences shiftDifference) {
        ContentValues values = new ContentValues();

        values.put("shift_id", id);
        values.put("total", shiftDifference.getReal());
        //values.put("input", shiftDifferences.get(i).getInput());
        values.put("difference", shiftDifference.getDiff());



        long check = database.insert("shift_difference", null, values);
        database.close();

        //if data insert success, its return 1, if failed return -1
        return true;
    }


    //update category
    public boolean updateCategory(String category_id, String category_name) {

        ContentValues values = new ContentValues();


        values.put("category_name", category_name);


        long check = database.update("product_category", values, "category_id=? ", new String[]{category_id});
        database.close();

        //if data insert success, its return 1, if failed return -1
        if (check == -1) {
            return false;
        } else {
            return true;
        }
    }


    //update payment method
    public boolean updatePaymentMethod(String payment_method_id, String payment_method_name,boolean payment_method_active) {

        ContentValues values = new ContentValues();


        values.put("payment_method_name", payment_method_name);
        values.put("payment_method_active", payment_method_active);



        long check = database.update("payment_method", values, "payment_method_id=? ", new String[]{payment_method_id});
        database.close();

        //if data insert success, its return 1, if failed return -1
        if (check == -1) {
            return false;
        } else {
            return true;
        }
    }


    //update order type
    public boolean updateOrderType(String typeId, String orderTypeName) {

        ContentValues values = new ContentValues();


        values.put("order_type_name", orderTypeName);


        long check = database.update("order_type", values, "order_type_id=? ", new String[]{typeId});
        database.close();

        //if data insert success, its return 1, if failed return -1
        if (check == -1) {
            return false;
        } else {
            return true;
        }
    }


    //update weight unit
    public boolean updateWeightUnit(String weightId, String weightUnit) {

        ContentValues values = new ContentValues();


        values.put("weight_unit", weightUnit);


        long check = database.update("product_weight", values, "weight_id=? ", new String[]{weightId});
        database.close();

        //if data insert success, its return 1, if failed return -1
        if (check == -1) {
            return false;
        } else {
            return true;
        }
    }


    //update customer
    public boolean updateCustomer(String customer_id, String customer_name, String customer_cell, String customer_email, String customer_address,boolean customer_active) {

        ContentValues values = new ContentValues();


        values.put("customer_name", customer_name);
        values.put("customer_cell", customer_cell);
        values.put("customer_email", customer_email);
        values.put("customer_address", customer_address);
        values.put("customer_active",customer_active);

        long check = database.update("customers", values, " customer_id=? ", new String[]{customer_id});
        database.close();

        //if data insert success, its return 1, if failed return -1
        if (check == -1) {
            return false;
        } else {
            return true;
        }
    }


    //update shop information
    public boolean updateShopInformation(String shop_name, String shop_contact, String shop_email, String shop_address, String shop_currency, String tax) {


        String shop_id = "1";
        ContentValues values = new ContentValues();


        values.put("shop_name", shop_name);
        values.put("shop_contact", shop_contact);
        values.put("shop_email", shop_email);
        values.put("shop_address", shop_address);
        values.put("shop_currency", shop_currency);
        values.put("tax", tax);

        long check = database.update("shop", values, "shop_id=? ", new String[]{shop_id});
        database.close();

        //if data insert success, its return 1, if failed return -1
        if (check == -1) {
            return false;
        } else {
            return true;
        }
    }


    //insert products
    public boolean addProduct(String product_name, String product_code, String product_category, String product_description, String product_buy_price, String product_sell_price, String product_stock, String product_supplier, String product_image, String weight_unit_id, String product_weight) {

        ContentValues values = new ContentValues();


        values.put("product_name", product_name);
        values.put("product_code", product_code);
        values.put("product_category", product_category);
        values.put("product_description", product_description);
        values.put("product_buy_price", product_buy_price);
        values.put("product_sell_price", product_sell_price);

        values.put("product_supplier", product_supplier);
        values.put("product_image", product_image);
        values.put("product_stock", product_stock);
        values.put("product_weight_unit_id", weight_unit_id);

        values.put("product_weight", product_weight);


        long check = database.insert("products", null, values);
        database.close();

        //if data insert success, its return 1, if failed return -1
        if (check == -1) {
            return false;
        } else {
            return true;
        }
    }


    //insert products
    public boolean updateProduct(String product_name, String product_code, String product_category, String product_description, String product_buy_price, String product_sell_price, String product_stock, String product_supplier, String product_image, String weight_unit_id, String product_weight, String product_id) {

        ContentValues values = new ContentValues();


        values.put("product_name", product_name);
        values.put("product_code", product_code);
        values.put("product_category", product_category);
        values.put("product_description", product_description);
        values.put("product_buy_price", product_buy_price);
        values.put("product_sell_price", product_sell_price);

        values.put("product_supplier", product_supplier);
        values.put("product_image", product_image);
        values.put("product_stock", product_stock);
        values.put("product_weight_unit_id", weight_unit_id);

        values.put("product_weight", product_weight);


        long check = database.update("products", values, "product_id=?", new String[]{product_id});
        database.close();

        //if data insert success, its return 1, if failed return -1
        if (check == -1) {
            return false;
        } else {
            return true;
        }
    }


    //insert expense
    public boolean addExpense(String expense_name, String expense_amount, String expense_note, String date, String time) {

        ContentValues values = new ContentValues();


        values.put("expense_name", expense_name);
        values.put("expense_amount", expense_amount);
        values.put("expense_note", expense_note);
        values.put("expense_date", date);
        values.put("expense_time", time);


        long check = database.insert("expense", null, values);
        database.close();

        //if data insert success, its return 1, if failed return -1
        if (check == -1) {
            return false;
        } else {
            return true;
        }
    }


    //update expense
    public boolean updateExpense(String expense_id, String expense_name, String expense_amount, String expense_note, String date, String time) {

        ContentValues values = new ContentValues();


        values.put("expense_name", expense_name);
        values.put("expense_amount", expense_amount);
        values.put("expense_note", expense_note);
        values.put("expense_date", date);
        values.put("expense_time", time);


        long check = database.update("expense", values, "expense_id=?", new String[]{expense_id});
        database.close();

        //if data insert success, its return 1, if failed return -1
        if (check == -1) {
            return false;
        } else {
            return true;
        }
    }


    //insert Suppliers
    public boolean addSuppliers(String suppliers_name, String suppliers_contact_person, String suppliers_cell, String suppliers_email, String suppliers_address) {

        ContentValues values = new ContentValues();


        values.put("suppliers_name", suppliers_name);
        values.put("suppliers_contact_person", suppliers_contact_person);
        values.put("suppliers_cell", suppliers_cell);
        values.put("suppliers_email", suppliers_email);
        values.put("suppliers_address", suppliers_address);

        long check = database.insert("suppliers", null, values);
        database.close();

        //if data insert success, its return 1, if failed return -1
        if (check == -1) {
            return false;
        } else {
            return true;
        }
    }


    //update Suppliers
    public boolean updateSuppliers(String suppliers_id, String suppliers_name, String suppliers_contact_person, String suppliers_cell, String suppliers_email, String suppliers_address) {

        ContentValues values = new ContentValues();


        values.put("suppliers_name", suppliers_name);
        values.put("suppliers_contact_person", suppliers_contact_person);
        values.put("suppliers_cell", suppliers_cell);
        values.put("suppliers_email", suppliers_email);
        values.put("suppliers_address", suppliers_address);

        long check = database.update("suppliers", values, "suppliers_id=?", new String[]{suppliers_id});
        database.close();

        //if data insert success, its return 1, if failed return -1
        if (check == -1) {
            return false;
        } else {
            return true;
        }
    }


    //get product image base 64
    @SuppressLint("Range")
    public String getProductImage(String product_id) {

        String image = "n/a";
        Cursor cursor = database.rawQuery("SELECT * FROM products WHERE product_id='" + product_id + "'", null);


        if (cursor.moveToFirst()) {
            do {


                image = cursor.getString(cursor.getColumnIndex("product_image"));


            } while (cursor.moveToNext());
        }


        cursor.close();
        database.close();
        return image;
    }

    @SuppressLint("Range")
    public int getShiftWithTimestamp(long timeStamp) {

        int id = 0;
        Cursor cursor = database.rawQuery("SELECT * FROM shift WHERE end_date_time= "+ timeStamp +"", null);


        if (cursor.moveToFirst()) {
            do {


                id = cursor.getInt(cursor.getColumnIndex("id"));


            } while (cursor.moveToNext());
        }


        cursor.close();
        database.close();
        return id;
    }

    @SuppressLint("Range")
    public HashMap<String,String> getUserWithEmailPassword(String email, String password) {

        Cursor cursor = database.rawQuery("SELECT * FROM user WHERE email='" + email + "' and password='" + password + "'", null);

        HashMap<String,String> hashMap=null;
        if (cursor.moveToFirst()) {
            hashMap=new HashMap<>();
            do {

                hashMap.put("name_en",cursor.getString(cursor.getColumnIndex("name_en")));
                hashMap.put("name_ar",cursor.getString(cursor.getColumnIndex("name_ar")));
                hashMap.put("email",cursor.getString(cursor.getColumnIndex("email")));
                hashMap.put("password",cursor.getString(cursor.getColumnIndex("password")));
                hashMap.put("username",cursor.getString(cursor.getColumnIndex("username")));

            } while (cursor.moveToNext());
        }


        cursor.close();
        database.close();
        return hashMap;
    }


    //get product weight unit name
    @SuppressLint("Range")
    public String getWeightUnitName(String weight_unit_id) {

        String weight_unit_name = "n/a";
        Cursor cursor = database.rawQuery("SELECT * FROM product_weight WHERE weight_id=" + weight_unit_id + "", null);


        if (cursor.moveToFirst()) {
            do {


                weight_unit_name = cursor.getString(cursor.getColumnIndex("weight_unit"));


            } while (cursor.moveToNext());
        }


        cursor.close();
        database.close();
        return weight_unit_name;
    }


    //get product weight unit name
    @SuppressLint("Range")
    public String getSupplierName(String supplier_id) {

        String supplier_name = "n/a";
        Cursor cursor = database.rawQuery("SELECT * FROM suppliers WHERE suppliers_id=" + supplier_id + "", null);


        if (cursor.moveToFirst()) {
            do {


                supplier_name = cursor.getString(cursor.getColumnIndex("suppliers_name"));


            } while (cursor.moveToNext());
        }


        cursor.close();
        database.close();
        return supplier_name;
    }


    //get product weight unit name
    @SuppressLint("Range")
    public String getCategoryName(String category_id) {

        String product_category = "n/a";
        Cursor cursor = database.rawQuery("SELECT * FROM product_category WHERE category_id=" + category_id + "", null);


        if (cursor.moveToFirst()) {
            do {


                product_category = cursor.getString(cursor.getColumnIndex("category_name"));


            } while (cursor.moveToNext());
        }


        cursor.close();
        database.close();
        return product_category;
    }


    //Add product into cart
    public int addToCart(String product_id, String weight, String weight_unit, String price, int qty, String stock) {


        Cursor result = database.rawQuery("SELECT * FROM product_cart WHERE product_id='" + product_id + "'", null);
        if (result.getCount() >= 1) {

            return 2;

        } else {
            ContentValues values = new ContentValues();
            values.put("product_id", product_id);
            values.put("product_weight", weight);
            values.put("product_weight_unit", weight_unit);
            values.put("product_price", price);
            values.put("product_qty", qty);
            values.put("stock", stock);

            long check = database.insert("product_cart", null, values);


            database.close();


            //if data insert success, its return 1, if failed return -1
            if (check == -1) {
                return -1;
            } else {
                return 1;
            }
        }

    }

    public void updateProductInCart(int cart_id, int count) {


        SQLiteStatement result = database.compileStatement("UPDATE product_cart SET product_qty=product_qty + count WHERE product_id='" + cart_id + "'");


        result.execute();


        database.close();


    }


    //get cart product
    public ArrayList<HashMap<String, String>> getCartProduct() {
        ArrayList<HashMap<String, String>> product = new ArrayList<>();
        Cursor cursor = database.rawQuery("SELECT * FROM product_cart", null);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<String, String>();


                map.put("cart_id", cursor.getString(0));
                map.put("product_id", cursor.getString(1));
                map.put("product_weight", cursor.getString(2));
                map.put("product_weight_unit", cursor.getString(3));
                map.put("product_price", cursor.getString(4));
                map.put("product_qty", cursor.getString(5));
                map.put("stock", cursor.getString(6));


                product.add(map);
            } while (cursor.moveToNext());
        }

        cursor.close();
        database.close();
        return product;
    }


    //insert order in order list
    public void insertOrder(String order_id, JSONObject obj,Context context) {

        ContentValues values = new ContentValues();
        ContentValues values2 = new ContentValues();
        ContentValues values3 = new ContentValues();

        try {
            String order_date = obj.getString("order_date");
            String order_time = obj.getString("order_time");
            long order_timestamp = obj.getLong("order_timestamp");
            String order_type = obj.getString("order_type");
            String order_payment_method = obj.getString("order_payment_method");
            String customer_name = obj.getString("customer_name");
            String tax = obj.getString("tax");
            String discount = obj.getString("discount");
            String ecr_code = obj.getString("ecr_code");
            double in_tax_total = obj.getDouble("in_tax_total");
            double ex_tax_total = obj.getDouble("ex_tax_total");
            double paid_amount = obj.getDouble("paid_amount");
            double change_amount = obj.getDouble("change_amount");
            String tax_number = obj.getString("tax_number");
            String sequence_text = obj.getString("sequence_text");

            values.put("invoice_id", order_id);
            values.put("order_date", order_date);
            values.put("order_time", order_time);
            values.put("order_timestamp", order_timestamp);
            values.put("order_type", order_type);
            values.put("order_payment_method", order_payment_method);
            values.put("customer_name", customer_name);
            values.put("original_order_id", "");
            values.put("card_details", -1);
            values.put("ecr_code", ecr_code);
            values.put("tax", tax);
            values.put("discount", discount);
            values.put("in_tax_total", in_tax_total);
            values.put("ex_tax_total", ex_tax_total);
            values.put("paid_amount", paid_amount);
            values.put("change_amount", change_amount);
            values.put("tax_number", tax_number);
            values.put("sequence_text", sequence_text);
            values.put(Constant.ORDER_STATUS, Constant.PENDING);


            database.insert("order_list", null, values);

            database.delete("product_cart", null, null);


        } catch (JSONException e) {
            e.printStackTrace();
        }


        try {

            JSONArray result = obj.getJSONArray("lines");

            for (int i = 0; i < result.length(); i++) {
                JSONObject jo = result.getJSONObject(i);
                String product_name = jo.getString("product_name"); //ref
                String product_weight = jo.getString("product_weight");
                String product_qty = jo.getString("product_qty");
                String product_price = jo.getString("product_price");
                String product_image = jo.getString("product_image");
                String product_order_date = jo.getString("product_order_date");


                String product_id = jo.getString("product_id");
                String stock = jo.getString("stock");
                double in_tax_total = obj.getDouble("in_tax_total");
                double ex_tax_total = obj.getDouble("ex_tax_total");
                int updated_stock = Integer.parseInt(stock) - Integer.parseInt(product_qty);


                values2.put("invoice_id", order_id);
                values2.put("product_name", product_name);
                values2.put("product_weight", product_weight);
                values2.put("in_tax_total", in_tax_total);
                values2.put("ex_tax_total", ex_tax_total);
                values2.put("tax_amount", in_tax_total-ex_tax_total);
                values2.put("product_qty", product_qty);
                values2.put("product_price", product_price);
                values2.put("tax_percentage", getProductTax(product_id));
                values2.put("product_image", product_image);
                values2.put("product_order_date", product_order_date);
                values2.put(Constant.ORDER_STATUS, Constant.PENDING);

                //for stock update
                values3.put("product_stock", updated_stock);
                //for order insert
                database.insert("order_details", null, values2);

                //updating stock
                database.update("products", values3, "product_id=?", new String[]{product_id});

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        database.close();
    }


    @SuppressLint("Range")
    public ArrayList<HashMap<String, String>> getOrderList() {
        ArrayList<HashMap<String, String>> orderList = new ArrayList<>();
        Cursor cursor = database.rawQuery("SELECT * FROM order_list ORDER BY order_id DESC", null);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<>();

                map.put("invoice_id", cursor.getString(cursor.getColumnIndex("invoice_id")));
                map.put("order_date", cursor.getString(cursor.getColumnIndex("order_date")));
                map.put("order_time", cursor.getString(cursor.getColumnIndex("order_time")));
                map.put("order_type", cursor.getString(cursor.getColumnIndex("order_type")));
                map.put("order_payment_method", cursor.getString(cursor.getColumnIndex("order_payment_method")));
                map.put("customer_name", cursor.getString(cursor.getColumnIndex("customer_name")));
                map.put("tax", cursor.getString(cursor.getColumnIndex("tax")));
                map.put("discount", cursor.getString(cursor.getColumnIndex("discount")));
                map.put("card_details", cursor.getString(cursor.getColumnIndex("card_details")));
                map.put("original_order_id", cursor.getString(cursor.getColumnIndex("original_order_id")));
                map.put("ecr_code", cursor.getString(cursor.getColumnIndex("ecr_code")));
                map.put("ex_tax_total", cursor.getString(cursor.getColumnIndex("ex_tax_total")));
                map.put("in_tax_total", cursor.getString(cursor.getColumnIndex("in_tax_total")));
                map.put("paid_amount", cursor.getString(cursor.getColumnIndex("paid_amount")));
                map.put("change_amount", cursor.getString(cursor.getColumnIndex("change_amount")));
                map.put("tax_number", cursor.getString(cursor.getColumnIndex("tax_number")));
                map.put("sequence_text", cursor.getString(cursor.getColumnIndex("sequence_text")));

                map.put(Constant.ORDER_STATUS, cursor.getString(cursor.getColumnIndex(Constant.ORDER_STATUS)));


                orderList.add(map);
            } while (cursor.moveToNext());
        }
        cursor.close();
        database.close();
        return orderList;
    }

    @SuppressLint("Range")
    public ArrayList<HashMap<String, String>> getOrderListWithTime(long time) {
        ArrayList<HashMap<String, String>> orderList = new ArrayList<>();
        Cursor cursor = database.rawQuery("SELECT * FROM order_list WHERE order_timestamp > "+time+"", null);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<>();

                map.put("invoice_id", cursor.getString(cursor.getColumnIndex("invoice_id")));
                map.put("order_date", cursor.getString(cursor.getColumnIndex("order_date")));
                map.put("order_time", cursor.getString(cursor.getColumnIndex("order_time")));
                map.put("order_type", cursor.getString(cursor.getColumnIndex("order_type")));
                map.put("order_payment_method", cursor.getString(cursor.getColumnIndex("order_payment_method")));
                map.put("customer_name", cursor.getString(cursor.getColumnIndex("customer_name")));
                map.put("tax", cursor.getString(cursor.getColumnIndex("tax")));
                map.put("discount", cursor.getString(cursor.getColumnIndex("discount")));
                map.put("card_details", cursor.getString(cursor.getColumnIndex("card_details")));
                map.put("original_order_id", cursor.getString(cursor.getColumnIndex("original_order_id")));
                map.put("ecr_code", cursor.getString(cursor.getColumnIndex("ecr_code")));
                map.put("ex_tax_total", cursor.getString(cursor.getColumnIndex("ex_tax_total")));
                map.put("in_tax_total", cursor.getString(cursor.getColumnIndex("in_tax_total")));
                map.put("paid_amount", cursor.getString(cursor.getColumnIndex("paid_amount")));
                map.put("change_amount", cursor.getString(cursor.getColumnIndex("change_amount")));
                map.put("tax_number", cursor.getString(cursor.getColumnIndex("tax_number")));
                map.put("sequence_text", cursor.getString(cursor.getColumnIndex("sequence_text")));

                map.put(Constant.ORDER_STATUS, cursor.getString(cursor.getColumnIndex(Constant.ORDER_STATUS)));


                orderList.add(map);
            } while (cursor.moveToNext());
        }
        cursor.close();
        database.close();
        return orderList;
    }


    @SuppressLint("Range")
    public ArrayList<HashMap<String, String>> searchOrderList(String s) {
        ArrayList<HashMap<String, String>> orderList = new ArrayList<>();
        Cursor cursor = database.rawQuery("SELECT * FROM order_list WHERE customer_name LIKE '%" + s + "%' OR invoice_id LIKE '%" + s + "%'  OR order_status LIKE '%" + s + "%' ORDER BY order_id DESC", null);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("order_id", cursor.getString(cursor.getColumnIndex("product_sell_price")));
                map.put("card_details", cursor.getString(cursor.getColumnIndex("product_id")));
                map.put("change_amount", cursor.getString(cursor.getColumnIndex("product_active")));
                map.put("customer_name", cursor.getString(cursor.getColumnIndex("product_buy_price")));
                map.put("discount", cursor.getString(cursor.getColumnIndex("product_category")));
                map.put("ecr_code", cursor.getString(cursor.getColumnIndex("product_code")));
                map.put("ex_tax_total", cursor.getString(cursor.getColumnIndex("product_description")));
                map.put("in_tax_total", cursor.getString(cursor.getColumnIndex("product_image")));
                map.put("invoice_id", cursor.getString(cursor.getColumnIndex("product_name_en")));
                map.put("order_date", cursor.getString(cursor.getColumnIndex("product_name_ar")));
                map.put("order_payment_method", cursor.getString(cursor.getColumnIndex("product_stock")));
                map.put("order_status", cursor.getString(cursor.getColumnIndex("product_supplier")));
                map.put("order_time", cursor.getString(cursor.getColumnIndex("product_tax")));
                map.put("order_type", cursor.getString(cursor.getColumnIndex("product_weight")));
                map.put("original_order_id", cursor.getString(cursor.getColumnIndex("product_weight_unit_id")));
                map.put("paid_amount", cursor.getString(cursor.getColumnIndex("product_weight_unit_id")));
                map.put("sequence_text", cursor.getString(cursor.getColumnIndex("product_weight_unit_id")));
                map.put("tax", cursor.getString(cursor.getColumnIndex("product_weight_unit_id")));
                map.put("tax_number", cursor.getString(cursor.getColumnIndex("product_weight_unit_id")));
                orderList.add(map);
            } while (cursor.moveToNext());
        }
        cursor.close();
        database.close();
        return orderList;
    }


    //get order history data
    @SuppressLint("Range")
    public ArrayList<HashMap<String, String>> getOrderDetailsList(String order_id) {
        ArrayList<HashMap<String, String>> orderDetailsList = new ArrayList<>();
        Cursor cursor = database.rawQuery("SELECT * FROM order_details WHERE invoice_id='" + order_id + "' ORDER BY order_details_id DESC", null);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("order_details_id", cursor.getString(cursor.getColumnIndex("order_details_id")));
                map.put("ex_tax_total", cursor.getString(cursor.getColumnIndex("ex_tax_total")));
                map.put("in_tax_total", cursor.getString(cursor.getColumnIndex("in_tax_total")));
                map.put("invoice_id", cursor.getString(cursor.getColumnIndex("invoice_id")));
                map.put("order_status", cursor.getString(cursor.getColumnIndex("order_status")));
                map.put("product_id", cursor.getString(cursor.getColumnIndex("product_id")));
                map.put("product_image", cursor.getString(cursor.getColumnIndex("product_image")));
                map.put("product_name", cursor.getString(cursor.getColumnIndex("product_name")));
                map.put("product_order_date", cursor.getString(cursor.getColumnIndex("product_order_date")));
                map.put("product_price", cursor.getString(cursor.getColumnIndex("product_price")));
                map.put("product_qty", cursor.getString(cursor.getColumnIndex("product_qty")));
                map.put("product_weight", cursor.getString(cursor.getColumnIndex("product_weight")));
                map.put("tax_amount", cursor.getString(cursor.getColumnIndex("tax_amount")));
                map.put("tax_percentage", cursor.getString(cursor.getColumnIndex("tax_percentage")));
                orderDetailsList.add(map);
            } while (cursor.moveToNext());
        }

        cursor.close();
        database.close();
        return orderDetailsList;
    }


    //get order history data
    @SuppressLint("Range")
    public ArrayList<HashMap<String, String>> getAllSalesItems() {
        ArrayList<HashMap<String, String>> orderDetailsList = new ArrayList<>();
        Cursor cursor = database.rawQuery("SELECT * FROM order_details  WHERE order_status='" + Constant.COMPLETED + "' ORDER BY order_details_id DESC", null);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("order_details_id", cursor.getString(cursor.getColumnIndex("order_details_id")));
                map.put("ex_tax_total", cursor.getString(cursor.getColumnIndex("ex_tax_total")));
                map.put("in_tax_total", cursor.getString(cursor.getColumnIndex("in_tax_total")));
                map.put("invoice_id", cursor.getString(cursor.getColumnIndex("invoice_id")));
                map.put("order_status", cursor.getString(cursor.getColumnIndex("order_status")));
                map.put("product_id", cursor.getString(cursor.getColumnIndex("product_id")));
                map.put("product_image", cursor.getString(cursor.getColumnIndex("product_image")));
                map.put("product_name", cursor.getString(cursor.getColumnIndex("product_name")));
                map.put("product_order_date", cursor.getString(cursor.getColumnIndex("product_order_date")));
                map.put("product_price", cursor.getString(cursor.getColumnIndex("product_price")));
                map.put("product_qty", cursor.getString(cursor.getColumnIndex("product_qty")));
                map.put("product_weight", cursor.getString(cursor.getColumnIndex("product_weight")));
                map.put("tax_amount", cursor.getString(cursor.getColumnIndex("tax_amount")));
                map.put("tax_percentage", cursor.getString(cursor.getColumnIndex("tax_percentage")));
                orderDetailsList.add(map);
            } while (cursor.moveToNext());
        }

        cursor.close();
        database.close();
        return orderDetailsList;
    }


    //get order history data
    @SuppressLint("Range")
    public ArrayList<HashMap<String, String>> getSalesReport(String type) {
        ArrayList<HashMap<String, String>> orderDetailsList = new ArrayList<>();
        Cursor cursor = null;
        if (type.equals("all")) {
            cursor = database.rawQuery("SELECT * FROM order_details WHERE order_status='" + Constant.COMPLETED + "'   ORDER BY order_details_id DESC", null);
        } else if (type.equals("daily")) {
            String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(new Date());

            cursor = database.rawQuery("SELECT * FROM order_details  WHERE order_status='" + Constant.COMPLETED + "' AND product_order_date='" + currentDate + "' ORDER BY order_Details_id DESC", null);

        } else if (type.equals("monthly")) {

            String currentMonth = new SimpleDateFormat("MM", Locale.ENGLISH).format(new Date());
            String sql = "SELECT * FROM order_details  WHERE order_status='" + Constant.COMPLETED + "' AND strftime('%m', product_order_date) = '" + currentMonth + "' ";

            cursor = database.rawQuery(sql, null);

        } else if (type.equals("yearly")) {

            String currentYear = new SimpleDateFormat("yyyy", Locale.ENGLISH).format(new Date());
            Log.d("YEAR", currentYear);
            String sql = "SELECT * FROM order_details WHERE order_status='" + Constant.COMPLETED + "' AND strftime('%Y', product_order_date) = '" + currentYear + "' ";

            cursor = database.rawQuery(sql, null);

        }


        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("order_details_id", cursor.getString(cursor.getColumnIndex("order_details_id")));
                map.put("ex_tax_total", cursor.getString(cursor.getColumnIndex("ex_tax_total")));
                map.put("in_tax_total", cursor.getString(cursor.getColumnIndex("in_tax_total")));
                map.put("invoice_id", cursor.getString(cursor.getColumnIndex("invoice_id")));
                map.put("order_status", cursor.getString(cursor.getColumnIndex("order_status")));
                map.put("product_id", cursor.getString(cursor.getColumnIndex("product_id")));
                map.put("product_image", cursor.getString(cursor.getColumnIndex("product_image")));
                map.put("product_name", cursor.getString(cursor.getColumnIndex("product_name")));
                map.put("product_order_date", cursor.getString(cursor.getColumnIndex("product_order_date")));
                map.put("product_price", cursor.getString(cursor.getColumnIndex("product_price")));
                map.put("product_qty", cursor.getString(cursor.getColumnIndex("product_qty")));
                map.put("product_weight", cursor.getString(cursor.getColumnIndex("product_weight")));
                map.put("tax_amount", cursor.getString(cursor.getColumnIndex("tax_amount")));
                map.put("tax_percentage", cursor.getString(cursor.getColumnIndex("tax_percentage")));
                orderDetailsList.add(map);
            } while (cursor.moveToNext());
        }

        cursor.close();
        database.close();
        return orderDetailsList;
    }


    //get order history data
    @SuppressLint("Range")
    public ArrayList<HashMap<String, String>> getExpenseReport(String type) {
        ArrayList<HashMap<String, String>> orderDetailsList = new ArrayList<>();
        Cursor cursor = null;
        if (type.equals("all")) {
            cursor = database.rawQuery("SELECT * FROM expense  ORDER BY expense_id DESC", null);
        } else if (type.equals("daily")) {
            String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(new Date());

            cursor = database.rawQuery("SELECT * FROM expense WHERE   expense_date='" + currentDate + "' ORDER BY expense_id DESC", null);

        } else if (type.equals("monthly")) {

            String currentMonth = new SimpleDateFormat("MM", Locale.ENGLISH).format(new Date());
            String sql = "SELECT * FROM expense WHERE strftime('%m', expense_date) = '" + currentMonth + "' ";

            cursor = database.rawQuery(sql, null);

        } else if (type.equals("yearly")) {

            String currentYear = new SimpleDateFormat("yyyy", Locale.ENGLISH).format(new Date());
            String sql = "SELECT * FROM expense WHERE strftime('%Y', expense_date) = '" + currentYear + "' ";

            cursor = database.rawQuery(sql, null);

        }


        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<String, String>();

                map.put("expense_id", cursor.getString(cursor.getColumnIndex("expense_id")));
                map.put("expense_name", cursor.getString(cursor.getColumnIndex("expense_name")));
                map.put("expense_note", cursor.getString(cursor.getColumnIndex("expense_note")));
                map.put("expense_amount", cursor.getString(cursor.getColumnIndex("expense_amount")));
                map.put("expense_date", cursor.getString(cursor.getColumnIndex("expense_date")));
                map.put("expense_time", cursor.getString(cursor.getColumnIndex("expense_time")));

                orderDetailsList.add(map);
            } while (cursor.moveToNext());
        }

        cursor.close();
        database.close();
        return orderDetailsList;
    }


    //calculate total price in month
    public float getMonthlySalesAmount(String month, String getYear) {


        float total_price = 0;
        Cursor cursor = null;


        String year = getYear;


        String sql = "SELECT * FROM order_details WHERE order_status='" + Constant.COMPLETED + "'  AND  strftime('%m', product_order_date) = '" + month + "' AND strftime('%Y', product_order_date) = '" + year + "'  ";

        cursor = database.rawQuery(sql, null);


        if (cursor.moveToFirst()) {
            do {

                @SuppressLint("Range") float price = Float.parseFloat(cursor.getString(cursor.getColumnIndex("product_price")));
                @SuppressLint("Range") int qty = Integer.parseInt(cursor.getString(cursor.getColumnIndex("product_qty")));
                float sub_total = price * qty;
                total_price = total_price + sub_total;


            } while (cursor.moveToNext());
        } else {
            total_price = 0;
        }
        cursor.close();
        database.close();

        Log.d("total_price", "" + total_price);
        return total_price;
    }


    //calculate total price in month
    public float getMonthlyExpenseAmount(String month, String getYear) {


        float total_cost = 0;
        Cursor cursor = null;


        String year = getYear;


        String sql = "SELECT * FROM expense WHERE strftime('%m', expense_date) = '" + month + "' AND strftime('%Y', expense_date) = '" + year + "'  ";

        cursor = database.rawQuery(sql, null);


        if (cursor.moveToFirst()) {
            do {

                float cost = Float.parseFloat(cursor.getString(3));

                total_cost = total_cost + cost;


            } while (cursor.moveToNext());
        } else {
            total_cost = 0;
        }
        cursor.close();
        database.close();

        Log.d("total_price", "" + total_cost);
        return total_cost;
    }


    //delete product from cart
    public boolean deleteProductFromCart(String id) {


        long check = database.delete("product_cart", "cart_id=?", new String[]{id});

        database.close();

        if (check == 1) {
            return true;
        } else {
            return false;
        }

    }


    //get product data
    @SuppressLint("Range")
    public ArrayList<HashMap<String, String>> getTabProducts(String category_id) {
        ArrayList<HashMap<String, String>> product = new ArrayList<>();
        Cursor cursor = database.rawQuery("SELECT * FROM products WHERE product_category = '" + category_id + "' ORDER BY product_id DESC", null);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("product_id", cursor.getString(cursor.getColumnIndex("product_id")));
                map.put("product_active", cursor.getString(cursor.getColumnIndex("product_active")));
                map.put("product_buy_price", cursor.getString(cursor.getColumnIndex("product_buy_price")));
                map.put("product_category", cursor.getString(cursor.getColumnIndex("product_category")));
                map.put("product_code", cursor.getString(cursor.getColumnIndex("product_code")));
                map.put("product_description", cursor.getString(cursor.getColumnIndex("product_description")));
                map.put("product_image", cursor.getString(cursor.getColumnIndex("product_image")));
                map.put("product_name_en", cursor.getString(cursor.getColumnIndex("product_name_en")));
                map.put("product_name_ar", cursor.getString(cursor.getColumnIndex("product_name_ar")));
                map.put("product_sell_price", cursor.getString(cursor.getColumnIndex("product_sell_price")));
                map.put("product_stock", cursor.getString(cursor.getColumnIndex("product_stock")));
                map.put("product_supplier", cursor.getString(cursor.getColumnIndex("product_supplier")));
                map.put("product_tax", cursor.getString(cursor.getColumnIndex("product_tax")));
                map.put("product_weight", cursor.getString(cursor.getColumnIndex("product_weight")));
                map.put("product_weight_unit_id", cursor.getString(cursor.getColumnIndex("product_weight_unit_id")));
                product.add(map);
            } while (cursor.moveToNext());
        }
        cursor.close();
        database.close();
        return product;
    }


    //get cart item count
    public int getCartItemCount() {

        Cursor cursor = database.rawQuery("SELECT * FROM product_cart", null);
        int itemCount = cursor.getCount();


        cursor.close();
        database.close();
        return itemCount;
    }


    //delete product from cart
    public void updateProductQty(String id, String qty) {

        ContentValues values = new ContentValues();

        values.put("product_qty", qty);

        long check = database.update("product_cart", values, "cart_id=?", new String[]{id});


    }


    //get product name
    @SuppressLint("Range")
    public String getProductName(String product_id) {

        String product_name = "n/a";
        Cursor cursor = database.rawQuery("SELECT * FROM products WHERE product_id='" + product_id + "'", null);


        if (cursor.moveToFirst()) {
            do {
                product_name = cursor.getString(cursor.getColumnIndex("product_name_en"));
            } while (cursor.moveToNext());
        }

        cursor.close();
        database.close();
        return product_name;
    }

    @SuppressLint("Range")
    public double getProductTax(String product_id) {

        double product_Tax = 0;
        Cursor cursor = database.rawQuery("SELECT * FROM products WHERE product_id='" + product_id + "'", null);


        if (cursor.moveToFirst()) {
            do {
                product_Tax = cursor.getDouble(cursor.getColumnIndex("product_tax"));
            } while (cursor.moveToNext());
        }

        cursor.close();
        return product_Tax;
    }


    //get product name
    @SuppressLint("Range")
    public String getCurrency() {

        String currency = "n/a";
        Cursor cursor = database.rawQuery("SELECT * FROM shop", null);


        if (cursor.moveToFirst()) {
            do {


                currency = cursor.getString(cursor.getColumnIndex("shop_currency"));


            } while (cursor.moveToNext());
        }


        cursor.close();
        database.close();
        return currency;
    }


    //calculate total price of product
    public double getTotalPriceWithoutTax() {


        double total_price = 0;

        Cursor cursor = database.rawQuery("SELECT * FROM product_cart", null);
        if (cursor.moveToFirst()) {
            do {
                String priceId=cursor.getString(cursor.getColumnIndex("product_id"));
                double tax = 1+getProductTax(priceId)/100.0;
                double price = Double.parseDouble(cursor.getString(cursor.getColumnIndex("product_price")))/tax;
                int qty = Integer.parseInt(cursor.getString(cursor.getColumnIndex("product_qty")));
                double sub_total = price * qty;
                total_price = total_price + sub_total;


            } while (cursor.moveToNext());
        } else {
            total_price = 0;
        }
        cursor.close();
        database.close();
        return total_price;
    }

    public double getTotalPriceWithTax() {


        double total_price = 0;

        Cursor cursor = database.rawQuery("SELECT * FROM product_cart", null);
        if (cursor.moveToFirst()) {
            do {
                double price = Double.parseDouble(cursor.getString(cursor.getColumnIndex("product_price")));
                int qty = Integer.parseInt(cursor.getString(cursor.getColumnIndex("product_qty")));
                double sub_total = price * qty;
                total_price = total_price + sub_total;


            } while (cursor.moveToNext());
        } else {
            total_price = 0;
        }
        cursor.close();
        database.close();
        return total_price;
    }

    //calculate total discount of product
    public double getTotalDiscount(String type) {


        double total_discount = 0;
        Cursor cursor = null;


        if (type.equals("monthly")) {

            String currentMonth = new SimpleDateFormat("MM", Locale.ENGLISH).format(new Date());

            String sql = "SELECT * FROM order_list WHERE order_status='" + Constant.COMPLETED + "'  AND strftime('%m', order_date) = '" + currentMonth + "' ";

            cursor = database.rawQuery(sql, null);

        } else if (type.equals("yearly")) {

            String currentYear = new SimpleDateFormat("yyyy", Locale.ENGLISH).format(new Date());
            String sql = "SELECT * FROM order_list WHERE order_status='" + Constant.COMPLETED + "'  AND strftime('%Y', order_date) = '" + currentYear + "' ";

            cursor = database.rawQuery(sql, null);

        } else if (type.equals("daily")) {
            String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(new Date());

            cursor = database.rawQuery("SELECT * FROM order_list WHERE order_status='" + Constant.COMPLETED + "'  AND   order_date='" + currentDate + "' ORDER BY order_id DESC", null);

        } else {
            cursor = database.rawQuery("SELECT * FROM order_list WHERE order_status='" + Constant.COMPLETED + "'", null);

        }

        if (cursor.moveToFirst()) {
            do {

                @SuppressLint("Range") double discount = Double.parseDouble(cursor.getString(cursor.getColumnIndex("discount")));
                total_discount = total_discount + discount;


            } while (cursor.moveToNext());
        } else {
            total_discount = 0;
        }
        cursor.close();
        database.close();
        return total_discount;
    }


    //calculate total discount of product
    public double getTotalDiscountForGraph(String type, int currentYear) {


        double total_discount = 0;
        Cursor cursor = null;


        if (type.equals("monthly")) {

            String currentMonth = new SimpleDateFormat("MM", Locale.ENGLISH).format(new Date());

            String sql = "SELECT * FROM order_list WHERE order_status='" + Constant.COMPLETED + "'  AND strftime('%m', order_date) = '" + currentMonth + "' ";

            cursor = database.rawQuery(sql, null);

        } else if (type.equals("yearly")) {

            String sql = "SELECT * FROM order_list WHERE order_status='" + Constant.COMPLETED + "'  AND strftime('%Y', order_date) = '" + currentYear + "' ";

            cursor = database.rawQuery(sql, null);

        } else if (type.equals("daily")) {
            String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(new Date());

            cursor = database.rawQuery("SELECT * FROM order_list WHERE order_status='" + Constant.COMPLETED + "'  AND   order_date='" + currentDate + "' ORDER BY order_id DESC", null);

        } else {
            cursor = database.rawQuery("SELECT * FROM order_list WHERE order_status='" + Constant.COMPLETED + "'", null);

        }

        if (cursor.moveToFirst()) {
            do {

                @SuppressLint("Range") double discount = Double.parseDouble(cursor.getString(cursor.getColumnIndex("discount")));
                total_discount = total_discount + discount;


            } while (cursor.moveToNext());
        } else {
            total_discount = 0;
        }
        cursor.close();
        database.close();
        return total_discount;
    }


    //calculate total tax of product
    public double getTotalTax(String type) {


        double total_tax = 0;
        Cursor cursor = null;


        if (type.equals("monthly")) {

            String currentMonth = new SimpleDateFormat("MM", Locale.ENGLISH).format(new Date());

            String sql = "SELECT * FROM order_list WHERE order_status='" + Constant.COMPLETED + "'  AND  strftime('%m', order_date) = '" + currentMonth + "' ";

            cursor = database.rawQuery(sql, null);

        } else if (type.equals("yearly")) {

            String currentYear = new SimpleDateFormat("yyyy", Locale.ENGLISH).format(new Date());
            String sql = "SELECT * FROM order_list WHERE order_status='" + Constant.COMPLETED + "'  AND strftime('%Y', order_date) = '" + currentYear + "' ";

            cursor = database.rawQuery(sql, null);

        } else if (type.equals("daily")) {
            String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(new Date());

            cursor = database.rawQuery("SELECT * FROM order_list WHERE order_status='" + Constant.COMPLETED + "'  AND   order_date='" + currentDate + "' ORDER BY order_id DESC", null);

        } else {
            cursor = database.rawQuery("SELECT * FROM order_list WHERE order_status='" + Constant.COMPLETED + "' ", null);

        }

        if (cursor.moveToFirst()) {
            do {

                @SuppressLint("Range") double tax = Double.parseDouble(cursor.getString(cursor.getColumnIndex("tax")));
                total_tax = total_tax + tax;


            } while (cursor.moveToNext());
        } else {
            total_tax = 0;
        }
        cursor.close();
        database.close();
        return total_tax;
    }


    //calculate total tax of product
    public double getTotalTaxForGraph(String type, int currentYear) {


        double total_tax = 0;
        Cursor cursor = null;


        if (type.equals("monthly")) {

            String currentMonth = new SimpleDateFormat("MM", Locale.ENGLISH).format(new Date());

            String sql = "SELECT * FROM order_list WHERE order_status='" + Constant.COMPLETED + "'  AND  strftime('%m', order_date) = '" + currentMonth + "' ";

            cursor = database.rawQuery(sql, null);

        } else if (type.equals("yearly")) {

            String sql = "SELECT * FROM order_list WHERE order_status='" + Constant.COMPLETED + "'  AND strftime('%Y', order_date) = '" + currentYear + "' ";

            cursor = database.rawQuery(sql, null);

        } else if (type.equals("daily")) {
            String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(new Date());

            cursor = database.rawQuery("SELECT * FROM order_list WHERE order_status='" + Constant.COMPLETED + "'  AND   order_date='" + currentDate + "' ORDER BY order_id DESC", null);

        } else {
            cursor = database.rawQuery("SELECT * FROM order_list WHERE order_status='" + Constant.COMPLETED + "' ", null);

        }

        if (cursor.moveToFirst()) {
            do {

                @SuppressLint("Range") double tax = Double.parseDouble(cursor.getString(cursor.getColumnIndex("tax")));
                total_tax = total_tax + tax;


            } while (cursor.moveToNext());
        } else {
            total_tax = 0;
        }
        cursor.close();
        database.close();
        return total_tax;
    }
    //calculate total price of product
    public double getTotalOrderPrice(String type) {


        double total_price = 0;
        Cursor cursor = null;


        if (type.equals("monthly")) {

            String currentMonth = new SimpleDateFormat("MM", Locale.ENGLISH).format(new Date());

            String sql = "SELECT * FROM order_details WHERE order_status='" + Constant.COMPLETED + "'  AND strftime('%m', product_order_date) = '" + currentMonth + "' ";

            cursor = database.rawQuery(sql, null);

        } else if (type.equals("yearly")) {

            String currentYear = new SimpleDateFormat("yyyy", Locale.ENGLISH).format(new Date());
            String sql = "SELECT * FROM order_details WHERE order_status='" + Constant.COMPLETED + "'  AND  strftime('%Y', product_order_date) = '" + currentYear + "' ";

            cursor = database.rawQuery(sql, null);

        } else if (type.equals("daily")) {
            String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(new Date());

            cursor = database.rawQuery("SELECT * FROM order_details WHERE order_status='" + Constant.COMPLETED + "'  AND   product_order_date='" + currentDate + "' ORDER BY order_Details_id DESC", null);

        } else {
            cursor = database.rawQuery("SELECT * FROM order_details WHERE order_status='" + Constant.COMPLETED + "' ", null);

        }

        if (cursor.moveToFirst()) {
            do {

                @SuppressLint("Range") double price = Double.parseDouble(cursor.getString(cursor.getColumnIndex("product_price")));
                @SuppressLint("Range") int qty = Integer.parseInt(cursor.getString(cursor.getColumnIndex("product_qty")));
                double sub_total = price * qty;
                total_price = total_price + sub_total;


            } while (cursor.moveToNext());
        } else {
            total_price = 0;
        }
        cursor.close();
        database.close();
        return total_price;
    }


    //calculate total price of product
    public double getTotalOrderPriceForGraph(String type, int currentYear) {


        double total_price = 0;
        Cursor cursor = null;


        if (type.equals("monthly")) {

            String currentMonth = new SimpleDateFormat("MM", Locale.ENGLISH).format(new Date());

            String sql = "SELECT * FROM order_details WHERE order_status='" + Constant.COMPLETED + "'  AND strftime('%m', product_order_date) = '" + currentMonth + "' ";

            cursor = database.rawQuery(sql, null);

        } else if (type.equals("yearly")) {

            String sql = "SELECT * FROM order_details WHERE order_status='" + Constant.COMPLETED + "'  AND  strftime('%Y', product_order_date) = '" + currentYear + "' ";

            cursor = database.rawQuery(sql, null);

        } else if (type.equals("daily")) {
            String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(new Date());

            cursor = database.rawQuery("SELECT * FROM order_details WHERE order_status='" + Constant.COMPLETED + "'  AND   product_order_date='" + currentDate + "' ORDER BY order_Details_id DESC", null);

        } else {
            cursor = database.rawQuery("SELECT * FROM order_details WHERE order_status='" + Constant.COMPLETED + "' ", null);

        }

        if (cursor.moveToFirst()) {
            do {

                @SuppressLint("Range") double price = Double.parseDouble(cursor.getString(cursor.getColumnIndex("product_price")));
                @SuppressLint("Range") int qty = Integer.parseInt(cursor.getString(cursor.getColumnIndex("product_qty")));
                double sub_total = price * qty;
                total_price = total_price + sub_total;


            } while (cursor.moveToNext());
        } else {
            total_price = 0;
        }
        cursor.close();
        database.close();
        return total_price;
    }


    //calculate total price of product
    public double totalOrderPrice(String invoice_id) {


        double total_price = 0;


        Cursor cursor = database.rawQuery("SELECT * FROM order_details WHERE invoice_id='" + invoice_id + "'", null);


        if (cursor.moveToFirst()) {
            do {

                @SuppressLint("Range") double price = Double.parseDouble(cursor.getString(cursor.getColumnIndex("product_price")));
                @SuppressLint("Range") int qty = Integer.parseInt(cursor.getString(cursor.getColumnIndex("product_qty")));
                double sub_total = price * qty;
                total_price = total_price + sub_total;


            } while (cursor.moveToNext());
        } else {
            total_price = 0;
        }
        cursor.close();
        database.close();
        return total_price;
    }


    //calculate total price of expense
    public double getTotalExpense(String type) {


        double total_cost = 0;
        Cursor cursor = null;


        if (type.equals("monthly")) {

            String currentMonth = new SimpleDateFormat("MM", Locale.ENGLISH).format(new Date());

            String sql = "SELECT * FROM expense WHERE strftime('%m', expense_date) = '" + currentMonth + "' ";

            cursor = database.rawQuery(sql, null);

        } else if (type.equals("yearly")) {

            String currentYear = new SimpleDateFormat("yyyy", Locale.ENGLISH).format(new Date());
            String sql = "SELECT * FROM expense WHERE strftime('%Y', expense_date) = '" + currentYear + "' ";

            cursor = database.rawQuery(sql, null);

        } else if (type.equals("daily")) {
            String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(new Date());

            cursor = database.rawQuery("SELECT * FROM expense WHERE   expense_date='" + currentDate + "' ORDER BY expense_id DESC", null);

        } else {
            cursor = database.rawQuery("SELECT * FROM expense", null);

        }

        if (cursor.moveToFirst()) {
            do {

                @SuppressLint("Range") double expense = Double.parseDouble(cursor.getString(cursor.getColumnIndex("expense_amount")));

                total_cost = total_cost + expense;


            } while (cursor.moveToNext());
        } else {
            total_cost = 0;
        }
        cursor.close();
        database.close();
        return total_cost;
    }


    //calculate total price of expense
    public double getTotalExpenseForGraph(String type, int currentYear) {


        double total_cost = 0;
        Cursor cursor = null;


        if (type.equals("monthly")) {

            String currentMonth = new SimpleDateFormat("MM", Locale.ENGLISH).format(new Date());

            String sql = "SELECT * FROM expense WHERE strftime('%m', expense_date) = '" + currentMonth + "' ";

            cursor = database.rawQuery(sql, null);

        } else if (type.equals("yearly")) {


            String sql = "SELECT * FROM expense WHERE strftime('%Y', expense_date) = '" + currentYear + "' ";

            cursor = database.rawQuery(sql, null);

        } else if (type.equals("daily")) {
            String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(new Date());

            cursor = database.rawQuery("SELECT * FROM expense WHERE   expense_date='" + currentDate + "' ORDER BY expense_id DESC", null);

        } else {
            cursor = database.rawQuery("SELECT * FROM expense", null);

        }

        if (cursor.moveToFirst()) {
            do {

                @SuppressLint("Range") double expense = Double.parseDouble(cursor.getString(cursor.getColumnIndex("expense_amount")));

                total_cost = total_cost + expense;


            } while (cursor.moveToNext());
        } else {
            total_cost = 0;
        }
        cursor.close();
        database.close();
        return total_cost;
    }


    public int getAllUser() {

        int id = 0;
        Cursor cursor = database.rawQuery("SELECT * FROM users", null);

        Log.i("datadata", "" + cursor.moveToFirst());
        if (cursor.moveToFirst()) {
            do {

                for (int i = 0; i < 3; i++) {
                    Log.i("datadata", cursor.getColumnName(i));
                }
                id = cursor.getInt(0);


            } while (cursor.moveToNext());
        }


        cursor.close();
        database.close();
        return id;
    }

    //get customer data
    @SuppressLint("Range")
    public ArrayList<HashMap<String, String>> getCustomers() {
        ArrayList<HashMap<String, String>> customer = new ArrayList<>();
        Cursor cursor = database.rawQuery("SELECT * FROM customers ORDER BY customer_id DESC", null);
        if (cursor.moveToFirst()) {

            do {
                HashMap<String, String> map = new HashMap<>();


                map.put("customer_id", cursor.getString(cursor.getColumnIndex("customer_id")));
                map.put("customer_name", cursor.getString(cursor.getColumnIndex("customer_name")));
                map.put("customer_cell", cursor.getString(cursor.getColumnIndex("customer_cell")));
                map.put("customer_email", cursor.getString(cursor.getColumnIndex("customer_email")));
                map.put("customer_address", cursor.getString(cursor.getColumnIndex("customer_address")));
                map.put("customer_active", cursor.getString(cursor.getColumnIndex("customer_active")));


                customer.add(map);
            } while (cursor.moveToNext());
        }
        cursor.close();
        database.close();
        return customer;
    }


    //get order type data
    @SuppressLint("Range")
    public ArrayList<HashMap<String, String>> getOrderType() {
        ArrayList<HashMap<String, String>> order_type = new ArrayList<>();
        Cursor cursor = database.rawQuery("SELECT * FROM order_type ORDER BY order_type_id DESC", null);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<String, String>();


                map.put("order_type_id", cursor.getString(cursor.getColumnIndex("order_type_id")));
                map.put("order_type_name", cursor.getString(cursor.getColumnIndex("order_type_name")));


                order_type.add(map);
            } while (cursor.moveToNext());
        }
        cursor.close();
        database.close();
        return order_type;
    }


    //get order type data
    @SuppressLint("Range")
    public ArrayList<HashMap<String, String>> getPaymentMethod() {
        ArrayList<HashMap<String, String>> payment_method = new ArrayList<>();
        Cursor cursor = database.rawQuery("SELECT * FROM payment_method ORDER BY payment_method_id DESC", null);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("payment_method_id", cursor.getString(cursor.getColumnIndex("payment_method_id")));
                map.put("payment_method_name", cursor.getString(cursor.getColumnIndex("payment_method_name")));
                map.put("payment_method_active", cursor.getString(cursor.getColumnIndex("payment_method_active")));

                payment_method.add(map);
            } while (cursor.moveToNext());
        }
        cursor.close();
        database.close();
        return payment_method;
    }


    public void getConfigurationTable() {
        //ArrayList<HashMap<String, String>> payment_method = new ArrayList<>();
        Cursor cursor = database.rawQuery("SELECT * FROM user", null);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<String, String>();
                //cursor.getString(cursor.getColumnIndex("payment_method_id"))
            } while (cursor.moveToNext());
        }
        cursor.close();
        database.close();
    }


    //get customer data
    @SuppressLint("Range")
    public ArrayList<HashMap<String, String>> searchCustomers(String s) {
        ArrayList<HashMap<String, String>> customer = new ArrayList<>();
        Cursor cursor = database.rawQuery("SELECT * FROM customers WHERE customer_name LIKE '%" + s + "%' ORDER BY customer_id DESC", null);

        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<String, String>();


                map.put("customer_id", cursor.getString(cursor.getColumnIndex("customer_id")));
                map.put("customer_name", cursor.getString(cursor.getColumnIndex("customer_name")));
                map.put("customer_cell", cursor.getString(cursor.getColumnIndex("customer_cell")));
                map.put("customer_email", cursor.getString(cursor.getColumnIndex("customer_email")));
                map.put("customer_address", cursor.getString(cursor.getColumnIndex("customer_address")));
                map.put("customer_active", cursor.getString(cursor.getColumnIndex("customer_active")));



                customer.add(map);
            } while (cursor.moveToNext());
        }
        cursor.close();
        database.close();
        return customer;
    }


    //get customer data
    public ArrayList<HashMap<String, String>> searchSuppliers(String s) {
        ArrayList<HashMap<String, String>> customer = new ArrayList<>();
        Cursor cursor = database.rawQuery("SELECT * FROM suppliers WHERE suppliers_name LIKE '%" + s + "%' ORDER BY suppliers_id DESC", null);

        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<String, String>();


                map.put("suppliers_id", cursor.getString(0));
                map.put("suppliers_name", cursor.getString(1));
                map.put("suppliers_contact_person", cursor.getString(2));
                map.put("suppliers_cell", cursor.getString(3));
                map.put("suppliers_email", cursor.getString(4));
                map.put("suppliers_address", cursor.getString(5));


                customer.add(map);
            } while (cursor.moveToNext());
        }
        cursor.close();
        database.close();
        return customer;
    }


    //get shop information
    public ArrayList<HashMap<String, String>> getShopInformation() {
        ArrayList<HashMap<String, String>> shop_info = new ArrayList<>();
        Cursor cursor = database.rawQuery("SELECT * FROM shop", null);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<String, String>();


                map.put("shop_name", cursor.getString(1));
                map.put("shop_contact", cursor.getString(2));
                map.put("shop_email", cursor.getString(3));
                map.put("shop_address", cursor.getString(4));
                map.put("shop_currency", cursor.getString(5));
                map.put("tax", cursor.getString(6));


                shop_info.add(map);
            } while (cursor.moveToNext());
        }
        cursor.close();
        database.close();
        return shop_info;
    }


    //get product data
    @SuppressLint("Range")
    public ArrayList<HashMap<String, String>> getProducts() {
        ArrayList<HashMap<String, String>> product = new ArrayList<>();
        Cursor cursor = database.rawQuery("SELECT * FROM products ORDER BY product_id DESC", null);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<>();

                map.put("product_id", cursor.getString(cursor.getColumnIndex("product_id")));
                map.put("product_active", cursor.getString(cursor.getColumnIndex("product_active")));
                map.put("product_buy_price", cursor.getString(cursor.getColumnIndex("product_buy_price")));
                map.put("product_category", cursor.getString(cursor.getColumnIndex("product_category")));
                map.put("product_code", cursor.getString(cursor.getColumnIndex("product_code")));
                map.put("product_description", cursor.getString(cursor.getColumnIndex("product_description")));
                map.put("product_image", cursor.getString(cursor.getColumnIndex("product_image")));
                map.put("product_name_en", cursor.getString(cursor.getColumnIndex("product_name_en")));
                map.put("product_name_ar", cursor.getString(cursor.getColumnIndex("product_name_ar")));
                map.put("product_sell_price", cursor.getString(cursor.getColumnIndex("product_sell_price")));
                map.put("product_stock", cursor.getString(cursor.getColumnIndex("product_stock")));
                map.put("product_supplier", cursor.getString(cursor.getColumnIndex("product_supplier")));
                map.put("product_tax", cursor.getString(cursor.getColumnIndex("product_tax")));
                map.put("product_weight", cursor.getString(cursor.getColumnIndex("product_weight")));
                map.put("product_weight_unit_id", cursor.getString(cursor.getColumnIndex("product_weight_unit_id")));
                map.put("product_count", "0");


                product.add(map);
            } while (cursor.moveToNext());
        }
        cursor.close();
        database.close();
        return product;
    }


    //get product data
    @SuppressLint("Range")
    public ArrayList<HashMap<String, String>> getProductsInfo(String product_id) {
        ArrayList<HashMap<String, String>> product = new ArrayList<>();
        Cursor cursor = database.rawQuery("SELECT * FROM products WHERE product_id='" + product_id + "'", null);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<String, String>();

                map.put("product_id", cursor.getString(cursor.getColumnIndex("product_id")));
                map.put("product_active", cursor.getString(cursor.getColumnIndex("product_active")));
                map.put("product_buy_price", cursor.getString(cursor.getColumnIndex("product_buy_price")));
                map.put("product_category", cursor.getString(cursor.getColumnIndex("product_category")));
                map.put("product_code", cursor.getString(cursor.getColumnIndex("product_code")));
                map.put("product_description", cursor.getString(cursor.getColumnIndex("product_description")));
                map.put("product_image", cursor.getString(cursor.getColumnIndex("product_image")));
                map.put("product_name_en", cursor.getString(cursor.getColumnIndex("product_name_en")));
                map.put("product_name_ar", cursor.getString(cursor.getColumnIndex("product_name_ar")));
                map.put("product_sell_price", cursor.getString(cursor.getColumnIndex("product_sell_price")));
                map.put("product_stock", cursor.getString(cursor.getColumnIndex("product_stock")));
                map.put("product_supplier", cursor.getString(cursor.getColumnIndex("product_supplier")));
                map.put("product_tax", cursor.getString(cursor.getColumnIndex("product_tax")));
                map.put("product_weight", cursor.getString(cursor.getColumnIndex("product_weight")));
                map.put("product_weight_unit_id", cursor.getString(cursor.getColumnIndex("product_weight_unit_id")));

                product.add(map);
            } while (cursor.moveToNext());
        }
        cursor.close();
        database.close();
        return product;
    }


    //get product data
    @SuppressLint("Range")
    public ArrayList<HashMap<String, String>> getAllExpense() {
        ArrayList<HashMap<String, String>> product = new ArrayList<>();
        Cursor cursor = database.rawQuery("SELECT * FROM expense ORDER BY expense_id DESC", null);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<String, String>();


                map.put("expense_id", cursor.getString(cursor.getColumnIndex("expense_id")));
                map.put("expense_name", cursor.getString(cursor.getColumnIndex("expense_name")));
                map.put("expense_note", cursor.getString(cursor.getColumnIndex("expense_note")));
                map.put("expense_amount", cursor.getString(cursor.getColumnIndex("expense_amount")));
                map.put("expense_date", cursor.getString(cursor.getColumnIndex("expense_date")));
                map.put("expense_time", cursor.getString(cursor.getColumnIndex("expense_time")));


                product.add(map);
            } while (cursor.moveToNext());
        }
        cursor.close();
        database.close();
        return product;
    }


    //get product category data
    @SuppressLint("Range")
    public ArrayList<HashMap<String, String>> getProductCategory() {
        ArrayList<HashMap<String, String>> product_category = new ArrayList<>();
        Cursor cursor = database.rawQuery("SELECT * FROM product_category ORDER BY category_id DESC", null);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<String, String>();


                map.put("category_id", cursor.getString(cursor.getColumnIndex("category_id")));
                map.put("category_name", cursor.getString(cursor.getColumnIndex("category_name")));

                product_category.add(map);
            } while (cursor.moveToNext());
        }

        cursor.close();
        database.close();

        return product_category;
    }


    //get product category data
    @SuppressLint("Range")
    public ArrayList<HashMap<String, String>> searchProductCategory(String s) {
        ArrayList<HashMap<String, String>> product_category = new ArrayList<>();
        Cursor cursor = database.rawQuery("SELECT * FROM product_category WHERE category_name LIKE '%" + s + "%' ORDER BY category_id DESC ", null);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<String, String>();


                map.put("category_id", cursor.getString(cursor.getColumnIndex("category_id")));
                map.put("category_name", cursor.getString(cursor.getColumnIndex("category_name")));


                product_category.add(map);
            } while (cursor.moveToNext());
        }

        cursor.close();
        database.close();

        return product_category;
    }


    //get product payment method
    @SuppressLint("Range")
    public ArrayList<HashMap<String, String>> searchPaymentMethod(String s) {
        ArrayList<HashMap<String, String>> payment_method = new ArrayList<>();
        Cursor cursor = database.rawQuery("SELECT * FROM payment_method WHERE payment_method_name LIKE '%" + s + "%' ORDER BY payment_method_id DESC ", null);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<String, String>();


                map.put("payment_method_id", cursor.getString(cursor.getColumnIndex("payment_method_id")));
                map.put("payment_method_name", cursor.getString(cursor.getColumnIndex("payment_method_name")));



                payment_method.add(map);
            } while (cursor.moveToNext());
        }

        cursor.close();
        database.close();

        return payment_method;
    }


    //search
    @SuppressLint("Range")
    public ArrayList<HashMap<String, String>> searchOrderType(String s) {
        ArrayList<HashMap<String, String>> payment_method = new ArrayList<>();
        Cursor cursor = database.rawQuery("SELECT * FROM order_type WHERE order_type_name LIKE '%" + s + "%' ", null);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<String, String>();


                map.put("order_type_id", cursor.getString(cursor.getColumnIndex("order_type_id")));
                map.put("order_type_name", cursor.getString(cursor.getColumnIndex("order_type_name")));

                payment_method.add(map);
            } while (cursor.moveToNext());
        }

        cursor.close();
        database.close();

        return payment_method;
    }


    //search
    @SuppressLint("Range")
    public ArrayList<HashMap<String, String>> searchUnit(String s) {
        ArrayList<HashMap<String, String>> unit = new ArrayList<>();
        Cursor cursor = database.rawQuery("SELECT * FROM product_weight WHERE weight_unit LIKE '%" + s + "%' ", null);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<String, String>();


                map.put("weight_id", cursor.getString(cursor.getColumnIndex("weight_id")));
                map.put("weight_unit", cursor.getString(cursor.getColumnIndex("weight_unit")));

                unit.add(map);
            } while (cursor.moveToNext());
        }

        cursor.close();
        database.close();

        return unit;
    }


    //get product supplier data
    @SuppressLint("Range")
    public ArrayList<HashMap<String, String>> getProductSupplier() {
        ArrayList<HashMap<String, String>> product_suppliers = new ArrayList<>();
        Cursor cursor = database.rawQuery("SELECT * FROM suppliers", null);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<String, String>();


                map.put("suppliers_id", cursor.getString(cursor.getColumnIndex("suppliers_id")));
                map.put("suppliers_name", cursor.getString(cursor.getColumnIndex("suppliers_name")));

                product_suppliers.add(map);
            } while (cursor.moveToNext());
        }

        cursor.close();
        database.close();

        return product_suppliers;
    }


    //get product supplier data
    @SuppressLint("Range")
    public ArrayList<HashMap<String, String>> getWeightUnit() {
        ArrayList<HashMap<String, String>> product_weight_unit = new ArrayList<>();
        Cursor cursor = database.rawQuery("SELECT * FROM product_weight", null);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<String, String>();


                map.put("weight_id", cursor.getString(cursor.getColumnIndex("weight_id")));
                map.put("weight_unit", cursor.getString(cursor.getColumnIndex("weight_unit")));

                product_weight_unit.add(map);
            } while (cursor.moveToNext());
        }

        cursor.close();
        database.close();

        return product_weight_unit;
    }

    //get product data
    @SuppressLint("Range")
    public ArrayList<HashMap<String, String>> searchExpense(String s) {
        ArrayList<HashMap<String, String>> product = new ArrayList<>();
        Cursor cursor = database.rawQuery("SELECT * FROM expense WHERE expense_name LIKE '%" + s + "%' ORDER BY expense_id DESC", null);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<String, String>();

                map.put("expense_id", cursor.getString(cursor.getColumnIndex("expense_id")));
                map.put("expense_name", cursor.getString(cursor.getColumnIndex("expense_name")));
                map.put("expense_note", cursor.getString(cursor.getColumnIndex("expense_note")));
                map.put("expense_amount", cursor.getString(cursor.getColumnIndex("expense_amount")));
                map.put("expense_date", cursor.getString(cursor.getColumnIndex("expense_date")));
                map.put("expense_time", cursor.getString(cursor.getColumnIndex("expense_time")));


                product.add(map);
            } while (cursor.moveToNext());
        }
        database.close();
        return product;
    }


    //get product data
    @SuppressLint("Range")
    public ArrayList<HashMap<String, String>> getSearchProducts(String s) {
        ArrayList<HashMap<String, String>> product = new ArrayList<>();
        Cursor cursor = database.rawQuery("SELECT * FROM products WHERE product_name LIKE '%" + s + "%' OR product_code LIKE '%" + s + "%' ORDER BY product_id DESC", null);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("product_id", cursor.getString(cursor.getColumnIndex("product_id")));
                map.put("product_active", cursor.getString(cursor.getColumnIndex("product_active")));
                map.put("product_buy_price", cursor.getString(cursor.getColumnIndex("product_buy_price")));
                map.put("product_category", cursor.getString(cursor.getColumnIndex("product_category")));
                map.put("product_code", cursor.getString(cursor.getColumnIndex("product_code")));
                map.put("product_description", cursor.getString(cursor.getColumnIndex("product_description")));
                map.put("product_image", cursor.getString(cursor.getColumnIndex("product_image")));
                map.put("product_name_en", cursor.getString(cursor.getColumnIndex("product_name_en")));
                map.put("product_name_ar", cursor.getString(cursor.getColumnIndex("product_name_ar")));
                map.put("product_sell_price", cursor.getString(cursor.getColumnIndex("product_sell_price")));
                map.put("product_stock", cursor.getString(cursor.getColumnIndex("product_stock")));
                map.put("product_supplier", cursor.getString(cursor.getColumnIndex("product_supplier")));
                map.put("product_tax", cursor.getString(cursor.getColumnIndex("product_tax")));
                map.put("product_weight", cursor.getString(cursor.getColumnIndex("product_weight")));
                map.put("product_weight_unit_id", cursor.getString(cursor.getColumnIndex("product_weight_unit_id")));
                product.add(map);
            } while (cursor.moveToNext());
        }
        cursor.close();
        database.close();
        return product;
    }


    //Add product into cart
    public int addToCart(String product_name, String price, String weight, int qty, String base64Image, String ref, String tva_tx, String product_id) {

        Cursor result = database.rawQuery("SELECT * FROM cart WHERE product_name='" + product_name + "' AND price='" + price + "' AND weight='" + weight + "'", null);
        if (result.getCount() >= 1) {

            return 2;
        } else {
            ContentValues values = new ContentValues();
            values.put("product_name", product_name);
            values.put("price", price);
            values.put("weight", weight);
            values.put("qty", qty);
            values.put("image", base64Image);

            values.put("ref", ref); //desc
            values.put("tva_tx", tva_tx);
            values.put("fk_product", product_id);


            long check = database.insert("cart", null, values);

            database.close();


            //if data insert success, its return 1, if failed return -1
            if (check == -1) {
                return -1;
            } else {
                return 1;
            }

        }

    }


    //get suppliers data
    public ArrayList<HashMap<String, String>> getSuppliers() {
        ArrayList<HashMap<String, String>> supplier = new ArrayList<>();
        Cursor cursor = database.rawQuery("SELECT * FROM suppliers ORDER BY suppliers_id DESC", null);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<String, String>();


                map.put("suppliers_id", cursor.getString(0));
                map.put("suppliers_name", cursor.getString(1));
                map.put("suppliers_contact_person", cursor.getString(2));
                map.put("suppliers_cell", cursor.getString(3));
                map.put("suppliers_email", cursor.getString(4));
                map.put("suppliers_address", cursor.getString(5));


                supplier.add(map);
            } while (cursor.moveToNext());
        }
        database.close();
        return supplier;
    }

    @SuppressLint("Range")
    public HashMap<String, String> getConfiguration() {
        HashMap<String, String> configuration = new HashMap<>();
        Cursor cursor = null;
        try {
            cursor = database.rawQuery("SELECT * FROM configuration WHERE id=" + 1, null);
            if (cursor != null && cursor.moveToFirst()) {
                configuration.put("id", cursor.getString(cursor.getColumnIndex("id")));
                configuration.put("ecr_code", cursor.getString(cursor.getColumnIndex("ecr_code")));
                configuration.put("merchant_id", cursor.getString(cursor.getColumnIndex("merchant_id")));
                configuration.put("merchant_logo", cursor.getString(cursor.getColumnIndex("merchant_logo")));
                configuration.put("merchant_tax_number", cursor.getString(cursor.getColumnIndex("merchant_tax_number")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            database.close();
        }
        return configuration;
    }

    @SuppressLint("Range")
    public String getSequence(int sequenceId, String ecrCode) {
        String sequence = "";
        Cursor cursor = null;
        int nextValue = -1;
        String prefix = "";
        try {
            cursor = database.rawQuery("SELECT * FROM sequence_text WHERE id=" + sequenceId, null);
            if (cursor != null && cursor.moveToFirst()) {
                nextValue = Integer.parseInt(cursor.getString(cursor.getColumnIndex("id")))+1;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        sequence = ecrCode + " - 001 - " +  cursor.getString(cursor.getColumnIndex("type_perfix")) + String.format("%010d", nextValue);
        boolean updated = updateSequence(nextValue,sequenceId);
        if(!updated)
            throw new RuntimeException("sequence is not updated");
        if (cursor != null) {
            cursor.close();
        }
        database.close();
        return sequence;
    }

    private boolean updateSequence(int nextValue, int sequenceId) {
        ContentValues values = new ContentValues();
        values.put("current_value", nextValue);
        long check = database.update("sequence_text", values, "id=? ", new String[]{String.valueOf(sequenceId)});
        database.close();
        return check != -1;
    }


    //delete customer
    public boolean deleteCustomer(String customer_id) {


        long check = database.delete("customers", "customer_id=?", new String[]{customer_id});

        database.close();

        if (check == 1) {
            return true;
        } else {
            return false;
        }

    }


    //delete category
    public boolean deleteCategory(String category_id) {


        long check = database.delete("product_category", "category_id=?", new String[]{category_id});

        database.close();

        if (check == 1) {
            return true;
        } else {
            return false;
        }

    }


    //delete payment method
    public boolean deletePaymentMethod(String payment_method_id) {


        long check = database.delete("payment_method", "payment_method_id=?", new String[]{payment_method_id});

        database.close();

        if (check == 1) {
            return true;
        } else {
            return false;
        }

    }


    //delete order Type
    public boolean deleteOrderType(String typeId) {


        long check = database.delete("order_type", "order_type_id=?", new String[]{typeId});
        database.close();

        if (check == 1) {
            return true;
        } else {
            return false;
        }

    }


    //delete unit
    public boolean deleteUnit(String unitId) {

        long check = database.delete("product_weight", "weight_id=?", new String[]{unitId});
        database.close();

        if (check == 1) {
            return true;
        } else {
            return false;
        }

    }


    //update order
    public boolean updateOrder(String invoiceId, String orderStatus) {


        ContentValues contentValues = new ContentValues();
        contentValues.put(Constant.ORDER_STATUS, orderStatus);

        long check = database.update(Constant.orderList, contentValues, "invoice_id=?", new String[]{invoiceId});
        database.update(Constant.orderDetails, contentValues, "invoice_id=?", new String[]{invoiceId});


        database.close();

        if (check == 1) {
            return true;
        } else {
            return false;
        }

    }


    //delete product
    public boolean deleteProduct(String product_id) {


        long check = database.delete("products", "product_id=?", new String[]{product_id});
        long check2 = database.delete("product_cart", "product_id=?", new String[]{product_id});

        database.close();

        if (check == 1) {
            return true;
        } else {
            return false;
        }

    }


    //delete product
    public boolean deleteExpense(String expense_id) {


        long check = database.delete("expense", "expense_id=?", new String[]{expense_id});

        database.close();

        if (check == 1) {
            return true;
        } else {
            return false;
        }

    }


    //delete supplier
    public boolean deleteSupplier(String customer_id) {


        long check = database.delete("suppliers", "suppliers_id=?", new String[]{customer_id});

        database.close();

        if (check == 1) {
            return true;
        } else {
            return false;
        }

    }
}