package com.app.smartpos.settings.end_shift;

public class EndShiftModel {
    String real_cash;
    String employee_cash;
    String differences;
    String total_transactions;
    String total_amount;
    String total_tax;

    public EndShiftModel(String real_cash, String employee_cash, String differences, String total_transactions, String total_amount, String total_tax) {
        this.real_cash = real_cash;
        this.employee_cash = employee_cash;
        this.differences = differences;
        this.total_transactions = total_transactions;
        this.total_amount = total_amount;
        this.total_tax = total_tax;
    }

    public String getReal_cash() {
        return real_cash;
    }

    public String getEmployee_cash() {
        return employee_cash;
    }

    public String getDifferences() {
        return differences;
    }

    public String getTotal_transactions() {
        return total_transactions;
    }

    public String getTotal_amount() {
        return total_amount;
    }

    public String getTotal_tax() {
        return total_tax;
    }
}
