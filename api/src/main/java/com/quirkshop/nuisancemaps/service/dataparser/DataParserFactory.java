package com.quirkshop.nuisancemaps.service.dataparser;

import com.quirkshop.nuisancemaps.config.DataParserType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DataParserFactory {

    @Autowired
    CSVDataParser csvDataParser;

    @Autowired
    JSONDataParser jsonDataParser;

    public DataParser getDataParser(DataParserType dataParserType) {

        switch (dataParserType) {
        case CSV:
            return csvDataParser;
        case JSON:
            return jsonDataParser;

        default:
            throw new IllegalArgumentException("Unsupported DataParserType: " + dataParserType);

        }
    }

}
