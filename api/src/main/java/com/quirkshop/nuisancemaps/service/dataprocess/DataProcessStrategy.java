package com.quirkshop.nuisancemaps.service.dataprocess;

import java.io.InputStream;

import com.quirkshop.nuisancemaps.model.DataJob;
import com.quirkshop.nuisancemaps.service.dataparser.DataParser;

public interface DataProcessStrategy {

    public InputStream fetchData(DataJob dataJob);

    public void process(DataJob dataJob, InputStream inputStream, DataParser dataParser);

}
