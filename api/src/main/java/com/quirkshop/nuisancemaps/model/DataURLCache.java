package com.quirkshop.nuisancemaps.model;

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
        @Index(name = "idx_hash_code_data_url_cache", columnList = "hashCode")
})
public class DataURLCache {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "data_url_cache_seq")
    @SequenceGenerator(name = "data_url_cache_seq", allocationSize = 1)
    private Long id;

    @Column(nullable = false, unique = true)
    private String hashCode;

    @Column(nullable = false, unique = true)
    private String url;

    @Column(nullable = false)
    private Long count = 1L;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getHashCode() {
        return hashCode;
    }

    public void setHashCode(String hashCode) {
        this.hashCode = hashCode;
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

}
