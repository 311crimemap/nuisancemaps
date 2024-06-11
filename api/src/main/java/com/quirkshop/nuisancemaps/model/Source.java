package com.quirkshop.nuisancemaps.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.locationtech.jts.geom.Point;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;


@Entity
@Table(name = "source",
       indexes = @Index(name = "source_config_entity_idx", columnList = "sourceConfigEntity"))
public class Source {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "source_seq")
    @SequenceGenerator(name = "source_seq", allocationSize = 1)
    private Integer id;

    @Column(unique = true)
    @JsonProperty("source_config_id")
    private Integer sourceConfigId; // per json entry

    @JsonProperty("source_config_entity")
    private String sourceConfigEntity; // City, State: maybe same location but old/new config endpoints

    @JsonProperty("source_config_notes")
    private String sourceConfigNotes;

    @JsonProperty("location")
    private Point location;

    @JsonProperty("icon_name")
    private String iconName;

    @JsonProperty("icon_unicode")
    private String iconUnicode;

    private String category;
    private String description;
    private String url;

    @JsonProperty("num_records")
    private Integer numRecords;

    @OneToOne //NB: creates unique constraint mapping_id
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

    public Source(String category, String description, String url) {
        this.category = category;
        this.description = description;
        this.url = url;
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
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

    public Integer getNumRecords() {
        return numRecords;
    }

    public void setNumRecords(Integer numRecords) {
        this.numRecords = numRecords;
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

    public Point getLocation() {
        return location;
    }

    public void setLocation(Point location) {
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

}
