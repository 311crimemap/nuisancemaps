package com.quirkshop.nuisancemaps.model;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

@Entity
@Table(name = "data_job")
public class DataJob {

    // Each job sends request
    // pages: https://dev.socrata.com/docs/paging.html

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "data_job_seq")
    @SequenceGenerator(name = "data_job_seq", allocationSize = 1)
    private Integer id;

    @JsonManagedReference
    @ManyToOne
    @JoinColumn(name = "source_id", nullable = false)
    private Source source;

    @JsonIgnore
    @OneToMany(mappedBy = "dataJob", fetch = FetchType.LAZY)
    private List<DataError> dataErrors = new ArrayList<DataError>();

    // status: pending, queued, fetch start / fetch error / fetch complete /
    // completed, error
    @Enumerated(EnumType.STRING)
    private DataJobStatus status;
    private String url; // actual crawlURL, uses source as base?
    private int paramLimit;
    private int paramOffset;
    private String orderKey;
    private Integer numFetched;
    private Integer numProcessed;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime createdAt;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime updatedAt;

    public DataJob() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    public DataJob(Source source, int paramLimit, int paramOffset, String orderKey) {
        this.source = source;
        this.paramLimit = paramLimit;
        this.paramOffset = paramOffset;
        this.orderKey = orderKey;
        this.status = DataJobStatus.QUEUED;
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    public String buildURLFields(Map<String, Object> mapping) {
        Collection<String> fields = new ArrayList<String>();
        mapping.values().forEach(value -> {
            String field = (String) value;
            if (!field.isEmpty()) {
                fields.add((String) value);
            }
        });
        return String.join(",", fields);
    }

    public String buildURL() throws UnsupportedEncodingException {
        String sourceURL = this.getSourceURL();

        // collect fields
        Map<String, Object> mapping = source.getMapping();
        String $select = buildURLFields(mapping);

        String _url = UriComponentsBuilder.fromUriString(sourceURL)
                .queryParam("$limit", Integer.toString(paramLimit))
                .queryParam("$offset", Integer.toString(paramOffset))
                .queryParam("$order", orderKey)
                .queryParam("$select", $select)
                .build()
                .toUriString();

        this.url = _url;
        return this.url;
    }

    public String getSourceURL() {
        return this.source.getUrl();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Source getSource() {
        return source;
    }

    public DataJobStatus getStatus() {
        return status;
    }

    public void setStatus(DataJobStatus status) {
        this.status = status;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getParamLimit() {
        return paramLimit;
    }

    public void setParamLimit(int paramLimit) {
        this.paramLimit = paramLimit;
    }

    public int getParamOffset() {
        return paramOffset;
    }

    public void setParamOffset(int paramOffset) {
        this.paramOffset = paramOffset;
    }

    public String getOrderKey() {
        return orderKey;
    }

    public void setOrderKey(String orderKey) {
        this.orderKey = orderKey;
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

    public void setSource(Source source) {
        this.source = source;
    }

    public Integer getNumProcessed() {
        return numProcessed;
    }

    public void setNumProcessed(Integer numResults) {
        this.numProcessed = numResults;
    }

    public Integer getNumFetched() {
        return numFetched;
    }

    public void setNumFetched(Integer numFetched) {
        this.numFetched = numFetched;
    }

}
