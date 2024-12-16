package com.quirkshop.nuisancemaps.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

/*
 * Note the composite id key (locale_id, category)
 *
 * This "model" is a MATERIALIZED VIEW - when querying via JPA there ends up
 * being a duplicate data error; the first record for each list per locale_id is
 * repeated for the entire list; e.g. JPA can't distinguish between records with
 * the same locale_id - can't distinguish a primary key.
 *
 * Setting a composite @Id across multiple fields forces uniqueness per row, and
 * allows propery querying of materialized view.
 * (LocaleCategoryMinMaxReportedAtRepository).
 *
 * * @IdClass(LocaleCategoryMinMaxReportedAt.class):
 * establishes all class's fields are implicitly members of composite key
 *
 */
@Entity
@IdClass(LocaleCategoryMinMaxReportedAt.class)
// NB: unique index created for concurrent refresh of materialized view
// (migration 70)
// * REFRESH MATERIALIZED VIEW CONCURRENTLY locale_category_min_max_reported_at;
@Table(name = "locale_category_min_max_reported_at")
public class LocaleCategoryMinMaxReportedAt {

    @Id
    @Column(name = "locale_id")
    @JsonIgnore
    private int localeId;

    @Column(name = "category")
    private String category;

    @Column(name = "min_reported_at")
    private LocalDateTime minReportedAt;

    @Column(name = "max_reported_at")
    private LocalDateTime maxReportedAt;

    @Column(name = "count")
    private int count;

    @Column(name = "dj_max_updated_at")
    private LocalDateTime djMaxUpdatedAt;

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public LocalDateTime getMinReportedAt() {
        return minReportedAt;
    }

    public void setMinReportedAt(LocalDateTime minReportedAt) {
        this.minReportedAt = minReportedAt;
    }

    public LocalDateTime getMaxReportedAt() {
        return maxReportedAt;
    }

    public void setMaxReportedAt(LocalDateTime maxReportedAt) {
        this.maxReportedAt = maxReportedAt;
    }

    public int getLocaleId() {
        return localeId;
    }

    public void setLocaleId(int localeId) {
        this.localeId = localeId;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public LocalDateTime getDjMaxUpdatedAt() {
        return djMaxUpdatedAt;
    }

    public void setDjMaxUpdatedAt(LocalDateTime djMaxUpdatedAt) {
        this.djMaxUpdatedAt = djMaxUpdatedAt;
    }
}
