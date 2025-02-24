package com.quirkshop.nuisancemaps.service.dataprocess;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.quirkshop.nuisancemaps.WorkerApplication;
import com.quirkshop.nuisancemaps.config.DataParserType;
import com.quirkshop.nuisancemaps.model.Source;
import com.quirkshop.nuisancemaps.model.datajob.DataJob;
import com.quirkshop.nuisancemaps.model.datajob.DataJobStatus;
import com.quirkshop.nuisancemaps.repository.DataJobRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import net.logstash.logback.argument.StructuredArguments;

@Component
public class FileDataPreProcessor {

    private static final String ZIPFILE_PARAM = "zipfile";
    private static final String FETCH_DATA_DIR = System.getenv("FETCH_DATA_DIR");
    private static final Logger log = LoggerFactory.getLogger(WorkerApplication.class);

    @Autowired
    private DataJobRepository dataJobRepository;

    /**
     * Preprocesses the given DataJob by building the filename, updating status,
     * and invoking ZIP preprocessing.
     *
     * @param dataJob       the DataJob to preprocess
     * @param enableProcess flag indicating whether to perform the preprocessing
     * @return the path of the preprocessed file, or null if an error occurs
     */

    public String preProcess(DataJob dataJob, boolean enableProcess) {

        String preProcessFilePath = null;

        try {
            String filename = dataJob.buildFilename();
            String filePath = String.join("/", FETCH_DATA_DIR, filename);

            dataJob.setStatus(DataJobStatus.PREPROCESS);
            dataJobRepository.save(dataJob);

            preProcessFilePath = preProcessZIP(dataJob, filePath, enableProcess);

            // TODO: chain any other preprocess steps
            // preProcessFilePath = preProcessXYZ(dataJob, preProcessFilePath,

        } catch (Exception e) {
            return null;
        }

        return preProcessFilePath;

    }

    /**
     * Extracts specific file from ZIP archive from the given DataJob.
     *
     * @param dataJob       the DataJob with details of .zip file
     * @param inputFilePath the path of zip file
     * @param enableProcess flag indicating whether to perform extraction
     *
     * @return the output file path after extraction (or input path if no
     *         extraction)
     *
     * @throws MalformedURLException if the URL is malformed
     * @throws IOException           if an I/O error occurs during processing
     */
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

        Map<String, Object> logDetails = Map.of("inputFilePath", inputFilePath);
        log.info("[preProcessZip] Start extraction",
                StructuredArguments.entries(Map.of("data", logDetails)));

        // unzip to outputFile
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

                    log.info("[preProcessZip] Extracted",
                            StructuredArguments.entries(Map.of("data", Map.of("path", outputFile.getAbsolutePath()))));

                    return outputFilePath; // Stop after extracting the specific file
                }
            }
        } catch (Exception e) {
            Map<String, Object> logErr = Map.of("URL", dataJob.getUrl(),
                    "inputFilePath", inputFilePath);

            log.error("[preProcessZip] ERR Extracted",
                    StructuredArguments.entries(Map.of("data", logErr)));
        }

        return outputFilePath;
    }

    /**
     * Cleans up resources associated with a given DataJob by deleting the
     * preprocessed file if it exists.
     *
     * @param dataJob
     */

    public void cleanup(DataJob dataJob) {

        try {
            String preProcessFilePath = preProcess(dataJob, false);

            if (preProcessFilePath == null)
                return;

            File file = new File(preProcessFilePath);
            if (file.exists()) {
                log.info("[preProcessZip] Deleting",
                        StructuredArguments.entries(Map.of("data", Map.of("filePath", preProcessFilePath))));
                file.delete();
            }

        } catch (Exception e) {
            e.printStackTrace();
            dataJob.setStatus(DataJobStatus.PREPROCESS_CLEANUP_ERROR);
            dataJobRepository.save(dataJob);
        }

    }

}
