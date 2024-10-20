package com.app.smartpos.settings.Synchronization.workers;

import android.content.Context;
import android.os.Environment;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.app.smartpos.database.DatabaseOpenHelper;

public class ExportFileWorker extends Worker {
    public ExportFileWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        String fileName = getInputData().getString("fileName");
        String invoiceLastSync = getInputData().getString("invoiceBusinessId");
        String shiftLastSync = getInputData().getString("shiftBusinessId");
        String[] lastSync = new String[]{invoiceLastSync, shiftLastSync};
        try {
            DatabaseOpenHelper db = new DatabaseOpenHelper(getApplicationContext());
            String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
            db.exportTablesToNewDatabase(getApplicationContext().getCacheDir().getAbsolutePath() + "/" + fileName, lastSync);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.failure();
        }
        return Result.success(getInputData());


    }
}
