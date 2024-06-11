package com.quirkshop.nuisancemaps.dto;

public class SourceDTO {

    private Integer sourceConfigId; // per json entry
    private String sourceConfigEntity; // City, State: maybe same location but old/new config endpoints
    private String sourceConfigNotes;

    private Double[] location;
    private String iconName;
    private String iconUnicode;

    private String category;
    private String description;
    private String url;

    private Integer numRecords;

    public SourceDTO(Integer sourceConfigId, String sourceConfigEntity, String sourceConfigNotes, Double[] location,
            String iconName, String iconUnicode, String category, String description, Integer numRecords) {
        this.sourceConfigId = sourceConfigId;
        this.sourceConfigEntity = sourceConfigEntity;
        this.sourceConfigNotes = sourceConfigNotes;
        this.location = location;
        this.iconName = iconName;
        this.iconUnicode = iconUnicode;
        this.category = category;
        this.description = description;
        this.numRecords = numRecords;
    }

    public Integer getSourceConfigId() {
        return sourceConfigId;
    }

    public void setSourceConfigId(Integer sourceConfigId) {
        this.sourceConfigId = sourceConfigId;
    }

    public String getSourceConfigEntity() {
        return sourceConfigEntity;
    }

    public void setSourceConfigEntity(String sourceConfigEntity) {
        this.sourceConfigEntity = sourceConfigEntity;
    }

    public String getSourceConfigNotes() {
        return sourceConfigNotes;
    }

    public void setSourceConfigNotes(String sourceConfigNotes) {
        this.sourceConfigNotes = sourceConfigNotes;
    }

    public Double[] getLocation() {
        return location;
    }

    public void setLocation(Double[] location) {
        this.location = location;
    }

    public String getIconName() {
        return iconName;
    }

    public void setIconName(String iconName) {
        this.iconName = iconName;
    }

    public String getIconUnicode() {
        return iconUnicode;
    }

    public void setIconUnicode(String iconUnicode) {
        this.iconUnicode = iconUnicode;
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

}
