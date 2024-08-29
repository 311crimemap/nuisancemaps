package com.quirkshop.nuisancemaps.service.dataparser;

import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import com.google.common.collect.Iterables;
import com.quirkshop.nuisancemaps.model.Category;
import com.quirkshop.nuisancemaps.model.Data311;
import com.quirkshop.nuisancemaps.model.DataCrime;
import com.quirkshop.nuisancemaps.model.DataError;
import com.quirkshop.nuisancemaps.model.DataEntity;
import com.quirkshop.nuisancemaps.model.Source;
import com.quirkshop.nuisancemaps.model.Locale;
import com.quirkshop.nuisancemaps.model.PendingTextCategory;
import com.quirkshop.nuisancemaps.model.datajob.DataJob;
import com.quirkshop.nuisancemaps.repository.Data311Repository;
import com.quirkshop.nuisancemaps.repository.DataCrimeRepository;
import com.quirkshop.nuisancemaps.repository.DataErrorRepository;
import com.quirkshop.nuisancemaps.repository.PendingTextCategoryRepository;
import com.quirkshop.nuisancemaps.repository.DataEntityRepository;
import com.quirkshop.nuisancemaps.service.DataEntityMappingService;
import com.quirkshop.nuisancemaps.service.TextCategoryService;
import com.quirkshop.nuisancemaps.util.ParseCounter;

import org.apache.commons.lang3.StringUtils;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class DataParser {
    @Autowired
    protected DataCrimeRepository datacrimeRepo;

    @Autowired
    protected Data311Repository data311Repo;

    @Autowired
    protected DataErrorRepository dataErrorRepository;

    @Autowired
    protected TextCategoryService textCategoryService;

    @Autowired
    protected PendingTextCategoryRepository pendingTextCategoryRepository;

    @Autowired
    protected DataEntityMappingService dataEntityMappingService;

    protected final int BATCH_SIZE = Integer.parseInt(System.getenv("BATCH_SIZE"));
    protected final int SRID = 4326; // spatial reference id
    protected static final Logger log = LoggerFactory.getLogger(DataParser.class);

    protected StringWriter sw = new StringWriter();
    protected PrintWriter pw = new PrintWriter(sw);
    protected GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), SRID);

    // Types
    protected Class<? extends DataEntity> dataEntityClass;
    protected DataEntityRepository<? extends DataEntity> dataEntityRepository;

    HashMap<String, DataEntity> parseNewDataMap = new HashMap<String, DataEntity>();
    List<String> reportNums = new ArrayList<String>();

    public void parse(DataJob dataJob, InputStream inputStream, ParseCounter parseCounter) {
        throw new Error("Missing Implementation");
    }

    /*
     * Common Helpers
     */

    public void addDataEntity(DataEntity dataEntity, ParseCounter parseCounter) {

        String reportNum = dataEntity.getReportNum();

        // skip case
        Category orgCategory = dataEntity.getOrgCategory();
        if (orgCategory != null &&
                textCategoryService.lookupIsSkip(orgCategory.getId())) {

            parseCounter.numSkippedIncrement();

        } else {

            parseNewDataMap.put(reportNum, dataEntity);
            reportNums.add(reportNum);

            parseCounter.numBuiltIncrement();
        }

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

    public void logMissingException(Source source, String content, Exception e) {
        String logStr = String.format("[DataParser] error: %s | %s | id: %s", e.getClass(),
                source.getDescription(), source.getId());

        // log.info(logStr);
        // sw.getBuffer().setLength(0);
        // e.printStackTrace(pw);
        // log.info(content);
    }

    public void logException(DataJob dataJob, String content, Exception e) {
        Source source = dataJob.getSource();
        String logStr = String.format("[DataParser] error: %s | %s | id: %s", e.getClass(),
                source.getDescription(), source.getId());

        log.info(logStr);
        sw.getBuffer().setLength(0);
        e.printStackTrace(pw);

        String error_msg = StringUtils
                .substring(String.join(" - ", logStr, sw.toString()),
                        0, 4096);

        DataError dataError = new DataError(dataJob, content, error_msg);
        dataErrorRepository.save(dataError);

        log.info(content);
        log.info(error_msg);
    }

    public void batchSave(Source source, ParseCounter parseCounter) {
        String logStr = String.format("[DataParser:batchSave ] sourceId: %s | numSaved: %d",
                source.getId(),
                parseNewDataMap.size());

        replaceWithNew(source, reportNums, parseCounter, parseNewDataMap);
        saveAll(parseCounter, parseNewDataMap);

        log.info(logStr);

        reportNums.clear();
        parseNewDataMap.clear();
    }

    private void replaceWithNew(Source source, List<String> reportNums, ParseCounter parseCounter,
            HashMap<String, DataEntity> parseNewDataMap) {
        int numReplaced = 0;

        Locale locale = source.getLocale();

        // query any existing in shared locale
        // allows for different data sources (source_id) contributing to same area
        List<? extends DataEntity> existing = dataEntityRepository
                .findAllBySource_Locale_IdAndReportNumIn(locale.getId(), reportNums);

        // replace existing with new
        for (DataEntity dataEntityDB : existing) {
            int id = dataEntityDB.getId();
            String reportNum = dataEntityDB.getReportNum();
            DataEntity dNew = parseNewDataMap.getOrDefault(reportNum, null);

            // info preservation

            // For each attribute, preserve and use old value if new value
            // becomes null
            Field[] fields = DataEntity.class.getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                try {
                    Object newValue = field.get(dNew);
                    Object oldValue = field.get(dataEntityDB);

                    if (newValue == null && oldValue != null) {
                        field.set(dNew, oldValue);
                    }

                } catch (Exception e) {
                    log.info("[DataParser] field err: " + e.getMessage());
                }
            }

            if (dNew != null) {
                dNew.setId(id); // set id to overwrite
                numReplaced++;
            }
        }

        parseCounter.setNumReplace(parseCounter.getNumReplaced() + numReplaced);
        parseCounter.setNumDuplicates(parseCounter.getNumDuplicates() + existing.size());
    }

    private void saveAll(ParseCounter parseCounter, HashMap<String, DataEntity> parseNewDataMap) {
        Iterable<DataEntity> i = dataEntityRepository
                .saveAllEntities(parseNewDataMap.values());

        int numProcessed = Iterables.size(i);
        parseCounter.setNumProcessed(parseCounter.getNumProcessed() + numProcessed);
    }

    protected void savePendingTextCategories(DataJob dataJob, Source source, HashSet<String> pendingReportCategories) {

        List<PendingTextCategory> pendingTextCategories = new ArrayList<PendingTextCategory>();

        for (String reportCategory : pendingReportCategories) {
            PendingTextCategory ptc = new PendingTextCategory(dataJob, source.getCategory(), reportCategory);
            pendingTextCategories.add(ptc);
        }

        try {

            if (pendingTextCategories.size() > 0) {
                log.info("[DataParser] savePendingTextCategories() num: " + pendingTextCategories.size());
                pendingTextCategoryRepository.saveAll(pendingTextCategories);
            }

        } catch (Exception e) {
            log.error("[DataParser] " + e.getMessage());
        }
    }

}
