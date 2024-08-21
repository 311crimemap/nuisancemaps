package com.quirkshop.nuisancemaps.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

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

@Entity
@Table(name = "geocode")
public class Geocode {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "geocode_seq")
    @SequenceGenerator(name = "geocode_seq", allocationSize = 1)
    private Integer id;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "source_id", nullable = false)
    private Source source;

    @Column(length = 512)
    private String address;
    private Double latitude;
    private Double longitude;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime createdAt;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime updatedAt;

    public Geocode() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

}
