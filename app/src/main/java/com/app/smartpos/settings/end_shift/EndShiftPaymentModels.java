package com.app.smartpos.settings.end_shift;

import android.widget.EditText;
import android.widget.TextView;

public class EndShiftPaymentModels {
    EditText inputPaymentCashEt;
    TextView paymentCashErrorTv;
    String type;
    double cash;

    boolean error=false;

    public EndShiftPaymentModels(EditText inputPaymentCashEt, TextView paymentCashErrorTv, String type, double cash) {
        this.inputPaymentCashEt = inputPaymentCashEt;
        this.paymentCashErrorTv = paymentCashErrorTv;
        this.type = type;
        this.cash = cash;
    }

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }
}
