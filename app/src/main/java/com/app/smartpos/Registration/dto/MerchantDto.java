package com.app.smartpos.Registration.dto;

public class MerchantDto {
    private String name;
    private String merchantId;
    private String VATNumber;
    private String logo;
    private String companyPhone;
    private String currency;
    private String companyEmail;

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public String getVATNumber() {
        return VATNumber;
    }

    public void setVATNumber(String VATNumber) {
        this.VATNumber = VATNumber;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getCompanyPhone() {
        return companyPhone;
    }

    public void setCompanyPhone(String companyPhone) {
        this.companyPhone = companyPhone;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getCompanyEmail() {
        return companyEmail;
    }

    public void setCompanyEmail(String companyEmail) {
        this.companyEmail = companyEmail;
    }

    // toString() method
    @Override
    public String toString() {
        return "Merchant{" +
                "name='" + name + '\'' +
                ", merchantId='" + merchantId + '\'' +
                ", VATNumber='" + VATNumber + '\'' +
                ", logo='" + logo + '\'' +
                ", companyPhone='" + companyPhone + '\'' +
                ", currency='" + currency + '\'' +
                ", companyEmail='" + companyEmail + '\'' +
                '}';
    }
}
