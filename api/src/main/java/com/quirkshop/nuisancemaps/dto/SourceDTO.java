package com.quirkshop.nuisancemaps.dto;

import com.quirkshop.nuisancemaps.config.DataParserType;
import com.quirkshop.nuisancemaps.config.DataProcessType;
import com.quirkshop.nuisancemaps.model.datajob.DataJobConfiguratorType;

public class SourceDTO {

    private Integer id;
    private Integer sourceConfigId;
    private String category;
    private String description;
    private Boolean recurring;
    private String url;
    private DataParserType dataParserType;
    private DataProcessType dataProcessType;
    private DataJobConfiguratorType dataJobConfiguratorType;
    private String startReportedAt;

    public SourceDTO(Integer id,
            Integer sourceConfigId, String category, String url, String description,
            Boolean recurring,
            DataParserType dataParserType, DataProcessType dataProcessType,
            DataJobConfiguratorType dataJobConfiguratorType,
            String startReportedAt) {
        this.id = id;
        this.sourceConfigId = sourceConfigId;
        this.category = category;
        this.url = url;
        this.description = description;
        this.recurring = recurring;
        this.dataParserType = dataParserType;
        this.dataProcessType = dataProcessType;
        this.dataJobConfiguratorType = dataJobConfiguratorType;
        this.startReportedAt = startReportedAt;
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

    public Boolean getRecurring() {
        return recurring;
    }

    public void setRecurring(Boolean recurring) {
        this.recurring = recurring;
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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public DataJobConfiguratorType getDataJobConfiguratorType() {
        return dataJobConfiguratorType;
    }

    public void setDataJobConfiguratorType(DataJobConfiguratorType dataJobConfiguratorType) {
        this.dataJobConfiguratorType = dataJobConfiguratorType;
    }

    public String getStartReportedAt() {
        return startReportedAt;
    }

    public void setStartReportedAt(String startReportedAt) {
        this.startReportedAt = startReportedAt;
    }
}
