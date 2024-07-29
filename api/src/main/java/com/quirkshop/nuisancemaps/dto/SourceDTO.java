package com.quirkshop.nuisancemaps.dto;

import com.quirkshop.nuisancemaps.config.DataParserType;
import com.quirkshop.nuisancemaps.config.DataProcessingType;

public class SourceDTO {

    private Integer id;
    private Integer sourceConfigId;
    private String category;
    private String description;
    private String url;
    private DataParserType dataParserType;
    private DataProcessingType dataProcessingType;
    private Integer numRecords;

    public SourceDTO(Integer id, Integer sourceConfigId, String category, String url, String description,
            DataParserType dataParserType, DataProcessingType dataProcessingType, Integer numRecords) {
        this.id = id;
        this.sourceConfigId = sourceConfigId;
        this.category = category;
        this.url = url;
        this.description = description;
        this.dataParserType = dataParserType;
        this.dataProcessingType = dataProcessingType;
        this.numRecords = numRecords;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getSourceConfigId() {
        return sourceConfigId;
    }

    public void setSourceConfigId(Integer sourceConfigId) {
        this.sourceConfigId = sourceConfigId;
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

    public DataParserType getDataParserType() {
        return dataParserType;
    }

    public void setDataParserType(DataParserType dataParserType) {
        this.dataParserType = dataParserType;
    }

    public DataProcessingType getDataProcessingType() {
        return dataProcessingType;
    }

    public void setDataProcessingType(DataProcessingType dataProcessingType) {
        this.dataProcessingType = dataProcessingType;
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
