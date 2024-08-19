package com.quirkshop.nuisancemaps.model.datajob;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

import org.springframework.web.util.UriComponentsBuilder;

/*
 * parameters
 * StartDate: String
 * EndDate: String
 * numDays: int
 */
public class APDIncidentReportURL implements DataJobURL {

    public String buildInitURL(DataJob dataJob) {

        HashMap<String, Object> parameters = dataJob.getParameters();

        String startDate = "07/01/2024"; // last csv contains data from 07/06/2024

        parameters.putIfAbsent("paramStartDate", startDate);
        parameters.putIfAbsent("paramNumDays", 6);

        return buildAPDParamsURL(dataJob);
    }

    public String buildNextURL(DataJob dataJob) {

        HashMap<String, Object> parameters = dataJob.getParameters();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");

        String startDate = (String) parameters
                .getOrDefault("paramStartDate", "07/01/2024");
        String endDate = (String) parameters
                .getOrDefault("paramEndDate", LocalDate.now().format(formatter));
        int numDays = (int) parameters.getOrDefault("paramNumDays", 6);

        LocalDate _startDate = LocalDate.parse(startDate, formatter);
        LocalDate _endDate = LocalDate.parse(endDate, formatter);

        if (_startDate.plusDays(numDays).isBefore(_endDate)) {
            // increment start date by numDays
            parameters.put("paramStartDate",
                    _startDate.plusDays(numDays).format(formatter));
            parameters.put("paramEndDate", endDate);
            parameters.put("paramNumDays", parameters.getOrDefault("numDays", 6));

            return buildAPDParamsURL(dataJob);
        }

        return null;
    }

    public String buildAPDParamsURL(DataJob dataJob) {

        HashMap<String, Object> parameters = dataJob.getParameters();

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
