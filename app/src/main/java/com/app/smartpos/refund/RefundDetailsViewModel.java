package com.app.smartpos.refund;

import static com.app.smartpos.Constant.BASE_URL;
import static com.app.smartpos.Constant.DOWNLOAD_FILE_NAME_GZIP;
import static com.app.smartpos.Constant.REFUND_URL;
import static com.app.smartpos.Constant.SYNC_URL;
import static com.app.smartpos.utils.SSLUtils.getUnsafeOkHttpClient;

import android.annotation.SuppressLint;
import android.app.Application;
import android.view.View;
import android.widget.Toast;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.work.Data;
import androidx.work.ListenableWorker;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkContinuation;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.app.smartpos.R;
import com.app.smartpos.Registration.RegistrationWorker;
import com.app.smartpos.checkout.NoVatNumberDialog;
import com.app.smartpos.common.Utils;
import com.app.smartpos.database.DatabaseAccess;
import com.app.smartpos.refund.Model.RefundModel;
import com.app.smartpos.utils.BaseActivity;
import com.app.smartpos.utils.GsonUtils;
import com.app.smartpos.utils.Hasher;
import com.app.smartpos.utils.MultiLanguageApp;
import com.app.smartpos.utils.SharedPrefUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RefundDetailsViewModel extends ViewModel {

    MutableLiveData<RefundModel> liveData;
    private OneTimeWorkRequest refundRequest;

    public MutableLiveData<RefundModel> getLiveData() {
        if (liveData == null) {
            liveData = new MutableLiveData<>();
        }
        return liveData;
    }

    public void start(BaseActivity activity, String sequenceId, DatabaseAccess databaseAccess) {
        //Utils.addLog("datadata",SharedPrefUtils.getAuthorization());
        databaseAccess.open();
        HashMap<String, String> conf = databaseAccess.getConfiguration();

        Data refundInputData = new Data.Builder()
                .putString("apikey", SharedPrefUtils.getApiKey())
                .putString("tenantId", conf.get("merchant_id"))
                .putString("Authorization", SharedPrefUtils.getAuthorization())
                .putString("sequenceId",sequenceId)
                .build();
        refundRequest = new OneTimeWorkRequest.Builder(RefundWorker.class)
                .setInputData(refundInputData)
                .build();
        WorkContinuation continuation = WorkManager.getInstance()
                .beginWith(refundRequest);
        continuation.enqueue();
        // cannot observe in view model
        WorkManager.getInstance().getWorkInfoByIdLiveData(refundRequest.getId())
                .observe(activity, workInfo -> {
                    if (workInfo != null && workInfo.getState().isFinished()) {
                        // Work is finished, close pending screen or perform any action
                        handleWorkCompletion(workInfo);
                    }
                    if(workInfo.getState() == WorkInfo.State.FAILED){
                        Toast.makeText(MultiLanguageApp.getApp(), MultiLanguageApp.getApp().getString(R.string.not_authorized_user), Toast.LENGTH_SHORT).show();                    }
                });

//        AndroidNetworking.get(REFUND_URL + sequenceId)
//                .addHeaders("apikey", SharedPrefUtils.getApiKey())
//                .addHeaders("tenantId", conf.get("merchant_id"))
//                .addHeaders("Authorization", SharedPrefUtils.getAuthorization())
//                .setTag("GET INVOICE DETAILS")
//                .setPriority(Priority.HIGH)
//                .build().getAsJSONObject(new JSONObjectRequestListener() {
//                    @Override
//                    public void onResponse(JSONObject response) {
//                        Utils.addLog("datadata", response.toString());
//                        try {
//                            if (response.getInt("code") == 200) {
//                                liveData.postValue(new RefundModel(response.getJSONObject("data").getJSONArray("returnedObj").getJSONObject(0).toString(), databaseAccess));
//                            } else if (response.getInt("code") == 404) {
//                                liveData.postValue(null);
//                            }
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//                    }
//
//                    @Override
//                    public void onError(ANError anError) {
//                        Utils.addLog("datadata_error", anError.getMessage() + " " + anError.getErrorDetail() + " " + anError.getErrorCode());
//                    }
//                });

    }
    private void handleWorkCompletion(WorkInfo workInfo) {
        if (workInfo.getState() == WorkInfo.State.SUCCEEDED && workInfo.getId().equals(refundRequest.getId())) {
            liveData.postValue((RefundModel) GsonUtils.deserializeFromJson(workInfo.getOutputData().getString("refundModel"),RefundModel.class));
//            closePendingScreen();
        } else if (workInfo.getState() == WorkInfo.State.FAILED && workInfo.getId().equals(refundRequest.getId())) {
            liveData.postValue(null);
        }
    }
}
