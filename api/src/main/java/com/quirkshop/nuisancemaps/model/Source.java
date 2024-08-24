package com.quirkshop.nuisancemaps.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.quirkshop.nuisancemaps.config.DataParserType;
import com.quirkshop.nuisancemaps.config.DataProcessType;
import com.quirkshop.nuisancemaps.dto.SourceDTO;
import com.quirkshop.nuisancemaps.model.datajob.DataJob;
import com.quirkshop.nuisancemaps.model.datajob.DataJobConfiguratorType;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

@Entity
@Table(name = "source")
public class Source {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "source_seq")
    @SequenceGenerator(name = "source_seq", allocationSize = 1)
    private Integer id;

    @Column(unique = true)
    private Integer sourceConfigId; // per json entry

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "locale_id", nullable = false)
    private Locale locale;

    private String category;
    private String description;

    @Column(length = 1024)
    private String url;

    @Column(length = 1024)
    private String cookie;

    private DataParserType dataParserType;
    private DataProcessType dataProcessType;

    @Column(name = "data_job_configurator_type")
    private DataJobConfiguratorType dataJobConfiguratorType;

    @OneToOne // NB: creates unique constraint mapping_id
    @JoinColumn(name = "mapping_id", nullable = false)
    private Mapping mapping;

    @JsonIgnore
    @OneToMany(mappedBy = "source", fetch = FetchType.LAZY)
    private List<DataCrime> dataCrimes = new ArrayList<DataCrime>();

    @JsonIgnore
    @OneToMany(mappedBy = "source", fetch = FetchType.LAZY)
    private List<Data311> data311s = new ArrayList<Data311>();

    @JsonIgnore
    @OneToMany(mappedBy = "source", fetch = FetchType.LAZY)
    private List<DataJob> dataJobs = new ArrayList<DataJob>();

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime createdAt;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime updatedAt;

    public Source() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    public Source(Locale locale, String category, String description, String url,
            DataParserType dataParserType, DataProcessType dataProcessType,
            DataJobConfiguratorType dataJobConfiguratorType) {
        this.locale = locale;
        this.category = category;
        this.description = description;
        this.url = url;
        this.dataParserType = dataParserType;
        this.dataProcessType = dataProcessType;
        this.dataJobConfiguratorType = dataJobConfiguratorType;
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    public List<Data311> getData311s() {
        return data311s;
    }

    public void setData311s(List<Data311> data311s) {
        this.data311s = data311s;
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

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getCookie() {
        return cookie;
    }

    public void setCookie(String cookie) {
        this.cookie = cookie;
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

    public List<DataCrime> getDataCrimes() {
        return dataCrimes;
    }

    public void setDataCrimes(List<DataCrime> dataCrimes) {
        this.dataCrimes = dataCrimes;
    }

    public List<DataJob> getDataJobs() {
        return dataJobs;
    }

    public void setDataJobs(List<DataJob> dataJobs) {
        this.dataJobs = dataJobs;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Mapping getMapping() {
        return mapping;
    }

    public void setMapping(Mapping mapping) {
        this.mapping = mapping;
    }

    public DataJobConfiguratorType getDataJobConfiguratorType() {
        return dataJobConfiguratorType;
    }

    public void setDataJobConfiguratorType(DataJobConfiguratorType dataJobConfiguratorType) {
        this.dataJobConfiguratorType = dataJobConfiguratorType;
    }

    public SourceDTO toDTO() {
        SourceDTO sourceDTO = new SourceDTO(this.getId(),
                this.getSourceConfigId(),
                this.getCategory(),
                this.getUrl(),
                this.getDescription(),
                this.getDataParserType(),
                this.getDataProcessType(),
                this.getDataJobConfiguratorType());
        return sourceDTO;
    }

}
