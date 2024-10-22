package com.app.smartpos.database;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.app.smartpos.R;
import com.app.smartpos.common.Utils;
import com.app.smartpos.utils.MultiLanguageApp;
import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteOpenHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import es.dmoral.toasty.Toasty;

public class DatabaseOpenHelper extends SQLiteOpenHelper {
    public static String DATABASE_PASSWORD;
    public static final String DATABASE_NAME = "smart_pos.db";
    private static final int DATABASE_VERSION = 55;
    private final Context mContext;
    private static DatabaseOpenHelper instance;

    public DatabaseOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
        //setForcedUpgrade();
    }


    @Override
    public synchronized SQLiteDatabase getWritableDatabase(char[] password) {

//        Uri uri = Uri.fromFile(new File("//android_asset/databases/your_database.db"));
//        InputStream inputStream = MultiLanguageApp.getApp().getAssets().open("databases/your_database.db");
//
//        return SQLiteDatabase.openDatabase(uri.getPath(),DATABASE_PASSWORD,null,SQLiteDatabase.OPEN_READWRITE);
        return super.getWritableDatabase(password);
    }

    public void backup(String outFileName) {

        //database path
        final String inFileName = mContext.getDatabasePath(DATABASE_NAME).toString();

        try {

            File dbFile = new File(inFileName);
            FileInputStream fis = new FileInputStream(dbFile);

            // Open the empty db as the output stream
            OutputStream output = new FileOutputStream(outFileName);

            // Transfer bytes from the input file to the output file
            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                output.write(buffer, 0, length);
            }

            // Close the streams
            output.flush();
            output.close();
            fis.close();

            Toasty.success(mContext, mContext.getString(R.string.backup_completed_successfully), Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Toasty.error(mContext, R.string.unable_to_backup_database_retry, Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }


    public void importDB(String inFileName) {

        final String outFileName = mContext.getDatabasePath("smart_pos.db").toString();


        try {

            File dbFile = new File(inFileName);
            FileInputStream fis = new FileInputStream(dbFile);

            // Open the empty db as the output stream
            OutputStream output = new FileOutputStream(outFileName);

            // Transfer bytes from the input file to the output file
            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                output.write(buffer, 0, length);
            }

            // Close the streams
            output.flush();
            output.close();
            fis.close();

            Toasty.success(mContext, R.string.database_Import_completed, Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Toasty.error(mContext, R.string.unable_to_import_database_retry, Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    public void mergeDatabases(String newDbFilePath) {
        SQLiteDatabase existingDb = getWritableDatabase(DATABASE_PASSWORD);
        android.database.sqlite.SQLiteDatabase newDb = android.database.sqlite.SQLiteDatabase.openDatabase(newDbFilePath, null, android.database.sqlite.SQLiteDatabase.OPEN_READONLY);

        try {
            existingDb.beginTransaction();

            // Get tables from new database
            String[] tables = {"products", "payment_method", "card_type", "user"};
            for (String table : tables) {
                // Delete all rows in the existing table
                existingDb.delete(table, null, null);

                // Insert rows from the new database
                String query = "SELECT * FROM " + table;
                try (Cursor cursor = newDb.rawQuery(query, null)) {
                    while (cursor.moveToNext()) {
                        ContentValues values = new ContentValues();
                        int columnCount = cursor.getColumnCount();
                        for (int i = 0; i < columnCount; i++) {
                            values.put(cursor.getColumnName(i), cursor.getString(i));
                        }
                        existingDb.insert(table, null, values);
                    }
                }
            }

            existingDb.setTransactionSuccessful();
        } finally {
            existingDb.endTransaction();
            newDb.close();
        }
    }

    public void readProductDatabase(String newDbFilePath) {
        //SQLiteDatabase existingDb = getWritableDatabase();
        android.database.sqlite.SQLiteDatabase newDb = android.database.sqlite.SQLiteDatabase.openDatabase(newDbFilePath, null, android.database.sqlite.SQLiteDatabase.OPEN_READONLY);

        try {
            // existingDb.beginTransaction();

            // Get tables from new database
            String[] tables = {"product_image"};
            for (String table : tables) {
                // Delete all rows in the existing table
                //existingDb.delete(table, null, null);

                // Insert rows from the new database
                String query = "SELECT * FROM " + table;
                DatabaseAccess databaseAccess = DatabaseAccess.getInstance(MultiLanguageApp.getApp());
                try (Cursor cursor = newDb.rawQuery(query, null)) {
                    while (cursor.moveToNext()) {
                        ContentValues values = new ContentValues();
                        int columnCount = cursor.getColumnCount();
                        for (int i = 0; i < columnCount; i++) {
                            values.put(cursor.getColumnName(i), cursor.getString(i));
                        }
                        databaseAccess.open();
                        databaseAccess.updateProductImage(values);
                        Utils.addLog("datadata", values.toString());
                    }
                }
            }

            //  existingDb.setTransactionSuccessful();
        } finally {
            //existingDb.endTransaction();
            newDb.close();
        }
    }

    public void exportTablesToNewDatabase(String newDbFilePath, String[] lastSync) {
        // Delete the existing file if it exists
        File dbFile = new File(newDbFilePath);
        Utils.addLog("datadata_base", newDbFilePath);
        if (dbFile.exists()) {
            dbFile.delete();
        }
        SQLiteDatabase existingDb = getWritableDatabase(DATABASE_PASSWORD);
        android.database.sqlite.SQLiteDatabase newDb = android.database.sqlite.SQLiteDatabase.openOrCreateDatabase(newDbFilePath, null);
        try {
            newDb.beginTransaction();
            exportInvoice(existingDb, newDb, lastSync[0]);
            exportShift(existingDb, newDb, lastSync[1]);
            newDb.setTransactionSuccessful();
        } finally {
            newDb.endTransaction();
            newDb.close();
        }
    }

    private void exportShift(SQLiteDatabase existingDb, android.database.sqlite.SQLiteDatabase newDb, String lastSync) {
        try {
            copyTableSchema(existingDb, newDb, "shift");
            copyTableSchema(existingDb, newDb, "credit_calculations");
            String lastSyncShift = String.format("SELECT MAX(id)  FROM shift WHERE sequence = '%s'", lastSync);
            Cursor cursor = existingDb.rawQuery(lastSyncShift, null);
            Integer lastSyncId = -1;
            // Check if the cursor has any results
            if (cursor.moveToFirst()) {
                // Retrieve the first column value as a string
                String result = cursor.getString(0);

                // Check if the result is not null
                if (result != null) {
                    lastSyncId = Integer.valueOf(result); // Assign the result to lastSyncId
                }
            }

            // Copy rows from the existing database table to the new one
            String shiftQuery = "SELECT * FROM shift WHERE id >" + lastSyncId + ";";
            try (Cursor shiftCursor = existingDb.rawQuery(shiftQuery, null)) {
                while (shiftCursor.moveToNext()) {
                    ContentValues shiftValues = new ContentValues();
                    for (int i = 0; i < shiftCursor.getColumnCount(); i++) {
                        shiftValues.put(shiftCursor.getColumnName(i), shiftCursor.getString(i));
                    }
                    newDb.insert("shift", null, shiftValues);
                    @SuppressLint("Range") String shiftId = shiftCursor.getString(shiftCursor.getColumnIndex("sequence"));
                    String creditCalculations = String.format("SELECT * FROM credit_calculations WHERE shift_id = '%s'", shiftId);
                    try (Cursor creditCursor = existingDb.rawQuery(creditCalculations, null)) {
                        while (creditCursor.moveToNext()) {
                            ContentValues creditValues = new ContentValues();
                            for (int i = 0; i < creditCursor.getColumnCount(); i++) {
                                creditValues.put(creditCursor.getColumnName(i), creditCursor.getString(i));
                            }
                            newDb.insert("credit_calculations", null, creditValues);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void exportInvoice(SQLiteDatabase existingDb, android.database.sqlite.SQLiteDatabase newDb, String lastSync) {
        try {
            copyTableSchema(existingDb, newDb, "order_list");
            copyTableSchema(existingDb, newDb, "order_details");
            String lastSyncOrderList = String.format("SELECT MAX(order_id)  FROM order_list WHERE invoice_id = '%s'", lastSync);
            Cursor cursor = existingDb.rawQuery(lastSyncOrderList, null);
            Integer lastSyncId = -1;
            // Check if the cursor has any results
            if (cursor.moveToFirst()) {
                // Retrieve the first column value as a string
                String result = cursor.getString(0);

                // Check if the result is not null
                if (result != null) {
                    lastSyncId = Integer.valueOf(result); // Assign the result to lastSyncId
                }
            }

            // Copy rows from the existing database table to the new one
            String shiftQuery = String.format("SELECT * FROM order_list WHERE order_id > %s", lastSyncId);
            try (Cursor shiftCursor = existingDb.rawQuery(shiftQuery, null)) {
                Utils.addLog("datadata_shift", shiftCursor.toString());
                while (shiftCursor.moveToNext()) {
                    ContentValues orderListValues = new ContentValues();
                    for (int i = 0; i < shiftCursor.getColumnCount(); i++) {
                        Utils.addLog("datadata_value", shiftCursor.getColumnName(i) + " " + shiftCursor.getString(i));
                        orderListValues.put(shiftCursor.getColumnName(i), shiftCursor.getString(i));
                    }
                    Utils.addLog("datadata_base_list", orderListValues.toString());
                    newDb.insert("order_list", null, orderListValues);
                    @SuppressLint("Range") String orderListId = shiftCursor.getString(shiftCursor.getColumnIndex("invoice_id"));
                    String orderDetails = String.format("SELECT * FROM order_details WHERE invoice_id = '%s'", orderListId);
                    try (Cursor creditCursor = existingDb.rawQuery(orderDetails, null)) {
                        while (creditCursor.moveToNext()) {
                            ContentValues detailsValues = new ContentValues();
                            for (int i = 0; i < creditCursor.getColumnCount(); i++) {
                                detailsValues.put(creditCursor.getColumnName(i), creditCursor.getString(i));
                            }
                            Utils.addLog("datadata_base_details", detailsValues.toString());

                            newDb.insert("order_details", null, detailsValues);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void copyTableSchema(SQLiteDatabase sourceDb, android.database.sqlite.SQLiteDatabase targetDb, String tableName) {
        // Retrieve schema information from the source database
        String schemaQuery = "PRAGMA table_info(" + tableName + ")";
        Cursor cursor = sourceDb.rawQuery(schemaQuery, null);

        // Build CREATE TABLE statement
        StringBuilder createTableQuery = new StringBuilder("CREATE TABLE IF NOT EXISTS " + tableName + " (");

        while (cursor.moveToNext()) {
            @SuppressLint("Range") String columnName = cursor.getString(cursor.getColumnIndex("name"));
            @SuppressLint("Range") String columnType = cursor.getString(cursor.getColumnIndex("type"));
            Utils.addLog("datadata_table", columnName + " " + columnType);
            createTableQuery.append(columnName).append(" ").append(columnType).append(", ");
        }
        Utils.addLog("datadata_table", createTableQuery.toString());
        // Remove trailing comma and space
        if (createTableQuery.length() > 0) {
            createTableQuery.setLength(createTableQuery.length() - 2);
        }
        createTableQuery.append(");");

        cursor.close();

        // Execute CREATE TABLE statement in the target database
        targetDb.execSQL(createTableQuery.toString());
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String card_type="CREATE TABLE card_type(\n" +
                "  id INTEGER NOT NULL,\n" +
                "  name TEXT,\n" +
                "  code TEXT,\n" +
                "  active BOOLEAN,\n" +
                "  PRIMARY KEY(id)\n" +
                ")";
        sqLiteDatabase.execSQL(card_type);
        String configuration="CREATE TABLE configuration(\n" +
                "  id INTEGER NOT NULL,\n" +
                "  ecr_code TEXT,\n" +
                "  merchant_id TEXT,\n" +
                "  merchant_tax_number TEXT,\n" +
                "  merchant_logo TEXT,\n" +
                "  PRIMARY KEY(id)\n" +
                ")";
        sqLiteDatabase.execSQL(configuration);
        String credit_calculations="CREATE TABLE credit_calculations(\n" +
                "  id INTEGER NOT NULL,\n" +
                "  credit_code TEXT,\n" +
                "  shift_id INTEGER,\n" +
                "  total DOUBLE,\n" +
                "  difference DOUBLE,\n" +
                "  PRIMARY KEY(id)\n" +
                ")";
        sqLiteDatabase.execSQL(credit_calculations);
        String customers="CREATE TABLE \"customers\" (\n" +
                "\t\"customer_id\"\tINTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                "\t\"customer_name\"\tTEXT,\n" +
                "\t\"customer_cell\"\tTEXT,\n" +
                "\t\"customer_email\"\tTEXT,\n" +
                "\t\"customer_address\"\tTEXT\n" +
                ", customer_active BOOLEAN, customer_sequence TEXT)";
        sqLiteDatabase.execSQL(customers);
        String demo="CREATE TABLE demo (ID integer primary key, Name varchar(20), Hint text )";
        sqLiteDatabase.execSQL(demo);
        String expense="CREATE TABLE \"expense\"\n" +
                "(\n" +
                "    expense_id       INTEGER\n" +
                "        primary key autoincrement,\n" +
                "    expense_name     TEXT,\n" +
                "    expense_note     TEXT,\n" +
                "    expense_amount   TEXT,\n" +
                "    expense_date     TEXT,\n" +
                "    expense_time     TEXT,\n" +
                "    expense_sequence TEXT\n" +
                ")";
        sqLiteDatabase.execSQL(expense);
        String order_details="CREATE TABLE \"order_details\"\n" +
                "(\n" +
                "    order_details_id   INTEGER\n" +
                "        primary key autoincrement,\n" +
                "    invoice_id         TEXT,\n" +
                "    product_name_en    TEXT,\n" +
                "    product_weight     TEXT,\n" +
                "    product_qty        TEXT,\n" +
                "    product_price      TEXT,\n" +
                "    product_image      TEXT,\n" +
                "    product_order_date TEXT,\n" +
                "    order_status       TEXT,\n" +
                "    ex2_tax_total       DOUBLE,\n" +
                "    in2_tax_total       DOUBLE,\n" +
                "    tax_amount2         DOUBLE,\n" +
                "    tax_percentage     DOUBLE,\n" +
                "    product_uuid       INTEGER,\n" +
                "    product_name_ar    TEXT\n" +
                ", description TEXT, product_description TEXT, tax_amount TEXT, in_tax_total TEXT, ex_tax_total TEXT)";
        sqLiteDatabase.execSQL(order_details);
        String order_list="CREATE TABLE \"order_list\"\n" +
                "(\n" +
                "    order_id             INTEGER\n" +
                "        primary key autoincrement,\n" +
                "    invoice_id           TEXT,\n" +
                "    order_date           TEXT,\n" +
                "    order_time           TEXT,\n" +
                "    order_type           TEXT,\n" +
                "    order_payment_method TEXT,\n" +
                "    customer_name        TEXT,\n" +
                "    tax                  TEXT,\n" +
                "    discount             TEXT,\n" +
                "    order_status         TEXT,\n" +
                "    card_details         INTEGER,\n" +
                "    original_order_id    INTEGER,\n" +
                "    ecr_code             TEXT,\n" +
                "    ex2_tax_total         DOUBLE,\n" +
                "    in2_tax_total         DOUBLE,\n" +
                "    paid_amount2          DOUBLE,\n" +
                "    change_amount        DOUBLE,\n" +
                "    tax_number           TEXT,\n" +
                "    order_timestamp      integer\n" +
                ", qr_code TEXT, operation_type TEXT, card_type_code TEXT, approval_code TEXT, sequence_text text, operation_sub_type TEXT, in_tax_total TEXT, ex_tax_total TEXT, paid_amount TEXT)";
        sqLiteDatabase.execSQL(order_list);
        String order_type="CREATE TABLE \"order_type\" (\n" +
                "\t\"order_type_id\"\tINTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                "\t\"order_type_name\"\tTEXT\n" +
                ")";
        sqLiteDatabase.execSQL(order_type);
        String payment_methods="CREATE TABLE \"payment_method\" (\n" +
                "\t\"payment_method_id\"\tINTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                "\t\"payment_method_name\"\tTEXT\n" +
                ", payment_method_active BOOLEAN, payment_method_code TEXT)";
        sqLiteDatabase.execSQL(payment_methods);
        String product_cart="CREATE TABLE \"product_cart\" (\n" +
                "\t\"cart_id\"\tINTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                "\t\"product_id\"\tTEXT,\n" +
                "\t\"product_weight\"\tTEXT,\n" +
                "\t\"product_weight_unit\"\tTEXT,\n" +
                "\t\"product_price\"\tTEXT,\n" +
                "\t\"product_qty\"\tINTEGER,\n" +
                "\t\"stock\"\tTEXT\n" +
                ", product_uuid text, product_description TEXT)";
        sqLiteDatabase.execSQL(product_cart);
        String product_category="CREATE TABLE \"product_category\" (\n" +
                "\t\"category_id\"\tINTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                "\t\"category_name\"\tTEXT\n" +
                ")";
        sqLiteDatabase.execSQL(product_category);
        String product_image="CREATE TABLE product_image (\n" +
                "\tid INTEGER PRIMARY KEY,\n" +
                "\timage_url TEXT,\n" +
                "\tbase64_image TEXT,\n" +
                "\timage_thumbnail_url TEXT,\n" +
                "\timage_thumbnail TEXT,\n" +
                "\tproduct_uuid TEXT\n" +
                ")";
        sqLiteDatabase.execSQL(product_image);
        String product_weight="CREATE TABLE \"product_weight\" (\n" +
                "\t\"weight_id\"\tINTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                "\t\"weight_unit\"\tTEXT\n" +
                ")";
        sqLiteDatabase.execSQL(product_weight);
        String products="CREATE TABLE \"products\"\n" +
                "(\n" +
                "    product_id             INTEGER not null\n" +
                "        primary key autoincrement,\n" +
                "    product_name_ar        TEXT,\n" +
                "    product_name_en        TEXT,\n" +
                "    product_code           TEXT,\n" +
                "    product_category       TEXT,\n" +
                "    product_description    TEXT,\n" +
                "    product_buy_price      TEXT,\n" +
                "    product_sell_price     TEXT,\n" +
                "    product_supplier       TEXT,\n" +
                "    product_image          TEXT,\n" +
                "    product_stock          TEXT,\n" +
                "    product_weight_unit_id TEXT,\n" +
                "    product_weight         TEXT,\n" +
                "    product_active         BOOLEAN,\n" +
                "    product_tax            DOUBLE,\n" +
                "    product_uuid           TEXT\n" +
                ")";
        sqLiteDatabase.execSQL(products);
        String sequence_text="CREATE TABLE sequence_text(\n" +
                "  id INTEGER NOT NULL,\n" +
                "  type_perfix TEXT,\n" +
                "  current_value TEXT,\n" +
                "  PRIMARY KEY(id)\n" +
                ")";
        sqLiteDatabase.execSQL(sequence_text);
        String shift = "CREATE TABLE shift(\n" +
                "  id INTEGER NOT NULL,\n" +
                "  sequence TEXT,\n" +
                "  device_id TEXT,\n" +
                "  username TEXT,\n" +
                "  start_date_time DATE,\n" +
                "  end_date_time DATE,\n" +
                "  total_cash DOUBLE,\n" +
                "  difference_cash DOUBLE,\n" +
                "  leave_cash DOUBLE,\n" +
                "  start_cash DOUBLE,\n" +
                "  num_successful_transaction DOUBLE,\n" +
                "  num_canceled_transaction DOUBLE,\n" +
                "  num_returned_transaction DOUBLE, notes text, user_mail TEXT,\n" +
                "  PRIMARY KEY(id)\n" +
                ")";
        sqLiteDatabase.execSQL(shift);
        String shop="CREATE TABLE \"shop\" (\n" +
                "\t\"shop_id\"\tINTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                "\t\"shop_name\"\tTEXT,\n" +
                "\t\"shop_contact\"\tTEXT,\n" +
                "\t\"shop_email\"\tTEXT,\n" +
                "\t\"shop_address\"\tTEXT,\n" +
                "\t\"shop_currency\"\tTEXT,\n" +
                "\t\"tax\"\tTEXT\n" +
                ")";
        sqLiteDatabase.execSQL(shop);

        String suppliers="CREATE TABLE \"suppliers\" (\n" +
                "\t\"suppliers_id\"\tINTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                "\t\"suppliers_name\"\tTEXT,\n" +
                "\t\"suppliers_contact_person\"\tTEXT,\n" +
                "\t\"suppliers_cell\"\tTEXT,\n" +
                "\t\"suppliers_email\"\tTEXT,\n" +
                "\t\"suppliers_address\"\tTEXT\n" +
                ")";
        sqLiteDatabase.execSQL(suppliers);
        String user="CREATE TABLE user(\n" +
                "  id INTEGER NOT NULL,\n" +
                "  name_en TEXT,\n" +
                "  name_ar TEXT,\n" +
                "  email TEXT,\n" +
                "  password TEXT NOT NULL,\n" +
                "  username TEXT NOT NULL, mobile TEXT,\n" +
                "  PRIMARY KEY(id)\n" +
                ")";
        sqLiteDatabase.execSQL(user);
        Utils.addLog("datadata_created","done");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}