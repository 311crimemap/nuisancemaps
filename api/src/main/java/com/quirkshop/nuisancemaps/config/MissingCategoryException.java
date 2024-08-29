package com.quirkshop.nuisancemaps.config;

public class MissingCategoryException extends Exception {

    private String reportCategory;

    public MissingCategoryException(String errorMessage) {
        super(errorMessage);
    }

    public MissingCategoryException(String reportCategory, String errorMessage) {
        super(errorMessage);
        this.reportCategory = reportCategory;
    }

    public String getReportCategory() {
        return reportCategory;
    }
}
