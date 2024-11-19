package com.app.smartpos.Registration.Model;

import static com.app.smartpos.common.CrashReport.CustomExceptionHandler.addToDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class CompanyModel implements Serializable {
    String companyName;
    String companyCode;


    public CompanyModel(String jsonString) {
        try {
            JSONObject json = new JSONObject(jsonString);

            companyCode = json.getString("companyCode");
            companyName = json.getString("companyName");
        } catch (JSONException e) {
            addToDatabase(e,"read-company-model-from-json");
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
