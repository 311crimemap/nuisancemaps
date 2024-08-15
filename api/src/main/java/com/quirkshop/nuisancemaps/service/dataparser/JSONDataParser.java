package com.quirkshop.nuisancemaps.service.dataparser;

import java.io.InputStream;

import com.fasterxml.jackson.databind.JsonNode;
import com.quirkshop.nuisancemaps.config.MissingCoordinateException;
import com.quirkshop.nuisancemaps.config.MissingReportCategoryException;
import com.quirkshop.nuisancemaps.model.DataJob;
import com.quirkshop.nuisancemaps.model.IDataEntity;
import com.quirkshop.nuisancemaps.model.Source;
import com.quirkshop.nuisancemaps.util.ParseCounter;

import org.apache.commons.lang3.StringUtils;
import org.jsfr.json.JsonSurfer;
import org.jsfr.json.JsonSurferJackson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope("prototype")
public class JSONDataParser extends DataParser {

    @Autowired
    JSONNodeFieldExtractor jsonNodeFieldExtractor;

    @Override
    public void parse(DataJob dataJob, InputStream inputStream, ParseCounter parseCounter) {
        Source source = dataJob.getSource();
        setTypes(source);

        textCategoryService.refreshTextCategoryIdMap();

        JsonSurfer surfer = JsonSurferJackson.INSTANCE;

        surfer.configBuilder()
                .bind("$[*]", (item, context) -> {

                    try {

                        IDataEntity dataEntity = dataEntityMappingService
                            .buildDataEntity(dataEntityClass, source, (JsonNode) item, geometryFactory, jsonNodeFieldExtractor);

                        addDataEntity(dataEntity, parseCounter);

                    } catch (MissingCoordinateException | MissingReportCategoryException e) {
                        String content = StringUtils.substring(item.toString(), 0, 4096);
                        logMissingException(source, content, e);
                        parseCounter.numMissingIncrement();

                    } catch (Exception e) {
                        String content = StringUtils.substring(item.toString(), 0, 4096);
                        logException(dataJob, content, e);
                        parseCounter.numErrorsIncrement();
                    }

                    parseCounter.numFetchedIncrement();

                    if (reportNums.size() > BATCH_SIZE) {
                        batchSave(source, parseCounter);
                    }

                })
                .buildAndSurf(inputStream);

        // flush remaining
        batchSave(source, parseCounter);
    }

}
