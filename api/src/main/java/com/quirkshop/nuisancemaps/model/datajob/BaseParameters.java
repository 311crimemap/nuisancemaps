package com.quirkshop.nuisancemaps.model.datajob;

import java.util.HashMap;

public class BaseParameters implements DataJobParameters {

    private HashMap<String, Object> parameters;
    private String url;
    private HashMap<String, Object> nextParameters;
    private String nextUrl;

    public String buildInitURL(DataJob dataJob) {
        return dataJob.getSourceURL();
    }

    public String buildNextURL(DataJob dataJob) {
        return null;
    }

    public HashMap<String, Object> getParameters() {
        return parameters;
    }

    public String getUrl() {
        return url;
    }

    public HashMap<String, Object> getNextParameters() {
        return nextParameters;
    }

    public String getNextUrl() {
        return nextUrl;
    }


}
