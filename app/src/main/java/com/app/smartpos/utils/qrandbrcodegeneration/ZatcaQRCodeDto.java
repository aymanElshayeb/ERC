package com.app.smartpos.utils.qrandbrcodegeneration;


public class ZatcaQRCodeDto {
    private String sellerName;
    private String taxNumber;
    private String invoiceDate;
    private String totalAmountWithTax;
    private String taxAmount;

    public ZatcaQRCodeDto() {
    }

    public ZatcaQRCodeDto(String sellerName, String taxNumber, String invoiceDate, String totalAmountWithTax, String taxAmount) {
        this.sellerName = sellerName;
        this.taxNumber = taxNumber;
        this.invoiceDate = invoiceDate;
        this.totalAmountWithTax = totalAmountWithTax;
        this.taxAmount = taxAmount;
    }

    public String getSellerName() {
        return sellerName;
    }

    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }

    public String getTaxNumber() {
        return taxNumber;
    }

    public void setTaxNumber(String taxNumber) {
        this.taxNumber = taxNumber;
    }

    public String getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(String invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    public String getTotalAmountWithTax() {
        return totalAmountWithTax;
    }

    public void setTotalAmountWithTax(String totalAmountWithTax) {
        this.totalAmountWithTax = totalAmountWithTax;
    }

    public String getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(String taxAmount) {
        this.taxAmount = taxAmount;
    }
}
