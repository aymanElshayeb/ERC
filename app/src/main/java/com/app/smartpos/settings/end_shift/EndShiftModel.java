package com.app.smartpos.settings.end_shift;

import java.io.Serializable;
import java.util.HashMap;

public class EndShiftModel implements Serializable {
    HashMap<String, ShiftDifferences> shiftDifferences;
    int num_successful_transaction;
    int num_canceled_transaction;
    int num_returned_transaction;
    double total_amount;
    int totalRefunds;
    double total_tax;
    String deviceID;
    long startDateTime;
    long endDateTime;
    double startCash;
    double leaveCash;
    double totalRefundsAmount;
    double totalCardsAmount;
    String note;
    String sequence;
    String userName;

    public EndShiftModel(HashMap<String, ShiftDifferences> shiftDifferences, String sequence, String userName, int num_successful_transaction, int num_canceled_transaction, int num_returned_transaction, double total_amount, double total_tax, String deviceID, long startDateTime, long endDateTime, double startCash, double leaveCash, String note, double totalRefundsAmount, double totalCardsAmount) {
        this.shiftDifferences = shiftDifferences;
        this.sequence = sequence;
        this.userName = userName;
        this.num_successful_transaction = num_successful_transaction;
        this.num_canceled_transaction = num_canceled_transaction;
        this.num_returned_transaction = num_returned_transaction;
        this.total_amount = total_amount;
        this.total_tax = total_tax;
        this.deviceID = deviceID;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.startCash = startCash;
        this.leaveCash = leaveCash;
        this.note = note;
        this.totalRefundsAmount = totalRefundsAmount;
        this.totalCardsAmount = totalCardsAmount;
    }

    public HashMap<String, ShiftDifferences> getShiftDifferences() {
        return shiftDifferences;
    }

    public String getUserName() {
        return userName;
    }

    public int getNum_successful_transaction() {
        return num_successful_transaction;
    }

    public int getNum_canceled_transaction() {
        return num_canceled_transaction;
    }

    public int getNum_returned_transaction() {
        return num_returned_transaction;
    }

    public String getSequence() {
        return sequence;
    }

    public double getTotal_amount() {
        return total_amount;
    }

    public double getTotal_tax() {
        return total_tax;
    }

    public String getDeviceID() {
        return deviceID;
    }

    public long getStartDateTime() {
        return startDateTime;
    }

    public long getEndDateTime() {
        return endDateTime;
    }

    public double getStartCash() {
        return startCash;
    }

    public double getLeaveCash() {
        return leaveCash;
    }

    public String getNote() {
        return note;
    }

    public int getTotalRefunds() {
        return totalRefunds;
    }

    public double getTotalRefundsAmount() {
        return totalRefundsAmount;
    }

    public double getTotalCardsAmount() {
        return totalCardsAmount;
    }

    public void setTotalRefunds(int totalRefunds) {
        this.totalRefunds = totalRefunds;
    }

    public void setLeaveCash(double leaveCash) {
        this.leaveCash = leaveCash;
    }

    @Override
    public String toString() {
        return "EndShiftModel{" +
                "shiftDifferences=" + shiftDifferences +
                ", num_successful_transaction=" + num_successful_transaction +
                ", num_canceled_transaction=" + num_canceled_transaction +
                ", num_returned_transaction=" + num_returned_transaction +
                ", total_amount=" + total_amount +
                ", totalRefunds=" + totalRefunds +
                ", total_tax=" + total_tax +
                ", deviceID='" + deviceID + '\'' +
                ", startDateTime=" + startDateTime +
                ", endDateTime=" + endDateTime +
                ", startCash=" + startCash +
                ", leaveCash=" + leaveCash +
                ", note='" + note + '\'' +
                ", sequence='" + sequence + '\'' +
                ", userName='" + userName + '\'' +
                '}';
    }
}
