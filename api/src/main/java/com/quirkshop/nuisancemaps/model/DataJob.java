package com.quirkshop.nuisancemaps.model;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.quirkshop.nuisancemaps.config.DataParserType;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

@Entity
@Table(name = "data_job", indexes = {
        @Index(name = "idx_source_id_data_job", columnList = "source_id"),
        @Index(name = "idx_session_id_data_job", columnList = "sessionId")
})
public class DataJob {

    // Each job sends request
    // pages: https://dev.socrata.com/docs/paging.html

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "data_job_seq")
    @SequenceGenerator(name = "data_job_seq", allocationSize = 1)
    private Integer id;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "source_id", nullable = false)
    private Source source;

    private LocalDateTime sessionId;

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
    private boolean forceDownload = false;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime createdAt;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime updatedAt;

    public DataJob() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    public DataJob(LocalDateTime sessionId, Source source, int paramLimit, int paramOffset, String orderKey) {
        this.sessionId = sessionId;
        this.source = source;
        this.paramLimit = paramLimit;
        this.paramOffset = paramOffset;
        this.orderKey = orderKey;
        this.status = DataJobStatus.QUEUED;
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    // Map<String, Object> mapping
    public String buildURLFields(Mapping mapping) {

        List<String> fields = mapping.getAnnotationValues(MappingField::getField);

        return String.join(",", fields);
    }

    public String buildURL() throws UnsupportedEncodingException {

        Source source = this.getSource();

        // stand alone url, typical for full files/csv; no query string build
        if (source.getDataParserType().equals(DataParserType.CSV)) {
            this.setUrl(this.getSourceURL());
            return this.getUrl();
        }

        // OpenData endpoint; typically json with query parameters
        return buildOpenDataParamsURL();
    }

    public String buildOpenDataParamsURL() throws UnsupportedEncodingException {
        String sourceURL = this.getSourceURL();

        // collect fields
        Mapping mapping = source.getMapping();
        String $select = buildURLFields(mapping);

        String _url = UriComponentsBuilder.fromUriString(sourceURL)
                .queryParam("$limit", Integer.toString(paramLimit))
                .queryParam("$offset", Integer.toString(paramOffset))
                .queryParam("$order", orderKey)
                .queryParam("$select", $select)
                .build()
                .toUriString();

        this.setUrl(_url);
        return this.getUrl();
    }

    public String buildFilename() throws MalformedURLException {
        String sourceURL = this.getSourceURL();

        URL _url = new URL(sourceURL);
        String hostName = _url.getHost().replaceAll("/", "-");
        String filePath = _url.getPath().split("\\.")[0]
                .replaceAll("/", "-").substring(1); // skip the initial path prefix '/'
        String fileExtension = source.getDataParserType().toString().toLowerCase();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-H-mm");
        String formattedDateTime = this.getSessionId().format(formatter);

        String fileName = String.join("-", hostName, filePath,
                formattedDateTime + "." + fileExtension);

        return fileName;
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

    // replaces json Source association output to avoid recursive serialization
    @JsonProperty("sourceId")
    public Integer getJsonSourceId() {
        return source != null ? source.getId() : null;
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

    public LocalDateTime getSessionId() {
        return sessionId;
    }

    public void setSessionId(LocalDateTime sessionId) {
        this.sessionId = sessionId;
    }

    public List<DataError> getDataErrors() {
        return dataErrors;
    }

    public void setDataErrors(List<DataError> dataErrors) {
        this.dataErrors = dataErrors;
    }

    public boolean isForceDownload() {
        return forceDownload;
    }

    public void setForceDownload(boolean forceDownload) {
        this.forceDownload = forceDownload;
    }

}
