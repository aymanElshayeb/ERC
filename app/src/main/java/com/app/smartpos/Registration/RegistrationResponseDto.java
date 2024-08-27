package com.app.smartpos.Registration;

public class RegistrationResponseDto {
    private String ecrCode;
    private String deviceId;
    private String merchantId;
    private String username;
    private String token;
    private String merchantLogo;
    private String tax;

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

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getMerchantLogo() {
        return merchantLogo;
    }

    public void setMerchantLogo(String merchantLogo) {
        this.merchantLogo = merchantLogo;
    }

    public String getTax() {
        return tax;
    }

    public void setTax(String tax) {
        this.tax = tax;
    }

    @Override
    public String toString() {
        return "EcrResponse{" +
                "ecrCode='" + ecrCode + '\'' +
                ", deviceId='" + deviceId + '\'' +
                ", merchantId='" + merchantId + '\'' +
                ", username='" + username + '\'' +
                ", token='" + token + '\'' +
                ", merchantLogo='" + merchantLogo + '\'' +
                ", taxnumber='" + tax + '\'' +
                '}';
    }
}

