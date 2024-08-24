package com.quirkshop.nuisancemaps.model.datajob;

import java.util.HashMap;
import java.util.List;

import com.quirkshop.nuisancemaps.model.Mapping;
import com.quirkshop.nuisancemaps.model.MappingField;
import com.quirkshop.nuisancemaps.model.Source;

import org.springframework.web.util.UriComponentsBuilder;

public class OpenDataConfigurator implements DataJobConfigurator {

    private static final int PARAM_LIMIT = Integer.parseInt(System.getenv("WORKER_QUERY_LIMIT"));

    public DataJob initialize(DataJob dataJob) {
        if (dataJob == null)
            return null;

        HashMap<String, Object> parameters = dataJob.getParameters();
        if (parameters == null) {
            parameters = new HashMap<String, Object>();
        }

        parameters.putIfAbsent("paramLimit", PARAM_LIMIT);
        parameters.putIfAbsent("paramOffset", 0);

        String url = buildOpenDataParamsURL(dataJob, parameters);

        dataJob.setParameters(parameters);
        dataJob.setUrl(url);
        return dataJob;
    }

    public DataJob next(DataJob dataJob) {
        if (dataJob == null)
            return null;

        HashMap<String, Object> parameters = dataJob.getParameters();
        if (parameters == null) {
            parameters = new HashMap<String, Object>();
        }

        parameters.put("paramLimit", (Integer) parameters.getOrDefault("paramLimit", PARAM_LIMIT));
        parameters.put("paramOffset", (Integer) parameters.getOrDefault("paramOffset", 0) + PARAM_LIMIT);

        String url = buildOpenDataParamsURL(dataJob, parameters);

        dataJob.setParameters(parameters);
        dataJob.setUrl(url);
        return dataJob;
    }

    public String buildOpenDataParamsURL(DataJob dataJob, HashMap<String, Object> parameters) {

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

}
