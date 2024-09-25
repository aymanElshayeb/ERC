package com.app.smartpos.Registration;

import static com.app.smartpos.Constant.API_KEY;
import static com.app.smartpos.Constant.BASE_URL;
import static com.app.smartpos.Constant.CHECK_COMPANY_URL;

import android.os.Build;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.app.smartpos.Registration.Model.CompanyModel;
import com.app.smartpos.database.DatabaseAccess;
import com.app.smartpos.refund.Model.RefundModel;
import com.app.smartpos.utils.AuthoruzationHolder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            AndroidNetworking.get(CHECK_COMPANY_URL + "?email=" + email)
                    .addHeaders("apikey", API_KEY)
                    .addHeaders("Authorization", AuthoruzationHolder.getAuthorization())
                    .setTag("GET INVOICE DETAILS")
                    .setPriority(Priority.HIGH)
                    .build().getAsJSONObject(new JSONObjectRequestListener() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.i("datadata", response.toString());
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
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onError(ANError anError) {
                            Log.i("datadata_error", anError.getMessage() + " ");
                        }
                    });
        }
    }
}
