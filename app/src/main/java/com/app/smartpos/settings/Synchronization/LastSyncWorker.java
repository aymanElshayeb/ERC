package com.app.smartpos.settings.Synchronization;

import static com.app.smartpos.Constant.API_KEY;
import static com.app.smartpos.utils.SSLUtils.getUnsafeOkHttpClient;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.app.smartpos.utils.AuthoruzationHolder;
import com.app.smartpos.utils.baseDto.ServiceResult;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;

import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class LastSyncWorker extends Worker  {
    public LastSyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        String urL = getInputData().getString("url");
        String tenantId= getInputData().getString("tenantId");
        String authorization= getInputData().getString("Authorization");

        String ecrCode= getInputData().getString("ecrCode");
        if (urL==null || tenantId==null || authorization==null||ecrCode==null) {
            return Result.failure();
        }
        OkHttpClient client = getUnsafeOkHttpClient();
        Headers headers=new Headers.Builder().
                add("tenantId", tenantId).
                add("Authorization",authorization).
                add("apikey",API_KEY).
                add("ecrCode",ecrCode).
                build();
        Request request = new Request.Builder()
                .url(urL) // Replace with your server's upload endpoint
                .get()
                .headers(headers)
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                String responseBody = response.body().string();
                Gson gson=new Gson();
                ServiceResult<LastSyncResponseDto> result=gson.fromJson(responseBody, new TypeToken<ServiceResult<LastSyncResponseDto>>(){}.getType());
                LastSyncResponseDto lastSyncResponseDto=result.getData().getReturnedObj().get(0);
                Data data=new Data.Builder().
                        putString("invoiceBusinessId",lastSyncResponseDto.getInvoiceBusinessId()).
                        putString("shiftBusinessId",lastSyncResponseDto.getShiftBusinessId()).
                        putString("Authorization",authorization).
                        build();
                return Result.success(data); // Return success if the upload is successful
            } else {
                return Result.failure(); // Retry the work if the server returns an error
            }
        } catch (IOException e) {
            e.printStackTrace();
            return Result.failure(); // Return failure if there is an exception
        }
    }
}
