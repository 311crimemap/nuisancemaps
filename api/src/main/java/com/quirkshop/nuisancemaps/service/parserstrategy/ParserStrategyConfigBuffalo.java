package com.quirkshop.nuisancemaps.service.parserstrategy;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.JsonNode;
import com.quirkshop.nuisancemaps.WorkerApplication;
import com.quirkshop.nuisancemaps.model.MappingField;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * Buffalo
 */

public class ParserStrategyConfigBuffalo {

    private static final Logger log = LoggerFactory.getLogger(WorkerApplication.class);

    public static String ADDRESS_CSV_311_BUFFALO(Map<String, String> row, MappingField mappingField) {
        String address = null;

        try {
            String addressNumber = row.get("Address Number");
            String addressLine1 = row.get("Address Line 1");
            String addressLine2 = row.get("Address Line 2");
            String city = row.get("City");
            String state = row.get("State");
            String zipcode = row.get("Zipcode");

            // entails other missing, un-mappable data
            if (zipcode.toLowerCase().equals("unknown")) {
                return null;
            }

            address = Stream.of(addressNumber, addressLine1, addressLine2, city, state, zipcode)
                    .filter(s -> s != null && !s.isEmpty())
                    .collect(Collectors.joining(" "));

        } catch (Exception e) {
            log.error("[ADDRESS_CSV_311_BUFFALO] " + e.getMessage());
        }

        return address;
    }

    public static String ADDRESS2_CSV_311_BUFFALO(Map<String, String> row, MappingField mappingField) {
        String address = null;

        try {
            String reportedLocation = row.get("ReportedLocation");
            String city = row.get("City");
            String state = row.get("State");
            String zipcode = row.get("Zip");


            address = Stream.of(reportedLocation, city, state, zipcode)
                    .filter(s -> s != null && !s.isEmpty())
                    .collect(Collectors.joining(" "));

        } catch (Exception e) {
            log.error("[ADDRESS2_CSV_3131_BUFFALO] " + e.getMessage());
        }

        return address;
    }

    public static String ADDRESS2_JSON_311_BUFFALO(JsonNode item, MappingField mappingField) {
        String address = null;

        try {
            String reportedLocation = item.at("/reportedlocation").asText();
            String city = item.at("/city").asText();
            String state = item.at("/state").asText();
            String zipcode = item.at("/zip").asText();

            address = Stream.of(reportedLocation, city, state, zipcode)
                    .filter(s -> s != null && !s.isEmpty())
                    .collect(Collectors.joining(" "));

        } catch (Exception e) {
            log.error("[ADDRESS2_JSON_311_BUFFALO] " + e.getMessage());
        }

        return address;
    }

}
