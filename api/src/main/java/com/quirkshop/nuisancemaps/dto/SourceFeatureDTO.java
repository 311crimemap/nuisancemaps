package com.quirkshop.nuisancemaps.dto;

public class SourceFeatureDTO {
    private String type;
    private SourceDTO properties;
    private GeometryDTO geometry;

    public SourceFeatureDTO(String type, GeometryDTO geometry, SourceDTO properties) {
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

    public SourceDTO getProperties() {
        return properties;
    }

    public void setProperties(SourceDTO properties) {
        this.properties = properties;
    }

    public GeometryDTO getGeometry() {
        return geometry;
    }

    public void setGeometry(GeometryDTO geometry) {
        this.geometry = geometry;
    }
}
