package com.quirkshop.nuisancemaps.model.datajob;

public class DataJobURLFactory {

    public static DataJobURL create(DataJobURLType type) {

        switch (type) {
            case BASE:
                return new BaseURL();

            case OPENDATA:
                return new OpenDataURL();

            case ERSI:
                return new ERSIURL();

            case APDINCIDENTREPORT:
                return new APDIncidentReportURL();

            default:
                throw new Error("Missing Implementation");
        }

    }
}
