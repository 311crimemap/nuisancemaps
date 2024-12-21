package com.quirkshop.nuisancemaps.service.parserstrategy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.quirkshop.nuisancemaps.WorkerApplication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParserStrategyConfigCharlotte {

    private static final Logger log = LoggerFactory.getLogger(WorkerApplication.class);

    public static String ADDRESS_CSV_CRIME_CHARLOTTE(Map<String, String> row) {
        String address = null;
        try {
            String location = row.get("LOCATION");
            String city = row.get("CITY");
            String state = row.get("STATE");
            String zip = row.get("ZIP");

            address = Stream.of(location, city, state, zip)
                    .filter(s -> s != null && !s.isEmpty())
                    .collect(Collectors.joining(" "));

        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return address;
    }

    // 2023/08/17 00:00:00+00
    // contains timezone offset - need OffsetDateTime
    public static String REPORTED_AT_CSV_CRIME_CHARLOTTE(Map<String, String> row) {
        String dateStr = null;
        try {
            String text = row.get("DATE_REPORTED");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ssX");
            DateTimeFormatter outputFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

            dateStr = OffsetDateTime.parse(text, formatter)
                    .toLocalDateTime() // Strip the offset
                    .format(outputFormatter);

        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return dateStr;
    }

    // 2017/05/16 14:09:00+00
    public static String REPORTED_AT_CSV_311_CHARLOTTE(Map<String, String> row) {
        String dateStr = null;
        try {
            String text = row.get("RECEIVED_DATE");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ssx");
            DateTimeFormatter outputFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
            dateStr = LocalDateTime.parse(text, formatter).format(outputFormatter);

        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return dateStr;
    }

}
