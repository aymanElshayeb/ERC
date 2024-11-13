package com.app.smartpos.Registration;

import static com.app.smartpos.Constant.CHECK_COMPANY_URL;
import static com.app.smartpos.common.CrashReport.CustomExceptionHandler.addToDatabase;

import android.util.Log;
import android.widget.Toast;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.app.smartpos.Registration.Model.CompanyModel;
import com.app.smartpos.common.Utils;
import com.app.smartpos.utils.MultiLanguageApp;
import com.app.smartpos.utils.SharedPrefUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;

public class CheckCompaniesViewModel extends ViewModel {

    MutableLiveData<LinkedList<CompanyModel>> liveData;

    public MutableLiveData<LinkedList<CompanyModel>> getLiveData() {
        if (liveData == null) {
            liveData = new MutableLiveData<>();
        }
        return liveData;
    }

    public void start(String email) {
        AndroidNetworking.get(CHECK_COMPANY_URL + "?email=" + email)
                .addHeaders("apikey", SharedPrefUtils.getApiKey())
                .addHeaders("Authorization", SharedPrefUtils.getAuthorization())
                .setTag("GET INVOICE DETAILS")
                .setPriority(Priority.HIGH)
                .build().getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Utils.addLog("datadata", response.toString());
                        try {
                            LinkedList<CompanyModel> list = new LinkedList<>();
                            if (response.getInt("code") == 200) {

                                JSONArray array = response.getJSONObject("data").getJSONArray("returnedObj");
                                for (int i = 0; i < array.length(); i++) {
                                    list.addLast(new CompanyModel(array.getJSONObject(i).toString()));
                                }
                            } else if (response.getInt("code") == 404) {

                            }
                            liveData.postValue(list);
                        } catch (JSONException e) {
                            addToDatabase(e,"checkCompanyViewModel-read-response");
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        Utils.addLog("datadata_error", anError.getMessage() + " ");
                        Toast.makeText(MultiLanguageApp.getApp(), anError.getMessage() + " ", Toast.LENGTH_SHORT).show();
                        liveData.postValue(null);
                    }
                });

    }
}
