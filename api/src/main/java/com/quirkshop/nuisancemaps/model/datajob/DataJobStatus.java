package com.quirkshop.nuisancemaps.model.datajob;

public enum DataJobStatus {
    QUEUED("QUEUED"),
    START("START"),
    FETCH_START("FETCH_START"),
    FETCH_ERROR("FETCH_ERROR"),
    FETCH_COMPLETE("FETCH_COMPLETE"),
    PROCESS_START("PROCESS_START"),
    NO_SPACE_ERROR("NO_SPACE_ERROR"),
    BUILD_FILENAME_ERROR("BUILD_FILENAME_ERROR"),
    WRITE_FILE_ERROR("WRITE_FILE_ERROR"),
    WRITE_COMPLETE("WRITE_COMPLETE"),
    READ_FILE_START("READ_FILE_START"),
    READ_FILE_ERROR("READ_FILE_ERROR"),
    PARSE_ERROR("PARSE_ERROR"),
    PENDING("PENDING"), // pending createData
    CLEANUP("CLEANUP"),
    CLEANUP_ERROR("CLEANUP_ERROR"),
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
