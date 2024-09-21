package com.quirkshop.nuisancemaps.service.parserstrategy;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import com.quirkshop.nuisancemaps.WorkerApplication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * San Francisco
 */

public class ParserStrategyConfigSanFrancisco {

    private static final Logger log = LoggerFactory.getLogger(WorkerApplication.class);

    // 2023/03/16 10:15:00 PM
    public static String REPORTED_AT_CSV_CRIME_SAN_FRANCISCO(Map<String, String> row) {
        String dateStr = null;
        try {
            String text = row.get("Incident Datetime");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd hh:mm:ss a");
            DateTimeFormatter outputFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

            dateStr = LocalDate.parse(text, formatter)
                    .atStartOfDay()
                    .format(outputFormatter);

        } catch (Exception e) {
            log.error("[REPORTED_AT_CSV_CRIME_SAN_FRANCISCO] " + e.getMessage());
        }

        return dateStr;
    }

    // 06/09/2021 08:36:00 AM
    public static String REPORTED_AT_CSV_311_SAN_FRANCISCO(Map<String, String> row) {
        String dateStr = null;
        try {
            String text = row.get("Opened");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm:ss a");
            DateTimeFormatter outputFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

            dateStr = LocalDate.parse(text, formatter)
                    .atStartOfDay()
                    .format(outputFormatter);

        } catch (Exception e) {
            log.error("[REPORTED_AT_CSV_311_SAN_FRANCISCO] " + e.getMessage());
        }

        return dateStr;
    }

}
