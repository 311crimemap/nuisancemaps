package com.quirkshop.nuisancemaps.model;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

@Entity
@Table(name = "data_url_cache", indexes = {
        @Index(name = "idx_url_data_url_cache", columnList = "url")
})
public class DataURLCache {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "data_url_cache_seq")
    @SequenceGenerator(name = "data_url_cache_seq", allocationSize = 1)
    private Long id;

    @Column(nullable = false, unique = true) // ignore liquibase constraint generation
    private String url;

    @Column(nullable = false)
    private Long count = 1L;

    @JsonIgnore
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @JsonIgnore
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public DataURLCache(String path, LocalDateTime startDateTime, LocalDateTime endDateTime,
            double sw_lat, double sw_lng, double ne_lat, double ne_lng) {
        this.url = calcURL(path, startDateTime, endDateTime, sw_lat, sw_lng, ne_lat, ne_lng);
        this.count = 1L;
    }

    public String calcURL(String path, LocalDateTime startDateTime, LocalDateTime endDateTime,
            double sw_lat, double sw_lng, double ne_lat, double ne_lng) {
        return String.format("%s?startDateTime=%s&endDateTime=%s&sw_lat=%s&sw_lng=%s&ne_lat=%s&ne_lng=%s",
                path, startDateTime, endDateTime, sw_lat, sw_lng, ne_lat, ne_lng);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
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
