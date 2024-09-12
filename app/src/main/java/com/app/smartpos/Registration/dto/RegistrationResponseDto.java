package com.app.smartpos.Registration.dto;

public class RegistrationResponseDto {
    private String ecrCode;
    private String deviceId;
    private MerchantDto merchant;
    private String token;
    private TaxDto tax;

    // Getters and Setters
    public String getEcrCode() {
        return ecrCode;
    }

    public void setEcrCode(String ecrCode) {
        this.ecrCode = ecrCode;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public MerchantDto getMerchant() {
        return merchant;
    }

    public void setMerchant(MerchantDto merchant) {
        this.merchant = merchant;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public TaxDto getTax() {
        return tax;
    }

    public void setTax(TaxDto tax) {
        this.tax = tax;
    }

    @Override
    public String toString() {
        return "RegistrationResponse{" +
                "ecrCode='" + ecrCode + '\'' +
                ", deviceId='" + deviceId + '\'' +
                ", merchant=" + (merchant != null ? merchant.toString() : "null") +
                ", token='" + token + '\'' +
                ", tax=" + (tax != null ? tax.toString() : "null") +
                '}';
    }


}

