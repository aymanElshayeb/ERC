package com.app.smartpos.database;

public class DownloadSyncronization {
    public void synchronizeDataBase(final DatabaseOpenHelper db, String filePath) {
        System.out.println("Entered");
        db.mergeDatabases(filePath);
    }

    public void productImagesSynchronizeDataBase(final DatabaseOpenHelper db, String filePath) {
        System.out.println("Entered");
        db.readProductDatabase(filePath);
    }
}
