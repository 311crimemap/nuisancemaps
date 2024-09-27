package com.quirkshop.nuisancemaps.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Index;

import org.locationtech.jts.geom.Point;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.locationtech.jts.geom.GeometryFactory;

@Entity
@Table(name = "data_311", indexes = {
        @Index(name = "idx_report_num_data_311", columnList = "reportNum"),
        @Index(name = "idx_source_id_data_311", columnList = "source_id"),
        @Index(name = "idx_reported_at_desc_data_311", columnList = "reportedAt DESC"),
        // Combined Spatial GIST + Vanilla index uses btree_gist extension
        @Index(name = "idx_gist_point_reported_at_data_311", columnList = "point, reportedAt")
})
public class Data311 implements DataEntity {
    // TODO: status update, other fields

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "data_311_seq")
    @SequenceGenerator(name = "data_311_seq", allocationSize = 1)
    private Integer id;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "source_id", nullable = false)
    private Source source;

    private String reportNum; // indexed in db
    private String reportCategory;
    private String description;

    @Column(length=512)
    private String address;
    private String location;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = true)
    private Category orgCategory; // category is common field with data; orgCategory is from classifier

    private Double latitude;
    private Double longitude;

    @Transient // exclude from persistence operations (migrations)
    private GeometryFactory _geometryFactory;

    @JsonIgnore
    private Point point;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime reportedAt;

    @JsonIgnore
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @JsonIgnore
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public Data311() {}

    public Data311(Source source) {
        this.setSource(source);
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

    public void setSource(Source source) {
        this.source = source;
    }

    public String getReportNum() {
        return reportNum;
    }

    public void setReportNum(String reportNum) {
        this.reportNum = reportNum;
    }

    public String getReportCategory() {
        return reportCategory;
    }

    public void setReportCategory(String reportCategory) {
        this.reportCategory = reportCategory;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Category getOrgCategory() {
        return orgCategory;
    }

    public void setOrgCategory(Category orgCategory) {
        this.orgCategory = orgCategory;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Point getPoint() {
        return point;
    }

    public void setPoint(Point point) {
        this.point = point;
    }

    public LocalDateTime getReportedAt() {
        return reportedAt;
    }

    public void setReportedAt(LocalDateTime reportedAt) {
        this.reportedAt = reportedAt;
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
