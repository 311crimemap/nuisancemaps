package com.quirkshop.nuisancemaps.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.quirkshop.nuisancemaps.model.datajob.DataJob;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "pending_text_category", uniqueConstraints = {
        @UniqueConstraint(name = "PendingTextCategory_UniqueDataTypeAndText", columnNames = { "dataType", "text" })
})
public class PendingTextCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pending_text_category_seq")
    @SequenceGenerator(name = "pending_text_category_seq", allocationSize = 1)
    private Integer id;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "data_job_id", nullable = false)
    private DataJob dataJob;

    private String dataType; // 311 or crime

    private String text;

    public PendingTextCategory() {
    }

    public PendingTextCategory(DataJob dataJob, String dataType, String text) {
        this.dataJob = dataJob;
        this.dataType = dataType;
        this.text = text;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public DataJob getDataJob() {
        return dataJob;
    }

    public void setDataJob(DataJob dataJob) {
        this.dataJob = dataJob;
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

}
