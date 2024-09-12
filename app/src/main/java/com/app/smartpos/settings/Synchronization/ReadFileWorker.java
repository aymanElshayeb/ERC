package com.app.smartpos.settings.Synchronization;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.app.smartpos.database.DatabaseOpenHelper;
import com.app.smartpos.database.DownloadSyncronization;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ReadFileWorker extends Worker {

    public ReadFileWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        String fileName = getInputData().getString("fileName");
        if (fileName == null) {
            return Result.failure();
        }

        try {
            DatabaseOpenHelper db = new DatabaseOpenHelper(getApplicationContext());
            //old one -> Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
            String downloadsUri = getApplicationContext().getCacheDir().getAbsolutePath();
            synchronizeDataBase(db,downloadsUri+"/"+fileName);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.failure();
        }
        return Result.success();
    }
    private void synchronizeDataBase(final DatabaseOpenHelper db, String filePath) {
        DownloadSyncronization downloadSyncronization = new DownloadSyncronization();
        downloadSyncronization.synchronizeDataBase(db,filePath);
    }
}
