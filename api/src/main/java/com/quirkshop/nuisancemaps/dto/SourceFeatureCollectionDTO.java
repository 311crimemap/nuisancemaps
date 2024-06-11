package com.quirkshop.nuisancemaps.dto;

import java.util.List;

public class SourceFeatureCollectionDTO {
    private String type;
    private List<SourceFeatureDTO> features;

    public SourceFeatureCollectionDTO(String type, List<SourceFeatureDTO> features) {
        this.type = type;
        this.features = features;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<SourceFeatureDTO> getFeatures() {
        return features;
    }

    public void setFeatures(List<SourceFeatureDTO> features) {
        this.features = features;
    }
}
