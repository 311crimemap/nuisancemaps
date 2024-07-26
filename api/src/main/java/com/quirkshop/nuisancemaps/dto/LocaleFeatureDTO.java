package com.quirkshop.nuisancemaps.dto;

public class LocaleFeatureDTO {
    private String type;
    private LocaleDTO properties;
    private GeometryDTO geometry;

    public LocaleFeatureDTO(String type, GeometryDTO geometry, LocaleDTO properties) {
        this.type = type;
        this.geometry = geometry;
        this.properties = properties;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public LocaleDTO getProperties() {
        return properties;
    }

    public void setProperties(LocaleDTO properties) {
        this.properties = properties;
    }

    public GeometryDTO getGeometry() {
        return geometry;
    }

    public void setGeometry(GeometryDTO geometry) {
        this.geometry = geometry;
    }
}
