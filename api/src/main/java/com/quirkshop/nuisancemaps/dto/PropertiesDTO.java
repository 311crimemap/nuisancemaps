package com.quirkshop.nuisancemaps.dto;

import java.time.LocalDateTime;

public class PropertiesDTO {
    private String reportNum;
    private String category;
    private String location;
    private LocalDateTime reportedAt;

    public PropertiesDTO(String category, String location, LocalDateTime reportedAt, String reportNum) {
        this.category = category;
        this.location = location;
        this.reportedAt = reportedAt;
        this.reportNum = reportNum;
    }

    public String getReportNum() {
        return reportNum;
    }

    public void setReportNum(String reportNum) {
        this.reportNum = reportNum;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public LocalDateTime getReportedAt() {
        return reportedAt;
    }

    public void setReportedAt(LocalDateTime reportedAt) {
        this.reportedAt = reportedAt;
    }
}
