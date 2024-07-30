package com.quirkshop.nuisancemaps.service.dataparser;
import com.fasterxml.jackson.databind.JsonNode;

import com.quirkshop.nuisancemaps.model.DataJob;
import com.quirkshop.nuisancemaps.model.Source;

public interface DataParser {

    public void parse(DataJob dataJob);

    public JsonNode parseData(Source source, DataJob dataJob, String jsonResponse);

}
