package com.quirkshop.nuisancemaps.service.parserstrategy;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import com.quirkshop.nuisancemaps.WorkerApplication;
import com.quirkshop.nuisancemaps.model.MappingField;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * Multi
 */

public class ParserStrategyConfigMulti {

    private static final Logger log = LoggerFactory.getLogger(WorkerApplication.class);

    // 05/11/2023 07:56:33 AM
    // 01/09/2022 01:18:38 AM
    public static String REPORTED_AT_CSV_MMddyyyyhhmmssa(Map<String, String> row, MappingField mappingField) {
        String dateStr = null;
        try {
            String field = mappingField.getField();
            String text = row.get(field);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm:ss a");
            DateTimeFormatter outputFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
            dateStr = LocalDateTime.parse(text, formatter).format(outputFormatter);
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return dateStr;
    }

}
