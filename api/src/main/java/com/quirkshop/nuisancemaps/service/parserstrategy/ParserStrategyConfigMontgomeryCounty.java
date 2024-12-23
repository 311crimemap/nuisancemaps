package com.quirkshop.nuisancemaps.service.parserstrategy;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.JsonNode;
import com.quirkshop.nuisancemaps.WorkerApplication;
import com.quirkshop.nuisancemaps.model.MappingField;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParserStrategyConfigMontgomeryCounty {

    private static final Logger log = LoggerFactory.getLogger(WorkerApplication.class);

    public static String ADDRESS_CSV_CRIME_MONTGOMERY_COUNTY(Map<String, String> row, MappingField mappingField) {
        String address = null;
        try {
            String blockAddr = row.get("Block Address");
            String city = row.get("City");
            String state = row.get("State");
            String zipcode = row.get("Zip Code");

            address = Stream.of(blockAddr, city, state, zipcode)
                    .filter(s -> s != null && !s.isEmpty())
                    .collect(Collectors.joining(" "));

        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return address;
    }

    public static String ADDRESS_JSON_CRIME_MONTGOMERY_COUNTY(JsonNode item, MappingField mappingField) {
        String address = null;
        try {
            String location = item.at("/location").asText();
            String city = item.at("/city").asText();
            String state = item.at("/state").asText();
            String zipcode = item.at("/zip_code").asText();

            address = Stream.of(location, city, state, zipcode)
                    .filter(s -> s != null && !s.isEmpty())
                    .collect(Collectors.joining(" "));

        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return address;
    }

}
