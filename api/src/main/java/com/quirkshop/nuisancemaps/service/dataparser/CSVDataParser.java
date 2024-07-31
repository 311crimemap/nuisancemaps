package com.quirkshop.nuisancemaps.service.dataparser;

import java.io.InputStream;

import com.fasterxml.jackson.databind.JsonNode;
import com.quirkshop.nuisancemaps.model.DataJob;
import com.quirkshop.nuisancemaps.model.Source;
import com.quirkshop.nuisancemaps.util.ParseCounter;

import org.springframework.stereotype.Service;

@Service
public class CSVDataParser implements DataParser {

    @Override
    public void parse(DataJob dataJob, InputStream inputStream, ParseCounter parseCounter) {
        // TODO Auto-generated method stub
    }

    @Override
    public JsonNode parseData(DataJob dataJob, InputStream inputStream) {
        // TODO Auto-generated method stub
        return null;
    }
}
