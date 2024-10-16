package com.app.smartpos.Registration;

import static com.app.smartpos.utils.SSLUtils.getUnsafeOkHttpClient;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.app.smartpos.Constant;
import com.app.smartpos.R;
import com.app.smartpos.Registration.dto.RegistrationResponseDto;
import com.app.smartpos.common.Utils;
import com.app.smartpos.database.DatabaseAccess;
import com.app.smartpos.utils.baseDto.ServiceRequest;
import com.app.smartpos.utils.baseDto.ServiceResult;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RegistrationWorker extends Worker {
    public RegistrationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        String email = getInputData().getString("email");
        String password = getInputData().getString("password");
        String tenantId = getInputData().getString("tenantId");
        String deviceId = getInputData().getString("deviceId");
        String urlString = getInputData().getString("url");
        if (email == null || password == null || tenantId == null || deviceId == null) {
            return Result.failure();
        }
        // Perform registration logic here
        OkHttpClient client = getUnsafeOkHttpClient();
        Headers headers = new Headers.Builder().
                add("tenantId", tenantId).
                add("apikey", Constant.API_KEY).
                build();
        //prepare the dto class
        JSONObject json = new JSONObject();
        JSONObject data = new JSONObject();

        try {
            data.put("email", email);
            data.put("password", password);
            data.put("deviceId", deviceId);
            json.put("data",data);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        Gson gson = new Gson();

        Utils.addLog("datadata_register", json.toString());
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(json.toString(), JSON);
        Request request = new Request.Builder()
                .url(urlString)
                .post(body)
                .headers(headers)
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                String responseBody = response.body().string();
                ServiceResult<RegistrationResponseDto> result = gson.fromJson(responseBody, new TypeToken<ServiceResult<RegistrationResponseDto>>() {
                }.getType());
                if (result.getCode() == 400 && result.getFault().getStatusCode().equalsIgnoreCase("E0000004")) {
                    Data outputData = new Data.Builder().putString("errorMessage", getApplicationContext().getString(R.string.non_admin_register)).build();
                    return Result.failure(outputData);
                }
                if (result.getCode() != 200) {
                    Data outputData = new Data.Builder().putString("errorMessage", getApplicationContext().getString(R.string.failed_to_register)).build();
                    return Result.failure(outputData);
                }
                RegistrationResponseDto registrationResponseDto = result.getData().getReturnedObj().get(0);
                DatabaseAccess databaseAccess = DatabaseAccess.getInstance(getApplicationContext());
                databaseAccess.open();
                databaseAccess.addConfiguration(registrationResponseDto);
                databaseAccess.open();
                databaseAccess.addShop(registrationResponseDto, databaseAccess);
                String authorization = registrationResponseDto.getToken();
                Data outputData = new Data.Builder().
                        putString("Authorization", authorization).
                        putString("ecrCode", registrationResponseDto.getEcrCode()).
                        build();
                return Result.success(outputData);
            } else {
                Data outputData = new Data.Builder().putString("errorMessage", getApplicationContext().getString(R.string.failed_to_register)).build();
                return Result.failure(outputData);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return Result.failure();
        }
    }
}
