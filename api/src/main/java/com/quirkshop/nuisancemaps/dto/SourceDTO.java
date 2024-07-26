package com.quirkshop.nuisancemaps.dto;

public class SourceDTO {

    private String category;
    private String description;
    private String url;
    private Integer numRecords;


    public SourceDTO(String category, String url, String description, Integer numRecords) {
        this.category = category;
        this.url = url;
        this.description = description;
        this.numRecords = numRecords;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getNumRecords() {
        return numRecords;
    }

    public void setNumRecords(Integer numRecords) {
        this.numRecords = numRecords;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

}
