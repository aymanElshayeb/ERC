package com.app.smartpos.settings.Synchronization;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.MenuItem;
import android.view.View;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkContinuation;
import androidx.work.WorkManager;

import com.app.smartpos.R;
import com.app.smartpos.database.DatabaseOpenHelper;
import com.app.smartpos.downloaddatadialog.DownloadDataDialog;
import com.app.smartpos.settings.backup.LocalBackup;
import com.app.smartpos.utils.BaseActivity;

public class DataBaseBackupActivity extends BaseActivity {

    ProgressDialog loading;
    private LocalBackup localBackup;
    CardView cardLocalBackUp, cardLocalImport, cardExportToExcel, cardBackupToDrive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_database_backup);

        getSupportActionBar().setHomeButtonEnabled(true); //for back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//for back button
        getSupportActionBar().setTitle(R.string.data_backup);

        final DatabaseOpenHelper db = new DatabaseOpenHelper(getApplicationContext());
        cardLocalBackUp = findViewById(R.id.download_backup);
        cardLocalImport = findViewById(R.id.upload_backup);




        localBackup = new LocalBackup(this);



        cardLocalImport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DownloadDataDialog dialog=DownloadDataDialog.newInstance(DownloadDataDialog.OPERATION_UPLOAD);
                dialog.show(getSupportFragmentManager(),"dialog");
            }
        });


        cardLocalBackUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DownloadDataDialog dialog=DownloadDataDialog.newInstance(DownloadDataDialog.OPERATION_DOWNLOAD);
                dialog.show(getSupportFragmentManager(),"dialog");

            }
        });

    }




    //for back button
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home:
                // app icon in action bar clicked; goto parent activity.
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public boolean checkStoragePermissions(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            //Android is 11 (R) or above
            return Environment.isExternalStorageManager();
        }else {
            //Below android 11
            int write = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int read = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);

            return read == PackageManager.PERMISSION_GRANTED && write == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void enqueue() {
        //username Admin
        //password 01111Mm&
        Data writeFile  = new Data.Builder().
                build();

        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(ExportFileWorker.class)
                .setInputData(writeFile)
                .build();

        WorkContinuation continuation = WorkManager.getInstance(this)
                .beginWith(workRequest);
        continuation.enqueue();
    }


}
