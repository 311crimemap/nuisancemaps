package com.quirkshop.nuisancemaps.dto;

import java.util.List;

public class LocaleDTO {

    private Integer id;
    private String name;
    private String description;
    private Double[] location;
    private String iconName;
    private String iconUnicode;
    private List<SourceDTO> sourceDTOs;

    public LocaleDTO(Integer id, String name, String description, Double[] location, String iconName, String iconUnicode, List<SourceDTO> sourceDTOs) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.location = location;
        this.iconName = iconName;
        this.iconUnicode = iconUnicode;
        this.sourceDTOs = sourceDTOs;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double[] getLocation() {
        return location;
    }

    public void setLocation(Double[] location) {
        this.location = location;
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

    public List<SourceDTO> getSourceDTOs() {
        return sourceDTOs;
    }

    public void setSourceDTOs(List<SourceDTO> sourceDTOs) {
        this.sourceDTOs = sourceDTOs;
    }

}
