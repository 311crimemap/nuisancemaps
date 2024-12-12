package com.quirkshop.nuisancemaps.service.dataprocess;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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

            // preProcessFilePath = preProcessXLS(dataJob, preProcessFilePath,
            // enableProcess);
            // System.out.println("PREPROCESSXLS: " + preProcessFilePath);

        } catch (Exception e) {
            // on error return null
            return null;

        }

        return preProcessFilePath;

    }

    // unzip
    public String preProcessZIP(DataJob dataJob, String inputFilePath, boolean enableProcess)
            throws MalformedURLException, IOException {
        String outputFilePath = inputFilePath;
        Source source = dataJob.getSource();
        DataParserType dataParserType = source.getDataParserType();

        // determine zipfile with extension and added query stuff param
        if (!(inputFilePath.contains("zipfile")))
            return inputFilePath;

        // filename is listed as stuffed query parameter ZIPFILE_PARAM
        // check if there is a ZIPFILE_PARAM file to extract

        String url = dataJob.getUrl();
        String zipFile = UriComponentsBuilder.fromUriString(url)
                .build()
                .getQueryParams()
                .getFirst(ZIPFILE_PARAM);

        if (zipFile == null)
            return inputFilePath;

        // e.g host.com/test/xyz.zip?zipfile=2024.csv ->
        // host.com-test-xvz.zip-zipfile_2024.csv

        // filename from source
        String fileName = dataJob.buildFilename();
        String extension = "-" + zipFile;
        outputFilePath = String.join("/", FETCH_DATA_DIR, fileName) + extension;

        if (!enableProcess)
            return outputFilePath;

        // unzip to csv
        try (ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(inputFilePath))) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {

                if (entry.getName().equals(zipFile)) {

                    File outputFile = new File(outputFilePath);

                    // Ensure parent directories are created
                    File parentDir = outputFile.getParentFile();
                    if (parentDir != null && !parentDir.exists()) {
                        parentDir.mkdirs();
                    }

                    try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(outputFile))) {
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = zipInputStream.read(buffer)) != -1) {
                            bos.write(buffer, 0, bytesRead);
                        }
                    }

                    log.info("Extracted: " + outputFile.getAbsolutePath());
                    return outputFilePath; // Stop after extracting the specific file
                }
            }
        } catch (Exception e) {
            // Log
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
            // .xls or .xlsz
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
