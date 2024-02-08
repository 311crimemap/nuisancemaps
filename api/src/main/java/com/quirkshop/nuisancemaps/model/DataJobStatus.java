package com.quirkshop.nuisancemaps.model;

public enum DataJobStatus {
    QUEUED("QUEUED"),
    START("START"),
    FETCH_START("FETCH_START"),
    FETCH_ERROR("FETCH_ERROR"),
    FETCH_COMPLETE("FETCH_COMPLETE"),
    PARSE_ERROR("PARSE_ERROR"),
    PENDING("PENDING"), // pending createData
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
