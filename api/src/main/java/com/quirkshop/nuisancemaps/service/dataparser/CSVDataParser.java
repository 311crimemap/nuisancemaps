package com.quirkshop.nuisancemaps.service.dataparser;

import com.fasterxml.jackson.databind.JsonNode;
import com.quirkshop.nuisancemaps.model.DataJob;
import com.quirkshop.nuisancemaps.model.Source;

import org.springframework.stereotype.Service;

@Service
public class CSVDataParser implements DataParser {

    @Override
    public void parse(DataJob dataJob) {
        // TODO Auto-generated method stub
    }

    @Override
    public JsonNode parseData(Source source, DataJob dataJob, String jsonResponse) {
        // TODO Auto-generated method stub
        return null;
    }


}
