package com.quirkshop.nuisancemaps.service.parserstrategy;

import com.fasterxml.jackson.databind.JsonNode;
import com.quirkshop.nuisancemaps.WorkerApplication;
import com.quirkshop.nuisancemaps.model.MappingField;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * SEATTLE
 */

public class ParserStrategyConfigSeattle {

    private static final Logger log = LoggerFactory.getLogger(WorkerApplication.class);

    // handle "REDACTED"
    public static String LATITUDE_JSON_CRIME_SEATTLE(JsonNode item, MappingField mappingField) {
        String latitude = null;

        try {
            latitude = item.at("/latitude").asText();
            if (latitude.equals("REDACTED"))
                return null;
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return latitude;
    }

    // handle "REDACTED"
    public static String LONGITUDE_JSON_CRIME_SEATTLE(JsonNode item, MappingField mappingField) {
        String longitude = null;

        try {
            longitude = item.at("/longitude").asText();
            if (longitude.equals("REDACTED"))
                return null;
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return longitude;
    }

}
