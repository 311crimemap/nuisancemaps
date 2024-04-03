package com.quirkshop.nuisancemaps.dto;

import com.quirkshop.nuisancemaps.model.Category;

public class CategoryAPIDTO<T> {

    private String status;
    private T data;

    public CategoryAPIDTO(String status, T data) {
        this.status = status;
        this.data = data;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public T getData() {
        return data;
    }
    public void setData(T data) {
        this.data = data;
    }

}
