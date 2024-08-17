package com.quirkshop.nuisancemaps.dto;

import com.quirkshop.nuisancemaps.config.DataParserType;
import com.quirkshop.nuisancemaps.config.DataProcessType;
import com.quirkshop.nuisancemaps.model.datajob.DataJobURLType;

public class SourceDTO {

    private Integer id;
    private Integer sourceConfigId;
    private String category;
    private String description;
    private String url;
    private DataParserType dataParserType;
    private DataProcessType dataProcessType;
    private DataJobURLType dataJobURLType;
    private Integer numRecords;

    public SourceDTO(Integer id, Integer sourceConfigId, String category, String url, String description,
            DataParserType dataParserType, DataProcessType dataProcessType, DataJobURLType dataJobURLType,
            Integer numRecords) {
        this.id = id;
        this.sourceConfigId = sourceConfigId;
        this.category = category;
        this.url = url;
        this.description = description;
        this.dataParserType = dataParserType;
        this.dataProcessType = dataProcessType;
        this.dataJobURLType = dataJobURLType;
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

    public DataProcessType getDataProcessType() {
        return dataProcessType;
    }

    public void setDataProcessType(DataProcessType dataProcessType) {
        this.dataProcessType = dataProcessType;
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

    public DataJobURLType getDataJobURLType() {
        return dataJobURLType;
    }

    public void setDataJobURLType(DataJobURLType dataJobURLType) {
        this.dataJobURLType = dataJobURLType;
    }

}
