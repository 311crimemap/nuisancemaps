package com.quirkshop.nuisancemaps.dto;

import java.util.List;

import com.quirkshop.nuisancemaps.model.Category;

public class InitDTO {

    private List<SourceDTO> sources;
    private List<Category> categories;

    public InitDTO(List<SourceDTO> sources, List<Category> categories) {
        this.sources = sources;
        this.categories = categories;
    }

    public List<SourceDTO> getSources() {
        return sources;
    }

    public void setSources(List<SourceDTO> sources) {
        this.sources = sources;
    }

    public List<Category> getCategories() {
        return categories;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }

}
