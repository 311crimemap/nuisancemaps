package com.quirkshop.nuisancemaps.dto;

public class TextLabelDTO {

    private String dataType;
    private String text;
    private Integer label;

    public TextLabelDTO(String dataType, String text, Integer label) {
        this.dataType = dataType;
        this.text = text;
        this.label = label;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Integer getLabel() {
        return label;
    }

    public void setLabel(Integer label) {
        this.label = label;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }
}
