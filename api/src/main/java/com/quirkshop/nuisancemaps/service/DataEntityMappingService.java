package com.quirkshop.nuisancemaps.service;

import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;

import com.quirkshop.nuisancemaps.config.MissingCategoryException;
import com.quirkshop.nuisancemaps.config.MissingCoordinateException;
import com.quirkshop.nuisancemaps.config.MissingReportCategoryException;
import com.quirkshop.nuisancemaps.model.Category;
import com.quirkshop.nuisancemaps.model.IDataEntity;
import com.quirkshop.nuisancemaps.model.Mapping;
import com.quirkshop.nuisancemaps.model.Source;
import com.quirkshop.nuisancemaps.service.dataparser.FieldExtractor;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DataEntityMappingService {

    @Autowired
    private TextCategoryService textCategoryService;

    public <T> IDataEntity buildDataEntity(Class<? extends IDataEntity> dataEntityClass, Source source,
            T item,
            GeometryFactory geometryFactory,
            FieldExtractor<T> extractor)
            throws NoSuchMethodException, IllegalAccessException, InstantiationException, InvocationTargetException,
            MissingCategoryException, MissingReportCategoryException, MissingCoordinateException {

        String report_num = extractor.extract(Mapping::getReportNum, source, item);
        String reportCategory = extractor.extract(Mapping::getReportCategory, source, item);
        String description = extractor.extract(Mapping::getDescription, source, item);
        String address = extractor.extract(Mapping::getAddress, source, item);
        String location = extractor.extract(Mapping::getLocation, source, item);

        String lat = extractor.extract(Mapping::getLatitude, source, item);
        String lng = extractor.extract(Mapping::getLongitude, source, item);

        String reported_at1 = extractor.extract(Mapping::getReportedAt, source, item);
        String reported_at2 = extractor.extract(Mapping::getReportedAt2, source, item);

        Double latitude = (lat == null || lat.isEmpty()) ? null : Double.parseDouble(lat);
        Double longitude = (lng == null || lng.isEmpty()) ? null : Double.parseDouble(lng);

        // format
        reportCategory = formatString(reportCategory);
        location = formatString(location);

        // validate
        validateReportCategory(source, reportCategory);

        Point point = buildValidPoint(source, geometryFactory, latitude, longitude);

        Category orgCategory = textCategoryService.lookupCategory(source.getCategory(), reportCategory);
        validateCategory(source, orgCategory, reportCategory);

        LocalDateTime reported_at = reported_at1.isEmpty() ? LocalDateTime.parse(reported_at2)
                : LocalDateTime.parse(reported_at1);

        IDataEntity dataEntity = dataEntityClass.getConstructor(Source.class).newInstance(source);

        setDataEntityFields(dataEntity, report_num, reportCategory, description, address, location,
                orgCategory, latitude, longitude, point, reported_at);

        return dataEntity;

    }

    /*
     * helpers
     */

    // consistent format for Strings:
    // - remove any nbsp;
    // - collapse any additional spaces like html
    //
    // For address, location and reportCategory: used in other lookups /
    // "caches" so ensure consistent match is important (location / Geocode,
    // reportCategory / TextCategory)
    private String formatString(String input) {
        if (input == null)
            return input;

        return input
                .replaceAll("\u00A0", " ")
                .replaceAll("\\s+", " ");
    }

    private void setDataEntityFields(IDataEntity dataEntity, String report_num, String reportCategory,
            String description, String address, String location, Category orgCategory,
            Double latitude, Double longitude, Point point, LocalDateTime reported_at) {
        dataEntity.setReportNum(report_num);
        dataEntity.setReportCategory(reportCategory);
        dataEntity.setDescription(description);
        dataEntity.setAddress(address);
        dataEntity.setLocation(location);
        dataEntity.setOrgCategory(orgCategory);
        dataEntity.setLatitude(latitude);
        dataEntity.setLongitude(longitude);
        dataEntity.setPoint(point);
        dataEntity.setReportedAt(reported_at);
        dataEntity.setUpdatedAt(LocalDateTime.now());
    }

    private void validateReportCategory(Source source, String reportCategory) throws MissingReportCategoryException {
        if (reportCategory == null || reportCategory.isEmpty()) {
            String errString = String.format(
                    "Missing reportCategory | dataType: %s | id: %s | %s | sourceURL: %s",
                    source.getCategory(), source.getSourceConfigId(), source.getDescription(), source.getUrl());
            throw new MissingReportCategoryException(errString);
        }

    }

    private Point buildValidPoint(Source source, GeometryFactory geometryFactory, Double latitude, Double longitude)
            throws MissingCoordinateException {
        Point point = null;

        if (latitude != null && longitude != null) {
            // GeoJSON/WKT is long, lat (order is "reversed").
            Coordinate coordinate = new Coordinate(longitude, latitude);
            point = geometryFactory.createPoint(coordinate);
        } else {
            String errString = String
                    .format("Missing coordinates: (lat: %s, lng: %s) | dataType: %s | id: %s | %s | sourceURL: %s",
                            latitude, longitude, source.getCategory(), source.getSourceConfigId(),
                            source.getDescription(),
                            source.getUrl());
            throw new MissingCoordinateException(errString);
        }

        return point;
    }

    // categories clarification nomenclature
    //
    // source.category: crime / 311 / etc
    // dataEntity.report_category: data report instance from raw data
    // Category: our created, labeled categories
    private Category validateCategory(Source source, Category category, String reportCategory)
            throws MissingCategoryException {

        if (category == null) {
            String errString = String
                    .format("Missing category: %s | dataType: %s | id: %s | %s | sourceURL: %s",
                            reportCategory,
                            source.getCategory(), source.getSourceConfigId(), source.getDescription(), source.getUrl());
            throw new MissingCategoryException(errString);
        }
        return null;
    }
}
