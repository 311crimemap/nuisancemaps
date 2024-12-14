package com.quirkshop.nuisancemaps.service.parserstrategy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.quirkshop.nuisancemaps.WorkerApplication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParserStrategyConfigHouston {

    private static final Logger log = LoggerFactory.getLogger(WorkerApplication.class);

    // LocalDateTime.parse has ISO defaults that cannot handle nanosecond precision
    // 2024-12-12 06:11:02.0000000
    public static String REPORTED_AT_CSV_311_HOUSTON(Map<String, String> row) {
        String dateStr = null;
        try {
            String text = row.get("Created Date Local");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSS");
            DateTimeFormatter outputFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
            dateStr = LocalDateTime.parse(text, formatter).format(outputFormatter);
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return dateStr;
    }

    // 2024-01-04T00:00:00
    // hour: 0
    // NB: input is converted in XLSDataParser to ISO_LOCAL_DATE_TIME
    public static String REPORTED_AT_XLS_CRIME_HOUSTON(Map<String, String> row) {
        String dateStr = null;
        try {
            String text = row.get("RMSOccurrenceDate");
            Integer hour = Integer.parseInt(row.getOrDefault("RMSOccurrenceHour", "0"));

            // input ISO_LOCAL_DATE_TIME, but want to add hour
            DateTimeFormatter inputFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
            LocalDateTime dateTime = LocalDateTime.parse(text, inputFormatter);

            dateStr = dateTime
                    .withHour(hour)
                    .withMinute(0)
                    .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return dateStr;
    }

    public static String ADDRESS_XLS_CRIME_HOUSTON(Map<String, String> row) {
        String address = null;
        try {
            String streetNo = row.get("StreetNo");
            String suffix = row.get("Suffix");
            String streetName = row.get("StreetName");
            String streetType = row.get("StreetType");
            String city = row.get("City");
            String zipcode = row.get("ZIPCode");

            address = Stream.of(streetNo, suffix, streetName, streetType, city, zipcode)
                    .filter(s -> s != null && !s.isEmpty())
                    .collect(Collectors.joining(" "));

        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return address;
    }

}
