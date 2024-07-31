package com.app.smartpos.settings.end_shift;

import java.util.LinkedList;

public class EndShiftModel {
    LinkedList<ShiftDifferences> shiftDifferences;
    String total_transactions;
    String total_amount;
    String total_tax;

    String date;

    public EndShiftModel(LinkedList<ShiftDifferences> shiftDifferences, String total_transactions, String total_amount, String total_tax,String date) {
        this.shiftDifferences = shiftDifferences;
        this.total_transactions = total_transactions;
        this.total_amount = total_amount;
        this.total_tax = total_tax;
        this.date=date;
    }

    public LinkedList<ShiftDifferences> getShiftDifferences() {
        return shiftDifferences;
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

    public String getDate() {
        return date;
    }
}
