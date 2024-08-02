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
public class JSONDataParser extends DataParser {

    public void parse(DataJob dataJob, InputStream inputStream, ParseCounter parseCounter) {
        Source source = dataJob.getSource();
        setTypes(source);

        textCategoryService.refreshTextCategoryIdMap();

        JsonSurfer surfer = JsonSurferJackson.INSTANCE;

        surfer.configBuilder()
                .bind("$[*]", (item, context) -> {

                    try {
                        IDataEntity dataEntity = dataEntityMappingService
                                .buildDataEntity(dataEntityClass, source, (JsonNode) item, geometryFactory);

                        addDataEntity(dataEntity, parseCounter);

                    } catch (MissingCoordinateException | MissingReportCategoryException e) {
                        String content = StringUtils.substring(item.toString(), 0, 4096);
                        logMissingException(source, content, e);

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

    // DEPRECATEED
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
