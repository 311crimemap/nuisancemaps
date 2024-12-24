package com.quirkshop.nuisancemaps.service.parserstrategy;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.JsonNode;
import com.quirkshop.nuisancemaps.WorkerApplication;
import com.quirkshop.nuisancemaps.model.MappingField;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParserStrategyConfigOakland {

    private static final Logger log = LoggerFactory.getLogger(WorkerApplication.class);

    public static String ADDRESS_CSV_CRIME_OAKLAND(Map<String, String> row, MappingField mappingField) {
        String address = null;
        try {
            String addr = row.getOrDefault("ADDRESS", "");
            String city = row.getOrDefault("CITY", "");
            String state = row.getOrDefault("STATE", "");

            address = Stream.of(addr, city, state)
                    .filter(s -> s != null && !s.isEmpty())
                    .collect(Collectors.joining(" "));

        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return address;
    }

    public static String ADDRESS_CSV_CRIME_OAKLAND_FULL(Map<String, String> row, MappingField mappingField) {
        String address = null;
        try {
            String addr = row.getOrDefault("Address", "");
            String city = row.getOrDefault("City", "");
            String state = row.getOrDefault("State", "");

            address = Stream.of(addr, city, state)
                    .filter(s -> s != null && !s.isEmpty())
                    .collect(Collectors.joining(" "));

        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return address;
    }

    public static String ADDRESS_CSV_311_OAKLAND(Map<String, String> row, MappingField mappingField) {
        String address = null;
        try {
            String addr = row.getOrDefault("PROBADDRESS", "");
            String city = row.getOrDefault("City", "");
            String state = row.getOrDefault("State", "");
            String zipcode = row.getOrDefault("Zipcode", "");

            address = Stream.of(addr, city, state, zipcode)
                    .filter(s -> s != null && !s.isEmpty())
                    .collect(Collectors.joining(" "));

        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return address;
    }

    public static String ADDRESS_JSON_311_OAKLAND(JsonNode item, MappingField mappingField) {
        String address = null;
        try {
            String addr = item.at("/probaddress").asText();
            String city = item.at("/city").asText();
            String state = item.at("/state").asText();
            String zipcode = item.at("/zipcode").asText();

            address = Stream.of(addr, city, state, zipcode)
                    .filter(s -> s != null && !s.isEmpty())
                    .collect(Collectors.joining(" "));

        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return address;
    }

}
