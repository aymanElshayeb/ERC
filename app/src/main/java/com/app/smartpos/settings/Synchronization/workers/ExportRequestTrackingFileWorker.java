package com.app.smartpos.settings.Synchronization.workers;

import static com.app.smartpos.common.CrashReport.CustomExceptionHandler.addToDatabase;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.app.smartpos.database.DatabaseOpenHelper;

public class ExportRequestTrackingFileWorker extends Worker {
    public ExportRequestTrackingFileWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        String fileName = getInputData().getString("fileName");
        try {
            DatabaseOpenHelper db = new DatabaseOpenHelper(getApplicationContext());
            db.exportRequestTrackingToNewDatabase(getApplicationContext().getCacheDir().getAbsolutePath() + "/" + fileName);
        } catch (Exception e) {
            addToDatabase(e,"doWork-exportRequestTrackingFileWorker");
            e.printStackTrace();
            return Result.failure();
        }
        return Result.success(getInputData());


    }
}
