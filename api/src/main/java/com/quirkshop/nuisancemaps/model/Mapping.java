package com.quirkshop.nuisancemaps.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

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
    @Target(ElementType.METHOD)
    public @interface Mapped {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "mapping_seq")
    @SequenceGenerator(name = "mapping_seq", allocationSize = 1)
    private Integer id;

    private String orderKey;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "field", column = @Column(name = "description_field")),
            @AttributeOverride(name = "pointer", column = @Column(name = "description_pointer")),
            @AttributeOverride(name = "parsingStrategy", column = @Column(name = "description_parsing_strategy"))
    })
    private MappingField description;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "field", column = @Column(name = "report_num_field")),
            @AttributeOverride(name = "pointer", column = @Column(name = "report_num_pointer")),
            @AttributeOverride(name = "parsingStrategy", column = @Column(name = "report_num_parsing_strategy"))
    })
    private MappingField reportNum;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "field", column = @Column(name = "report_category_field")),
            @AttributeOverride(name = "pointer", column = @Column(name = "report_category_pointer")),
            @AttributeOverride(name = "parsingStrategy", column = @Column(name = "report_category_parsing_strategy"))
    })
    private MappingField reportCategory;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "field", column = @Column(name = "location_field")),
            @AttributeOverride(name = "pointer", column = @Column(name = "location_pointer")),
            @AttributeOverride(name = "parsingStrategy", column = @Column(name = "location_parsing_strategy"))
    })
    private MappingField location;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "field", column = @Column(name = "latitude_field")),
            @AttributeOverride(name = "pointer", column = @Column(name = "latitude_pointer")),
            @AttributeOverride(name = "parsingStrategy", column = @Column(name = "latitude_parsing_strategy"))
    })
    private MappingField latitude;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "field", column = @Column(name = "longitude_field")),
            @AttributeOverride(name = "pointer", column = @Column(name = "longitude_pointer")),
            @AttributeOverride(name = "parsingStrategy", column = @Column(name = "longitude_parsing_strategy"))
    })
    private MappingField longitude;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "field", column = @Column(name = "reported_at_field")),
            @AttributeOverride(name = "pointer", column = @Column(name = "reported_at_pointer")),
            @AttributeOverride(name = "parsingStrategy", column = @Column(name = "reported_at_parsing_strategy"))
    })
    private MappingField reportedAt;

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

    public Mapping(MappingField reportNum, MappingField reportCategory, String orderKey, MappingField description,
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
    public List<String> getAnnotationValues(Function<MappingField, String> mapper) {
        List<String> values = new ArrayList<String>();
        List<Method> methods = this.getAnnotatedMappings();

        for (Method method : methods) {
            try {

                String value = null;
                if (method.getReturnType() == String.class) {
                    value = (String) method.invoke(this);
                } else {
                    MappingField result = (MappingField) method.invoke(this);
                    value = mapper.apply(result);
                }

                if (value != null && !value.isEmpty()) {
                    values.add(value);
                }

            } catch (Exception e) {
            }
        }

        return values;
    }

    public List<Method> getAnnotatedMappings() {
        List<Method> annotatedMethods = new ArrayList<Method>();
        Class<?> clazz = this.getClass();
        Method[] methods = clazz.getMethods();

        for (Method method : methods) {
            Annotation annotation = method.getAnnotation(Mapped.class);
            if (annotation == null)
                continue;

            annotatedMethods.add(method);
        }

        return annotatedMethods;
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

    public String getOrderKey() {
        return orderKey;
    }

    public void setOrderKey(String orderKey) {
        this.orderKey = orderKey;
    }

    @Mapped
    public MappingField getDescription() {
        return description;
    }

    public void setDescription(MappingField description) {
        this.description = description;
    }

    @Mapped
    public MappingField getReportNum() {
        return reportNum;
    }

    public void setReportNum(MappingField reportNum) {
        this.reportNum = reportNum;
    }

    @Mapped
    public MappingField getReportCategory() {
        return reportCategory;
    }

    public void setReportCategory(MappingField reportCategory) {
        this.reportCategory = reportCategory;
    }

    @Mapped
    public MappingField getLocation() {
        return location;
    }

    public void setLocation(MappingField location) {
        this.location = location;
    }

    @Mapped
    public MappingField getLatitude() {
        return latitude;
    }

    public void setLatitude(MappingField latitude) {
        this.latitude = latitude;
    }

    @Mapped
    public MappingField getLongitude() {
        return longitude;
    }

    public void setLongitude(MappingField longitude) {
        this.longitude = longitude;
    }

    @Mapped
    public MappingField getReportedAt() {
        return reportedAt;
    }

    public void setReportedAt(MappingField reportedAt) {
        this.reportedAt = reportedAt;
    }

    @Mapped
    public MappingField getReportedAt2() {
        return reportedAt2;
    }

    public void setReportedAt2(MappingField reportedAt2) {
        this.reportedAt2 = reportedAt2;
    }

}
