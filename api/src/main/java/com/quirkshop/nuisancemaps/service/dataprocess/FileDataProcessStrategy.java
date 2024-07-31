package com.quirkshop.nuisancemaps.service.dataprocess;

import java.io.InputStream;

import com.quirkshop.nuisancemaps.model.DataJob;
import com.quirkshop.nuisancemaps.service.dataparser.DataParser;

import org.springframework.stereotype.Service;

@Service
public class FileDataProcessStrategy implements DataProcessStrategy {

    @Override
    public InputStream fetchData(DataJob dataJob) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void process(DataJob dataJob, InputStream inputStream, DataParser dataParser) {
        // TODO Auto-generated method stub
        // save stream to disk:
        // check free space
        // set file, filename
        // write to file

        // parse
    }

}
