package com.quirkshop.nuisancemaps.service.parserstrategy;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.fasterxml.jackson.databind.JsonNode;
import com.quirkshop.nuisancemaps.WorkerApplication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class ParserStrategyConfig {

    private static final Logger log = LoggerFactory.getLogger(WorkerApplication.class);

    @Bean
    public Map<ParserStrategy, Function<JsonNode, String>> parsingFunctionsJSON() {

        Map<ParserStrategy, Function<JsonNode, String>> parsingFunctions = new HashMap<>();

        parsingFunctions.put(ParserStrategy.LATITUDE_311_DALLAS,
                ParserStrategyConfigDallas::LATITUDE_311_DALLAS);
        parsingFunctions.put(ParserStrategy.LONGITUDE_311_DALLAS,
                ParserStrategyConfigDallas::LONGITUDE_311_DALLAS);
        parsingFunctions.put(ParserStrategy.REPORTEDAT_CRIME_DALLAS,
                ParserStrategyConfigDallas::REPORTEDAT_CRIME_DALLAS);
        parsingFunctions.put(ParserStrategy.REPORTEDAT2_CRIME_DALLAS,
                ParserStrategyConfigDallas::REPORTEDAT2_CRIME_DALLAS);
        parsingFunctions.put(ParserStrategy.STREET_NAME_ERSI_AUSTIN,
                ParserStrategyConfigAustin::STREET_NAME_ERSI_AUSTIN);
        parsingFunctions.put(ParserStrategy.OCCURRENCE_DATE_ERSI_AUSTIN,
                ParserStrategyConfigAustin::OCCURRENCE_DATE_ERSI_AUSTIN);

        return parsingFunctions;
    }

    @Bean
    public Map<ParserStrategy, Function<Map<String, String>, String>> parsingFunctionsMap() {

        Map<ParserStrategy, Function<Map<String, String>, String>> parsingFunctions = new HashMap<>();

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

        return parsingFunctions;
    }

}
