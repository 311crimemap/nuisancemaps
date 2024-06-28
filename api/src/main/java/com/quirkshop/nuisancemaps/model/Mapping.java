package com.quirkshop.nuisancemaps.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.format.annotation.DateTimeFormat;

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

    // mapped parse Fields
    @JsonProperty("report_num")
    @Mapped
    private String reportNum;

    @JsonProperty("report_category")
    @Mapped
    private String reportCategory;

    @JsonProperty("order_key")
    @Mapped
    private String orderKey;

    @Mapped
    private String description;

    @Mapped
    private String location;

    @Mapped
    private String latitude;

    @Mapped
    private String longitude;

    @JsonProperty("reported_at")
    @Mapped
    private String reportedAt;

    @JsonProperty("reported_at2")
    @Mapped
    private String reportedAt2;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime createdAt;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime updatedAt;

    public Mapping() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    public Mapping(String reportNum, String reportCategory, String orderKey, String description, String location, String latitude,
            String longitude, String reportedAt, String reportedAt2) {
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
            !void.class.equals(method.getReturnType()); //return type not void
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getReportNum() {
        return reportNum;
    }

    public void setReportNum(String reportNum) {
        this.reportNum = reportNum;
    }

    public String getReportCategory() {
        return reportCategory;
    }

    public void setReportCategory(String reportCategory) {
        this.reportCategory = reportCategory;
    }

    public String getOrderKey() {
        return orderKey;
    }

    public void setOrderKey(String orderKey) {
        this.orderKey = orderKey;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getReportedAt() {
        return reportedAt;
    }

    public void setReportedAt(String reportedAt) {
        this.reportedAt = reportedAt;
    }

    public String getReportedAt2() {
        return reportedAt2;
    }

    public void setReportedAt2(String reportedAt2) {
        this.reportedAt2 = reportedAt2;
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

}
