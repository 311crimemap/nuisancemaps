package com.quirkshop.nuisancemaps.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.quirkshop.nuisancemaps.config.PointDeserializer;
import com.quirkshop.nuisancemaps.dto.LocaleDTO;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.locationtech.jts.geom.Point;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.Column;
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

    private String name; // display name (city, state)
    private String description; // full description
    private String city; // city name
    private String state; // state name

    @Column(length = 4096)
    private String attribution; // text for attribution

    private boolean enabled = false; // activates locale for display (allows for prep sources, data)

    @JsonDeserialize(using = PointDeserializer.class)
    private Point location;

    private String iconName;
    private String iconUnicode;

    @JsonIgnore
    @OneToMany(mappedBy = "locale", fetch = FetchType.LAZY)
    private List<Source> sources = new ArrayList<Source>();

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public Locale() {
    }

    public LocaleDTO toDTO() {

        Double[] location = { this.getLocation().getX(), this.getLocation().getY() };

        Set<String> categories = this.getSources().stream()
                .map(Source::getCategory)
                .collect(Collectors.toSet());

        LocaleDTO localeDTO = new LocaleDTO(this.getId(),
                this.getName(),
                this.getDescription(),
                this.getCity(),
                this.getState(),
                this.getAttribution(),
                this.isEnabled(),
                location,
                this.getIconName(),
                this.getIconUnicode(),
                categories);

        return localeDTO;
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

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getAttribution() {
        return attribution;
    }

    public void setAttribution(String attribution) {
        this.attribution = attribution;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
