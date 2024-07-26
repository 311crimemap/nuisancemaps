package com.quirkshop.nuisancemaps.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.quirkshop.nuisancemaps.config.PointDeserializer;
import com.quirkshop.nuisancemaps.dto.LocaleDTO;
import com.quirkshop.nuisancemaps.dto.SourceDTO;

import org.locationtech.jts.geom.Point;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

@Entity
@Table(name = "locale")
public class Locale {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "locale_seq")
    @SequenceGenerator(name = "locale_seq", allocationSize = 1)
    private Integer id;

    private String name; // city, state
    private String description; // full description

    @JsonDeserialize(using = PointDeserializer.class)
    private Point location;

    private String iconName;
    private String iconUnicode;

    @JsonIgnore
    @OneToMany(mappedBy = "locale", fetch = FetchType.LAZY)
    private List<Source> sources = new ArrayList<Source>();

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime createdAt;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime updatedAt;

    public Locale() {
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Point getLocation() {
        return location;
    }

    public void setLocation(Point location) {
        this.location = location;
    }

    public String getIconName() {
        return iconName;
    }

    public void setIconName(String iconName) {
        this.iconName = iconName;
    }

    public String getIconUnicode() {
        return iconUnicode;
    }

    public void setIconUnicode(String iconUnicode) {
        this.iconUnicode = iconUnicode;
    }

    public List<Source> getSources() {
        return sources;
    }

    public void setSources(List<Source> sources) {
        this.sources = sources;
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

    public LocaleDTO toDTO() {

        Double[] location = { this.getLocation().getX(), this.getLocation().getY() };

        List<SourceDTO> sourceDTOs = this.sources.stream()
                .map(Source::toDTO)
                .collect(Collectors.toList());

        LocaleDTO localeDTO = new LocaleDTO(this.getId(),
                this.getName(),
                this.getDescription(),
                location,
                this.getIconName(),
                this.getIconUnicode(),
                sourceDTOs);

        return localeDTO;
    }
}
