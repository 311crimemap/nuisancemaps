package com.quirkshop.nuisancemaps.model.datajob;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.quirkshop.nuisancemaps.model.DataError;
import com.quirkshop.nuisancemaps.model.Source;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.Column;
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

    @Column(length = 1024)
    private String url; // actual crawlURL, uses source as base?

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "parameters", columnDefinition = "jsonb")
    private HashMap<String, Object> parameters;

    private int paramLimit;
    private int paramOffset; // csv: readLines
    private String orderKey;
    private Integer numFetched; // csv: valid lines (skip malformed rows)
    private Integer numProcessed; // csv: valid entity save to DB
    private boolean forceDownload = false;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private static final int PARAM_LIMIT = Integer.parseInt(System.getenv("WORKER_QUERY_LIMIT"));

    public DataJob() {
        this.parameters = new HashMap<String, Object>();
    }

    public DataJob(LocalDateTime sessionId, Source source, String orderKey) {
        this.sessionId = sessionId;
        this.source = source;
        this.orderKey = orderKey;
        this.parameters = new HashMap<String, Object>();
        this.paramLimit = PARAM_LIMIT;
        this.paramOffset = 0;
        this.status = DataJobStatus.QUEUED;
    }

    public DataJob(DataJob dataJob) {
        this.sessionId = dataJob.getSessionId();
        ;
        this.source = dataJob.getSource();
        this.orderKey = dataJob.getOrderKey();
        this.parameters = dataJob.getParameters();
        this.url = dataJob.getUrl();
        this.paramLimit = dataJob.getParamLimit();
        this.paramOffset = dataJob.getParamOffset();
        this.status = DataJobStatus.QUEUED;
    }

    public String buildFilename() throws MalformedURLException {

        // want filename from DataJob URL - generated from DataJobconfigurator
        // not initial but static source url
        String url = this.getUrl();
        URL _url = new URL(url);

        String hostName = _url.getHost().replaceAll("/", "-");
        String filePath = _url.getPath().split("\\.")[0]
                .replaceAll("/", "-").substring(1); // skip the initial path prefix '/'
        String query = _url.getQuery();

        // append query string to filename to differentiate dataJob / source
        // if no query
        String params = "";
        if (query != null) {
            params = "-" + query
                    .replaceAll("/", "-") // avoid date format breaking into subdirectories
                    .replaceAll("&", "__")
                    .replaceAll("=", "_");
        }

        String fileExtension = source.getDataParserType().toString().toLowerCase();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-H-mm");
        String formattedDateTime = this.getSessionId().format(formatter);

        String fileName = String.join("-", hostName, filePath + params,
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

    public HashMap<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(HashMap<String, Object> parameters) {
        this.parameters = parameters;
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
