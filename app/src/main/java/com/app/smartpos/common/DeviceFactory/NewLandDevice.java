package com.app.smartpos.common.DeviceFactory;

import android.content.Intent;

import com.app.smartpos.common.Consts;
import com.app.smartpos.common.ThirdTag;

public class NewLandDevice extends Device implements DeviceInterface{
    @Override
    public Intent pay(double total) {
        Intent intent = new Intent();
        intent.setPackage(Consts.PACKAGE);
        intent.setAction(Consts.CARD_ACTION);
        intent.putExtra(ThirdTag.CHANNEL_ID, "acquire");
        intent.putExtra(ThirdTag.TRANS_TYPE, 2);
        intent.putExtra(ThirdTag.OUT_ORDERNO, "12345");
        intent.putExtra(ThirdTag.AMOUNT, total);
        intent.putExtra(ThirdTag.INSERT_SALE, true);
        intent.putExtra(ThirdTag.RF_FORCE_PSW, true);
        return intent;
    }

    @Override
    public String resultHeader() {
        return "madaTransactionResult";
    }

    @Override
    public String jsonActivityResult() {
        return ThirdTag.JSON_DATA;
    }
}
