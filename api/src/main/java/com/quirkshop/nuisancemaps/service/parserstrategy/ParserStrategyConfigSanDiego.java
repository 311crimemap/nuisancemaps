package com.quirkshop.nuisancemaps.service.parserstrategy;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import com.quirkshop.nuisancemaps.WorkerApplication;
import com.quirkshop.nuisancemaps.model.MappingField;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * SAN DIEGO
 */

public class ParserStrategyConfigSanDiego {

    private static final Logger log = LoggerFactory.getLogger(WorkerApplication.class);

    // 2021-07-11 20:45:00
    public static String REPORTED_AT_CSV_CRIME_SAN_DIEGO(Map<String, String> row, MappingField mappingField) {
        String dateStr = null;

        try {
            String text = row.get("occured_on");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            DateTimeFormatter outputFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
            dateStr = LocalDateTime.parse(text, formatter).format(outputFormatter);
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return dateStr;
    }
}
