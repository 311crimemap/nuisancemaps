package com.quirkshop.nuisancemaps.model.datajob;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

/*
 * parameters
 * StartDate: String
 * EndDate: String
 * numDays: int
 */
public class APDIncidentReportConfigurator implements DataJobConfigurator {

    public DataJob initialize(DataJob dataJob) {
        if (dataJob == null)
            return null;

        HashMap<String, Object> parameters = dataJob.getParameters();
        if (parameters == null) {
            parameters = new HashMap<String, Object>();
        }

        String sourceURL = dataJob.getSourceURL();
        UriComponents uriComponents = UriComponentsBuilder.fromUriString(sourceURL).build();

        // defaults
        // String startDate = "07/01/2024"; // last csv contains data from 07/06/2024
        String startDate = uriComponents.getQueryParams().getFirst("startdate");

        parameters.putIfAbsent("paramStartDate", startDate);
        parameters.putIfAbsent("paramNumDays", 6);

        String url = buildAPDParamsURL(dataJob, parameters);

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

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");

        String startDate = (String) parameters.getOrDefault("paramStartDate", "07/01/2024");
        String endDate = (String) parameters.getOrDefault("paramEndDate",
                LocalDate.now().format(formatter));
        int numDays = (int) parameters.getOrDefault("paramNumDays", 6);

        LocalDate _startDate = LocalDate.parse(startDate, formatter);
        LocalDate _endDate = LocalDate.parse(endDate, formatter);

        if (_startDate.plusDays(numDays + 1).isBefore(_endDate)) {
            // increment start date by numDays
            parameters.put("paramStartDate",
                    _startDate.plusDays(numDays + 1).format(formatter));
            parameters.put("paramEndDate", endDate);
            parameters.put("paramNumDays", parameters.getOrDefault("numDays", 6));

            String url = buildAPDParamsURL(dataJob, parameters);
            dataJob.setParameters(parameters);
            dataJob.setUrl(url);

            return dataJob;
        }

        return null;
    }

    public String buildAPDParamsURL(DataJob dataJob, HashMap<String, Object> parameters) {

        String paramStartDate = (String) parameters.get("paramStartDate");
        int paramNumDays = (int) parameters.get("paramNumDays");

        String baseURL = String.format("https://services.austintexas.gov/police/reports/search2.cfm");

        // parameters to get crime reports anywhere in austin from range
        // [startDate, startdate + numDays]
        String url = UriComponentsBuilder.fromUriString(baseURL)
                .queryParam("startdate", paramStartDate)
                .queryParam("numdays", paramNumDays)
                .queryParam("address", "")
                .queryParam("rucrext", "")
                .queryParam("tract_num", "")
                .queryParam("zipcode", "")
                .queryParam("zone", "")
                .queryParam("district", "")
                .queryParam("city", "")
                .queryParam("choice", "criteria")
                .queryParam("Submit", "Submit")
                .build()
                .toUriString();

        return url;
    }

}
