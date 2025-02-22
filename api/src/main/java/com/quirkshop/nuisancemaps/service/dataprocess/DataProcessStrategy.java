package com.quirkshop.nuisancemaps.service.dataprocess;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import com.quirkshop.nuisancemaps.WorkerApplication;
import com.quirkshop.nuisancemaps.model.Source;
import com.quirkshop.nuisancemaps.model.datajob.DataJob;
import com.quirkshop.nuisancemaps.model.datajob.DataJobStatus;
import com.quirkshop.nuisancemaps.service.dataparser.DataParser;
import com.quirkshop.nuisancemaps.util.ParseCounter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.logstash.logback.argument.StructuredArguments;

public interface DataProcessStrategy {

    public static final Logger log = LoggerFactory.getLogger(WorkerApplication.class);

    /**
     * Fetches data from the specified URL in the provided DataJob, checking for
     * existing files and handling download logic.
     *
     * @param dataJob The DataJob containing the URL and associated metadata.
     * @return An InputStream to the fetched data, or null if fetch was not
     *         successful or data already exists.
     */
    public InputStream fetchData(DataJob dataJob);

    /**
     * Processes the data from the provided InputStream, given the DataJob and
     * specified DataParser. This process typically includes writing the
     * InputStream to memory, a file; any preprocessing steps and data
     * extraction.
     *
     * This method is expected to break into three main stepss:
     * 1. Stream Handling -> file, or noop
     * 2. Preprocessing (e.g. unzip)
     * 3. parse(): execute dataParser.parse() to create records
     *
     * Updates the data job status along the way.
     *
     * @param dataJob     The DataJob that contains details for processing.
     * @param inputStream The InputStream of data to process.
     * @param dataParser  The parser used to parse the data file after processing.
     */
    public void process(DataJob dataJob, InputStream inputStream, DataParser dataParser);

    /**
     * Cleans up resources associated with the specified DataJob.
     * Can include deleting temporary files created during processing.
     *
     * @param dataJob.
     */

    public void cleanup(DataJob dataJob);

    default void setJobStatus(Source source, DataJob dataJob, ParseCounter parseCounter) {

        final int ERROR_RATE = 5;

        // 5% error rate, mark job as failed to figure out consistent error
        if (parseCounter.getNumErrors() > (parseCounter.getNumProcessed() / ERROR_RATE))

        {
            dataJob.setStatus(DataJobStatus.ERROR);
        }

        dataJob.setNumFetched(parseCounter.getNumFetched());
        dataJob.setNumProcessed(parseCounter.getNumProcessed());

        HashMap<String, Object> logDetails = new HashMap<>();
        logDetails.put("Category", source.getCategory());
        logDetails.put("Description", source.getDescription());
        logDetails.put("Offset", dataJob.getParamOffset());
        logDetails.put("Fetched", parseCounter.getNumFetched());
        logDetails.put("RowErrors", parseCounter.getNumRowErrors());
        logDetails.put("Skipped", parseCounter.getNumSkipped());
        logDetails.put("Missing", parseCounter.getNumMissing());
        logDetails.put("Built", parseCounter.getNumBuilt());
        logDetails.put("Processed", parseCounter.getNumProcessed());
        logDetails.put("Errors", parseCounter.getNumErrors());
        logDetails.put("Duplicates", parseCounter.getNumDuplicates());

        log.info("[DataProcessStrategy] JobStatus",
                StructuredArguments.entries(Map.of("data", logDetails)));
    }

}
