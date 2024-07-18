package com.quirkshop.nuisancemaps.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;

@Entity
@Table(name = "mapping")
public class Mapping {
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Mapped {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "mapping_seq")
    @SequenceGenerator(name = "mapping_seq", allocationSize = 1)
    private Integer id;

    @Mapped
    private String orderKey;

    @Mapped
    private String description;

    @Mapped
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "field", column = @Column(name = "report_num_field")),
            @AttributeOverride(name = "pointer", column = @Column(name = "report_num_pointer")),
            @AttributeOverride(name = "parsingStrategy", column = @Column(name = "report_num_parsing_strategy"))
    })
    private MappingField reportNum;

    @Mapped
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "field", column = @Column(name = "report_category_field")),
            @AttributeOverride(name = "pointer", column = @Column(name = "report_category_pointer")),
            @AttributeOverride(name = "parsingStrategy", column = @Column(name = "report_category_parsing_strategy"))
    })
    private MappingField reportCategory;

    @Mapped
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "field", column = @Column(name = "location_field")),
            @AttributeOverride(name = "pointer", column = @Column(name = "location_pointer")),
            @AttributeOverride(name = "parsingStrategy", column = @Column(name = "location_parsing_strategy"))
    })
    private MappingField location;

    @Mapped
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "field", column = @Column(name = "latitude_field")),
            @AttributeOverride(name = "pointer", column = @Column(name = "latitude_pointer")),
            @AttributeOverride(name = "parsingStrategy", column = @Column(name = "latitude_parsing_strategy"))
    })
    private MappingField latitude;

    @Mapped
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "field", column = @Column(name = "longitude_field")),
            @AttributeOverride(name = "pointer", column = @Column(name = "longitude_pointer")),
            @AttributeOverride(name = "parsingStrategy", column = @Column(name = "longitude_parsing_strategy"))
    })
    private MappingField longitude;

    @Mapped
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "field", column = @Column(name = "reported_at_field")),
            @AttributeOverride(name = "pointer", column = @Column(name = "reported_at_pointer")),
            @AttributeOverride(name = "parsingStrategy", column = @Column(name = "reported_at_parsing_strategy"))
    })
    private MappingField reportedAt;

    @Mapped
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "field", column = @Column(name = "reported_at2_field")),
            @AttributeOverride(name = "pointer", column = @Column(name = "reported_at2_pointer")),
            @AttributeOverride(name = "parsingStrategy", column = @Column(name = "reported_at2_parsing_strategy"))
    })
    private MappingField reportedAt2;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime createdAt;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime updatedAt;

    public Mapping() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    public Mapping(MappingField reportNum, MappingField reportCategory, String orderKey, String description,
            MappingField location, MappingField latitude,
            MappingField longitude, MappingField reportedAt, MappingField reportedAt2) {
        this.reportNum = reportNum;
        this.reportCategory = reportCategory;
        this.orderKey = orderKey;
        this.description = description;
        this.location = location;
        this.latitude = latitude;
        this.longitude = longitude;
        this.reportedAt = reportedAt;
        this.reportedAt2 = reportedAt2;

        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    /*
     * collects value returned by getters of @Mapped annotated fields
     */
    public List<String> getFields() {
        List<String> fieldValues = new ArrayList<>();
        Class<?> clazz = this.getClass();
        Method[] methods = clazz.getMethods();
        try {
            for (Method method : methods) {
                if (isGetter(method)) {
                    Annotation annotation = method.getAnnotation(Mapped.class);
                    if (annotation != null) {
                        Object value = method.invoke(this);
                        String stringVal = value != null ? value.toString() : null;
                        fieldValues.add(stringVal);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace(); // Handle exception appropriately
        }
        return fieldValues;
    }

    private boolean isGetter(Method method) {
        return method.getName().startsWith("get") &&
                method.getParameterCount() == 0 &&
                !void.class.equals(method.getReturnType()); // return type not void
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public MappingField getReportNum() {
        return reportNum;
    }

    public void setReportNum(MappingField reportNum) {
        this.reportNum = reportNum;
    }

    public MappingField getReportCategory() {
        return reportCategory;
    }

    public void setReportCategory(MappingField reportCategory) {
        this.reportCategory = reportCategory;
    }

    public String getOrderKey() {
        return orderKey;
    }

    public void setOrderKey(String orderKey) {
        this.orderKey = orderKey;
    }

    public MappingField getLocation() {
        return location;
    }

    public void setLocation(MappingField location) {
        this.location = location;
    }

    public MappingField getLatitude() {
        return latitude;
    }

    public void setLatitude(MappingField latitude) {
        this.latitude = latitude;
    }

    public MappingField getLongitude() {
        return longitude;
    }

    public void setLongitude(MappingField longitude) {
        this.longitude = longitude;
    }

    public MappingField getReportedAt() {
        return reportedAt;
    }

    public void setReportedAt(MappingField reportedAt) {
        this.reportedAt = reportedAt;
    }

    public MappingField getReportedAt2() {
        return reportedAt2;
    }

    public void setReportedAt2(MappingField reportedAt2) {
        this.reportedAt2 = reportedAt2;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
