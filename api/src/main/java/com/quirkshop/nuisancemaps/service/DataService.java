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
import org.springframework.stereotype.Service;
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

@Service
public class DataService {

    private ObjectMapper objectMapper;
    private static final Logger log = LoggerFactory.getLogger(DataService.class);
    private final int ERROR_RATE = 5;
    private final int SRID = 4326; // spatial reference id

    @Autowired
    private DataCrimeRepository datacrimeRepo;

    @Autowired
    private Data311Repository data311Repo;

    @Autowired
    private DataErrorRepository dataErrorRepository;

    @Autowired
    private TextCategoryService textCategoryService;

    // Types
    private Class<? extends IDataEntity> dataEntityClass;
    private IDataEntityRepository dataEntityRepository;

    public DataService() {
        this.objectMapper = new ObjectMapper();
    }

    public JsonNode parseData(Source source, DataJob dataJob, String jsonResponse) {

        JsonNode rootNode = null;

        try {
            ObjectMapper mapper = new ObjectMapper();
            rootNode = mapper.readTree(jsonResponse);
        } catch (Exception e) {
            log.info("[CreateData] Parsing Error");
            e.printStackTrace();
            dataJob.setStatus(DataJobStatus.PARSE_ERROR);
        }

        return rootNode;
    }


    public void createData(Source source, DataJob dataJob, String jsonResponse) {

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), SRID);

        //List<Map<String, Object>> responseList = parseData(source, dataJob, jsonResponse);
        JsonNode rootNode = parseData(source, dataJob, jsonResponse);
        int numFetched = rootNode == null ? 0 : rootNode.size();
        dataJob.setNumFetched(numFetched);
        if (dataJob.getStatus() == DataJobStatus.PARSE_ERROR)
            return;

        setTypes(source);

        createDataEntities(dataJob, source, rootNode, geometryFactory, sw, pw);
    }

    public void setTypes(Source source) {
        switch (source.getCategory()) {
            case "crime":
                dataEntityRepository = datacrimeRepo;
                dataEntityClass = DataCrime.class;
                break;
            case "311":
                dataEntityRepository = data311Repo;
                dataEntityClass = Data311.class;
                break;
            default:
                break;
        }
    }

    public void createDataEntities(DataJob dataJob, Source source, JsonNode rootNode,
            GeometryFactory geometryFactory, StringWriter sw, PrintWriter pw) {
        int numFetched = 0;
        int numSkipped = 0;
        int numBuilt = 0;
        int numProcessed = 0;
        int errors = 0;

        // build parseMap
        Mapping mapping = source.getMapping();
        HashMap<String, IDataEntity> parseNewDataMap = new HashMap<String, IDataEntity>();
        List<String> report_nums = new ArrayList<String>(rootNode.size());

        // refresh lookups TextCategoryIdMap
        textCategoryService.refreshTextCategoryIdMap();

        for (JsonNode item : rootNode) {

            try {
                String report_num = item.at(mapping.getReportNum()).toString();

                IDataEntity dataEntity = buildDataEntity(source, item, geometryFactory);
                // skip case
                Category orgCategory = dataEntity.getOrgCategory();
                if (orgCategory != null &&
                        textCategoryService.lookupIsSkip(orgCategory.getId())) {
                    numSkipped++;
                    numFetched++;
                    continue;
                }

                parseNewDataMap.put(report_num, dataEntity);
                report_nums.add(report_num);
                numBuilt++;

            } catch(MissingCoordinateException e) {
                String logStr = String.format("[DataService] MissingCoordinate error: %s | id: %s",
                        source.getDescription(), source.getId());

                log.info(logStr);
                sw.getBuffer().setLength(0);
                e.printStackTrace(pw);

                String content = StringUtils.substring(item.toString(), 0, 4096);

                log.info(content);

            } catch (MissingCategoryException e) {
                String logStr = String.format("[DataService] MissingCategory error: %s | id: %s",
                        source.getDescription(), source.getId());

                log.info(logStr);
                errors++;
                sw.getBuffer().setLength(0);
                e.printStackTrace(pw);

                String error_msg = StringUtils.substring(sw.toString(), 0, 4096);
                String content = StringUtils.substring(item.toString(), 0, 4096);

                DataError dataError = new DataError(dataJob, content, error_msg);
                dataErrorRepository.save(dataError);

                log.info(content);

            } catch (Exception e) {
                String logStr = String.format("[DataService] createDataEntities error: %s | id: %s",
                        source.getDescription(), source.getId());

                log.info(logStr);
                errors++;
                // e.printStackTrace appends to sw
                // so only want most recent error
                sw.getBuffer().setLength(0);
                e.printStackTrace(pw);

                String error_msg = StringUtils.substring(sw.toString(), 0, 4096);
                String content = StringUtils.substring(item.toString(), 0, 4096);

                DataError dataError = new DataError(dataJob, content, error_msg);
                dataErrorRepository.save(dataError);

                log.info(content);
                log.info(error_msg);
            }

            numFetched++;
        }

        // query any existing
        List<IDataEntity> existing = dataEntityRepository
            .findAllBySourceIdAndReportNumIn(source.getId(), report_nums);

        // replace existing with new
        for (IDataEntity dataEntityDB : existing) {
            int id = dataEntityDB.getId();
            String report_num = dataEntityDB.getReportNum();
            IDataEntity dNew = parseNewDataMap.getOrDefault(report_num, null);
            if (dNew != null) {
                dNew.setId(id); // set id to overwrite
            }
        }

        // saveAll
        List<String> result = new ArrayList<String>();

        Iterable<IDataEntity> i = dataEntityRepository.saveAllEntities(parseNewDataMap.values());
        numProcessed = Iterables.size(i);

        setJobStatus(source, dataJob, errors, numFetched, numSkipped, numBuilt, numProcessed, existing.size());
    }

    public void setJobStatus(Source source, DataJob dataJob, int numErrors, int numFetched, int numSkipped,
            int numBuilt,
            int numProcessed,
            int numDuplicate) {

        // 5% error rate, mark job as failed to figure out consistent error
        if (numErrors > (numProcessed / ERROR_RATE))

        {
            dataJob.setStatus(DataJobStatus.ERROR);
        }

        dataJob.setNumFetched(numFetched);
        dataJob.setNumProcessed(numProcessed);
        String logStats = String.format(
                "%s - %s: | Offset: %s | Fetched: %s | Skipped: %s | Built: %s | Processed: %s | Errors: %s | Duplicates: %s",
                source.getCategory(), source.getDescription(), dataJob.getParamOffset(), numFetched, numSkipped,
                numBuilt,
                numProcessed, numErrors,
                numDuplicate);
        log.info(logStats);
    }

    public IDataEntity buildDataEntity(Source source, JsonNode item, GeometryFactory geometryFactory)
            throws NoSuchMethodException, IllegalAccessException, InstantiationException, InvocationTargetException,
            MissingCategoryException, MissingCoordinateException {

        Mapping mapping = source.getMapping();
        String report_num = item.at(mapping.getReportNum()).asText();
        String reportCategory = item.at(mapping.getReportCategory()).asText();
        String description = item.at(mapping.getDescription()).asText();
        String location = item.at(mapping.getLocation()).asText();
        String lat = item.at(mapping.getLatitude()).asText();
        String lng = item.at(mapping.getLongitude()).asText();
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
