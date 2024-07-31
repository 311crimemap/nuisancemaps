package com.quirkshop.nuisancemaps.service.dataparser;
import java.io.InputStream;

import com.fasterxml.jackson.databind.JsonNode;

import com.quirkshop.nuisancemaps.model.DataJob;

public interface DataParser {

    public void parse(DataJob dataJob, InputStream inputStream);

    public JsonNode parseData(DataJob dataJob, InputStream inputStream);

}
