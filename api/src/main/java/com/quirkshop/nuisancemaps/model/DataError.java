package com.quirkshop.nuisancemaps.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import com.quirkshop.nuisancemaps.model.datajob.DataJob;

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

    @Column(length = 4096)
    private String content;

    @Column(length = 4096)
    private String errorMsg;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public DataError() {}

    public DataError(DataJob dataJob, String content, String errorMsg) {
        this.dataJob = dataJob;
        this.content = content;
        this.errorMsg = errorMsg;
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

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
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

}
