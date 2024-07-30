package com.quirkshop.nuisancemaps.service.dataparser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.quirkshop.nuisancemaps.model.Source;
import com.quirkshop.nuisancemaps.service.DataService;
import com.quirkshop.nuisancemaps.model.DataJob;
import com.quirkshop.nuisancemaps.model.DataJobStatus;

import org.springframework.stereotype.Service;

@Service
public class JSONDataParser implements DataParser {

    private static final Logger log = LoggerFactory.getLogger(DataService.class);

    @Override
    public void parse(DataJob dataJob) {
        // TODO Auto-generated method stub
    }

    @Override
    public JsonNode parseData(Source source, DataJob dataJob, String jsonResponse) {

        JsonNode rootNode = null;

        try {
            ObjectMapper mapper = new ObjectMapper();
            rootNode = mapper.readTree(jsonResponse);
        } catch (Exception e) {
            log.info("[createData:parseData] JSON Parsing Error");
            e.printStackTrace();
            log.info(String.format("[parseData ERR]: %s",
                    jsonResponse != null ? jsonResponse.substring(0, 100) : null));
            dataJob.setStatus(DataJobStatus.PARSE_ERROR);
        }

        return rootNode;
    }

}
