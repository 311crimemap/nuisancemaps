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
import org.locationtech.jts.geom.GeometryFactory;

@Entity
@Table(name = "data_crime", indexes = {
        @Index(name = "idx_report_num_data_crime", columnList = "reportNum"),
        @Index(name = "idx_source_id_data_crime", columnList = "source_id"),
        @Index(name = "idx_reported_at_crime", columnList = "reportedAt"),
        // NB: spatial GIST index specified via liquibase migration
        @Index(name = "idx_point_data_crime", columnList = "point")

})
public class DataCrime implements IDataEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "data_crime_seq")
    @SequenceGenerator(name = "data_crime_seq", allocationSize = 1)
    private Integer id;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "source_id", nullable = false)
    private Source source;

    private String reportNum;
    private String reportCategory;
    private String description;

    @Column(length = 512)
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
    private LocalDateTime createdAt;

    @JsonIgnore
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime updatedAt;

    public DataCrime() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    } // default required by JPA

    public DataCrime(Source source) {
        this.setSource(source);
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
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

    public void setReportCategory(String category) {
        this.reportCategory = category;
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
