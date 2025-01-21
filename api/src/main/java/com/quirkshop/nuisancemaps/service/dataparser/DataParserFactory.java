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

    @Autowired
    private ObjectFactory<APDIncidentReportDataParser> apdHTMLDataParserFactory;

    @Autowired
    private ObjectFactory<XLSSAXDataParser> xlsDataParserFactory;

    @Autowired
    private ObjectFactory<CSVCustomDataParser> csvCustomDataParserFactory;

    public DataParser getDataParser(DataParserType dataParserType) {

        switch (dataParserType) {
            case CSV:
                return csvDataParserFactory.getObject();
            case JSON:
                return jsonDataParserFactory.getObject();
            case APDINCIDENTREPORT:
                return apdHTMLDataParserFactory.getObject();
            case XLS:
                return xlsDataParserFactory.getObject();
            case CSVCUSTOM:
                return csvCustomDataParserFactory.getObject();
            default:
                throw new IllegalArgumentException("Unsupported DataParserType: " + dataParserType);

        }
    }

}
