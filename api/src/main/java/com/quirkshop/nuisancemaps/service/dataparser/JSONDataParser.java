package com.quirkshop.nuisancemaps.service.dataparser;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Iterables;
import com.quirkshop.nuisancemaps.config.MissingCoordinateException;
import com.quirkshop.nuisancemaps.config.MissingReportCategoryException;
import com.quirkshop.nuisancemaps.model.Category;
import com.quirkshop.nuisancemaps.model.Data311;
import com.quirkshop.nuisancemaps.model.DataCrime;
import com.quirkshop.nuisancemaps.model.DataError;
import com.quirkshop.nuisancemaps.model.DataJob;
import com.quirkshop.nuisancemaps.model.DataJobStatus;
import com.quirkshop.nuisancemaps.model.IDataEntity;
import com.quirkshop.nuisancemaps.model.Source;
import com.quirkshop.nuisancemaps.repository.Data311Repository;
import com.quirkshop.nuisancemaps.repository.DataCrimeRepository;
import com.quirkshop.nuisancemaps.repository.DataErrorRepository;
import com.quirkshop.nuisancemaps.repository.IDataEntityRepository;
import com.quirkshop.nuisancemaps.service.DataEntityMappingService;
import com.quirkshop.nuisancemaps.service.DataService;
import com.quirkshop.nuisancemaps.service.TextCategoryService;
import com.quirkshop.nuisancemaps.util.ParseCounter;

import org.apache.commons.lang3.StringUtils;
import org.jsfr.json.JsonSurfer;
import org.jsfr.json.JsonSurferJackson;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class JSONDataParser implements DataParser {

    @Autowired
    private DataCrimeRepository datacrimeRepo;

    @Autowired
    private Data311Repository data311Repo;

    @Autowired
    private DataErrorRepository dataErrorRepository;

    @Autowired
    private TextCategoryService textCategoryService;

    @Autowired
    private DataEntityMappingService dataEntityMappingService;

    private final int SRID = 4326; // spatial reference id
    private static final Logger log = LoggerFactory.getLogger(DataService.class);

    // Types
    private Class<? extends IDataEntity> dataEntityClass;
    private IDataEntityRepository dataEntityRepository;

    @Override
    public void parse(DataJob dataJob, InputStream inputStream, ParseCounter parseCounter) {

        // build parseMap
        Source source = dataJob.getSource();
        HashMap<String, IDataEntity> parseNewDataMap = new HashMap<String, IDataEntity>();
        List<String> reportNums = new ArrayList<String>();

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), SRID);

        setTypes(source);

        textCategoryService.refreshTextCategoryIdMap();

        JsonSurfer surfer = JsonSurferJackson.INSTANCE;

        surfer.configBuilder()
                .bind("$[*]", (item, context) -> {

                    try {
                        IDataEntity dataEntity = dataEntityMappingService
                                .buildDataEntity(dataEntityClass, source, (JsonNode) item, geometryFactory);

                        String reportNum = dataEntity.getReportNum();

                        // skip case
                        Category orgCategory = dataEntity.getOrgCategory();
                        if (orgCategory != null &&
                                textCategoryService.lookupIsSkip(orgCategory.getId())) {

                            parseCounter.numSkippedIncrement();
                            parseCounter.numFetchedIncrement();

                        } else {

                            parseNewDataMap.put(reportNum, dataEntity);
                            reportNums.add(reportNum);

                            parseCounter.numBuiltIncrement();
                        }

                    } catch (MissingCoordinateException | MissingReportCategoryException e) {
                        String logStr = String.format("[DataService] error: %s | id: %s",
                                source.getDescription(), source.getId());

                        log.info(logStr);
                        sw.getBuffer().setLength(0);
                        e.printStackTrace(pw);

                        String content = StringUtils.substring(item.toString(), 0, 4096);

                        log.info(content);

                    } catch (Exception e) {
                        String logStr = String.format("[DataService] error: %s | id: %s",
                                source.getDescription(), source.getId());

                        log.info(logStr);
                        parseCounter.numErrorsIncrement();
                        sw.getBuffer().setLength(0);
                        e.printStackTrace(pw);

                        String error_msg = StringUtils.substring(sw.toString(), 0, 4096);
                        String content = StringUtils.substring(item.toString(), 0, 4096);

                        DataError dataError = new DataError(dataJob, content, error_msg);
                        dataErrorRepository.save(dataError);

                        log.info(content);
                    }

                    parseCounter.numFetchedIncrement();

                    if (reportNums.size() > 100) {

                        replaceWithNew(source, reportNums, parseCounter, parseNewDataMap);
                        saveAll(parseCounter, parseNewDataMap);

                        reportNums.clear();
                        parseNewDataMap.clear();
                    }

                })
                .buildAndSurf(inputStream);

        // flush remaining
        replaceWithNew(source, reportNums, parseCounter, parseNewDataMap);
        saveAll(parseCounter, parseNewDataMap);

        reportNums.clear();
        parseNewDataMap.clear();

    }

    public void replaceWithNew(Source source, List<String> reportNums, ParseCounter parseCounter,
            HashMap<String, IDataEntity> parseNewDataMap) {
        int numReplaced = 0;

        // query any existing
        List<IDataEntity> existing = dataEntityRepository
                .findAllBySourceIdAndReportNumIn(source.getId(), reportNums);

        // replace existing with new
        for (IDataEntity dataEntityDB : existing) {
            int id = dataEntityDB.getId();
            String reportNum = dataEntityDB.getReportNum();
            IDataEntity dNew = parseNewDataMap.getOrDefault(reportNum, null);
            if (dNew != null) {
                dNew.setId(id); // set id to overwrite
                numReplaced++;
            }
        }

        parseCounter.setNumReplace(parseCounter.getNumReplaced() + numReplaced);
        parseCounter.setNumDuplicates(parseCounter.getNumDuplicates() + existing.size());
    }

    public void saveAll(ParseCounter parseCounter, HashMap<String, IDataEntity> parseNewDataMap) {
        Iterable<IDataEntity> i = dataEntityRepository
                .saveAllEntities(parseNewDataMap.values());

        int numSaved = Iterables.size(i);
        parseCounter.setNumProcessed(numSaved);
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

    // DEPRECATEED
    @Override
    public JsonNode parseData(DataJob dataJob, InputStream inputStream) {

        JsonNode rootNode = null; // rootNode reads entire tree in memory

        try {
            ObjectMapper mapper = new ObjectMapper();
            rootNode = mapper.readTree(inputStream);
        } catch (Exception e) {
            log.info("[createData:parseData] JSON Parsing Error");
            e.printStackTrace();
            log.info(String.format("[parseData ERR]: %s", e.getMessage()));
            dataJob.setStatus(DataJobStatus.PARSE_ERROR);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return rootNode;
    }

}
