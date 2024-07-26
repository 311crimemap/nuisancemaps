package com.quirkshop.nuisancemaps.dto;

import java.util.List;

public class LocaleFeatureCollectionDTO {
    private String type;
    private List<LocaleFeatureDTO> features;

    public LocaleFeatureCollectionDTO(String type, List<LocaleFeatureDTO> features) {
        this.type = type;
        this.features = features;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<LocaleFeatureDTO> getFeatures() {
        return features;
    }

    public void setFeatures(List<LocaleFeatureDTO> features) {
        this.features = features;
    }
}
