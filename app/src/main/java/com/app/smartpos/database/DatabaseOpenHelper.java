package com.app.smartpos.database;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

import com.app.smartpos.R;
import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;

import es.dmoral.toasty.Toasty;

public class DatabaseOpenHelper extends SQLiteAssetHelper {
    public static final String DATABASE_NAME = "smart_pos.db";
    private static final int DATABASE_VERSION = 54;
    private Context mContext;

    public DatabaseOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
        setForcedUpgrade();
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
        SQLiteDatabase existingDb = getWritableDatabase();
        SQLiteDatabase newDb = SQLiteDatabase.openDatabase(newDbFilePath, null, SQLiteDatabase.OPEN_READONLY);

        try {
            existingDb.beginTransaction();

            // Get tables from new database
            String[] tables = {"products","payment_method","card_type","user"};
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

    public void exportTablesToNewDatabase(String newDbFilePath,String[] lastSync ) {
        // Delete the existing file if it exists
        File dbFile = new File(newDbFilePath);
        if (dbFile.exists()) {
            dbFile.delete();
        }
        SQLiteDatabase existingDb = getWritableDatabase();
        SQLiteDatabase newDb = SQLiteDatabase.openOrCreateDatabase(newDbFilePath, null);
        try {
            newDb.beginTransaction();
            exportInvoice(existingDb,newDb,lastSync[0]);
            exportShift(existingDb,newDb,lastSync[1]);
            newDb.setTransactionSuccessful();
        } finally {
            newDb.endTransaction();
            newDb.close();
        }
    }
    private void exportShift(SQLiteDatabase existingDb, SQLiteDatabase newDb,String lastSync){
        try {
            copyTableSchema(existingDb, newDb, "shift");
            copyTableSchema(existingDb, newDb, "credit_calculations");
            String lastSyncShift=String.format("SELECT MAX(id)  FROM shift WHERE sequence = '%s'", lastSync);
            Cursor cursor = existingDb.rawQuery(lastSyncShift, null);
            Integer lastSyncId=-1;
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
            String shiftQuery = "SELECT * FROM shift WHERE id >"+ lastSyncId+";";
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
        }catch (Exception e){
             e.printStackTrace();
        }
    }

    private void exportInvoice(SQLiteDatabase existingDb, SQLiteDatabase newDb,String lastSync){
        try {
            copyTableSchema(existingDb, newDb, "order_list");
            copyTableSchema(existingDb, newDb, "order_details");
            String lastSyncOrderList=String.format("SELECT MAX(order_id)  FROM order_list WHERE invoice_id = '%s'", lastSync);
            Cursor cursor = existingDb.rawQuery(lastSyncOrderList, null);
            Integer lastSyncId=-1;
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
            String shiftQuery =String.format("SELECT * FROM order_list WHERE order_id > %s", lastSyncId);
            try (Cursor shiftCursor = existingDb.rawQuery(shiftQuery, null)) {
                while (shiftCursor.moveToNext()) {
                    ContentValues orderListValues = new ContentValues();
                    for (int i = 0; i < shiftCursor.getColumnCount(); i++) {
                        orderListValues.put(shiftCursor.getColumnName(i), shiftCursor.getString(i));
                    }
                    newDb.insert("order_list", null, orderListValues);
                    @SuppressLint("Range") String orderListId = shiftCursor.getString(shiftCursor.getColumnIndex("invoice_id"));
                    String orderDetails = String.format("SELECT * FROM order_details WHERE invoice_id = '%s'", orderListId);
                    try (Cursor creditCursor = existingDb.rawQuery(orderDetails, null)) {
                        while (creditCursor.moveToNext()) {
                            ContentValues detailsValues = new ContentValues();
                            for (int i = 0; i < creditCursor.getColumnCount(); i++) {
                                detailsValues.put(creditCursor.getColumnName(i), creditCursor.getString(i));
                            }
                            newDb.insert("order_details", null, detailsValues);
                        }
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public  void copyTableSchema(SQLiteDatabase sourceDb, SQLiteDatabase targetDb, String tableName) {
        // Retrieve schema information from the source database
        String schemaQuery = "PRAGMA table_info(" + tableName + ")";
        Cursor cursor = sourceDb.rawQuery(schemaQuery, null);

        // Build CREATE TABLE statement
        StringBuilder createTableQuery = new StringBuilder("CREATE TABLE IF NOT EXISTS " + tableName + " (");

        while (cursor.moveToNext()) {
            @SuppressLint("Range") String columnName = cursor.getString(cursor.getColumnIndex("name"));
            @SuppressLint("Range") String columnType = cursor.getString(cursor.getColumnIndex("type"));
            createTableQuery.append(columnName).append(" ").append(columnType).append(", ");
        }

        // Remove trailing comma and space
        if (createTableQuery.length() > 0) {
            createTableQuery.setLength(createTableQuery.length() - 2);
        }
        createTableQuery.append(");");

        cursor.close();

        // Execute CREATE TABLE statement in the target database
        targetDb.execSQL(createTableQuery.toString());
    }


}