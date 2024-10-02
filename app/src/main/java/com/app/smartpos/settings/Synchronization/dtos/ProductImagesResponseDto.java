package com.app.smartpos.settings.Synchronization.dtos;

public class ProductImagesResponseDto {
    private Long imagesSize;
    private String lastUpdateTimestamp;

    public Long getImagesSize() {
        return imagesSize;
    }

    public void setImagesSize(Long imagesSize) {
        this.imagesSize = imagesSize;
    }

    public String getLastUpdateTimestamp() {
        return lastUpdateTimestamp;
    }

    public void setLastUpdateTimestamp(String lastUpdateTimestamp) {
        this.lastUpdateTimestamp = lastUpdateTimestamp;
    }
}
