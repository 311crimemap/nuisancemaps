package com.quirkshop.nuisancemaps.model.datajob;

import java.util.HashMap;
import java.util.List;

import com.quirkshop.nuisancemaps.model.Mapping;
import com.quirkshop.nuisancemaps.model.MappingField;
import com.quirkshop.nuisancemaps.model.Source;

import org.springframework.web.util.UriComponentsBuilder;

public class ERSI_FeatureServerConfigurator implements DataJobConfigurator {

    private static final int PARAM_LIMIT = 32000;

    public DataJob initialize(DataJob dataJob) {
        if (dataJob == null)
            return null;

        HashMap<String, Object> parameters = dataJob.getParameters();
        if (parameters == null) {
            parameters = new HashMap<String, Object>();
        }

        parameters.putIfAbsent("paramLimit", PARAM_LIMIT);
        parameters.putIfAbsent("paramOffset", 0);
        dataJob.setParamOffset(0);

        String url = buildERSIParamsURL(dataJob, parameters);

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
        dataJob.setParamOffset(dataJob.getParamOffset() + PARAM_LIMIT);

        String url = buildERSIParamsURL(dataJob, parameters);

        dataJob.setParameters(parameters);
        dataJob.setUrl(url);
        return dataJob;
    }


    public String buildERSIParamsURL(DataJob dataJob, HashMap<String, Object> parameters) {

        int paramLimit = (Integer) parameters.getOrDefault("paramLimit", PARAM_LIMIT);
        int paramOffset = (Integer) parameters.getOrDefault("paramOffset", 0);

        Source source = dataJob.getSource();
        String sourceURL = source.getUrl();

        // collect fields
        Mapping mapping = source.getMapping();
        String outFields = buildURLFields(mapping);

        //?f=json
        //&where=1%3D1
        //&returnGeometry=false
        //&outFields={outFields}
        //&orderByFields=OBJECTID%20ASC
        //&resultOffset=0
        //&resultRecordCount=10
        //&resultType=standard

        String url = UriComponentsBuilder.fromUriString(sourceURL)
                .queryParam("f", "json")
                .queryParam("where", "1=1")
                .queryParam("returnGeometry", "false")
                .queryParam("outFields", outFields) //outFields
                .queryParam("orderByFields", "OBJECTID ASC")
                .queryParam("resultOffset", Integer.toString(paramOffset))
                .queryParam("resultRecordCount", Integer.toString(paramLimit))
                .queryParam("resultType", "standard")
                .build()
                .toUriString();

        return url;
    }

    // Map<String, Object> mapping
    public String buildURLFields(Mapping mapping) {

        List<String> fields = mapping.getAnnotationValues(MappingField::getField);

        // additional fields not in source config but useful
        fields.add("CITY");
        fields.add("ZipCode");

        return String.join(",", fields);
    }

}
