package com.quirkshop.nuisancemaps.dto;

import java.util.List;

public class FeatureCollectionDTO {
    private String type;
    private List<FeatureDTO> features;

    public FeatureCollectionDTO(String type, List<FeatureDTO> features) {
        this.type = type;
        this.features = features;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<FeatureDTO> getFeatures() {
        return features;
    }

    public void setFeatures(List<FeatureDTO> features) {
        this.features = features;
    }
}
