package com.quirkshop.nuisancemaps.dto;

public class FeatureDTO {
    private String type;
    private PropertiesDTO properties;
    private GeometryDTO geometry;

    public FeatureDTO(String type, GeometryDTO geometry, PropertiesDTO properties) {
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

    public PropertiesDTO getProperties() {
        return properties;
    }

    public void setProperties(PropertiesDTO properties) {
        this.properties = properties;
    }

    public GeometryDTO getGeometry() {
        return geometry;
    }

    public void setGeometry(GeometryDTO geometry) {
        this.geometry = geometry;
    }
}
