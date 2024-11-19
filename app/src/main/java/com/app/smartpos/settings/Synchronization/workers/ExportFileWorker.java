package com.app.smartpos.settings.Synchronization.workers;

import static com.app.smartpos.common.CrashReport.CustomExceptionHandler.addToDatabase;

import android.content.Context;
import android.os.Environment;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.app.smartpos.common.Utils;
import com.app.smartpos.database.DatabaseAccess;
import com.app.smartpos.database.DatabaseOpenHelper;
import com.app.smartpos.utils.MultiLanguageApp;

import java.util.HashMap;

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
        boolean fromRefund = getInputData().getBoolean("fromRefund",false);
        String[] lastSync = new String[]{invoiceLastSync, shiftLastSync};
        boolean needSync;
        try {
            DatabaseOpenHelper db = new DatabaseOpenHelper(getApplicationContext());
            String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
            needSync = db.exportTablesToNewDatabase(getApplicationContext().getCacheDir().getAbsolutePath() + "/" + fileName, lastSync,fromRefund);
        } catch (Exception e) {
            addToDatabase(e,"doWork-exportFileWorker");
            e.printStackTrace();
            return Result.failure();
        }
        if(needSync) {
            return Result.success(getInputData());
        }else{
            return Result.failure(new Data.Builder().putBoolean("needSync",needSync).build());
        }


    }
}
