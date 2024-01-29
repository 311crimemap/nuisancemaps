package com.quirkshop.nuisancemaps.model;

public enum DataJobStatus {
    QUEUED("QUEUED"),
    PENDING("PENDING"),
    FETCH_START("FETCH_START"),
    FETCH_ERROR("FETCH_ERROR"),
    FETCH_COMPLETE("FETCH_COMPLETE"),
    COMPLETED("COMPLETED"),
    ERROR("ERROR");

    private final String status;

    DataJobStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

}
