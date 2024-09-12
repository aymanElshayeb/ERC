package com.app.smartpos.settings.Synchronization;

public class LastSyncResponseDto {
    private String invoiceBusinessId;
    private String shiftBusinessId;

    public LastSyncResponseDto() {
    }

    public LastSyncResponseDto(String invoiceBusinessId, String shiftBusinessId) {
        this.invoiceBusinessId = invoiceBusinessId;
        this.shiftBusinessId = shiftBusinessId;
    }

    public String getInvoiceBusinessId() {
        return invoiceBusinessId;
    }

    public void setInvoiceBusinessId(String invoiceBusinessId) {
        this.invoiceBusinessId = invoiceBusinessId;
    }

    public String getShiftBusinessId() {
        return shiftBusinessId;
    }

    public void setShiftBusinessId(String shiftBusinessId) {
        this.shiftBusinessId = shiftBusinessId;
    }
}
