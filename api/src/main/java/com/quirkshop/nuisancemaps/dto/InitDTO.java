package com.quirkshop.nuisancemaps.dto;

import java.util.List;

import com.quirkshop.nuisancemaps.model.Category;

public class InitDTO {

    private SourceFeatureCollectionDTO sources;
    private List<Category> categories;

    public InitDTO(SourceFeatureCollectionDTO sources, List<Category> categories) {
        this.sources = sources;
        this.categories = categories;
    }

    public SourceFeatureCollectionDTO getSources() {
        return sources;
    }

    public void setSources(SourceFeatureCollectionDTO sources) {
        this.sources = sources;
    }

    public List<Category> getCategories() {
        return categories;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }

}
