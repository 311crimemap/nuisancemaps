package com.quirkshop.nuisancemaps.dto;

public class CategoryDTO {

    private Integer id;
    private String dataType;
    private String text;
    private Integer label;
    private String iconName;
    private String iconUnicode;

    public CategoryDTO(Integer id, String dataType, String text, Integer label, String iconName, String iconUnicode) {
        this.id = id;
        this.dataType = dataType;
        this.text = text;
        this.label = label;
        this.iconName = iconName;
        this.iconUnicode = iconUnicode;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
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

    public String getIconName() {
        return iconName;
    }

    public void setIconName(String iconName) {
        this.iconName = iconName;
    }

    public String getIconUnicode() {
        return iconUnicode;
    }

    public void setIconUnicode(String iconUnicode) {
        this.iconUnicode = iconUnicode;
    }

}
