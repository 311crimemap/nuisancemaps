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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Iterables;
import com.quirkshop.nuisancemaps.model.IDataEntity;
import com.quirkshop.nuisancemaps.model.Mapping;
import com.quirkshop.nuisancemaps.config.MissingCategoryException;
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

    public List<Map<String, Object>> parseData(Source source, DataJob dataJob, String jsonResponse) {

        List<Map<String, Object>> responseList = null;

        try {
            responseList = objectMapper.readValue(jsonResponse,
                    new TypeReference<List<Map<String, Object>>>() {
                    });
        } catch (JsonMappingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            log.info("[CreateData] Parsing Error");
            e.printStackTrace();
            dataJob.setStatus(DataJobStatus.PARSE_ERROR);
        }

        return responseList;
    }

    public void createData(Source source, DataJob dataJob, String jsonResponse) {

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), SRID);

        List<Map<String, Object>> responseList = parseData(source, dataJob, jsonResponse);
        int numFetched = responseList == null ? 0 : responseList.size();
        dataJob.setNumFetched(numFetched);
        if (dataJob.getStatus() == DataJobStatus.PARSE_ERROR)
            return;

        // if 0 but not last of dataset, something awry
        if (numFetched == 0 &&
                (dataJob.getParamOffset() + dataJob.getParamLimit() >= source.getNumRecords())) {
            dataJob.setStatus(DataJobStatus.ERROR);
            return;
        }

        setTypes(source);

        createDataEntities(dataJob, source, responseList, geometryFactory, sw, pw);
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

    public void createDataEntities(DataJob dataJob, Source source, List<Map<String, Object>> responseList,
            GeometryFactory geometryFactory, StringWriter sw, PrintWriter pw) {
        int numFetched = 0;
        int numSkipped = 0;
        int numBuilt = 0;
        int numProcessed = 0;
        int errors = 0;

        // build parseMap
        Mapping mapping = source.getMapping();
        HashMap<String, IDataEntity> parseNewDataMap = new HashMap<String, IDataEntity>();
        List<String> report_nums = new ArrayList<String>(responseList.size());

        // refresh lookups TextCategoryIdMap
        textCategoryService.refreshTextCategoryIdMap();

        for (Map<String, Object> responseObject : responseList) {

            try {
                String report_num = responseObject.get(mapping.getReportNum()).toString();

                IDataEntity dataEntity = buildDataEntity(source, responseObject, geometryFactory);
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

            } catch (Exception e) {
                log.info("[DataService] createDataEntities error");
                errors++;
                e.printStackTrace(pw);

                String error_msg = StringUtils.substring(sw.toString(), 0, 255);
                String content = StringUtils.substring(responseObject.toString(), 0, 255);

                DataError dataError = new DataError(dataJob, content, error_msg);
                dataErrorRepository.save(dataError);

                log.info(content);
                log.info(error_msg);
            }

            numFetched++;
        }

        // query any existing
        List<IDataEntity> existing = dataEntityRepository.findAllBySourceIdAndReportNumIn(source.getId(), report_nums);

        // replace existing with new
        for (IDataEntity dataEntityDB : existing) {
            int id = dataEntityDB.getId();
            String report_num = dataEntityDB.getReportNum();
            IDataEntity dNew = parseNewDataMap.getOrDefault(report_num, null);
            dNew.setId(id); // set id to overwrite
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

    public IDataEntity buildDataEntity(Source source, Map<String, Object> responseObject,
            GeometryFactory geometryFactory)
            throws NoSuchMethodException, IllegalAccessException, InstantiationException, InvocationTargetException,
            MissingCategoryException {

        Mapping mapping = source.getMapping();
        String report_num = responseObject.get(mapping.getReportNum()).toString();
        String reportCategory = responseObject.get(mapping.getReportCategory()).toString();
        String description = responseObject.getOrDefault(mapping.getDescription(), "").toString();
        String location = responseObject.getOrDefault(mapping.getLocation(), "").toString();
        String lat = responseObject.getOrDefault(mapping.getLatitude(), "").toString();
        String lng = responseObject.getOrDefault(mapping.getLongitude(), "").toString();
        String reported_at1 = responseObject.getOrDefault(mapping.getReportedAt(), "").toString();
        String reported_at2 = responseObject.getOrDefault(mapping.getReportedAt2(), "").toString();

        Double latitude = lat.isEmpty() ? null : Double.parseDouble(lat);
        Double longitude = lng.isEmpty() ? null : Double.parseDouble(lng);
        Coordinate coordinate = null;
        Point point = null;

        if (!lat.isEmpty() && !lng.isEmpty()) {
            // GeoJSON/WKT is long, lat (order is "reversed").
            coordinate = new Coordinate(longitude, latitude);
            point = geometryFactory.createPoint(coordinate);
        }

        LocalDateTime reported_at = reported_at1.isEmpty() ? LocalDateTime.parse(reported_at2)
                : LocalDateTime.parse(reported_at1);

        IDataEntity dataEntity = dataEntityClass.getConstructor(Source.class).newInstance(source);

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
