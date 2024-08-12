package com.app.smartpos.common;

/**
 * Third part call Field
 */
public class ThirdTag {
    /**
     * responseCode
     */
    public final static String RESPONSE_CODE = "responseCode";
    /**
     * response massage
     */
    public final static String MESSAGE = "message";
    /**
     * merchantId
     */
    public final static String MERCHANT_ID = "merchantId";
    /**
     * merchantNam
     */
    public final static String MERCHANT_NAME = "merchantNam";
    /**
     * terminalId
     */
    public final static String TERMINAL_ID = "terminalId";
    /**
     * channelId
     */
    public final static String CHANNEL_ID = "channelId";
    /**
     * operatorNo
     */
    public final static String OPER = "operatorNo";

    /**
     * transType
     */
    public final static String TRANS_TYPE = "transType";

    /**
     * Bitmap
     */
    public final static String Bitmap = "BitmapImage";

    /**

    /**
     * amount
     */
    public final static String AMOUNT = "amount";
    public final static String TRANS_AMOUNT = "transAmount";
    /**
     * cardNo
     */
    public final static String CARD_NO = "cardNo";
    /**
     * cardSerialNo
     */
    public final static String CARD_SN = "cardSerialNo";
    /**
     * expDate
     */
    public final static String EXP_DATE = "expDate";
    /**
     * voucherNo
     */
    public final static String TRACE_NO = "voucherNo";
    /**
     * batchNo
     */
    public final static String BATCH_NO = "batchNo";
    /**
     * referenceNo
     */
    public final static String REFERENCE_NO = "referenceNo";
    /**
     * authCode
     */
    public final static String AUTH_CODE = "authCode";
    /**
     * balance
     */
    public final static String BALANCE = "balance";
    /**
     *   original VoucherNo
     */
    public final static String OLD_TRACE_NO = "oriVoucherNo";

    /**
     * original AuthCode
     */
    public final static String OLD_AUTH_CODE = "oriAuthCode";

    /**
     * original ReferenceNo
     */
    public final static String OLD_REFERENCE_NO = "oriReferenceNo";
    /**
     * original transaction data  YYMMDDHHMMSS
     */
    public final static String OLD_TRANS_TIME = "oriTransTime";
    /**
     * transaction data   YYMMDDHHMMSS
     */
    public final static String TRANS_TIME = "transTime";


    /**
     *  external OrderNo
     */
    public final static String OUT_ORDERNO = "outOrderNo";

    /**
     * 付款码,收银台扫码后，结果通过该TAG传给收单
     *  using in scan transaction
     */
    public final static String PAY_CODE = "payCode";

    /**
     * 扫码订单号,扫码类交易的结果订单号可以放此
     * scan transaction
     */
    public final static String QR_ORDER = "payOrderNo";

    /**
     * 是否需要主管密码 ,默认显示.  true：显示  false：不显示
     *
     */
    public final static String IS_OPEN_ADMIN = "isOpenAdminVerify";
    /**
     * 读卡方式 ，
     *
     */
    public final static String CARD_INPUT_MODE = "cardInputMode";

    /**
     * 备注
     */
    public final static String REMARKS = "remarks";
    /**
     * 主密钥索引
     */
    public final static String MAIN_INDEX = "tmkIndex";

    //===========版本2.2.00增加============
    /**
     * 分期数
     */
    public final static String PERIODS = "periods";
    /**
     * 收单行名称
     */
    public final static String ACQ_NAME = "acqName";
    /**
     * 收单行编码
     */
    public final static String ACQ_CODE = "acqCode";
    /**
     * 发卡行名称
     */
    public final static String IIS_NAME = "iisName";
    /**
     * 发卡行编码
     */
    public final static String IIS_CODE = "iisCode";
    /**
     * 卡组织
     */
    public final static String INTER_ORG = "interOrg";
    /**
     * 货币类型
     */
    public final static String CURRENCY_CODE = "currencyCode";
    /**
     * 银行卡属性
     */
    public final static String CARD_ATTRIBUTE = "cardAttribute";
    /**
     * 原外部订单号
     */
    public final static String OLD_OUT_ORDERNO = "oriOutOrderNo";
    /**
     * 闪付凭密
     */
    public final static String RF_FORCE_PSW = "rfForcePsw";
    /**
     * contact transaction
     */
    public final static String INSERT_SALE = "insertSale";

    public final static  String JSON_DATA="JSON_DATA";

    //CR# 15.09.2021
    public final static  String XML_DATA="XML_DATA";
    //========================
}
