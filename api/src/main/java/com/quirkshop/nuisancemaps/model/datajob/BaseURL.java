package com.quirkshop.nuisancemaps.model.datajob;

public class BaseURL implements DataJobURL {

    public String buildInitURL(DataJob dataJob) {
        return dataJob.getSourceURL();
    }

    public String buildNextURL(DataJob dataJob) {
        return null;
    }
}
