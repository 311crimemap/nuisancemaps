package com.quirkshop.nuisancemaps.service.parserstrategy;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

import com.fasterxml.jackson.databind.JsonNode;
import com.quirkshop.nuisancemaps.WorkerApplication;
import com.quirkshop.nuisancemaps.model.MappingField;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class ParserStrategyConfig {

    private static final Logger log = LoggerFactory.getLogger(WorkerApplication.class);

    @Bean
    public Map<ParserStrategy, BiFunction<JsonNode, MappingField, String>> parsingFunctionsJSON() {

        Map<ParserStrategy, BiFunction<JsonNode, MappingField, String>> parsingFunctions = new HashMap<>();

        parsingFunctions.put(ParserStrategy.LATITUDE_311_DALLAS,
                ParserStrategyConfigDallas::LATITUDE_311_DALLAS);
        parsingFunctions.put(ParserStrategy.LONGITUDE_311_DALLAS,
                ParserStrategyConfigDallas::LONGITUDE_311_DALLAS);
        parsingFunctions.put(ParserStrategy.REPORTED_AT_CRIME_DALLAS,
                ParserStrategyConfigDallas::REPORTED_AT_CRIME_DALLAS);
        parsingFunctions.put(ParserStrategy.REPORTED_AT2_CRIME_DALLAS,
                ParserStrategyConfigDallas::REPORTED_AT2_CRIME_DALLAS);
        parsingFunctions.put(ParserStrategy.STREET_NAME_ERSI_AUSTIN,
                ParserStrategyConfigAustin::STREET_NAME_ERSI_AUSTIN);
        parsingFunctions.put(ParserStrategy.OCCURRENCE_DATE_ERSI_AUSTIN,
                ParserStrategyConfigAustin::OCCURRENCE_DATE_ERSI_AUSTIN);

        // los angeles
        parsingFunctions.put(ParserStrategy.REPORTED_AT_311_LA,
                ParserStrategyConfigLosAngeles::REPORTED_AT_311_LA);

        return parsingFunctions;
    }

    @Bean
    public Map<ParserStrategy, BiFunction<Map<String, String>, MappingField, String>> parsingFunctionsMap() {

        Map<ParserStrategy, BiFunction<Map<String, String>, MappingField, String>> parsingFunctions = new HashMap<>();

        // austin
        parsingFunctions.put(ParserStrategy.REPORTED_AT_CSV_AUSTIN,
                ParserStrategyConfigAustin::REPORTED_AT_CSV_AUSTIN);
        parsingFunctions.put(ParserStrategy.REPORTED_AT2_CSV_AUSTIN,
                ParserStrategyConfigAustin::REPORTED_AT2_CSV_AUSTIN);
        parsingFunctions.put(ParserStrategy.CREATED_DATE_CSV_AUSTIN,
                ParserStrategyConfigAustin::CREATED_DATE_CSV_AUSTIN);

        // dallas
        parsingFunctions.put(ParserStrategy.LATITUDE_CSV_CRIME_DALLAS,
                ParserStrategyConfigDallas::LATITUDE_CSV_CRIME_DALLAS);
        parsingFunctions.put(ParserStrategy.LONGITUDE_CSV_CRIME_DALLAS,
                ParserStrategyConfigDallas::LONGITUDE_CSV_CRIME_DALLAS);
        parsingFunctions.put(ParserStrategy.LATITUDE_CSV_311_DALLAS,
                ParserStrategyConfigDallas::LATITUDE_CSV_311_DALLAS);
        parsingFunctions.put(ParserStrategy.LONGITUDE_CSV_311_DALLAS,
                ParserStrategyConfigDallas::LONGITUDE_CSV_311_DALLAS);
        parsingFunctions.put(ParserStrategy.REPORTED_AT_CSV_311_DALLAS,
                ParserStrategyConfigDallas::REPORTED_AT_CSV_311_DALLAS);
        parsingFunctions.put(ParserStrategy.REPORTED_AT_CSV_CRIME_DALLAS,
                ParserStrategyConfigDallas::REPORTED_AT_CSV_CRIME_DALLAS);
        parsingFunctions.put(ParserStrategy.REPORTED_AT2_CSV_CRIME_DALLAS,
                ParserStrategyConfigDallas::REPORTED_AT2_CSV_CRIME_DALLAS);

        // chicago
        parsingFunctions.put(ParserStrategy.REPORTED_AT_CSV_CRIME_CHICAGO,
                ParserStrategyConfigChicago::REPORTED_AT_CSV_CRIME_CHICAGO);
        parsingFunctions.put(ParserStrategy.REPORTED_AT_CSV_311_CHICAGO,
                ParserStrategyConfigChicago::REPORTED_AT_CSV_311_CHICAGO);

        // san francisco
        parsingFunctions.put(ParserStrategy.REPORTED_AT_CSV_CRIME_SAN_FRANCISCO,
                ParserStrategyConfigSanFrancisco::REPORTED_AT_CSV_CRIME_SAN_FRANCISCO);
        parsingFunctions.put(ParserStrategy.REPORTED_AT_CSV_311_SAN_FRANCISCO,
                ParserStrategyConfigSanFrancisco::REPORTED_AT_CSV_311_SAN_FRANCISCO);

        // new york city
        parsingFunctions.put(ParserStrategy.REPORTED_AT_CSV_CRIME_NEW_YORK_CITY,
                ParserStrategyConfigNewYorkCity::REPORTED_AT_CSV_CRIME_NEW_YORK_CITY);
        parsingFunctions.put(ParserStrategy.REPORTED_AT2_CSV_CRIME_NEW_YORK_CITY,
                ParserStrategyConfigNewYorkCity::REPORTED_AT2_CSV_CRIME_NEW_YORK_CITY);
        parsingFunctions.put(ParserStrategy.REPORTED_AT_CSV_311_NEW_YORK_CITY,
                ParserStrategyConfigNewYorkCity::REPORTED_AT_CSV_311_NEW_YORK_CITY);

        // boston
        parsingFunctions.put(ParserStrategy.REPORTED_AT_CSV_CRIME_TIMEZONE_OFFSET_BOSTON,
                ParserStrategyConfigBoston::REPORTED_AT_CSV_CRIME_TIMEZONE_OFFSET_BOSTON);
        parsingFunctions.put(ParserStrategy.REPORTED_AT_CSV_CRIME_BOSTON,
                ParserStrategyConfigBoston::REPORTED_AT_CSV_CRIME_BOSTON);
        parsingFunctions.put(ParserStrategy.REPORTED_AT_CSV_311_BOSTON,
                ParserStrategyConfigBoston::REPORTED_AT_CSV_311_BOSTON);

        // los angeles
        parsingFunctions.put(ParserStrategy.REPORTED_AT_CSV_CRIME_LA,
                ParserStrategyConfigLosAngeles::REPORTED_AT_CSV_CRIME_LA);
        parsingFunctions.put(ParserStrategy.REPORTED_AT2_CSV_CRIME_LA,
                ParserStrategyConfigLosAngeles::REPORTED_AT2_CSV_CRIME_LA);
        parsingFunctions.put(ParserStrategy.REPORTED_AT_CSV_311_LA,
                ParserStrategyConfigLosAngeles::REPORTED_AT_CSV_311_LA);

        // houston
        parsingFunctions.put(ParserStrategy.REPORTED_AT_CSV_311_HOUSTON,
                ParserStrategyConfigHouston::REPORTED_AT_CSV_311_HOUSTON);
        parsingFunctions.put(ParserStrategy.REPORTED_AT_CSV2_311_HOUSTON,
                ParserStrategyConfigHouston::REPORTED_AT_CSV2_311_HOUSTON);
        parsingFunctions.put(ParserStrategy.REPORTED_AT_XLS_CRIME_HOUSTON,
                ParserStrategyConfigHouston::REPORTED_AT_XLS_CRIME_HOUSTON);
        parsingFunctions.put(ParserStrategy.ADDRESS_XLS_CRIME_HOUSTON,
                ParserStrategyConfigHouston::ADDRESS_XLS_CRIME_HOUSTON);

        // philadelphia
        parsingFunctions.put(ParserStrategy.REPORTED_AT_CSV_CRIME_PHILADELPHIA,
                ParserStrategyConfigPhiladelphia::REPORTED_AT_CSV_CRIME_PHILADELPHIA);
        parsingFunctions.put(ParserStrategy.REPORTED_AT_CSV_311_PHILADELPHIA,
                ParserStrategyConfigPhiladelphia::REPORTED_AT_CSV_311_PHILADELPHIA);

        // san diego
        parsingFunctions.put(ParserStrategy.REPORTED_AT_CSV_CRIME_SAN_DIEGO,
                ParserStrategyConfigSanDiego::REPORTED_AT_CSV_CRIME_SAN_DIEGO);

        // charlotte
        parsingFunctions.put(ParserStrategy.REPORTED_AT_CSV_CRIME_CHARLOTTE,
                ParserStrategyConfigCharlotte::REPORTED_AT_CSV_CRIME_CHARLOTTE);
        parsingFunctions.put(ParserStrategy.REPORTED_AT_CSV_311_CHARLOTTE,
                ParserStrategyConfigCharlotte::REPORTED_AT_CSV_311_CHARLOTTE);
        parsingFunctions.put(ParserStrategy.ADDRESS_CSV_CRIME_CHARLOTTE,
                ParserStrategyConfigCharlotte::ADDRESS_CSV_CRIME_CHARLOTTE);

        // denver
        parsingFunctions.put(ParserStrategy.REPORTED_AT_CSV_CRIME_DENVER,
                ParserStrategyConfigDenver::REPORTED_AT_CSV_CRIME_DENVER);
        parsingFunctions.put(ParserStrategy.REPORTED_AT_CSV_311_DENVER,
                ParserStrategyConfigDenver::REPORTED_AT_CSV_311_DENVER);

        // detroit
        parsingFunctions.put(ParserStrategy.REPORTED_AT_CSV_CRIME_DETROIT,
                ParserStrategyConfigDetroit::REPORTED_AT_CSV_CRIME_DETROIT);
        parsingFunctions.put(ParserStrategy.REPORTED_AT_CSV_311_DETROIT,
                ParserStrategyConfigDetroit::REPORTED_AT_CSV_311_DETROIT);

        // memphis
        parsingFunctions.put(ParserStrategy.REPORTED_AT_CSV_CRIME_MEMPHIS,
                ParserStrategyConfigMemphis::REPORTED_AT_CSV_CRIME_MEMPHIS);
        parsingFunctions.put(ParserStrategy.REPORTED_AT_CSV_311_MEMPHIS,
                ParserStrategyConfigMemphis::REPORTED_AT_CSV_311_MEMPHIS);
        parsingFunctions.put(ParserStrategy.LATITIUDE_CSV_311_MEMPHIS,
                ParserStrategyConfigMemphis::LATITIUDE_CSV_311_MEMPHIS);
        parsingFunctions.put(ParserStrategy.LONGITUDE_CSV_311_MEMPHIS,
                ParserStrategyConfigMemphis::LONGITUDE_CSV_311_MEMPHIS);

        return parsingFunctions;
    }

}
