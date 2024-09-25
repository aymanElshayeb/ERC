package com.app.smartpos.Registration.Model;

import android.util.Log;

import com.app.smartpos.Constant;
import com.app.smartpos.database.DatabaseAccess;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class CompanyModel implements Serializable {
    String companyName;
    String companyCode;


    public CompanyModel(String jsonString) {
        try {
            JSONObject json = new JSONObject(jsonString);

            companyCode = json.getString("companyCode");
            companyName = json.getString("companyName");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getCompanyCode() {
        return companyCode;
    }
}
