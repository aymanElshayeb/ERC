package com.app.smartpos.settings.Synchronization;

import android.content.Context;
import android.os.Environment;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.app.smartpos.database.DatabaseOpenHelper;

public class WriteFileWorker extends Worker {
    public WriteFileWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            DatabaseOpenHelper db = new DatabaseOpenHelper(getApplicationContext());
            db.exportTablesToNewDatabase(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath()+"/upload.db",null);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.failure();
        }
        return Result.success();


    }
}
