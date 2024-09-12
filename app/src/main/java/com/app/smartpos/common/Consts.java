package com.app.smartpos.common;


import android.os.Build;

public class Consts {
    /**
     *  payment application package name
     */
    public final static String MANUFACTURER  = Build.MANUFACTURER;
    public final static String PACKAGE  = "com.intersoft.acquire.mada";

    public final static String PACKAGE_UROVO  = "com.neoleap.urovo.launcher";

    /**
     *  service  action
     */
    public final static String SERVICE_ACTION  = "android.intent.action.intersoft.PAYMENT.SERVICE";

    /** bank aquire ，action*/
    public final static String CARD_ACTION = "android.intent.action.intersoft.PAYMENT";
    public final static String CARD_ACTION_UROVO_PURCHASE = "com.urovo.neoleap.launcher.PURCHASE";

    /** union pay scan ，action*/
    public final static String UNIONPAY_ACTION = "android.intent.action.intersoft.PAYMENT_UNION_SCAN";

    /**
     * installment
     */
    public final static String INSTALLMENT_ACTION = "android.intent.action.intersoft.PAYMENT_INSTALLMENT";
}
