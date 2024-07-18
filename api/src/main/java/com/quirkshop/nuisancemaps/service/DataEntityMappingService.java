package com.quirkshop.nuisancemaps.service;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
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
import com.quirkshop.nuisancemaps.model.Mapping;
import com.quirkshop.nuisancemaps.config.MissingCategoryException;
import com.quirkshop.nuisancemaps.config.MissingCoordinateException;
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

    public IDataEntity buildDataEntity(Class<? extends IDataEntity> dataEntityClass, Source source, JsonNode item,
            GeometryFactory geometryFactory)
            throws NoSuchMethodException, IllegalAccessException, InstantiationException, InvocationTargetException,
            MissingCategoryException, MissingCoordinateException {

        Mapping mapping = source.getMapping();
        String report_num = item.at(mapping.getReportNum()).asText();
        String reportCategory = item.at(mapping.getReportCategory()).asText();
        String description = item.at(mapping.getDescription()).asText();
        String location = item.at(mapping.getLocation()).asText();

        String lat = item.at(mapping.getLatitude()).asText();
        String lng = item.at(mapping.getLongitude()).asText();
        // isValueNode
        // isContainerNode

        String reported_at1 = item.at(mapping.getReportedAt()).asText();
        String reported_at2 = item.at(mapping.getReportedAt2()).asText();

        Double latitude = lat.isEmpty() ? null : Double.parseDouble(lat);
        Double longitude = lng.isEmpty() ? null : Double.parseDouble(lng);
        Coordinate coordinate = null;
        Point point = null;

        if (!lat.isEmpty() && !lng.isEmpty()) {
            // GeoJSON/WKT is long, lat (order is "reversed").
            coordinate = new Coordinate(longitude, latitude);
            point = geometryFactory.createPoint(coordinate);
        } else {
            String errString = String.format(
                    "Missing coordinates: (lat: %s, lng: %s) | dataType: %s, source: %s - %s | sourceURL: %s", lat, lng,
                    source.getCategory(), source.getSourceConfigId(), source.getSourceConfigEntity(),
                    source.getUrl());
            throw new MissingCoordinateException(errString);
        }

        // categories clarification
        // source.category: crime / 311 / etc
        // dataEntity.report_category: data report instance from raw data
        // Category: our created, labeled categories
        Category orgCategory = textCategoryService.lookupCategory(source.getCategory(), reportCategory);
        if (orgCategory == null) {
            String errString = String.format("Missing category: %s | dataType: %s, source: %s - %s | sourceURL: %s",
                    reportCategory, source.getCategory(), source.getSourceConfigId(), source.getSourceConfigEntity(),
                    source.getUrl());
            throw new MissingCategoryException(errString);
        }

        LocalDateTime reported_at = reported_at1.isEmpty() ? LocalDateTime.parse(reported_at2)
                : LocalDateTime.parse(reported_at1);

        IDataEntity dataEntity = dataEntityClass.getConstructor(Source.class).newInstance(source);

        dataEntity.setReportNum(report_num);
        dataEntity.setReportCategory(reportCategory);
        dataEntity.setDescription(description.isEmpty() ? null : description);
        dataEntity.setLocation(location.isEmpty() ? null : location);
        dataEntity.setOrgCategory(orgCategory);
        dataEntity.setLatitude(latitude);
        dataEntity.setLongitude(longitude);
        dataEntity.setPoint(point);
        dataEntity.setReportedAt(reported_at);
        dataEntity.setUpdatedAt(LocalDateTime.now());

        return dataEntity;
    }

}
