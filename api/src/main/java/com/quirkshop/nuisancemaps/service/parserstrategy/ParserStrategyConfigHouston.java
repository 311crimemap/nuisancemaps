package com.quirkshop.nuisancemaps.service.parserstrategy;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

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

}
