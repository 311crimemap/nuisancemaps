package com.quirkshop.nuisancemaps.dto;

import java.time.LocalDateTime;

public class PropertiesDTO {
    private String reportNum;
    private String reportCategory;
    private String location;
    private CategoryDTO category;
    private LocalDateTime reportedAt;

    public PropertiesDTO(String reportCategory, String location, LocalDateTime reportedAt, String reportNum, CategoryDTO category) {
        this.reportCategory = reportCategory;
        this.location = location;
        this.reportedAt = reportedAt;
        this.reportNum = reportNum;
        this.category = category;
    }

    public String getReportNum() {
        return reportNum;
    }

    public void setReportNum(String reportNum) {
        this.reportNum = reportNum;
    }

    public String getReportCategory() {
        return reportCategory;
    }

    public void setReportCategory(String category) {
        this.reportCategory = category;
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

    public CategoryDTO getCategory() {
        return category;
    }

    public void setCategory(CategoryDTO category) {
        this.category = category;
    }
}
