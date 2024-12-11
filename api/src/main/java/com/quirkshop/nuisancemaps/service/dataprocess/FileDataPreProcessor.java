package com.quirkshop.nuisancemaps.service.dataprocess;

import java.io.File;
import java.net.MalformedURLException;

import com.quirkshop.nuisancemaps.WorkerApplication;
import com.quirkshop.nuisancemaps.config.DataParserType;
import com.quirkshop.nuisancemaps.model.Source;
import com.quirkshop.nuisancemaps.model.datajob.DataJob;
import com.quirkshop.nuisancemaps.repository.DataJobRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class FileDataPreProcessor {

    private static final String ZIPFILE_PARAM = "zipfile";
    private static final String FETCH_DATA_DIR = System.getenv("FETCH_DATA_DIR");
    private static final Logger log = LoggerFactory.getLogger(WorkerApplication.class);

    @Autowired
    private DataJobRepository dataJobRepository;

    public String preProcess(DataJob dataJob, boolean enableProcess) {

        String preProcessFilePath = null;

        try {
            String filename = dataJob.buildFilename();
            String filePath = String.join("/", FETCH_DATA_DIR, filename);

            preProcessFilePath = preProcessZIP(dataJob, filePath, enableProcess);

            preProcessFilePath = preProcessXLS(dataJob, preProcessFilePath, enableProcess);

        } catch (Exception e) {
            // on error return null
            return null;

        }

        return preProcessFilePath;

    }

    // unzip
    public String preProcessZIP(DataJob dataJob, String inputFilePath, boolean enableProcess)
            throws MalformedURLException {
        String outputFilePath = inputFilePath;
        Source source = dataJob.getSource();
        DataParserType dataParserType = source.getDataParserType();

        if (!inputFilePath.endsWith(".zip"))
            return inputFilePath;

        // filename is listed as stuffed query parameter ZIPFILE_PARAM
        // check if there is a ZIPFILE_PARAM file to extract

        String url = source.getUrl();
        String zipFile = UriComponentsBuilder.fromUriString(url)
                .build()
                .getQueryParams()
                .getFirst(ZIPFILE_PARAM);

        if (zipFile == null)
            return inputFilePath;

        if (dataParserType == DataParserType.CSV) {

            // filename

            if (!enableProcess)
                return outputFilePath;

            // unzip to csv

        }

        return outputFilePath;
    }

    // convert excel
    public String preProcessXLS(DataJob dataJob, String inputFilePath, boolean enableProcess) {
        String outputFilePath = inputFilePath;
        Source source = dataJob.getSource();
        DataParserType dataParserType = source.getDataParserType();

        if (!(inputFilePath.endsWith(".xls") || inputFilePath.endsWith(".xlsx")))
            return inputFilePath;

        if (dataParserType == DataParserType.CSV) {

            // filename

            if (!enableProcess)
                return outputFilePath;

            // if enableProcess
        }

        return outputFilePath;
    }

    public void cleanup(DataJob dataJob) {

        try {
            String preProcessFilePath = preProcess(dataJob, false);

            if (preProcessFilePath == null)
                return;

            File file = new File(preProcessFilePath);
            if (file.exists()) {
                log.info(String.format("Deleting: %s", preProcessFilePath));
                file.delete();
            }

        } catch (Exception e) {
            e.printStackTrace();
            // dataJob.setStatus(DataJobStatus.PREPROCESS_CLEANUP_ERROR);

            dataJobRepository.save(dataJob);
        }

    }

}
