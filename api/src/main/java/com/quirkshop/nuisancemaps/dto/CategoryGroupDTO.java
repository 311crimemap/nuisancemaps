package com.quirkshop.nuisancemaps.dto;

import java.util.List;

import com.quirkshop.nuisancemaps.model.Category;

/*
 * transient class to extract JSON and create Category records
 */

public class CategoryGroupDTO {

    private List<Category> data311s;
    private List<Category> dataCrimes;

    public List<Category> getData311s() {
        return data311s;
    }

    public void setData311s(List<Category> data311s) {
        this.data311s = data311s;
    }

    public List<Category> getDataCrimes() {
        return dataCrimes;
    }

    public void setDataCrimes(List<Category> dataCrimes) {
        this.dataCrimes = dataCrimes;
    }

}
