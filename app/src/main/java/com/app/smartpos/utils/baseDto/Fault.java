package com.app.smartpos.utils.baseDto;

public class Fault {
    private String statusCode;
    private String statusDescription;
    private String statusDescriptionTranslation;

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public String getStatusDescription() {
        return statusDescription;
    }

    public void setStatusDescription(String statusDescription) {
        this.statusDescription = statusDescription;
    }

    public String getStatusDescriptionTranslation() {
        return statusDescriptionTranslation;
    }

    public void setStatusDescriptionTranslation(String statusDescriptionTranslation) {
        this.statusDescriptionTranslation = statusDescriptionTranslation;
    }
}
