package com.quirkshop.nuisancemaps.model;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.GeometryFactory;

@Entity
@Table(name = "data_crime")
public class DataCrime {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "data_crime_seq")
    @SequenceGenerator(name = "data_crime_seq", allocationSize = 1)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "source_id", nullable = false)
    private Source source;

    private String report_num;
    private String category;
    private String description;
    private String location;

    private double latitude;
    private double longitude;
    private GeometryFactory _geometryFactory;
    private Point point;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime reported_at;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime created_at;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime updated_at;

    public DataCrime() {
    } // default required by JPA

    public DataCrime(Source source, String report_num, String category, String description, String location,
            GeometryFactory geometryFactory, double latitude, double longitude, LocalDateTime reported_at) {
        this.setSource(source);
        this.report_num = report_num;
        this.category = category;
        this.description = description;
        this.location = location;
        this.latitude = latitude;
        this.longitude = longitude;
        this._geometryFactory = geometryFactory;
        this.point = this.buildPoint(latitude, longitude);
        // DateTimeFormatter formatter =
        // DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
        this.reported_at = reported_at;

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

    public Source getSource() {
        return source;
    }

    // set inverse relation
    public void setSource(Source source) {
        this.source = source;
        this.source.addDataCrime(this);
    }

    public String getReport_num() {
        return report_num;
    }

    public void setReport_num(String report_num) {
        this.report_num = report_num;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public Point buildPoint(double latitude, double longitude) {
        Coordinate coordinate = new Coordinate(latitude, longitude);
        return this._geometryFactory.createPoint(coordinate);
    }

    public Point getPoint() {
        return point;
    }

    public void setPoint(Point point) {
        this.point = point;
    }

    public LocalDateTime getReported_at() {
        return reported_at;
    }

    public void setReported_at(LocalDateTime reported_at) {
        this.reported_at = reported_at;
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
