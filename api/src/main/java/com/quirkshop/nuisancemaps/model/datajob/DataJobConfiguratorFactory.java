package com.quirkshop.nuisancemaps.model.datajob;

import org.springframework.stereotype.Component;

@Component
public class DataJobConfiguratorFactory {

    public static DataJobConfigurator create(DataJobConfiguratorType type) {

        switch (type) {
            case BASE:
                return new BaseConfigurator();

            case OPENDATA:
                return new OpenDataConfigurator();

            case ERSI:
                return new ERSIConfigurator();

            case APDINCIDENTREPORT:
                return new APDIncidentReportConfigurator();

            default:
                throw new Error("Missing Implementation");
        }

    }
}
