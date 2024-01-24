package com.quirkshop.nuisancemaps.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Index;

@Entity
@Table(name = "source", indexes = @Index(name = "source_config_entity_idx", columnList = "source_config_entity"))
public class Source {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "source_seq")
    @SequenceGenerator(name = "source_seq", allocationSize = 1)
    private Integer id;

    private Integer source_config_id; // per json entry
    private String source_config_entity; // City, State: maybe same location but old/new config endpoints
    private String category;
    private String description;
    private String url;

    @Transient
    private Map<String, Object> mapping;

    @OneToMany(mappedBy = "source", fetch = FetchType.LAZY)
    private List<DataCrime> dataCrimes = new ArrayList<DataCrime>();

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime created_at;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime updated_at;

    public Integer getSource_config_id() {
        return source_config_id;
    }

    public void setSource_config_id(Integer source_config_id) {
        this.source_config_id = source_config_id;
    }

    public String getSource_config_entity() {
        return source_config_entity;
    }

    public void setSource_config_entity(String source_config_entity) {
        this.source_config_entity = source_config_entity;
    }

    public Source() {
    }

    public Source(String category, String description, String url) {
        this.category = category;
        this.description = description;
        this.url = url;
    }

    public void addDataCrime(DataCrime crime) {
        this.dataCrimes.add(crime);
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

    public List<DataCrime> getDataCrimes() {
        System.out.println("HERE");
        return dataCrimes;
    }

    public void setDataCrimes(List<DataCrime> dataCrimes) {
        this.dataCrimes = dataCrimes;
    }

    public LocalDateTime getCreated_at() {
        return created_at;
    }

    public void setCreated_at(LocalDateTime created_at) {
        this.created_at = created_at;
    }

    public LocalDateTime getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(LocalDateTime updated_at) {
        this.updated_at = updated_at;
    }

    public Map<String, Object> getMapping() {
        return mapping;
    }

    public void setMapping(Map<String, Object> mapping) {
        this.mapping = mapping;
    }

}
