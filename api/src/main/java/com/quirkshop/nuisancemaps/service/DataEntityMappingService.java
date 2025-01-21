package com.quirkshop.nuisancemaps.service;

import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;

import com.quirkshop.nuisancemaps.config.InvalidCoordinateException;
import com.quirkshop.nuisancemaps.config.MissingCategoryException;
import com.quirkshop.nuisancemaps.config.MissingCoordinateException;
import com.quirkshop.nuisancemaps.config.MissingReportCategoryException;
import com.quirkshop.nuisancemaps.config.ThresholdReportedAtException;
import com.quirkshop.nuisancemaps.model.Category;
import com.quirkshop.nuisancemaps.model.DataEntity;
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

    private final LocalDateTime REPORTED_AT_THRESHOLD = LocalDateTime.of(2020, 1, 1, 0, 0);

    @Autowired
    private TextCategoryService textCategoryService;

    /**
     * Constructs a DataEntity from provided item data.
     *
     * @param dataEntityClass the class of the DataEntity [Data311, DataCrime ]
     * @param source          the Source class instance
     * @param item            the item e.g. row (csv, json) containing the relevant
     *                        data
     * @param geometryFactory the factory for creating geometric points
     * @param extractor       mapper of field values from the item rows
     *
     * @return an instance of DataEntity populated with extracted data
     *
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws InvocationTargetException
     * @throws MissingCategoryException
     * @throws MissingReportCategoryException
     * @throws InvalidCoordinateException
     * @throws MissingCoordinateException
     * @throws ThresholdReportedAtException
     */
    public <T> DataEntity buildDataEntity(Class<? extends DataEntity> dataEntityClass, Source source,
            T item,
            GeometryFactory geometryFactory,
            FieldExtractor<T> extractor)
            throws NoSuchMethodException, IllegalAccessException, InstantiationException, InvocationTargetException,
            MissingCategoryException, MissingReportCategoryException,
            InvalidCoordinateException, MissingCoordinateException, ThresholdReportedAtException {

        String report_num = extractor.extract(Mapping::getReportNum, source, item);
        String reportCategory = extractor.extract(Mapping::getReportCategory, source, item);
        String description = extractor.extract(Mapping::getDescription, source, item);
        String address = extractor.extract(Mapping::getAddress, source, item);
        String location = extractor.extract(Mapping::getLocation, source, item);

        String lat = extractor.extract(Mapping::getLatitude, source, item);
        String lng = extractor.extract(Mapping::getLongitude, source, item);

        String reported_at1 = extractor.extract(Mapping::getReportedAt, source, item);
        if (reported_at1 == null || reported_at1.isEmpty())
            reported_at1 = null;

        String reported_at2 = extractor.extract(Mapping::getReportedAt2, source, item);
        if (reported_at2 == null || reported_at2.isEmpty())
            reported_at2 = null;

        Double latitude = (lat == null || lat.isEmpty()) ? null : Double.parseDouble(lat);
        Double longitude = (lng == null || lng.isEmpty()) ? null : Double.parseDouble(lng);

        // Proper String formatting
        reportCategory = formatString(reportCategory);
        address = formatString(address);
        location = formatString(location);

        // MissingReportCategoryException - empty
        validateReportCategory(source, reportCategory);

        // InvalidCoordinateException | MissingCoordinateException - invalid / empty
        Point point = buildValidPoint(source, geometryFactory, latitude, longitude);

        // MissingCategoryException - empty textCategory lookup
        Category textCategory = textCategoryService.lookupCategory(source.getCategory(), reportCategory);
        validateCategory(source, textCategory, reportCategory);

        LocalDateTime reported_at = reported_at1 == null
                ? (reported_at2 == null ? null : LocalDateTime.parse(reported_at2))
                : LocalDateTime.parse(reported_at1);

        // ThresholdReportedAtException
        validateReportedAt(reported_at);

        DataEntity dataEntity = dataEntityClass.getConstructor(Source.class).newInstance(source);

        setDataEntityFields(dataEntity, report_num, reportCategory, description, address, location,
                textCategory, latitude, longitude, point, reported_at);

        return dataEntity;

    }

    /*
     * Helpers
     */

    /**
     * Formats a string by removing non-breaking spaces and collapsing
     * additional spaces (html).
     *
     * For address, location and reportCategory: used in other lookups /
     * "caches" so ensure consistent match is important (location / Geocode,
     * reportCategory / TextCategory)
     *
     * @param input the string to format
     * @return the formatted string, or null if input is null
     */
    private String formatString(String input) {
        if (input == null)
            return input;

        return input
                .replaceAll("\u00A0", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private void setDataEntityFields(DataEntity dataEntity, String report_num, String reportCategory,
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

    private void validateReportedAt(LocalDateTime reportedAt) throws ThresholdReportedAtException {
        if (reportedAt.isBefore(REPORTED_AT_THRESHOLD)) {
            String logStr = String.format("reportedAt %s: prior to threshold date %s",
                    reportedAt.toString(),
                    REPORTED_AT_THRESHOLD.toString());
            throw new ThresholdReportedAtException(logStr);
        }
    }

    private void validateReportCategory(Source source, String reportCategory) throws MissingReportCategoryException {
        if (reportCategory == null || reportCategory.isEmpty()) {
            String errString = String.format(
                    "Missing reportCategory | dataType: %s | id: %s | %s | sourceURL: %s",
                    source.getCategory(), source.getSourceConfigId(), source.getDescription(), source.getUrl());
            throw new MissingReportCategoryException(errString);
        }

    }

    /**
     * Builds a valid geometric point from latitude and longitude, checking for
     * validity.
     *
     * @param source          the Source instance
     * @param geometryFactory the geometry factory used to create the point
     * @param latitude        the latitude to validate and convert to a point
     * @param longitude       the longitude to validate and convert to a point
     *
     * @return a valid geometric point
     *
     * @throws InvalidCoordinateException if the coordinates are out of valid bounds
     * @throws MissingCoordinateException if the coordinates are missing
     */
    private Point buildValidPoint(Source source, GeometryFactory geometryFactory, Double latitude, Double longitude)
            throws InvalidCoordinateException, MissingCoordinateException {
        Point point = null;

        if (latitude != null && longitude != null) {

            /*
             * Add sanity check for bad coordinate data - USA extent
             * Latitude: 18.91° N to 71.39° N
             * Longitude: (-) 172.90° W to (-) 66.95° W
             */
            if (latitude < 18 || latitude > 72 ||
                    longitude < -173 || longitude > -65) {
                String errString = String
                        .format("Invalid coordinates: (lat: %s, lng: %s) | dataType: %s | id: %s | %s | sourceURL: %s",
                                latitude, longitude, source.getCategory(), source.getSourceConfigId(),
                                source.getDescription(),
                                source.getUrl());
                throw new InvalidCoordinateException(errString);
            }

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

    /*
     * Categories clarification nomenclature:
     *
     * Source.category: crime / 311 / etc
     * DataEntity.report_category: data report instance from raw data
     * Category: our created, labeled categories
     */

    /**
     * Validates the existence of a category based on the provided source and report
     * category.
     *
     * @param source         the source of the data
     * @param category       the category to validate
     * @param reportCategory the associated report category
     *
     * @throws MissingCategoryException if the category is missing
     *
     * @return the validated category
     */
    private Category validateCategory(Source source, Category category, String reportCategory)
            throws MissingCategoryException {

        if (category == null) {
            String errString = String
                    .format("Missing category: %s | dataType: %s | id: %s | %s | sourceURL: %s",
                            reportCategory,
                            source.getCategory(), source.getSourceConfigId(), source.getDescription(), source.getUrl());
            throw new MissingCategoryException(reportCategory, errString);
        }
        return null;
    }
}
