package com.app.smartpos.Registration.dto;

public class TaxDto {
    private Double percentage;

    public Double getPercentage() {
        return percentage;
    }

    public void setPercentage(Double percentage) {
        this.percentage = percentage;
    }

    @Override
    public String toString() {
        return "Tax{" +
                "percentage='" + percentage + '\'' +
                '}';
    }

}
