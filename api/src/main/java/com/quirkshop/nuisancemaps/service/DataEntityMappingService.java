package com.quirkshop.nuisancemaps.service;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;

import org.apache.commons.lang3.StringUtils;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Iterables;
import com.quirkshop.nuisancemaps.model.IDataEntity;
import com.quirkshop.nuisancemaps.model.Locale;
import com.quirkshop.nuisancemaps.model.Mapping;
import com.quirkshop.nuisancemaps.model.MappingField;
import com.quirkshop.nuisancemaps.config.MissingCategoryException;
import com.quirkshop.nuisancemaps.config.MissingCoordinateException;
import com.quirkshop.nuisancemaps.config.MissingReportCategoryException;
import com.quirkshop.nuisancemaps.config.ParserStrategy;
import com.quirkshop.nuisancemaps.model.Category;
import com.quirkshop.nuisancemaps.model.Data311;
import com.quirkshop.nuisancemaps.model.DataCrime;
import com.quirkshop.nuisancemaps.model.DataError;
import com.quirkshop.nuisancemaps.model.DataJob;
import com.quirkshop.nuisancemaps.model.DataJobStatus;
import com.quirkshop.nuisancemaps.model.Source;
import com.quirkshop.nuisancemaps.repository.Data311Repository;
import com.quirkshop.nuisancemaps.repository.DataCrimeRepository;
import com.quirkshop.nuisancemaps.repository.DataErrorRepository;
import com.quirkshop.nuisancemaps.repository.IDataEntityRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.springframework.stereotype.Service;

@Service
public class DataEntityMappingService {
    private final Map<ParserStrategy, Function<JsonNode, String>> parsingFunctions;

    @Autowired
    public DataEntityMappingService(Map<ParserStrategy, Function<JsonNode, String>> parsingFunctions) {
        this.parsingFunctions = parsingFunctions;
    }

    @Autowired
    private TextCategoryService textCategoryService;

    public String parseNode(JsonNode item, ParserStrategy strategy) {
        Function<JsonNode, String> parser = parsingFunctions.get(strategy);
        if (parser != null) {
            return parser.apply(item);
        }
        return null;
    }

    /*
     * hierarchy of parse methods
     * 1. vanilla string (simple getter method returns String; not MappingField)
     * 2. ParserStrategy method: if this is defined, prioritize it's use
     * 3. default MappingField pointer (json parse 'path expression')
     */
    public String parseEntity(Function<Mapping, ?> mapper, Source source, JsonNode item)
            throws NoSuchMethodException, SecurityException {

        Method method = mapper.getClass().getMethod("apply", Object.class);
        Class<?> returnType = method.getReturnType();

        Object mappedValue = mapper.apply(source.getMapping());
        String value = null;

        // Vanilla String
        if (returnType == String.class) {
            return (String) mappedValue;
        }

        // Parsing Strategy method if exists, otherwise use Pointer expression
        MappingField result = (MappingField) mappedValue;

        if (result == null)
            return null;

        if (result.getParsingStrategy() != null) {
            ParserStrategy strategy = ParserStrategy.valueOf(result.getParsingStrategy());
            value = parseNode(item, strategy);
        } else {
            value = item.at(result.getPointer()).asText();
        }

        return value;
    }

    public IDataEntity buildDataEntity(Class<? extends IDataEntity> dataEntityClass, Source source, JsonNode item,
            GeometryFactory geometryFactory)
            throws NoSuchMethodException, IllegalAccessException, InstantiationException, InvocationTargetException,
            MissingCategoryException, MissingReportCategoryException, MissingCoordinateException {
        Locale locale = source.getLocale();

        String report_num = parseEntity(Mapping::getReportNum, source, item);
        String reportCategory = parseEntity(Mapping::getReportCategory, source, item);
        String description = parseEntity(Mapping::getDescription, source, item);
        String location = parseEntity(Mapping::getLocation, source, item);

        String lat = parseEntity(Mapping::getLatitude, source, item);
        String lng = parseEntity(Mapping::getLongitude, source, item);

        String reported_at1 = parseEntity(Mapping::getReportedAt, source, item);
        String reported_at2 = parseEntity(Mapping::getReportedAt2, source, item);

        Double latitude = (lat == null || lat.isEmpty()) ? null : Double.parseDouble(lat);
        Double longitude = (lng == null || lng.isEmpty()) ? null : Double.parseDouble(lng);
        Coordinate coordinate = null;
        Point point = null;

        if (reportCategory == null || reportCategory.isEmpty()) {
            String errString = String.format(
                    "Missing reportCategory | dataType: %s | id: %s | %s | sourceURL: %s",
                    source.getCategory(), source.getSourceConfigId(), source.getDescription(), source.getUrl());
            throw new MissingReportCategoryException(errString);
        }

        if (latitude != null && longitude != null) {
            // GeoJSON/WKT is long, lat (order is "reversed").
            coordinate = new Coordinate(longitude, latitude);
            point = geometryFactory.createPoint(coordinate);
        } else {
            String errString = String.format(
                    "Missing coordinates: (lat: %s, lng: %s) | dataType: %s | id: %s | %s | sourceURL: %s", lat, lng,
                    source.getCategory(), source.getSourceConfigId(), source.getDescription(), source.getUrl());
            throw new MissingCoordinateException(errString);
        }

        // categories clarification
        // source.category: crime / 311 / etc
        // dataEntity.report_category: data report instance from raw data
        // Category: our created, labeled categories
        Category orgCategory = textCategoryService.lookupCategory(source.getCategory(), reportCategory);
        if (orgCategory == null) {
            String errString = String.format("Missing category: %s | dataType: %s | id: %s | %s | sourceURL: %s", reportCategory,
                    source.getCategory(), source.getSourceConfigId(), source.getDescription(), source.getUrl());
            throw new MissingCategoryException(errString);
        }

        LocalDateTime reported_at = reported_at1.isEmpty() ? LocalDateTime.parse(reported_at2)
                : LocalDateTime.parse(reported_at1);

        IDataEntity dataEntity = dataEntityClass.getConstructor(Source.class).newInstance(source);

        dataEntity.setReportNum(report_num);
        dataEntity.setReportCategory(reportCategory);
        dataEntity.setDescription(description);
        dataEntity.setLocation(location);
        dataEntity.setOrgCategory(orgCategory);
        dataEntity.setLatitude(latitude);
        dataEntity.setLongitude(longitude);
        dataEntity.setPoint(point);
        dataEntity.setReportedAt(reported_at);
        dataEntity.setUpdatedAt(LocalDateTime.now());

        return dataEntity;
    }

}
