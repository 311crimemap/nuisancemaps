package com.quirkshop.nuisancemaps.model.datajob;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

import org.springframework.web.util.UriComponentsBuilder;

public class ERSIParameters implements DataJobParameters {

    private HashMap<String, Object> parameters;
    private String url;
    private HashMap<String, Object> nextParameters;
    private String nextUrl;

    final static int MAX_MAP_SERVER = 8;

    public String buildInitURL(DataJob dataJob) {

        parameters = dataJob.getParameters();
        if (parameters == null) {
            parameters = new HashMap<String, Object>();
        }

        String startDate = "07/01/2024"; // last csv contains data from 07/06/2024
        String endDate = buildEndDate(LocalDate.now());

        parameters.putIfAbsent("paramStartDate", startDate);
        parameters.putIfAbsent("paramEndDate", endDate);
        parameters.putIfAbsent("paramMapServer", 1);

        this.url = buildERSIParamsURL(dataJob);
        return this.url;
    }

    // increment map server url 1-8
    // date range should remain
    public String buildNextURL(DataJob dataJob) {
        nextParameters = new HashMap<String, Object>();
        parameters = dataJob.getParameters();
        if (parameters == null) {
            parameters = new HashMap<String, Object>();
        }

        int nextVal = (int) parameters.getOrDefault("paramMapServer", 0) + 1;
        parameters.put("paramMapServer", nextVal);

        if (nextVal > MAX_MAP_SERVER) return null;

        this.nextUrl = buildERSIParamsURL(dataJob);
        return this.nextUrl;
    }

    public String buildEndDate(LocalDate date) {

        //LocalDate now = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        String endDate = date.format(formatter);
        return endDate;
    }

    public String buildERSIParamsURL(DataJob dataJob) {
        HashMap<String, Object> parameters = dataJob.getParameters();
        if (parameters == null) {
            parameters = new HashMap<String, Object>();
        }

        String paramStartDate = (String) parameters.get("paramStartDate");
        String paramEndDate= (String) parameters.get("paramEndDate");
        int paramMapServer = (int) parameters.get("paramMapServer");

        String baseURL = String.format("https://maps.austintexas.gov/gis/rest/APDCrimeViewer/APD_Reported_Crimes/MapServer/%d/query", paramMapServer);

        String dateStr = String
            .format("1=1 AND OCCURRENCE_DATE BETWEEN date '%s' AND date '%s' ",
                    paramStartDate, paramEndDate);

        // manual draw around city limits - "all data"
        String geometry = "{\"rings\":[[[3018272.9057345022,10242548.737086222],[3070539.572401169,10280948.737086222],[3290272.9057345022,10247882.070419556],[3234806.2390678357,9873482.070419554],[2911606.2390678357,9984415.403752888],[3018272.9057345022,10242548.737086222]]],\"spatialReference\":{\"wkid\":102739,\"latestWkid\":2277}}";

        String url = UriComponentsBuilder.fromUriString(baseURL)
            .queryParam("f", "json")
            .queryParam("where", dateStr)
            .queryParam("returnGeometry", "true")
            .queryParam("spatialRel", "esriSpatialRelIntersects")
            .queryParam("geometry", geometry)
            .queryParam("geometryType", "esriGeometryPolygon")
            .queryParam("inSR", 102739)
            .queryParam("outFields", "*")
            .queryParam("orderByFields", "OCCURRENCE_DATE")
            .queryParam("outSR", 4326)
            .build()
            .toUriString();

        return url;
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

    public static int getMaxMapServer() {
        return MAX_MAP_SERVER;
    }

}
