package com.quirkshop.nuisancemaps.service.parserstrategy;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.JsonNode;
import com.quirkshop.nuisancemaps.WorkerApplication;
import com.quirkshop.nuisancemaps.model.MappingField;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParserStrategyConfigKansasCity {

    private static final Logger log = LoggerFactory.getLogger(WorkerApplication.class);

    public static String ADDRESS_CSV_CRIME_KANSAS_CITY(Map<String, String> row, MappingField mappingField) {
        String address = null;
        try {
            String addr = row.get("Address");
            String city = row.get("City");
            String state = "MO";
            String zipcode = row.get("Zip Code");

            address = Stream.of(addr, city, state, zipcode)
                    .filter(s -> s != null && !s.isEmpty())
                    .collect(Collectors.joining(" "));

        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return address;

    }

    public static String ADDRESS_JSON_CRIME_KANSAS_CITY(JsonNode item, MappingField mappingField) {
        String address = null;
        try {
            String addr = item.at("/address").asText();
            String city = item.at("/city").asText();
            String state = "MO";
            String zipcode = item.at("/zip_code").asText();

            address = Stream.of(addr, city, state, zipcode)
                    .filter(s -> s != null && !s.isEmpty())
                    .collect(Collectors.joining(" "));

        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return address;
    }

    // POINT (-94.44111 39.04642)
    public static String LATITUDE_CSV_CRIME_KANSAS_CITY(Map<String, String> row, MappingField mappingField) {
        String latitude = null;

        try {
            String location = row.get("Location");
            if (location != null && location.contains("POINT")) {
                String[] coords = location.replace("POINT (", "").replace(")", "").split(" ");
                latitude = coords[1];
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return latitude;
    }

    // POINT (-94.44111 39.04642)
    public static String LONGITUDE_CSV_CRIME_KANSAS_CITY(Map<String, String> row, MappingField mappingField) {
        String longitude = null;

        try {
            String location = row.get("Location");
            if (location != null && location.contains("POINT")) {
                String[] coords = location.replace("POINT (", "").replace(")", "").split(" ");
                longitude = coords[0];
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return longitude;
    }
}
