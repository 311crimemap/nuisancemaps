package com.quirkshop.nuisancemaps.service.dataprocess;

import java.io.InputStream;

import com.quirkshop.nuisancemaps.model.DataJob;
import com.quirkshop.nuisancemaps.service.dataparser.DataParser;

import org.springframework.stereotype.Service;

@Service
public class FileDataProcessStrategy implements DataProcessStrategy {

    @Override
    public String fetch(DataJob datajob) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public InputStream fetchData(DataJob dataJob) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void process(DataJob dataJob, InputStream inputStream, DataParser dataParser) {
        // TODO Auto-generated method stub

    }

}
