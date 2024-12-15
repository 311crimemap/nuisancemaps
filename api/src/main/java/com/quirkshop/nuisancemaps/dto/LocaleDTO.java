package com.quirkshop.nuisancemaps.dto;

import java.util.List;
import java.util.Set;

public class LocaleDTO {

    private Integer id;
    private String name;
    private String description;
    private String city; // city name
    private String state; // state name
    private String attribution; // text for attribution
    private boolean enabled;; // activates locale for display (allows for prep sources, data)

    private Double[] location;
    private String iconName;
    private String iconUnicode;
    private Set<String> categories;

    public LocaleDTO(Integer id, String name, String description, String city, String state, String attribution,
            boolean enabled, Double[] location, String iconName, String iconUnicode, Set<String> categories) {
        this.id = id;
        this.name = name;
        this.city = city;
        this.state = state;
        this.attribution = attribution;
        this.enabled = enabled;
        this.description = description;
        this.location = location;
        this.iconName = iconName;
        this.iconUnicode = iconUnicode;
        this.categories = categories;
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

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getAttribution() {
        return attribution;
    }

    public void setAttribution(String attribution) {
        this.attribution = attribution;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
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

    public Set<String> getCategories() {
        return categories;
    }

    public void setCategories(Set<String> categories) {
        this.categories = categories;
    }

}
