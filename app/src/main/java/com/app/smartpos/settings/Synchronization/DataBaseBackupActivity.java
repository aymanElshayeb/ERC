package com.app.smartpos.settings.Synchronization;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import static java.lang.Math.log;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.ajts.androidmads.library.SQLiteToExcel;
import com.app.smartpos.R;
import com.app.smartpos.database.DatabaseOpenHelper;
import com.app.smartpos.settings.backup.BackupActivity;
import com.app.smartpos.settings.backup.LocalBackup;
import com.app.smartpos.utils.BaseActivity;
import com.obsez.android.lib.filechooser.ChooserDialog;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import es.dmoral.toasty.Toasty;

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


        checkPermissions();


        cardLocalImport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });


        cardLocalBackUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enqueueDownloadAndReadWorkers();
            }
        });

    }


    private static final int STORAGE_PERMISSION_CODE = 23;

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
    private void checkPermissions() {
        if (checkStoragePermissions()) {
            requestForStoragePermissions();
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
    private void requestForStoragePermissions() {
        //Android is 11 (R) or above
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            try {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                Uri uri = Uri.fromParts("package", this.getPackageName(), null);
                intent.setData(uri);
                storageActivityResultLauncher.launch(intent);
            }catch (Exception e){
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                storageActivityResultLauncher.launch(intent);
            }
        }else{
            //Below android 11
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                    },
                    STORAGE_PERMISSION_CODE
            );
        }

    }
    private ActivityResultLauncher<Intent> storageActivityResultLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    new ActivityResultCallback<ActivityResult>(){

                        @Override
                        public void onActivityResult(ActivityResult o) {
                            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
                                //Android is 11 (R) or above
                                if(Environment.isExternalStorageManager()){
                                    //Manage External Storage Permissions Granted
                                    Log.d(TAG, "onActivityResult: Manage External Storage Permissions Granted");
                                }else{
                                    Toast.makeText(DataBaseBackupActivity.this, "Storage Permissions Denied", Toast.LENGTH_SHORT).show();
                                }
                            }else{
                                //Below android 11

                            }
                        }
                    });


    private void enqueueDownloadAndReadWorkers() {
        Data downloadInputData = new Data.Builder()
                .putString("url", "https://gateway-am-wso2-nonprod.apps.nt-non-ocp.neotek.sa/ecr/v1/sync")
                .putString("fileName", "download.db.gz")
                .build();

        Data decompressInputData = new Data.Builder()
                .putString("fileName", "download.db.gz")
                .build();

        Data readInputData = new Data.Builder()
                .putString("fileName", "download.db")
                .build();

        OneTimeWorkRequest downloadRequest = new OneTimeWorkRequest.Builder(DownloadWorker.class)
                .setInputData(downloadInputData)
                .build();

        OneTimeWorkRequest decompressRequest = new OneTimeWorkRequest.Builder(DecompressWorker.class)
                .setInputData(decompressInputData)
                .build();

        OneTimeWorkRequest readRequest = new OneTimeWorkRequest.Builder(ReadFileWorker.class)
                .setInputData(readInputData)
                .build();

        WorkManager.getInstance(this)
                .beginWith(downloadRequest)
                .then(decompressRequest)
                .then(readRequest)
                .enqueue();
    }

}
