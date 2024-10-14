package com.app.smartpos.settings.Synchronization.workers;

import static com.app.smartpos.Constant.API_KEY;
import static com.app.smartpos.utils.SSLUtils.getUnsafeOkHttpClient;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.app.smartpos.common.Utils;
import com.app.smartpos.settings.Synchronization.dtos.ProductImagesResponseDto;
import com.app.smartpos.utils.baseDto.ServiceResult;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ProductImagesSizeWorker extends Worker {

    public ProductImagesSizeWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        String urL = getInputData().getString("url");
        String tenantId= getInputData().getString("tenantId");
        String authorization= getInputData().getString("Authorization");

        if (urL==null || tenantId==null || authorization==null) {
            Utils.addLog("datadata_worker","fail");
            return Result.failure();
        }
        FormBody formBody = new FormBody.Builder()
                .add("email", "email")
                .add("password","")
                .build();
        OkHttpClient client = getUnsafeOkHttpClient();
        Headers headers=new Headers.Builder().
                add("tenantId", tenantId).
                add("Authorization",authorization).
                add("apikey",API_KEY).
                build();
        Utils.addLog("datadata_worker",headers.toString());
        Utils.addLog("datadata_worker",authorization);
        Request request = new Request.Builder()
                .url(urL) // Replace with your server's upload endpoint
                .post(formBody)
                .headers(headers)
                .build();
        Utils.addLog("datadata_worker",request.toString());
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                Utils.addLog("datadata_worker","success");
                String responseBody = response.body().string();
                Utils.addLog("datadata_worker",responseBody);
                Gson gson=new Gson();
                ServiceResult<ProductImagesResponseDto> result=gson.fromJson(responseBody, new TypeToken<ServiceResult<ProductImagesResponseDto>>(){}.getType());
                ProductImagesResponseDto productImagesResponseDto=result.getData().getReturnedObj().get(0);
                Data data=new Data.Builder().
                        putLong("imagesSize",productImagesResponseDto.getImagesSize()).
                        putString("newUpdateTimestamp",productImagesResponseDto.getNewUpdateTimestamp()).
                        build();
                return Result.success(data); // Return success if the response is successful
            } else {

                return Result.failure(); // Retry the work if the server returns an error
            }
        } catch (IOException e) {
            e.printStackTrace();
            return Result.failure(); // Return failure if there is an exception
        }
    }
}
