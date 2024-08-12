package com.app.smartpos.Registration;

public class RegistrationRequestDto {
    private String username;
    private String password;
    private String deviceId;

    public RegistrationRequestDto(String username, String password, String deviceId) {
        this.username = username;
        this.password = password;
        this.deviceId = deviceId;
    }

    public RegistrationRequestDto() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

}
