package com.quirkshop.nuisancemaps.model;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

@Entity
@Table(name = "data_error")
public class DataError {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "data_error_seq")
    @SequenceGenerator(name = "data_error_seq", allocationSize = 1)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "data_job_id", nullable = false)
    private DataJob dataJob;

    private String content;

    private String error_msg;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime created_at;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime updated_at;

    public DataError() {
        LocalDateTime now = LocalDateTime.now();
        this.created_at = now;
        this.updated_at = now;
    }

    public DataError(DataJob dataJob, String content, String error_msg) {
        this.dataJob = dataJob;
        this.content = content;
        this.error_msg = error_msg;

        LocalDateTime now = LocalDateTime.now();
        this.created_at = now;
        this.updated_at = now;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public DataJob getDataJob() {
        return dataJob;
    }

    public void setDataJob(DataJob dataJob) {
        this.dataJob = dataJob;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getError_msg() {
        return error_msg;
    }

    public void setError_msg(String error_msg) {
        this.error_msg = error_msg;
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

}
