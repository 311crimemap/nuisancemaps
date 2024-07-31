package com.quirkshop.nuisancemaps.service.dataparser;
import java.io.InputStream;

import com.fasterxml.jackson.databind.JsonNode;

import com.quirkshop.nuisancemaps.model.DataJob;
import com.quirkshop.nuisancemaps.util.ParseCounter;

public interface DataParser {

    public void parse(DataJob dataJob, InputStream inputStream, ParseCounter parseCounter);

    public JsonNode parseData(DataJob dataJob, InputStream inputStream);

}
