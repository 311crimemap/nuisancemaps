package com.quirkshop.nuisancemaps.model.datajob;

public interface DataJobURL {
    public String buildInitURL(DataJob dataJob);

    public String buildNextURL(DataJob dataJob);
}
