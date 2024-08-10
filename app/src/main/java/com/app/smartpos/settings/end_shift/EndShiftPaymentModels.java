package com.app.smartpos.settings.end_shift;

import android.widget.EditText;
import android.widget.TextView;

public class EndShiftPaymentModels {
    EditText inputPaymentCashEt;
    TextView paymentCashErrorTv;
    String type;
    double real;

    boolean error=false;

    public EndShiftPaymentModels(EditText inputPaymentCashEt, TextView paymentCashErrorTv, String type, double real) {
        this.inputPaymentCashEt = inputPaymentCashEt;
        this.paymentCashErrorTv = paymentCashErrorTv;
        this.type = type;
        this.real = real;
    }

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }
}
