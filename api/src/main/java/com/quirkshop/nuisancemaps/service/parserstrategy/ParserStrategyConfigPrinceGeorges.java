package com.quirkshop.nuisancemaps.service.parserstrategy;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.JsonNode;
import com.quirkshop.nuisancemaps.WorkerApplication;
import com.quirkshop.nuisancemaps.model.MappingField;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParserStrategyConfigPrinceGeorges {

    private static final Logger log = LoggerFactory.getLogger(WorkerApplication.class);

    public static String ADDRESS_JSON_CRIME_PRINCE_GEORGES(JsonNode item, MappingField mappingField) {
        String address = null;
        try {

            String street_number = item.at("/street_number").asText();
            String street_address = item.at("/street_address").asText();

            address = Stream.of(street_number, street_address)
                    .filter(s -> s != null && !s.isEmpty())
                    .collect(Collectors.joining(" "));

        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return address;
    }

}
