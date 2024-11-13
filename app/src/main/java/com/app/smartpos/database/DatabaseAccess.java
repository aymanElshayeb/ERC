package com.app.smartpos.database;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

import com.app.smartpos.Constant;
import com.app.smartpos.R;
import com.app.smartpos.Registration.dto.RegistrationResponseDto;
import com.app.smartpos.common.Utils;
import com.app.smartpos.settings.end_shift.EndShiftModel;
import com.app.smartpos.settings.end_shift.ShiftDifferences;
import com.app.smartpos.utils.LocaleManager;
import com.app.smartpos.utils.MultiLanguageApp;
import com.app.smartpos.utils.SharedPrefUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


public class DatabaseAccess {
    private static DatabaseAccess instance;
    private final SQLiteOpenHelper openHelper;
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
        values.put("customer_active", customer_active);

        long check = database.insert("customers", null, values);
        database.close();

        //if data insert success, its return 1, if failed return -1
        return check != -1;
    }


    //insert category
    public boolean addCategory(String category_name) {

        ContentValues values = new ContentValues();


        values.put("category_name", category_name);


        long check = database.insert("product_category", null, values);
        database.close();

        //if data insert success, its return 1, if failed return -1
        return check != -1;
    }


    //insert payment method
    public boolean addPaymentMethod(String payment_method_name, boolean payment_method_active) {

        ContentValues values = new ContentValues();


        values.put("payment_method_name", payment_method_name);
        values.put("payment_method_active", payment_method_active);

        long check = database.insert("payment_method", null, values);
        database.close();

        //if data insert success, its return 1, if failed return -1
        return check != -1;
    }


    //insert unit
    public boolean addUnit(String unitName) {

        ContentValues values = new ContentValues();


        values.put("weight_unit", unitName);


        long check = database.insert("product_weight", null, values);
        database.close();

        //if data insert success, its return 1, if failed return -1
        return check != -1;
    }


    //insert order type
    public boolean addOrderType(String orderType) {

        ContentValues values = new ContentValues();


        values.put("order_type_name", orderType);


        long check = database.insert("order_type", null, values);
        database.close();

        //if data insert success, its return 1, if failed return -1
        return check != -1;
    }

    public String getLastShift(String key) {
        String result = "";
        Cursor cursor = database.rawQuery("SELECT * FROM shift ORDER BY id DESC LIMIT 1", null);
        if (cursor.moveToFirst()) {
            result = cursor.getString(cursor.getColumnIndex(key));
        }
        database.close();
        return result;
    }

    public int addShift(EndShiftModel endShiftModel) {
        Utils.addLog("datadata", endShiftModel.toString());
        ContentValues values = new ContentValues();

        values.put("sequence", endShiftModel.getSequence());
        values.put("device_id", endShiftModel.getDeviceID());
        values.put("username", endShiftModel.getUserName());
        values.put("start_date_time", endShiftModel.getStartDateTime());
        values.put("end_date_time", endShiftModel.getEndDateTime());

        String startDateIso = new Date(endShiftModel.getStartDateTime()).toString();
        String endDateIso = new Date(endShiftModel.getEndDateTime()).toString();

        values.put("start_date_time_iso", startDateIso);
        values.put("end_date_time_iso", endDateIso);
        double total_cash = 0;
        double diff_cash = 0;
        if (endShiftModel.getShiftDifferences().containsKey("CASH")) {
            ShiftDifferences shiftDifferences = endShiftModel.getShiftDifferences().get("CASH");
            total_cash = shiftDifferences.getReal();
            diff_cash = shiftDifferences.getDiff();
        }
        values.put("total_cash", Utils.trimLongDouble(total_cash));
        values.put("difference_cash", Utils.trimLongDouble(diff_cash));

        values.put("start_cash", endShiftModel.getStartCash());
        values.put("leave_cash", endShiftModel.getLeaveCash());

        values.put("num_successful_transaction", endShiftModel.getNum_successful_transaction());
        values.put("num_canceled_transaction", endShiftModel.getNum_canceled_transaction());
        values.put("num_returned_transaction", endShiftModel.getNum_returned_transaction());
        values.put("user_mail", SharedPrefUtils.getEmail(MultiLanguageApp.getApp()));

        values.put("notes", endShiftModel.getNote());

        long check = 0;
        try {
            check = database.insertOrThrow("shift", null, values);
        } catch (Exception e) {
            Utils.addLog("datadata", e.getMessage());
        }


        //if data insert success, its return 1, if failed return -1
        if (check == -1) {
            database.close();
            return -1;
        } else {
            return getShiftWithTimestamp(endShiftModel.getEndDateTime());
        }

    }

    public boolean addShiftCreditCalculations(String id, ShiftDifferences shiftDifference, String type) {
        ContentValues values = new ContentValues();

        values.put("shift_id", id);
        values.put("total", Utils.trimLongDouble(shiftDifference.getReal()));
        values.put("credit_code", shiftDifference.getCode());
        values.put("difference", Utils.trimLongDouble(shiftDifference.getDiff()));

        //Utils.addLog("datadata_shift_diff", values.toString());

        long check = database.insert("credit_calculations", null, values);
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
        return check != -1;
    }


    //update payment method
    public boolean updatePaymentMethod(String payment_method_id, String payment_method_name, boolean payment_method_active) {

        ContentValues values = new ContentValues();


        values.put("payment_method_name", payment_method_name);
        values.put("payment_method_active", payment_method_active);


        long check = database.update("payment_method", values, "payment_method_id=? ", new String[]{payment_method_id});
        database.close();

        //if data insert success, its return 1, if failed return -1
        return check != -1;
    }


    //update order type
    public boolean updateOrderType(String typeId, String orderTypeName) {

        ContentValues values = new ContentValues();


        values.put("order_type_name", orderTypeName);


        long check = database.update("order_type", values, "order_type_id=? ", new String[]{typeId});
        database.close();

        //if data insert success, its return 1, if failed return -1
        return check != -1;
    }


    //update weight unit
    public boolean updateWeightUnit(String weightId, String weightUnit) {

        ContentValues values = new ContentValues();


        values.put("weight_unit", weightUnit);


        long check = database.update("product_weight", values, "weight_id=? ", new String[]{weightId});
        database.close();

        //if data insert success, its return 1, if failed return -1
        return check != -1;
    }


    //update customer
    public boolean updateCustomer(String customer_id, String customer_name, String customer_cell, String customer_email, String customer_address, boolean customer_active) {

        ContentValues values = new ContentValues();


        values.put("customer_name", customer_name);
        values.put("customer_cell", customer_cell);
        values.put("customer_email", customer_email);
        values.put("customer_address", customer_address);
        values.put("customer_active", customer_active);

        long check = database.update("customers", values, " customer_id=? ", new String[]{customer_id});
        database.close();

        //if data insert success, its return 1, if failed return -1
        return check != -1;
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
        return check != -1;
    }


    //insert products
    public boolean addProduct(String product_name, String product_code, String product_category, String product_description, String product_buy_price, String product_sell_price, String product_stock, String product_supplier, String product_image, String weight_unit_id, String product_weight) {

        ContentValues values = new ContentValues();


        values.put("product_name_en", product_name);
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
        return check != -1;
    }


    public boolean updateProductImage(ContentValues productValues) {

        ContentValues values = new ContentValues();

        values.put("image_url", productValues.get("image_url") == null ? null : productValues.get("image_url").toString());
        values.put("base64_image", productValues.get("base64_image") == null ? null : productValues.get("base64_image").toString());
        values.put("image_thumbnail_url", productValues.get("image_thumbnail_url") == null ? null : productValues.get("image_thumbnail_url").toString());
        values.put("image_thumbnail", productValues.get("image_thumbnail") == null ? null : productValues.get("image_thumbnail").toString());
        values.put("product_uuid", productValues.get("product_uuid") == null ? null : productValues.get("product_uuid").toString());

        Utils.addLog("datadata", productValues.toString());
        Cursor cursor = database.rawQuery("SELECT * FROM product_image WHERE product_uuid= '" + productValues.get("product_uuid").toString() + "'", null);

        long check;
        if (cursor.moveToFirst()) {
            do {
                check = database.update("product_image", values, "product_uuid=?", new String[]{productValues.get("product_uuid").toString()});
                Utils.addLog("datadata_check", String.valueOf(check));
            } while (cursor.moveToNext());
        } else {
            check = database.insert("product_image", null, values);
        }

        database.close();

        return check != -1;
        //if data insert success, its return 1, if failed return -1

    }

    public String getProductImage(boolean isInternetConnected, String product_uuid) {

        Cursor cursor = database.rawQuery("SELECT * FROM product_image WHERE product_uuid= '" + product_uuid + "'", null);
        String image = null;
        if (cursor.moveToFirst()) {
            do {
                if (isInternetConnected) {
                    image = cursor.getString(cursor.getColumnIndex("image_thumbnail_url"));
                    if (image == null) {
                        image = cursor.getString(cursor.getColumnIndex("image_url"));
                    }
                }
                if (image == null) {
                    image = cursor.getString(cursor.getColumnIndex("image_thumbnail"));
                    if (image == null) {
                        image = cursor.getString(cursor.getColumnIndex("base64_image"));
                    }
                }

            } while (cursor.moveToNext());
        }

        database.close();

        return image;
        //if data insert success, its return 1, if failed return -1

    }

    //insert products
    public boolean updateProduct(String product_name, String product_code, String product_category, String product_description, String product_buy_price, String product_sell_price, String product_stock, String product_supplier, String product_image, String weight_unit_id, String product_weight, String product_id) {

        ContentValues values = new ContentValues();


        values.put("product_name_en", product_name);
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
        return check != -1;
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
        return check != -1;
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
        return check != -1;
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
        return check != -1;
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
        return check != -1;
    }


    //get product image base 64
//    @SuppressLint("Range")
//    public String getProductImage(String product_id) {
//
//        String image = "n/a";
//        Cursor cursor = database.rawQuery("SELECT * FROM products WHERE product_id='" + product_id + "'", null);
//
//
//        if (cursor.moveToFirst()) {
//            do {
//
//
//                image = cursor.getString(cursor.getColumnIndex("product_image"));
//
//
//            } while (cursor.moveToNext());
//        }
//
//
//        cursor.close();
//        database.close();
//        return image;
//    }

    @SuppressLint("Range")
    public int getShiftWithTimestamp(long timeStamp) {

        int id = 0;
        Cursor cursor = database.rawQuery("SELECT * FROM shift WHERE end_date_time= " + timeStamp, null);


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
    public HashMap<String, String> getUserWithUserName(String userName) {

        Cursor cursor = database.rawQuery("SELECT * FROM user WHERE username='" + userName + "'", null);

        HashMap<String, String> hashMap = null;
        if (cursor.moveToFirst()) {
            hashMap = new HashMap<>();
            do {
                hashMap.put("id", cursor.getString(cursor.getColumnIndex("id")));
                hashMap.put("name_en", cursor.getString(cursor.getColumnIndex("name_en")));
                hashMap.put("name_ar", cursor.getString(cursor.getColumnIndex("name_ar")));
                hashMap.put("email", cursor.getString(cursor.getColumnIndex("email")));
                hashMap.put("password", cursor.getString(cursor.getColumnIndex("password")));
                hashMap.put("username", cursor.getString(cursor.getColumnIndex("username")));

            } while (cursor.moveToNext());
        }


        cursor.close();
        database.close();
        return hashMap;
    }

    public List<HashMap<String, String>> getAllUsers() {

        Cursor cursor = database.rawQuery("SELECT * FROM user", null);

        List<HashMap<String, String>> list = new ArrayList<>();
        if (cursor.moveToFirst()) {
            HashMap<String, String> hashMap = new HashMap<>();
            do {
                hashMap.put("id", cursor.getString(cursor.getColumnIndex("id")));
                hashMap.put("name_en", cursor.getString(cursor.getColumnIndex("name_en")));
                hashMap.put("name_ar", cursor.getString(cursor.getColumnIndex("name_ar")));
                hashMap.put("email", cursor.getString(cursor.getColumnIndex("email")));
                hashMap.put("password", cursor.getString(cursor.getColumnIndex("password")));
                hashMap.put("username", cursor.getString(cursor.getColumnIndex("username")));
                hashMap.put("mobile", cursor.getString(cursor.getColumnIndex("mobile")));
                list.add(hashMap);
            } while (cursor.moveToNext());
        }


        cursor.close();
        database.close();
        return list;
    }

    public HashMap<String, String> getUserWithEmail(String email) {

        Cursor cursor = database.rawQuery("SELECT * FROM user WHERE email='" + email + "'", null);

        HashMap<String, String> hashMap = null;
        if (cursor.moveToFirst()) {
            hashMap = new HashMap<>();
            do {
                hashMap.put("id", cursor.getString(cursor.getColumnIndex("id")));
                hashMap.put("name_en", cursor.getString(cursor.getColumnIndex("name_en")));
                hashMap.put("name_ar", cursor.getString(cursor.getColumnIndex("name_ar")));
                hashMap.put("email", cursor.getString(cursor.getColumnIndex("email")));
                hashMap.put("password", cursor.getString(cursor.getColumnIndex("password")));
                hashMap.put("username", cursor.getString(cursor.getColumnIndex("username")));
                hashMap.put("mobile", cursor.getString(cursor.getColumnIndex("mobile")));

            } while (cursor.moveToNext());
        }


        cursor.close();
        database.close();
        return hashMap;
    }


    //get product weight unit name
    @SuppressLint("Range")
    public String getWeightUnitName(String weight_unit_id) {
        Cursor cursor = null;
        String weight_unit_name = "n/a";
        try {
            cursor = database.rawQuery("SELECT * FROM product_weight WHERE weight_id=" + weight_unit_id, null);
            if (cursor.moveToFirst()) {
                do {


                    weight_unit_name = cursor.getString(cursor.getColumnIndex("weight_unit"));


                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cursor.close();
            database.close();
        }
        return weight_unit_name;
    }


    //get product weight unit name
    @SuppressLint("Range")
    public String getSupplierName(String supplier_id) {

        String supplier_name = "n/a";
        Cursor cursor = database.rawQuery("SELECT * FROM suppliers WHERE suppliers_id=" + supplier_id, null);


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
        if (category_id.isEmpty() || category_id.isBlank()) return product_category;
        Cursor cursor = database.rawQuery("SELECT * FROM product_category WHERE category_id=" + category_id, null);


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
    public int addToCart(String product_id, String weight, String weight_unit, String price, int qty, String stock, String product_uuid, String description) {


        Cursor result = database.rawQuery("SELECT * FROM product_cart WHERE product_id='" + product_id + "'", null);
        if (result.getCount() >= 1) {

            return 2;

        } else {
            ContentValues values = new ContentValues();
            values.put("product_id", product_id);
            values.put("product_weight", weight);
            values.put("product_weight_unit", weight_unit);
            values.put("product_price", price);
            values.put("product_description", description);
            values.put("product_qty", qty);
            values.put("product_uuid", product_uuid);
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

    public void updateProductInCart(String product_id, String count) {


        SQLiteStatement result = database.compileStatement("UPDATE product_cart SET product_qty='" + count + "' WHERE product_id='" + product_id + "'");


        result.execute();


        database.close();


    }

    public void removeProductFromCart(String productId) {

        SQLiteStatement result = database.compileStatement("DELETE FROM product_cart WHERE product_id ='" + productId + "'");

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


                map.put("cart_id", cursor.getString(cursor.getColumnIndex("cart_id")));
                map.put("product_id", cursor.getString(cursor.getColumnIndex("product_id")));
                map.put("product_weight", cursor.getString(cursor.getColumnIndex("product_weight")));
                map.put("product_weight_unit", cursor.getString(cursor.getColumnIndex("product_weight_unit")));
                map.put("product_price", cursor.getString(cursor.getColumnIndex("product_price")));
                map.put("product_qty", cursor.getString(cursor.getColumnIndex("product_qty")));
                map.put("product_uuid", cursor.getString(cursor.getColumnIndex("product_uuid")));
                map.put("product_description", cursor.getString(cursor.getColumnIndex("product_description")));
                map.put("stock", cursor.getString(cursor.getColumnIndex("stock")));


                product.add(map);
            } while (cursor.moveToNext());
        }

        cursor.close();
        database.close();
        return product;
    }

    public HashMap<String, String> getCartProductById(String productId) {
        HashMap<String, String> map = null;
        Cursor cursor = database.rawQuery("SELECT * FROM product_cart where product_id = '" + productId + "'", null);
        if (cursor.moveToFirst()) {
            do {

                map = new HashMap<>();

                map.put("cart_id", cursor.getString(0));
                map.put("product_id", cursor.getString(1));
                map.put("product_weight", cursor.getString(2));
                map.put("product_weight_unit", cursor.getString(3));
                map.put("product_price", cursor.getString(4));
                map.put("product_qty", cursor.getString(5));
                map.put("stock", cursor.getString(6));

            } while (cursor.moveToNext());
        }

        cursor.close();
        database.close();
        return map;
    }


    public long insertCardDetails(String name, String code) {
        long id = 0;
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("code", code);
        values.put("active", 1);

        try {
            id = database.insertOrThrow("card_type", null, values);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Utils.addLog("datadata", String.valueOf(id));


        return id;
    }

    //insert order in order list
    public void insertOrder(String order_id, JSONObject obj, Context context, boolean deleteCart, DatabaseAccess databaseAccess) {

        ContentValues values = new ContentValues();
        ContentValues values2 = new ContentValues();
        ContentValues values3 = new ContentValues();
        String order_status = null;
        try {
            String order_date = obj.getString("order_date");
            String order_time = obj.getString("order_time");
            long order_timestamp = obj.getLong("order_timestamp");
            String order_type = obj.getString("order_type");
            order_status = obj.getString("order_status");
            String card_type_code = obj.getString("card_type_code");
            String approval_code = obj.getString("approval_code");
            String order_payment_method = obj.getString("order_payment_method");
            String customer_name = obj.getString("customer_name");
            String tax = obj.getString("tax");
            String discount = obj.getString("discount");
            String ecr_code = obj.getString("ecr_code");
            double in_tax_total = obj.getDouble("in_tax_total");
            double ex_tax_total = obj.getDouble("ex_tax_total");
            double paid_amount = obj.getDouble("paid_amount");
            double change_amount = obj.getDouble("change_amount");
//            String tax_number = obj.getString("tax_number");
            String original_order_id = obj.has("original_order_id") ? obj.getString("original_order_id") : "";
            String operation_type = obj.getString("operation_type");
            String operation_sub_type = obj.getString("operation_sub_type");
            boolean print = obj.getBoolean("printed");


            values.put("invoice_id", order_id);
            values.put("order_date", order_date);
            values.put("order_time", order_time);
            values.put("order_timestamp", order_timestamp);
            values.put("order_type", order_type);
            values.put("order_payment_method", order_payment_method);
            values.put("customer_name", customer_name);
            values.put("original_order_id", "");
            values.put("card_type_code", card_type_code);
            values.put("approval_code", approval_code);
            values.put("operation_sub_type", operation_sub_type);
            values.put("ecr_code", ecr_code);
            values.put("tax", tax);
            values.put("discount", discount);
            values.put("in_tax_total", in_tax_total);
            values.put("ex_tax_total", ex_tax_total);
            values.put("paid_amount", paid_amount);
            values.put("change_amount", change_amount);
//            values.put("tax_number", tax_number);
            values.put("operation_type", operation_type);
            values.put("original_order_id", original_order_id);
            values.put("order_status", order_status);
            values.put("printed", print);

            values.put("qr_code", "");


            database.insert("order_list", null, values);

            if (deleteCart)
                database.delete("product_cart", null, null);


        } catch (JSONException e) {
            e.printStackTrace();
        }


        try {

            JSONArray result = obj.getJSONArray("lines");

            for (int i = 0; i < result.length(); i++) {
                JSONObject jo = result.getJSONObject(i);
                String product_name_en = jo.getString("product_name_en");
                String product_name_ar = jo.getString("product_name_ar"); //ref
                String product_weight = jo.getString("product_weight");
                String product_qty = jo.getString("product_qty");
                String product_price = jo.getString("product_price");
                String product_image = jo.getString("product_image");
                String product_order_date = jo.getString("product_order_date");
                String product_uuid = jo.getString("product_uuid");
                String product_description = jo.getString("product_description");
                String product_id = getProductIdByUuid(product_uuid);
                double taxPercentage;
                databaseAccess.open();
                if (!product_description.isEmpty()) {
                    HashMap<String, String> shop = databaseAccess.getShopInformation();
                    taxPercentage = Double.parseDouble(shop.get("shop_tax"));
                } else
                    taxPercentage = getProductTax(product_id);
//                String stock = jo.getString("stock");
                double in_tax_total = Double.parseDouble(product_price) * Double.parseDouble(product_qty);
                double ex_tax_total = in_tax_total - ((in_tax_total * taxPercentage) / (100 + taxPercentage));
                int updated_stock = Integer.MAX_VALUE - Integer.parseInt(product_qty);


                values2.put("invoice_id", order_id);
                values2.put("product_name_en", product_name_en);
                values2.put("product_name_ar", product_name_ar);
                values2.put("product_uuid", product_uuid);
                values2.put("product_weight", product_weight);
                values2.put("in_tax_total", in_tax_total);
                values2.put("ex_tax_total", ex_tax_total);
                values2.put("tax_amount", in_tax_total - ex_tax_total);
                values2.put("product_qty", product_qty);
                values2.put("product_price", product_price);
                openDatabase();
                values2.put("tax_percentage", getProductTax(product_id));
                openDatabase();
                values2.put("product_image", product_image);
                values2.put("product_order_date", product_order_date);
                values2.put("order_status", order_status);
                values2.put("product_description", product_description);

                //for stock update
                values3.put("product_stock", updated_stock);
                //for order insert
                long check = database.insert("order_details", null, values2);
                Utils.addLog("datadata", "insertOrder: " + check);

                //updating stock
                database.update("products", values3, "product_id=?", new String[]{product_id});

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        database.close();
    }

    public void updateOrderPrintFlag(boolean value, String order_id) {

        ContentValues values = new ContentValues();

        values.put("printed", value);

        long check = database.update("order_list", values, "invoice_id=?", new String[]{order_id});
        database.close();

    }

    private void openDatabase() {
        DatabaseAccess databaseAccess_ = DatabaseAccess.getInstance(MultiLanguageApp.getApp());
        databaseAccess_.open();
    }

    @SuppressLint("Range")
    public String getProductIdByUuid(String productUuid) {
        String product_id = "n/a";
        Cursor cursor = database.rawQuery("SELECT * FROM products WHERE product_uuid='" + productUuid + "'", null);


        if (cursor.moveToFirst()) {
            do {
                product_id = cursor.getString(cursor.getColumnIndex("product_id"));
            } while (cursor.moveToNext());
        }
        database.close();
        cursor.close();
        return product_id;
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
                map.put("order_timestamp", cursor.getString(cursor.getColumnIndex("order_timestamp")));
                map.put("order_type", cursor.getString(cursor.getColumnIndex("order_type")));
                map.put("order_payment_method", cursor.getString(cursor.getColumnIndex("order_payment_method")));
                map.put("customer_name", cursor.getString(cursor.getColumnIndex("customer_name")));
                map.put("tax", cursor.getString(cursor.getColumnIndex("tax")));
                map.put("discount", cursor.getString(cursor.getColumnIndex("discount")));
                map.put("card_type_code", cursor.getString(cursor.getColumnIndex("card_type_code")));
                map.put("approval_code", cursor.getString(cursor.getColumnIndex("approval_code")));
                map.put("original_order_id", cursor.getString(cursor.getColumnIndex("original_order_id")));
                map.put("ecr_code", cursor.getString(cursor.getColumnIndex("ecr_code")));
                map.put("ex_tax_total", cursor.getString(cursor.getColumnIndex("ex_tax_total")));
                map.put("in_tax_total", cursor.getString(cursor.getColumnIndex("in_tax_total")));
                map.put("paid_amount", cursor.getString(cursor.getColumnIndex("paid_amount")));
                map.put("change_amount", cursor.getString(cursor.getColumnIndex("change_amount")));
                map.put("tax_number", cursor.getString(cursor.getColumnIndex("tax_number")));
                map.put("operation_type", cursor.getString(cursor.getColumnIndex("operation_type")));
                map.put("qr_code", cursor.getString(cursor.getColumnIndex("qr_code")));


                map.put(Constant.ORDER_STATUS, cursor.getString(cursor.getColumnIndex(Constant.ORDER_STATUS)));


                orderList.add(map);
            } while (cursor.moveToNext());
        }
        cursor.close();
        database.close();
        return orderList;
    }

    @SuppressLint("Range")
    public ArrayList<HashMap<String, String>> getOrderListPaginated(int offet, boolean isReund) {
        ArrayList<HashMap<String, String>> orderList = new ArrayList<>();
        String where = "";
        if (isReund) {
            where = "Where operation_type = 'invoice' OR order_status = '" + Constant.REFUNDED + "'";
        }
        Cursor cursor = database.rawQuery("SELECT * FROM order_list " + where + " ORDER BY order_id DESC LIMIT 10 OFFSET " + offet, null);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<>();
                map.put("invoice_id", cursor.getString(cursor.getColumnIndex("invoice_id")));
                map.put("order_date", cursor.getString(cursor.getColumnIndex("order_date")));
                map.put("order_time", cursor.getString(cursor.getColumnIndex("order_time")));
                map.put("order_timestamp", cursor.getString(cursor.getColumnIndex("order_timestamp")));
                map.put("order_type", cursor.getString(cursor.getColumnIndex("order_type")));
                map.put("order_payment_method", cursor.getString(cursor.getColumnIndex("order_payment_method")));
                map.put("customer_name", cursor.getString(cursor.getColumnIndex("customer_name")));
                map.put("tax", cursor.getString(cursor.getColumnIndex("tax")));
                map.put("discount", cursor.getString(cursor.getColumnIndex("discount")));
                map.put("card_type_code", cursor.getString(cursor.getColumnIndex("card_type_code")));
                map.put("approval_code", cursor.getString(cursor.getColumnIndex("approval_code")));
                map.put("original_order_id", cursor.getString(cursor.getColumnIndex("original_order_id")));
                map.put("ecr_code", cursor.getString(cursor.getColumnIndex("ecr_code")));
                map.put("ex_tax_total", cursor.getString(cursor.getColumnIndex("ex_tax_total")));
                map.put("in_tax_total", cursor.getString(cursor.getColumnIndex("in_tax_total")));
                map.put("paid_amount", cursor.getString(cursor.getColumnIndex("paid_amount")));
                map.put("change_amount", cursor.getString(cursor.getColumnIndex("change_amount")));
                map.put("tax_number", cursor.getString(cursor.getColumnIndex("tax_number")));
                map.put("operation_type", cursor.getString(cursor.getColumnIndex("operation_type")));
                map.put("operation_sub_type", cursor.getString(cursor.getColumnIndex("operation_sub_type")));
                map.put("qr_code", cursor.getString(cursor.getColumnIndex("qr_code")));
                map.put("printed", cursor.getString(cursor.getColumnIndex("printed")));


                map.put(Constant.ORDER_STATUS, cursor.getString(cursor.getColumnIndex(Constant.ORDER_STATUS)));


                orderList.add(map);
            } while (cursor.moveToNext());
        }
        cursor.close();
        database.close();
        return orderList;
    }

    @SuppressLint("Range")
    public HashMap<String, String> getOrderListByOrderId(String order_id) {
        HashMap<String, String> orderListMap = new HashMap<>();
        Cursor cursor = database.rawQuery("SELECT * FROM order_list WHERE invoice_id='" + order_id + "'", null);
        if (cursor.moveToFirst()) {
            do {
                orderListMap.put("invoice_id", cursor.getString(cursor.getColumnIndex("invoice_id")));
                orderListMap.put("order_date", cursor.getString(cursor.getColumnIndex("order_date")));
                orderListMap.put("order_time", cursor.getString(cursor.getColumnIndex("order_time")));
                orderListMap.put("order_timestamp", cursor.getString(cursor.getColumnIndex("order_timestamp")));
                orderListMap.put("order_type", cursor.getString(cursor.getColumnIndex("order_type")));
                orderListMap.put("order_payment_method", cursor.getString(cursor.getColumnIndex("order_payment_method")));
                orderListMap.put("customer_name", cursor.getString(cursor.getColumnIndex("customer_name")));
                orderListMap.put("tax", cursor.getString(cursor.getColumnIndex("tax")));
                orderListMap.put("discount", cursor.getString(cursor.getColumnIndex("discount")));
                orderListMap.put("card_type_code", cursor.getString(cursor.getColumnIndex("card_type_code")));
                orderListMap.put("approval_code", cursor.getString(cursor.getColumnIndex("approval_code")));
                orderListMap.put("original_order_id", cursor.getString(cursor.getColumnIndex("original_order_id")));
                orderListMap.put("ecr_code", cursor.getString(cursor.getColumnIndex("ecr_code")));
                orderListMap.put("ex_tax_total", cursor.getString(cursor.getColumnIndex("ex_tax_total")));
                orderListMap.put("in_tax_total", cursor.getString(cursor.getColumnIndex("in_tax_total")));
                orderListMap.put("paid_amount", cursor.getString(cursor.getColumnIndex("paid_amount")));
                orderListMap.put("change_amount", cursor.getString(cursor.getColumnIndex("change_amount")));
                orderListMap.put("tax_number", cursor.getString(cursor.getColumnIndex("tax_number")));
                orderListMap.put("operation_type", cursor.getString(cursor.getColumnIndex("operation_type")));
                orderListMap.put("operation_sub_type", cursor.getString(cursor.getColumnIndex("operation_sub_type")));
                orderListMap.put("qr_code", cursor.getString(cursor.getColumnIndex("qr_code")));
                orderListMap.put(Constant.ORDER_STATUS, cursor.getString(cursor.getColumnIndex(Constant.ORDER_STATUS)));
            } while (cursor.moveToNext());
        }
        cursor.close();
        database.close();
        return orderListMap;
    }

    @SuppressLint("Range")
    public ArrayList<HashMap<String, String>> getOrderListWithTime(long time) {
        ArrayList<HashMap<String, String>> orderList = new ArrayList<>();
        Cursor cursor = database.rawQuery("SELECT * FROM order_list WHERE order_timestamp > " + time, null);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<>();

                map.put("invoice_id", cursor.getString(cursor.getColumnIndex("invoice_id")));
                map.put("order_date", cursor.getString(cursor.getColumnIndex("order_date")));
                map.put("order_time", cursor.getString(cursor.getColumnIndex("order_time")));
                map.put("order_timestamp", cursor.getString(cursor.getColumnIndex("order_timestamp")));
                map.put("order_type", cursor.getString(cursor.getColumnIndex("order_type")));
                map.put("order_payment_method", cursor.getString(cursor.getColumnIndex("order_payment_method")));
                map.put("customer_name", cursor.getString(cursor.getColumnIndex("customer_name")));
                map.put("tax", cursor.getString(cursor.getColumnIndex("tax")));
                map.put("discount", cursor.getString(cursor.getColumnIndex("discount")));
                map.put("card_type_code", cursor.getString(cursor.getColumnIndex("card_type_code")));
                map.put("approval_code", cursor.getString(cursor.getColumnIndex("approval_code")));
                map.put("original_order_id", cursor.getString(cursor.getColumnIndex("original_order_id")));
                map.put("ecr_code", cursor.getString(cursor.getColumnIndex("ecr_code")));
                map.put("ex_tax_total", cursor.getString(cursor.getColumnIndex("ex_tax_total")));
                map.put("in_tax_total", cursor.getString(cursor.getColumnIndex("in_tax_total")));
                map.put("paid_amount", cursor.getString(cursor.getColumnIndex("paid_amount")));
                map.put("change_amount", cursor.getString(cursor.getColumnIndex("change_amount")));
                map.put("tax_number", cursor.getString(cursor.getColumnIndex("tax_number")));
                map.put("operation_type", cursor.getString(cursor.getColumnIndex("operation_type")));
                map.put("operation_sub_type", cursor.getString(cursor.getColumnIndex("operation_sub_type")));
                map.put("qr_code", cursor.getString(cursor.getColumnIndex("qr_code")));


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
                map.put("invoice_id", cursor.getString(cursor.getColumnIndex("invoice_id")));
                map.put("order_date", cursor.getString(cursor.getColumnIndex("order_date")));
                map.put("order_time", cursor.getString(cursor.getColumnIndex("order_time")));
                map.put("order_timestamp", cursor.getString(cursor.getColumnIndex("order_timestamp")));
                map.put("order_type", cursor.getString(cursor.getColumnIndex("order_type")));
                map.put("order_payment_method", cursor.getString(cursor.getColumnIndex("order_payment_method")));
                map.put("customer_name", cursor.getString(cursor.getColumnIndex("customer_name")));
                map.put("tax", cursor.getString(cursor.getColumnIndex("tax")));
                map.put("discount", cursor.getString(cursor.getColumnIndex("discount")));
                map.put("card_type_code", cursor.getString(cursor.getColumnIndex("card_type_code")));
                map.put("approval_code", cursor.getString(cursor.getColumnIndex("approval_code")));
                map.put("original_order_id", cursor.getString(cursor.getColumnIndex("original_order_id")));
                map.put("ecr_code", cursor.getString(cursor.getColumnIndex("ecr_code")));
                map.put("ex_tax_total", cursor.getString(cursor.getColumnIndex("ex_tax_total")));
                map.put("in_tax_total", cursor.getString(cursor.getColumnIndex("in_tax_total")));
                map.put("paid_amount", cursor.getString(cursor.getColumnIndex("paid_amount")));
                map.put("change_amount", cursor.getString(cursor.getColumnIndex("change_amount")));
                map.put("tax_number", cursor.getString(cursor.getColumnIndex("tax_number")));
                map.put("operation_type", cursor.getString(cursor.getColumnIndex("operation_type")));
                map.put("operation_sub_type", cursor.getString(cursor.getColumnIndex("operation_sub_type")));
                map.put(Constant.ORDER_STATUS, cursor.getString(cursor.getColumnIndex(Constant.ORDER_STATUS)));
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
        Utils.addLog("datadata_result", cursor.moveToFirst() + " " + "SELECT * FROM order_details WHERE invoice_id= " + order_id + " ORDER BY order_details_id DESC");
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("order_details_id", cursor.getString(cursor.getColumnIndex("order_details_id")));
                map.put("ex_tax_total", cursor.getString(cursor.getColumnIndex("ex_tax_total")));
                map.put("in_tax_total", cursor.getString(cursor.getColumnIndex("in_tax_total")));
                map.put("invoice_id", cursor.getString(cursor.getColumnIndex("invoice_id")));
                map.put("order_status", cursor.getString(cursor.getColumnIndex("order_status")));
                map.put("product_uuid", cursor.getString(cursor.getColumnIndex("product_uuid")));
                map.put("product_description", cursor.getString(cursor.getColumnIndex("product_description")));
                map.put("product_image", cursor.getString(cursor.getColumnIndex("product_image")));
                map.put("product_name_en", cursor.getString(cursor.getColumnIndex("product_name_en")));
                map.put("product_name_ar", cursor.getString(cursor.getColumnIndex("product_name_ar")));
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

    public void updateOrderDetailsItem(String column, String value, String order_details_id) {

        ContentValues values = new ContentValues();

        values.put(column, value);

        long check = database.update("order_details", values, "order_details_id=?", new String[]{order_details_id});


    }

    public void updateOrderListItem(String column, String value, String invoice_id) {

        ContentValues values = new ContentValues();

        values.put(column, value);

        long check = database.update("order_list", values, "invoice_id=?", new String[]{invoice_id});
        database.close();

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
                map.put("product_uuid", cursor.getString(cursor.getColumnIndex("product_uuid")));
                map.put("product_image", cursor.getString(cursor.getColumnIndex("product_image")));
                map.put("product_name_en", cursor.getString(cursor.getColumnIndex("product_name_en")));
                map.put("product_name_ar", cursor.getString(cursor.getColumnIndex("product_name_ar")));
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
                map.put("product_uuid", cursor.getString(cursor.getColumnIndex("product_uuid")));
                map.put("product_image", cursor.getString(cursor.getColumnIndex("product_image")));
                map.put("product_name_en", cursor.getString(cursor.getColumnIndex("product_name_en")));
                map.put("product_name_ar", cursor.getString(cursor.getColumnIndex("product_name_ar")));
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

        return total_cost;
    }


    //delete product from cart
    public boolean deleteProductFromCart(String id) {


        long check = database.delete("product_cart", "cart_id=?", new String[]{id});

        database.close();

        return check == 1;

    }


    //get product data
    @SuppressLint("Range")
    public ArrayList<HashMap<String, String>> getTabProducts(String category_id, boolean showActiveOnly) {
        ArrayList<HashMap<String, String>> product = new ArrayList<>();
        Cursor cursor = database.rawQuery("SELECT * FROM products WHERE product_category = '" + category_id + "' ORDER BY product_id DESC", null);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<String, String>();
                String active = cursor.getString(cursor.getColumnIndex("product_active"));
                map.put("product_id", cursor.getString(cursor.getColumnIndex("product_id")));
                map.put("product_uuid", cursor.getString(cursor.getColumnIndex("product_uuid")));
                map.put("product_active", active);
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
                if (!showActiveOnly || active.equals("1")) {
                    product.add(map);
                }
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
        database.close();

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
                if (cursor.getString(cursor.getColumnIndex("product_uuid")).equals("CUSTOM_ITEM")) {
                    product_Tax = getShopTax();
                }

            } while (cursor.moveToNext());
        }
        database.close();
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


                currency = cursor.getString(cursor.getColumnIndex("shop_currency")) + " ";
                if (LocaleManager.getLanguage(MultiLanguageApp.getApp()).equals("ar")) {
                    currency = MultiLanguageApp.getApp().getString(R.string.sar) + " ";
                }

            } while (cursor.moveToNext());
        }


        cursor.close();
        database.close();
        return currency;
    }

    public double getShopTax() {

        double tax = 0;
        Cursor cursor = database.rawQuery("SELECT * FROM shop", null);


        if (cursor.moveToFirst()) {
            do {


                tax = cursor.getDouble(cursor.getColumnIndex("tax"));


            } while (cursor.moveToNext());
        }

        database.close();
        cursor.close();
        return tax;
    }


    //calculate total price of product
    public double getTotalPriceWithoutTax() {


        double total_price = 0;

        Cursor cursor = database.rawQuery("SELECT * FROM product_cart", null);
        if (cursor.moveToFirst()) {
            do {
                String priceId = cursor.getString(cursor.getColumnIndex("product_id"));
                double tax = 1 + getProductTax(priceId) / 100.0;
                openDatabase();
                double price = Double.parseDouble(cursor.getString(cursor.getColumnIndex("product_price"))) / tax;
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

    public double totalOrderTax(String invoice_id) {


        double total_price = 0;


        Cursor cursor = database.rawQuery("SELECT * FROM order_details WHERE invoice_id='" + invoice_id + "'", null);
        Utils.addLog("datadata_tax2", String.valueOf(cursor.getCount()));

        if (cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") double price = Double.parseDouble(cursor.getString(cursor.getColumnIndex("product_price")));
                @SuppressLint("Range") int qty = Integer.parseInt(cursor.getString(cursor.getColumnIndex("product_qty")));
                @SuppressLint("Range") double tax = 1.0 + Double.parseDouble(cursor.getString(cursor.getColumnIndex("tax_percentage"))) / 100.0;
                double sub_total = (price - (price / tax)) * qty;
                total_price = total_price + sub_total;
                Utils.addLog("datadata_tax2", price + " " + qty + " " + tax + " " + total_price);

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

        Utils.addLog("datadata", String.valueOf(cursor.moveToFirst()));
        if (cursor.moveToFirst()) {
            do {

                for (int i = 0; i < 3; i++) {
                    Utils.addLog("datadata", cursor.getColumnName(i));
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
    public ArrayList<HashMap<String, String>> getPaymentMethod(boolean showActiveOnly) {
        ArrayList<HashMap<String, String>> payment_method = new ArrayList<>();
        Cursor cursor = database.rawQuery("SELECT * FROM payment_method ORDER BY payment_method_id DESC", null);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<String, String>();
                String active = cursor.getString(cursor.getColumnIndex("payment_method_active"));
                map.put("payment_method_id", cursor.getString(cursor.getColumnIndex("payment_method_id")));
                map.put("payment_method_name", cursor.getString(cursor.getColumnIndex("payment_method_name")));
                map.put("payment_method_active", active);
                if (!showActiveOnly || active.equals("1")) {
                    payment_method.add(map);
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        database.close();
        return payment_method;
    }

    @SuppressLint("Range")
    public ArrayList<HashMap<String, String>> getCardTypes(boolean showActiveOnly) {
        ArrayList<HashMap<String, String>> payment_method = new ArrayList<>();
        Cursor cursor = database.rawQuery("SELECT * FROM card_type ORDER BY id DESC", null);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<>();
                String active = cursor.getString(cursor.getColumnIndex("active"));
                map.put("active", active);
                map.put("code", cursor.getString(cursor.getColumnIndex("code")));
                map.put("name", cursor.getString(cursor.getColumnIndex("name")));
                map.put("CASH", "0");
                if (!showActiveOnly || active.equals("1")) {
                    payment_method.add(map);
                }
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
    @SuppressLint("Range")
    public HashMap<String, String> getShopInformation() {
        HashMap<String, String> shop_info = new HashMap<>();
        Cursor cursor = database.rawQuery("SELECT * FROM shop", null);
        if (cursor.moveToFirst()) {
            do {
                shop_info.put("shop_name", cursor.getString(cursor.getColumnIndex("shop_name")));
                shop_info.put("shop_contact", cursor.getString(cursor.getColumnIndex("shop_contact")));
                shop_info.put("shop_email", cursor.getString(cursor.getColumnIndex("shop_email")));
                shop_info.put("shop_address", cursor.getString(cursor.getColumnIndex("shop_address")));
                shop_info.put("shop_currency", cursor.getString(cursor.getColumnIndex("shop_currency")));
                shop_info.put("shop_tax", cursor.getString(cursor.getColumnIndex("tax")));
            } while (cursor.moveToNext());
        }
        cursor.close();
        database.close();
        return shop_info;
    }


    //get product data
    @SuppressLint("Range")
    public ArrayList<HashMap<String, String>> getProducts(boolean showActiveOnly) {
        ArrayList<HashMap<String, String>> product = new ArrayList<>();
        Cursor cursor = database.rawQuery("SELECT * FROM products ORDER BY product_id DESC", null);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<>();

                String product_uuid = cursor.getString(cursor.getColumnIndex("product_uuid"));
                String active = cursor.getString(cursor.getColumnIndex("product_active"));
                map.put("product_id", cursor.getString(cursor.getColumnIndex("product_id")));
                map.put("product_uuid", product_uuid);
                map.put("product_active", active);
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

                if ((!showActiveOnly || active.equals("1")) && !product_uuid.equals("CUSTOM_ITEM")) {
                    product.add(map);
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        database.close();
        return product;
    }

    @SuppressLint("Range")
    public HashMap<String, String> getCustomProduct() {
        HashMap<String, String> map = null;
        Cursor cursor = database.rawQuery("SELECT * FROM products Where product_uuid = 'CUSTOM_ITEM'", null);
        if (cursor.moveToFirst()) {
            do {
                map = new HashMap<>();

                String product_uuid = cursor.getString(cursor.getColumnIndex("product_uuid"));
                String active = cursor.getString(cursor.getColumnIndex("product_active"));
                map.put("product_id", cursor.getString(cursor.getColumnIndex("product_id")));
                map.put("product_uuid", product_uuid);
                map.put("product_active", active);
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

                if (!active.equals("1")) {
                    map = null;
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        database.close();
        return map;
    }

    public Boolean checkCustomProductInCart() {
        Cursor cursor = database.rawQuery("SELECT * FROM product_cart Where product_uuid = 'CUSTOM_ITEM'", null);
        boolean exist = cursor.moveToFirst();
        Utils.addLog("datadata_exist", String.valueOf(exist));
        cursor.close();
        database.close();
        return exist;
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
                map.put("product_uuid", cursor.getString(cursor.getColumnIndex("product_uuid")));
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

    @SuppressLint("Range")
    public ArrayList<HashMap<String, String>> getProductsInfoFromUUID(String product_uuid) {
        ArrayList<HashMap<String, String>> product = new ArrayList<>();
        Cursor cursor = database.rawQuery("SELECT * FROM products WHERE product_uuid='" + product_uuid + "'", null);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<String, String>();

                map.put("product_id", cursor.getString(cursor.getColumnIndex("product_id")));
                map.put("product_uuid", cursor.getString(cursor.getColumnIndex("product_uuid")));
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
                map.put("payment_method_active", cursor.getString(cursor.getColumnIndex("payment_method_active")));


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
    public ArrayList<HashMap<String, String>> getSearchProducts(String s, boolean showActiveOnly) {
        ArrayList<HashMap<String, String>> product = new ArrayList<>();
        Cursor cursor = database.rawQuery("SELECT * FROM products WHERE product_name_en LIKE '%" + s + "%' OR product_name_ar LIKE '%" + s + "%' OR product_uuid LIKE '%" + s + "%' OR product_code LIKE '%" + s + "%' ORDER BY product_id DESC", null);
        if (cursor.moveToFirst()) {
            do {
                String active = cursor.getString(cursor.getColumnIndex("product_active"));
                HashMap<String, String> map = new HashMap<>();
                String product_uuid = cursor.getString(cursor.getColumnIndex("product_uuid"));
                map.put("product_id", cursor.getString(cursor.getColumnIndex("product_id")));
                map.put("product_uuid", product_uuid);
                map.put("product_active", active);
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
                if ((!showActiveOnly || active.equals("1")) && !product_uuid.equals("CUSTOM_ITEM")) {
                    product.add(map);
                }
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
                configuration.put("invoice_merchant_id", cursor.getString(cursor.getColumnIndex("invoice_merchant_id")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (cursor != null) {
            cursor.close();
        }
        database.close();
        return configuration;
    }

    @SuppressLint("Range")
    public Boolean addConfiguration(RegistrationResponseDto registrationResponseDto) {

        ContentValues values = new ContentValues();

        values.put("ecr_code", registrationResponseDto.getEcrCode());
        values.put("merchant_id", registrationResponseDto.getMerchant().getMerchantId());
        values.put("merchant_logo", registrationResponseDto.getMerchant().getLogo() != null ? registrationResponseDto.getMerchant().getLogo() : "");
        values.put("merchant_tax_number", registrationResponseDto.getMerchant().getVATNumber());

        long check = database.insert("configuration", null, values);
        database.close();

        //if data insert success, its return 1, if failed return -1
        return check != -1;
    }

    @SuppressLint("Range")
    public Boolean addReport(String ecr,String merchantId,String type,String body) {

        ContentValues values = new ContentValues();

        values.put("ecr", ecr);
        values.put("merchnt_id", merchantId);
        values.put("type", type);
        values.put("body", body);

        long check = database.insertOrThrow("crash_report", null, values);
        database.close();

        //if data insert success, its return 1, if failed return -1
        return check != -1;
    }

    @SuppressLint("Range")
    public Boolean addConfiguration(String ecr_code, String merchant_id, String merchant_logo, String merchant_tax_number, String invoiceMerchantId) {

        ContentValues values = new ContentValues();

        values.put("ecr_code", ecr_code);
        values.put("merchant_id", merchant_id);
        values.put("invoice_merchant_id", invoiceMerchantId);
        values.put("merchant_logo", merchant_logo);
        values.put("merchant_tax_number", merchant_tax_number);

        long check = database.insert("configuration", null, values);
        database.close();

        //if data insert success, its return 1, if failed return -1
        return check != -1;
    }

    @SuppressLint("Range")
    public HashMap<String, String> getSequence(int sequenceId, String ecrCode) {
        HashMap<String, String> sequenceMap = new HashMap<>();
        String sequence = "";
        Cursor cursor = null;
        int nextValue = -1;
        String prefix = "";
        try {
            cursor = database.rawQuery("SELECT * FROM sequence_text WHERE id=" + sequenceId, null);
            if (cursor != null && cursor.moveToFirst()) {
                nextValue = Integer.parseInt(cursor.getString(cursor.getColumnIndex("current_value"))) + 1;
                prefix = cursor.getString(cursor.getColumnIndex("type_perfix"));
                Utils.addLog("datadata_seq", String.valueOf(nextValue));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (cursor != null) {
            cursor.close();
        }
        database.close();
        try {
            sequence = ecrCode + "-001-" + prefix + String.format(java.util.Locale.US, "%010d", nextValue);
            sequenceMap.put("sequence", sequence);
            sequenceMap.put("next_value", String.valueOf(nextValue));
            sequenceMap.put("sequence_id", String.valueOf(sequenceId));
        } catch (Exception e) {
            e.printStackTrace();

        }
        return sequenceMap;
    }

    public boolean updateSequence(int nextValue, int sequenceId) {
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

        return check == 1;

    }


    //delete category
    public boolean deleteCategory(String category_id) {


        long check = database.delete("product_category", "category_id=?", new String[]{category_id});

        database.close();

        return check == 1;

    }


    //delete payment method
    public boolean deletePaymentMethod(String payment_method_id) {


        long check = database.delete("payment_method", "payment_method_id=?", new String[]{payment_method_id});

        database.close();

        return check == 1;

    }


    //delete order Type
    public boolean deleteOrderType(String typeId) {


        long check = database.delete("order_type", "order_type_id=?", new String[]{typeId});
        database.close();

        return check == 1;

    }


    //delete unit
    public boolean deleteUnit(String unitId) {

        long check = database.delete("product_weight", "weight_id=?", new String[]{unitId});
        database.close();

        return check == 1;

    }


    //update order
    public boolean updateOrder(String invoiceId, String orderStatus) {


        ContentValues contentValues = new ContentValues();
        contentValues.put(Constant.ORDER_STATUS, orderStatus);

        long check = database.update(Constant.orderList, contentValues, "invoice_id=?", new String[]{invoiceId});
        database.update(Constant.orderDetails, contentValues, "invoice_id=?", new String[]{invoiceId});


        database.close();

        return check == 1;

    }


    //delete product
    public boolean deleteProduct(String product_id) {


        long check = database.delete("products", "product_id=?", new String[]{product_id});
        long check2 = database.delete("product_cart", "product_id=?", new String[]{product_id});

        database.close();

        return check == 1;

    }


    //delete product
    public boolean deleteExpense(String expense_id) {


        long check = database.delete("expense", "expense_id=?", new String[]{expense_id});

        database.close();

        return check == 1;

    }


    //delete supplier
    public boolean deleteSupplier(String customer_id) {


        long check = database.delete("suppliers", "suppliers_id=?", new String[]{customer_id});

        database.close();

        return check == 1;

    }

    public void addQrCodeToOrder(String orderId, String qrCodeBase64) {
        ContentValues values = new ContentValues();
        values.put("qr_code", qrCodeBase64);
        database.update("order_list", values, "invoice_id=? ", new String[]{orderId});
        database.close();
    }

    public void addDemoConfiguration() {
        ContentValues values = new ContentValues();
        values.put("ecr_code", "D0001");
        values.put("merchant_id", "cr7001406813");
        values.put("merchant_logo", "/9j/4AAQSkZJRgABAQAAAQABAAD/2wCEAAkGBxAQEBIQDxIPEBUSEBAQFRIPFRAQDxAQFRIWFhURFRUYHSghGBolGxUXITEhJykrLi4uFx8zODMuQyktLisBCgoKDg0OGhAQGi0gIB8vKy0rLS0tLS0tLS0rLS0tKy0tLS0rLS0tLS0rKzctLS0tLS0tLS0tLTUrLS0tLTctK//AABEIAKIBOAMBIgACEQEDEQH/xAAcAAEAAwADAQEAAAAAAAAAAAAAAQIDBQYHBAj/xABKEAACAQMCBAIGAwkNCQEAAAAAAQIDBBESIQUGMUETUQcUIjJhcReBsUJSU1SRocHR0yMzNVVicnSCkpSkstIIFiQlNEODs7QV/8QAGAEBAQEBAQAAAAAAAAAAAAAAAAECAwX/xAAgEQEBAAICAwADAQAAAAAAAAAAAQIRITEDEkEiUbEU/9oADAMBAAIRAxEAPwD3AZAYEghEgRIhP4Ey7EYf5yiG/gXKS7lwBgzczp9WII8TyQVPuy0qaZmotvGehQnJdEhR64NYwSM4e+/rG+BsADIAAAAAMK3vRLzop79PkUq+/ERliUmzYvCkl8fmRQ6y+f6yfE9pLs1kij1l8/1k50NQAZEMzw8Ya/Oaguxlh4xg1QKSm84Q7FwZ6peRaEsjQsACACv1jYCwIRIAAAQSCMAESABDQ0kgCuhFgABlF4e5qQ1ksBMzp+8/r+0ODXQom87FkG0ppGVLeWS0aXmapE4gGdWeMJGhjWW+evYTsMv76P5iMy++j+YjQ+un6iNDW7jt5eRrgbUZ5RcyoRws+f2Gpm9jCvs4vsiHOG/Xc+gjSvJF2MVUhs99lgtQ7vzZppXkiSWgACAAABjJ75Reo+3mTGOCzgUlN+Ran0Lmco43QGgITJIKR/ST+sJMncoIkhIkgAAAAAIbA7DIBMkjuSAAAApr+DLlE2tsfqKLJklYLCLEAAADK46fWalJ08+ZZ2Kyy5YzjC7FcvEk98It4K82PAXmy7gtS91fIuVpwx5/WWJQABAKOTbwu3dlzOj0+t5+ZREZSxnr9ponkootd0TR6flFFwAQZy95GhWcclfaL2NCJdCmZDDfUaFqfQsQkSQAAAAIyBIIyAJAAFW9g2WAFV1LAAAABWbwFH4slokDLW/0GpXQixaAMa91Tp48ScIZ6a5RjnHlkrSv6M2owq0pN9FGcJSe2dkmQfQDGd1Ti8SnCL8nKKf5DYaAAhvG72AkHzU+IUZNKNWlJvZJTg238Fk+kAAABSUN8p4+xlwBnpb6v8hoilatGC1TlGC85NRX5WTCakk4tNNJprdNPo0wLAwrXlKDxOpTg8ZxOUYvHnhs0pVYzSlCUZJ9HFqSfbqgLgAADNZe+cDH8ouhoCm675LkAAAGQSyAAAAEkEgAAAIbJKvqBG/mSn2ZLRVdSi4AIBxvMfFVZ2lxdOLqeBRnV0J6dWlZ057fM5I616Sf4Iv/AOiVf8pZ2PEeG8Ev+Zrm5uJVaGul4WVWdRU4QqOpop0lGMsRWh/lzu22dz5E9FN5w/iNveValnKFJ1dSpOrreuhUprGYJdZruYf7O3vcR/m2X23B7Odc87LqJHknPi/5jV/8P/rgeg8w8yUrF01VhVn4mvHhqDxo05zqkvvkdA56pyfEKrSb/eeib/7cTmvSnBt2uE3tcdE33pHtZ+LDzf5sM+rjf5Fd14ZexuKMK0FJRqR1JSxqS+OG0cFz7zbbcNoLx4zqyr6qdOhT2nV2xJ5+5itSy/5SwmffyisWNvn8EvtZ0/0t8Bu6lSx4jZ03cSsaviSoJOUpxVSnUUoxW8t6eGlv7Sa6Hi54SeW4/JaPPrHiNpYXFvXfL9xaTVWPhVK91fQWrZZiqlJKeFLoeyc785UOFUoSqxnVqVZONKjTxrqNYy8vpFZjvv7ywmeSekTme64jC0nPht1aqjWnLNRVJeJLEW4xTpxe2OuO5nzbzNccUu7S6trC6jOxzXcJRnUU1CrSmnhRTxmOHjLw/gy3Heto7rZ+lK6d3bWtzwqpaO5q06cXWq1IyUZzUdahKis4z0yj08/O/MnOVW94hY3zsbil6pKDlS9uetxqqbSloWOmN0dx+mV/xZd/2n/oM5YX5Db0jj9zVpWlxVt4eJVp29adKnplPXVjBuENMd5ZaSwt2ed+jbjPEZ3clxChxiU7hPVO4oOhw63VNSlFQi4rS37uds5XXqZ/TK/4ru/7T/0D6ZX/ABZef2n/AKCTG61ofDzpzlS4vwG6q0qVSkqd1a02qri3LM4Szt2wzvnLfE6VpwOzua8tFOlw20lJ9XjwIJJLu22kl3bR4xweyqrly/g6dTV6/aNR0TUmkqeWljPZn28Y50nccGp8K9SuoSp0bSl43tOLdB08y06c4eh7Z2ybuHyfs215p5ltuJT9drcBu7inCn4cbmVe7o01RhKT9p0qbprDlLL1PHnseqei+tQnwq2lbUPVabdfTR8SdfRi4qJ/ukknLLTfwzg8r/3/AKr4O+FysK+fVPVFWjrUdKhpjNw0dcLdZ6/M9L9D0JR4NaqcZRadztJOLX/FVcbP4Ezn4kdzABxVnDo/mRleTLUun1lpZ7FFZdi5SXYuAABAZCZIAjIJAEEgAAAAIaJAFcPzJSwSQ2BIK615lgB1r0k/wRf/ANEq/wCU7KcNzlw2pdcPurajp11bepThqeIuTjsm+xcex5j/ALO3vcR/m2X23B7OfnT0c83Q4HVvKd3b3DnUdCDhBQU6cqTq6lNTkvwixjP2Hd/pwsvxW9/w/wC0OueFuW4keqHRvSZz3U4Q7ZU6EK/jqu3rnKnp8LwumE858T8xwn04WX4re/4f9odI9I3N0OOVbOnaW9wp03XgoTUHOpKs6WlQUJP8G85x1+ZMPHd8w2925W4q7yyt7qUVTdelGq4J6lHUume5px3jdtY0XXu6saME1HMstyk+kYxWXKWz2S7M+fk3htS04fa21bTrpW9OE9LzFSS3Sfc8+9OFCUa/DbmtTlVtKNZqvGO8d6lJuMl09qEZRWdu2VkzJLlpXOQ9L/B5SUY1a7baX7zV6t4XY7LzLzTZ8OhGd5VVPW2oRSlOpPGM6YRTbSysvosrzPGPSVzFwq8VnHhqgpU6+aihQnbtQaioreKzuu3kcvzzK3p8y0J8VipWrt4KHiRc6G0Zpa494qo22u2qLexr0nCO5cN9KnC7itSoUp13OrUhShmjUinOTwstrbdnLcz86WHDXGN3W0zktUacIyqVXHpqcYp4Wz3eFszyvmG54dU47wp8M9V8NVbdT9UjCEPE9Y+6UUt8YOM5glXjzFduSsHU8R+H/wDq/wDS6NEPCaztq0Y052znvgektNvbeWebLLiUZSs6qqOGNcJRlTqQz0bjJJ4fmtjgZelrg6Um61TMZKOnwa2uTed0sbpY3fxXmdV5C5ZvlxaN/q4XGnipCvDhtVOnpnSkopU1lRzOMJdVvFvzOK9EPBbW6XFXc0aVZwhTjB1Yxm6er1hycM+63pjut/ZQ9ceR6Fc+lbg8I05esOfiLVinSrTlBZx+6LT7L26PfvjdHabPi1vWt1dU6tOVBwdTxc6aags6pSb93GHnOMYeTw30ccGtq/A+K161GlUqwp19FScYyqU9FoqkXCT3i1J52+HkacOjWlyjcKlqajdtzUc7UFVpyn/V3y/hkXCfDbv9f0vcHjJxVarPDxqhRrOL+TaWV8TtfL/GqN9bwurdydOprUXOLhL2JyhLKfTeLPHal/wJ8AdOkrON56motTpw9bd1pWtqbjlvVnDTxjGDvvoY/gS1/nXX/wBdYmWMk2O7AA5qyUGTpZoC7FNLLgEAAAACAGSSABIAAjI3IQ+sosgREkgGcuu5eTwU0t9SwGok0ug8NFU2uvQo1ABkdc5h5G4df1FWu6Guoo6dcJ1aUnFdFLw5LVjtnocX9E3Bfxaf94u/2h3cGplZ9HSPom4L+LT/ALxd/tDlOXuRuHWFR1rShoqOOjXOdWrKMX1UfEk9Oe+DsYFyt+gUrUozi4zjGUZLDjJKUZJ9mn1RWc90k0viKdTqm1t38yaHmnpP5LnUp2cOF2dKKhcyq1I28aFCKTjFamsxTex37jvAbS+pqnd0YVop5WrKlB9G4yWHF/FNHJAvtR1a29HfCaVSjVp2sYTt5RnTlCddNTjLUpT9v90ee8s+R9XM3JthxHS7uipyitMakJTp1VH73VFpuO72eVuc+Ce1HBcrco2fDI1I2cJQ8Vxc3KdSo5OOdPvN4959BwDlGxsVWVpSdP1hRVXNStU16dWPfk9Pvy6Y6nOgbo4HhPJ1ja21a0t6ThRuFNVYeJWk5qdPw5e1KTlH2VjZo+rgPL9rY0Hb2tPRScpTcJSnVy5JKWXNt746HKAbHU5ejbg7c5epUk6kXF6XUjGKf4OKlim/jFJnPcE4RQsqELa1h4dKGvTHVOeNU3OXtTbb9qTe77n3AW2gACAAAADKJfECyZJXCAFiCQBAJwAAAAqmCwKKxLAEFJ9Ui5SfXJOtFFitRbDWis5rsILU3sWKwWEWJQAAArU6P5FiGBnGmnh7dOhWpTSUnt8PgWhNpYae3kROTlsk1v322N87Gsei+RIBgAAAKTnjzfyLmf3f9X9JYKeK+uV8u5rGWfh8zOMva+5/SXj7z+otFwAZAFJVEiFU8y6GgCYIKzD6bFmUiyiXgSGw7gSiQCAAAAAAAAACGymfiBoV0IRkWAroRKiiQAAAFJT3wlkjxH96yj6vLaKqXm2jWho6r7po1Pmztu38jektlkWCwAMgAAAAAGU3iWX5YNQWDDUs5bb+ovTeW38jQDYhsxc2/gaz6P5MzhDKLBbCXUh5ZXTjqX19kBGGi8JZKqHmKRKNAAQQokgAAAAAAEYAY7gSAAKvqiUhJEZfkURjdFyqW+SxAAAAAAZy95fIjU30S+sSjLOdhGMl5GhE5ZSfxNjDw5Yxts8m0c9yUSACAAAAAAApOXZdWVw84z2yXQ1BSEuz6/aXIKz6P5MrR6fWXaM3BroWDSXQpSCqeZEJYRdDUpSK7s0jHBBIAIAAAAAAAAIY7gASAAAAAAAAAAAAAAAAAAAAAAAAAAKfdf1f0kT95fIgFBe8vkzUAUAAQZ1SkOpINTobAAyAAAAAAAAAAA//2Q==");
        values.put("merchant_tax_number", "300000434710003");
        database.insert("configuration", null, values);
        database.close();
    }

    public void addShop(RegistrationResponseDto registrationResponseDto, DatabaseAccess databaseAccess) {
        deleteShopInfo();
        databaseAccess.open();
        ContentValues values = new ContentValues();
        values.put("shop_address", "Riyadh, Saudi Arabia");
        values.put("shop_contact", registrationResponseDto.getMerchant().getCompanyPhone());
        values.put("shop_currency", registrationResponseDto.getMerchant().getCurrency());
        values.put("shop_email", registrationResponseDto.getMerchant().getCompanyEmail());
        values.put("shop_name", registrationResponseDto.getMerchant().getName());
        values.put("tax", registrationResponseDto.getTax().getPercentage());

        database.insert("shop", null, values);
    }

    public void addShop(String companyPhone, String currency, String companyEmail, String name, double percentage, DatabaseAccess databaseAccess) {
        deleteShopInfo();
        databaseAccess.open();
        ContentValues values = new ContentValues();
        values.put("shop_address", "Riyadh, Saudi Arabia");
        values.put("shop_contact", companyPhone);
        values.put("shop_currency", currency);
        values.put("shop_email", companyEmail);
        values.put("shop_name", name);
        values.put("tax", percentage);

        database.insert("shop", null, values);
    }

    public void deleteShopInfo() {
        SQLiteStatement result = database.compileStatement("DELETE FROM shop");
        result.execute();
        database.close();
    }
}