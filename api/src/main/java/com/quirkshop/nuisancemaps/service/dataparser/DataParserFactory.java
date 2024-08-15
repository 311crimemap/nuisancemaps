package com.quirkshop.nuisancemaps.service.dataparser;

import com.quirkshop.nuisancemaps.config.DataParserType;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DataParserFactory {

    @Autowired
    private ObjectFactory<CSVDataParser> csvDataParserFactory;

    @Autowired
    private ObjectFactory<JSONDataParser> jsonDataParserFactory;

    public DataParser getDataParser(DataParserType dataParserType) {

        switch (dataParserType) {
        case CSV:
            return csvDataParserFactory.getObject();
        case JSON:
            return jsonDataParserFactory.getObject();

        default:
            throw new IllegalArgumentException("Unsupported DataParserType: " + dataParserType);

        }
    }

}
