package com.quirkshop.nuisancemaps.model.datajob;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

import com.quirkshop.nuisancemaps.WorkerApplication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.web.util.UriComponentsBuilder;

public class ERSIConfigurator implements DataJobConfigurator {

    private static final Logger log = LoggerFactory.getLogger(WorkerApplication.class);
    final static int MAX_MAP_SERVER = 8;

    public DataJob initialize(DataJob dataJob) {
        if (dataJob == null)
            return null;

        HashMap<String, Object> parameters = dataJob.getParameters();
        if (parameters == null) {
            parameters = new HashMap<String, Object>();
        }

        // Date: ERSI returns max 1000 records per MapServer.
        // need to reduce date range to remain under that threshold (e.g 3 days, etc)
        //
        // TODO: decide whether next dataJob is an incremented mapServer, or
        // incremented date range. Easiestapproach is to hardcode mapConfigServer 1-8
        // in the source URL (so 8 new ERSI sources), and let next() increment
        // only dates.
        //
        // DEFER this implementation for now because opting not to use this as a
        // source anymore.

        String startDate = "07/01/2024"; // last csv contains data from 07/06/2024
        String endDate = buildEndDate(LocalDate.now());

        parameters.putIfAbsent("paramStartDate", startDate);
        parameters.putIfAbsent("paramEndDate", endDate);
        parameters.putIfAbsent("paramMapServer", 1);

        String url = buildERSIParamsURL(dataJob, parameters);
        dataJob.setParameters(parameters);
        dataJob.setUrl(url);

        return dataJob;
    }

    // increment map server url 1-8
    // date range should remain
    public DataJob next(DataJob dataJob) {
        log.info("[ERSIConfigurator] !! DEPRECATED !!");

        if (dataJob == null)
            return null;

        HashMap<String, Object> parameters = dataJob.getParameters();
        if (parameters == null) {
            parameters = new HashMap<String, Object>();
        }

        int nextVal = (int) parameters.getOrDefault("paramMapServer", 0) + 1;
        parameters.put("paramMapServer", nextVal);

        if (nextVal > MAX_MAP_SERVER)
            return null;

        String url = buildERSIParamsURL(dataJob, parameters);
        dataJob.setParameters(parameters);
        dataJob.setUrl(url);

        return dataJob;
    }

    public String buildEndDate(LocalDate date) {

        // LocalDate now = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        String endDate = date.format(formatter);
        return endDate;
    }

    public String buildERSIParamsURL(DataJob dataJob, HashMap<String, Object> parameters) {

        String paramStartDate = (String) parameters.get("paramStartDate");
        String paramEndDate = (String) parameters.get("paramEndDate");
        int paramMapServer = (int) parameters.get("paramMapServer");

        String baseURL = String.format(
                "https://maps.austintexas.gov/gis/rest/APDCrimeViewer/APD_Reported_Crimes/MapServer/%d/query",
                paramMapServer);

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

}
