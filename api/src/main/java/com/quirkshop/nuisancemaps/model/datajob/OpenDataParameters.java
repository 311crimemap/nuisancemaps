package com.quirkshop.nuisancemaps.model.datajob;

import java.util.HashMap;
import java.util.List;

import com.quirkshop.nuisancemaps.model.Mapping;
import com.quirkshop.nuisancemaps.model.MappingField;
import com.quirkshop.nuisancemaps.model.Source;

import org.springframework.web.util.UriComponentsBuilder;

public class OpenDataParameters implements DataJobParameters {

    private HashMap<String, Object> parameters;
    private String url;
    private HashMap<String, Object> nextParameters;
    private String nextUrl;

    private static final int PARAM_LIMIT = Integer.parseInt(System.getenv("WORKER_QUERY_LIMIT"));

    public String buildInitURL(DataJob dataJob) {
        // if parameters in the initial case are somehow set prior, use those
        parameters = dataJob.getParameters();
        if (parameters == null) {
            parameters = new HashMap<String, Object>();
        }

        parameters.putIfAbsent("paramLimit", PARAM_LIMIT);
        parameters.putIfAbsent("paramOffset", 0);

        this.url = buildOpenDataParamsURL(dataJob);
        return this.url;
    }

    public String buildNextURL(DataJob dataJob) {
        nextParameters = new HashMap<String, Object>();
        parameters = dataJob.getParameters();
        if (parameters == null) {
            parameters = new HashMap<String, Object>();
        }

        parameters.put("paramLimit", (Integer) parameters.getOrDefault("paramLimit", PARAM_LIMIT));
        parameters.put("paramOffset", (Integer) parameters.getOrDefault("paramOffset", 0) + PARAM_LIMIT);

        this.nextUrl = buildOpenDataParamsURL(dataJob);
        return this.nextUrl;
    }

    public String buildOpenDataParamsURL(DataJob dataJob) {
        HashMap<String, Object> parameters = dataJob.getParameters();
        if (parameters == null) {
            parameters = new HashMap<String, Object>();
        }

        int paramLimit = (Integer) parameters.getOrDefault("paramLimit", PARAM_LIMIT);
        int paramOffset = (Integer) parameters.getOrDefault("paramOffset", 0);

        Source source = dataJob.getSource();
        String sourceURL = source.getUrl();

        // collect fields
        Mapping mapping = source.getMapping();
        String $select = buildURLFields(mapping);

        String url = UriComponentsBuilder.fromUriString(sourceURL)
                .queryParam("$limit", Integer.toString(paramLimit))
                .queryParam("$offset", Integer.toString(paramOffset))
                .queryParam("$order", dataJob.getOrderKey())
                .queryParam("$select", $select)
                .build()
                .toUriString();

        return url;
    }

    // Map<String, Object> mapping
    public String buildURLFields(Mapping mapping) {

        List<String> fields = mapping.getAnnotationValues(MappingField::getField);

        return String.join(",", fields);
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

    public static int getParamLimit() {
        return PARAM_LIMIT;
    }

}
