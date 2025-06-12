package com.quirkshop.nuisancemaps.service.parserstrategy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.quirkshop.nuisancemaps.WorkerApplication;
import com.quirkshop.nuisancemaps.model.MappingField;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParserStrategyConfigHouston {

    private static final Logger log = LoggerFactory.getLogger(WorkerApplication.class);

    // LocalDateTime.parse has ISO defaults that cannot handle nanosecond precision
    // 2024-12-12 06:11:02.0000000
    public static String REPORTED_AT_CSV_311_HOUSTON(Map<String, String> row, MappingField mappingField) {
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

    // 2021-01-01 00:40:43
    public static String REPORTED_AT_CSV2_311_HOUSTON(Map<String, String> row, MappingField mappingField) {
        String dateStr = null;
        try {
            String text = row.get("SR CREATE DATE");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            DateTimeFormatter outputFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
            dateStr = LocalDateTime.parse(text, formatter).format(outputFormatter);
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return dateStr;
    }

    // 1/1/24 or also possibly 1/1/2024 given excel variability
    // hour: 0
    public static String REPORTED_AT_XLS_CRIME_HOUSTON(Map<String, String> row, MappingField mappingField) {
        String dateStr = null;
        try {
            String text = row.get("Occurrence Date");
            Integer hour = Integer.parseInt(row.getOrDefault("Occurrence Hour", "0"));

            List<DateTimeFormatter> formatters = Arrays.asList(DateTimeFormatter.ofPattern("M/d/yy"),
                                                               DateTimeFormatter.ofPattern("M/d/yyyy"));

            LocalDate date = null;
            for (DateTimeFormatter inputFormatter : formatters) {
                try {
                    date = LocalDate.parse(text, inputFormatter);
                    break;
                } catch (Exception e) {

                }
            }


            LocalDateTime dateTime = date.atTime(hour, 0);
            dateStr = dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return dateStr;
    }

    public static String ADDRESS_XLS_CRIME_HOUSTON(Map<String, String> row, MappingField mappingField) {
        String address = null;
        try {
            String streetNo = row.get("No / Block Range");
            String suffix = row.get("Suffix");
            String streetName = row.get("Street Name");
            String streetType = row.get("Street Type");
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
