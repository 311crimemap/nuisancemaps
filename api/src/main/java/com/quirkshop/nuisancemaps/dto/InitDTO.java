package com.quirkshop.nuisancemaps.dto;

import java.util.List;

import com.quirkshop.nuisancemaps.model.Category;

public class InitDTO {

    private LocaleFeatureCollectionDTO sources;
    private List<Category> categories;

    public InitDTO(LocaleFeatureCollectionDTO sources, List<Category> categories) {
        this.sources = sources;
        this.categories = categories;
    }

    public LocaleFeatureCollectionDTO getSources() {
        return sources;
    }

    public void setSources(LocaleFeatureCollectionDTO sources) {
        this.sources = sources;
    }

    public List<Category> getCategories() {
        return categories;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }

}
