package com.app.smartpos.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class DownloadSyncronization {
    public void synchronizeDataBase (final DatabaseOpenHelper db,String filePath) {
        System.out.println("Entered");
        db.mergeDatabases(filePath);
    }

    public void productImagesSynchronizeDataBase (final DatabaseOpenHelper db,String filePath) {
        System.out.println("Entered");
        db.readProductDatabase(filePath);
    }
}
