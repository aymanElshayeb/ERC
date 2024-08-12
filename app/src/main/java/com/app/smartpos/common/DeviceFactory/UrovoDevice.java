package com.app.smartpos.common.DeviceFactory;

import android.content.Intent;

import com.app.smartpos.common.Consts;
import com.app.smartpos.common.ThirdTag;

public class UrovoDevice extends Device implements DeviceInterface{
    @Override
    public Intent pay(double total) {
        Intent intent = new Intent();
        intent.setPackage(Consts.PACKAGE_UROVO);
        intent.setAction(Consts.CARD_ACTION_UROVO_PURCHASE);
        intent.putExtra(ThirdTag.TRANS_TYPE, "2");
        intent.putExtra(ThirdTag.AMOUNT, total);
        intent.putExtra(ThirdTag.IS_APP_2_APP, true);
        return intent;
    }

    @Override
    public String resultHeader() {
        return "TransactionResult";
    }
}
