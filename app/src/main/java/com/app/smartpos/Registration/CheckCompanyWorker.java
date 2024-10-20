package com.app.smartpos.Registration;

import static com.app.smartpos.utils.SSLUtils.getUnsafeOkHttpClient;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.app.smartpos.Constant;
import com.app.smartpos.R;
import com.app.smartpos.Registration.dto.RegistrationResponseDto;
import com.app.smartpos.utils.SharedPrefUtils;
import com.app.smartpos.utils.baseDto.ServiceRequest;
import com.app.smartpos.utils.baseDto.ServiceResult;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;

import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CheckCompanyWorker extends Worker {
    public CheckCompanyWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        String email = getInputData().getString("email");

        String deviceId = getInputData().getString("deviceId");
        String urlString = getInputData().getString("url") + "?email=" + email;
        if (email == null || deviceId == null) {
            return Result.failure();
        }
        // Perform registration logic here
        OkHttpClient client = getUnsafeOkHttpClient();
        Headers headers = new Headers.Builder().
                add("apikey", SharedPrefUtils.getApiKey()).
                build();
        //prepare the dto class
        RegistrationRequestDto registrationRequestDto = new RegistrationRequestDto();
        registrationRequestDto.setemail(email);
        registrationRequestDto.setDeviceId(deviceId);
        ServiceRequest<RegistrationRequestDto> serviceRequest = ServiceRequest.constructServiceRequest(registrationRequestDto);
        //prepare the req1uest
        Gson gson = new Gson();
        String jsonBody = gson.toJson(serviceRequest);
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(jsonBody, JSON);
        Request request = new Request.Builder()
                .url(urlString)
                .get()
                .headers(headers)
//                .addHeader("email",email)
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                String responseBody = response.body().string();
                ServiceResult<RegistrationResponseDto> result = gson.fromJson(responseBody, new TypeToken<ServiceResult<RegistrationResponseDto>>() {
                }.getType());
                Log.e("Result", result.toString());

                if (result.getCode() != 200) {
                    Data outputData = new Data.Builder().putString("errorMessage", getApplicationContext().getString(R.string.failed_to_register)).build();
                    return Result.failure(outputData);
                }
                RegistrationResponseDto registrationResponseDto = result.getData().getReturnedObj().get(0);
//                DatabaseAccess databaseAccess = DatabaseAccess.getInstance(getApplicationContext());
//                databaseAccess.open();
//                databaseAccess.addConfiguration(registrationResponseDto);
//                databaseAccess.open();
//                databaseAccess.addShop(registrationResponseDto,databaseAccess);
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
