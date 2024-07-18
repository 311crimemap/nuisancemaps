package com.quirkshop.nuisancemaps.model;

import jakarta.persistence.Embeddable;

@Embeddable
public class MappingField {

    private String field;
    private String pointer;
    private String parsingStrategy;

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getPointer() {
        return pointer;
    }

    public void setPointer(String pointer) {
        this.pointer = pointer;
    }

    public String getParsingStrategy() {
        return parsingStrategy;
    }

    public void setParsingStrategy(String parsingStrategy) {
        this.parsingStrategy = parsingStrategy;
    }
}
