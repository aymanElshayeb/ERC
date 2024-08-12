package com.app.smartpos.database;

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
    private static final int DATABASE_VERSION = 16;
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



}