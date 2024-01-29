package com.quirkshop.nuisancemaps.model;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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

    @ManyToOne
    @JoinColumn(name = "source_id", nullable = false)
    private Source source;

    // status: pending, queued, fetch start / fetch error / fetch complete /
    // completed, error
    private String status;
    private String url; // actual crawlURL, uses source as base?
    private int param_limit;
    private int param_offset;
    private String order_key;
    private int num_results;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime created_at;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime updated_at;

    public DataJob() {
    }

    public DataJob(Source source, int param_limit, int param_offset, String order_key) {
        this.source = source;
        this.param_limit = param_limit;
        this.param_offset = param_offset;
        this.order_key = order_key;
        this.status = "queued";
    }

    public String buildURL() throws UnsupportedEncodingException {
        String sourceURL = this.getSourceURL();
        String _url = UriComponentsBuilder.fromUriString(sourceURL)
                .queryParam("$limit", URLEncoder.encode(Integer.toString(param_limit), "UTF-8"))
                .queryParam("$offset", URLEncoder.encode(Integer.toString(param_offset), "UTF-8"))
                .queryParam("$order", URLEncoder.encode(order_key, "UTF-8"))
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getParam_limit() {
        return param_limit;
    }

    public void setParam_limit(int param_limit) {
        this.param_limit = param_limit;
    }

    public int getParam_offset() {
        return param_offset;
    }

    public void setParam_offset(int param_offset) {
        this.param_offset = param_offset;
    }

    public String getOrder_key() {
        return order_key;
    }

    public void setOrder_key(String orderKey) {
        this.order_key = orderKey;
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

    public void setSource(Source source) {
        this.source = source;
    }

    public int getNum_results() {
        return num_results;
    }

    public void setNum_results(int numResults) {
        this.num_results = numResults;
    }

}
