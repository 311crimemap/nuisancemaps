package com.quirkshop.nuisancemaps.service.dataparser;

import java.io.File;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Iterables;
import com.quirkshop.nuisancemaps.model.Category;
import com.quirkshop.nuisancemaps.model.Data311;
import com.quirkshop.nuisancemaps.model.DataCrime;
import com.quirkshop.nuisancemaps.model.DataEntity;
import com.quirkshop.nuisancemaps.model.DataError;
import com.quirkshop.nuisancemaps.model.PendingTextCategory;
import com.quirkshop.nuisancemaps.model.Source;
import com.quirkshop.nuisancemaps.model.datajob.DataJob;
import com.quirkshop.nuisancemaps.repository.Data311Repository;
import com.quirkshop.nuisancemaps.repository.DataCrimeRepository;
import com.quirkshop.nuisancemaps.repository.DataEntityRepository;
import com.quirkshop.nuisancemaps.repository.DataErrorRepository;
import com.quirkshop.nuisancemaps.repository.PendingTextCategoryRepository;
import com.quirkshop.nuisancemaps.service.DataEntityMappingService;
import com.quirkshop.nuisancemaps.service.TextCategoryService;
import com.quirkshop.nuisancemaps.util.ParseCounter;

import org.apache.commons.lang3.StringUtils;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import net.logstash.logback.argument.StructuredArguments;

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

    public void parse(DataJob dataJob, File file, InputStream inputStream, ParseCounter parseCounter) {
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

        Map<String, Object> logDetails = Map.of(
                "id", source.getId(),
                "errorClass", e.getClass(),
                "description", source.getDescription(),
                "content", content);

        String message = "[DataParser] ERR";
        log.error(message, StructuredArguments.entries(Map.of("data", logDetails)));

        sw.getBuffer().setLength(0);
        e.printStackTrace(pw);

        String error_msg = StringUtils
                .substring(String.join(" - ", message, logDetails.toString(), sw.toString()),
                        0, 4096);

        DataError dataError = new DataError(dataJob, content, error_msg);
        dataErrorRepository.save(dataError);
    }

    public void batchSave(Source source, ParseCounter parseCounter) {
        replaceWithNew(source, reportNums, parseCounter, parseNewDataMap);
        Map<String, Object> logDetails = Map.of(
                "sourceId", source.getId(),
                "numSaved", parseNewDataMap.size());
        saveAll(parseCounter, parseNewDataMap);
        log.info("[DataParser:batchSave]", StructuredArguments.entries(Map.of("data", logDetails)));

        reportNums.clear();
        parseNewDataMap.clear();
    }

    private void replaceWithNew(Source source, List<String> reportNums, ParseCounter parseCounter,
            HashMap<String, DataEntity> parseNewDataMap) {
        int numReplaced = 0;

        // Duplicate identity must match the database constraint. A report number can
        // legitimately exist in another source within the same locale.
        List<? extends DataEntity> existing = dataEntityRepository
                .findAllBySourceIdAndReportNumIn(source.getId(), reportNums);

        Map<String, DataEntity> existingByReportNum = new HashMap<>();
        for (DataEntity dataEntityDB : existing) {
            existingByReportNum.put(dataEntityDB.getReportNum(), dataEntityDB);
        }

        // Keep the persisted entity for duplicates. This preserves fields missing
        // from the feed and, crucially, lets unchanged rows be omitted from saveAll.
        for (Map.Entry<String, DataEntity> entry : new ArrayList<>(parseNewDataMap.entrySet())) {
            DataEntity dataEntityDB = existingByReportNum.get(entry.getKey());
            if (dataEntityDB != null) {
                if (copyChangedFields(entry.getValue(), dataEntityDB)) {
                    parseNewDataMap.put(entry.getKey(), dataEntityDB);
                    numReplaced++;
                } else {
                    parseNewDataMap.remove(entry.getKey());
                }
            }
        }

        parseCounter.setNumReplace(parseCounter.getNumReplaced() + numReplaced);
        parseCounter.setNumDuplicates(parseCounter.getNumDuplicates() + existing.size());
    }

    private boolean copyChangedFields(DataEntity incoming, DataEntity existing) {
        boolean changed = false;

        changed |= copyIfPresent(incoming.getReportCategory(), existing.getReportCategory(), existing::setReportCategory);
        changed |= copyIfPresent(incoming.getDescription(), existing.getDescription(), existing::setDescription);
        changed |= copyIfPresent(incoming.getAddress(), existing.getAddress(), existing::setAddress);
        changed |= copyIfPresent(incoming.getLocation(), existing.getLocation(), existing::setLocation);
        changed |= copyCategoryIfPresent(incoming.getOrgCategory(), existing.getOrgCategory(), existing::setOrgCategory);
        changed |= copyIfPresent(incoming.getLatitude(), existing.getLatitude(), existing::setLatitude);
        changed |= copyIfPresent(incoming.getLongitude(), existing.getLongitude(), existing::setLongitude);
        changed |= copyIfPresent(incoming.getPoint(), existing.getPoint(), existing::setPoint);
        changed |= copyIfPresent(incoming.getReportedAt(), existing.getReportedAt(), existing::setReportedAt);

        return changed;
    }

    private <T> boolean copyIfPresent(T incoming, T existing, java.util.function.Consumer<T> setter) {
        if (incoming != null && !Objects.equals(incoming, existing)) {
            setter.accept(incoming);
            return true;
        }
        return false;
    }

    private boolean copyCategoryIfPresent(Category incoming, Category existing,
            java.util.function.Consumer<Category> setter) {
        if (incoming != null && (existing == null || !Objects.equals(incoming.getId(), existing.getId()))) {
            setter.accept(incoming);
            return true;
        }
        return false;
    }

    private void saveAll(ParseCounter parseCounter, HashMap<String, DataEntity> parseNewDataMap) {
        Iterable<DataEntity> i = dataEntityRepository
                .saveAllEntities(parseNewDataMap.values());

        int numProcessed = Iterables.size(i);
        parseCounter.setNumProcessed(parseCounter.getNumProcessed() + numProcessed);
    }

    protected void savePendingTextCategories(DataJob dataJob, Source source, HashSet<String> pendingReportCategories) {
        List<String> pending = new ArrayList<String>(pendingReportCategories);

        Set<String> existingTextCategories = pendingTextCategoryRepository
                .findByDataTypeAndTextIn(source.getCategory(), pending)
                .stream()
                .map(PendingTextCategory::getText)
                .collect(Collectors.toSet());

        // filter out existing and save only new
        List<PendingTextCategory> pendingTextCategories = new ArrayList<>();

        for (String reportCategory : pendingReportCategories) {
            if (!existingTextCategories.contains(reportCategory)) {
                PendingTextCategory ptc = new PendingTextCategory(dataJob, source.getCategory(), reportCategory);
                pendingTextCategories.add(ptc);
            }
        }

        try {

            if (pendingTextCategories.size() > 0) {
                Map<String, Object> logDetails = Map.of("num", pendingTextCategories.size());
                log.info("[DataParser] savePendingTextCategories()",
                        StructuredArguments.entries(Map.of("data", logDetails)));
                pendingTextCategoryRepository.saveAll(pendingTextCategories);
            }

        } catch (Exception e) {
            log.error("[DataParser] ",
                    StructuredArguments.entries(Map.of("data", Map.of("error", e.getMessage()))));
        }
    }

}
