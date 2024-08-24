package com.quirkshop.nuisancemaps.model.datajob;

public class DataJobParametersFactory {

    public static DataJobParameters create(DataJobParametersType type) {

        switch (type) {
            case BASE:
                return new BaseParameters();

            case OPENDATA:
                return new OpenDataParameters();

            case ERSI:
                return new ERSIParameters();

            case APDINCIDENTREPORT:
                return new APDIncidentReportParameters();

            default:
                throw new Error("Missing Implementation");
        }

    }
}
