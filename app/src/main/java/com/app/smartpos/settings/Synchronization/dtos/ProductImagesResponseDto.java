package com.app.smartpos.settings.Synchronization.dtos;

public class ProductImagesResponseDto {
    private Long imagesSize;
    private String newUpdateTimestamp;

    public Long getImagesSize() {
        return imagesSize;
    }

    public void setImagesSize(Long imagesSize) {
        this.imagesSize = imagesSize;
    }

    public String getNewUpdateTimestamp() {
        return newUpdateTimestamp;
    }

    public void setNewUpdateTimestamp(String newUpdateTimestamp) {
        this.newUpdateTimestamp = newUpdateTimestamp;
    }
}
