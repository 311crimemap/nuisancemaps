package com.quirkshop.nuisancemaps.service.dataprocess;

import java.io.InputStream;

import com.quirkshop.nuisancemaps.WorkerApplication;
import com.quirkshop.nuisancemaps.model.Source;
import com.quirkshop.nuisancemaps.model.datajob.DataJob;
import com.quirkshop.nuisancemaps.model.datajob.DataJobStatus;
import com.quirkshop.nuisancemaps.service.dataparser.DataParser;
import com.quirkshop.nuisancemaps.util.ParseCounter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface DataProcessStrategy {

    public static final Logger log = LoggerFactory.getLogger(WorkerApplication.class);

    public InputStream fetchData(DataJob dataJob);

    public void process(DataJob dataJob, InputStream inputStream, DataParser dataParser);

    default void setJobStatus(Source source, DataJob dataJob, ParseCounter parseCounter) {

        final int ERROR_RATE = 5;

        // 5% error rate, mark job as failed to figure out consistent error
        if (parseCounter.getNumErrors() > (parseCounter.getNumProcessed() / ERROR_RATE))

        {
            dataJob.setStatus(DataJobStatus.ERROR);
        }

        dataJob.setNumFetched(parseCounter.getNumFetched());
        dataJob.setNumProcessed(parseCounter.getNumProcessed());

        String logStats = String.format(
                "%s - %s: | Offset: %s | Fetched: %d | RowErrors: %d | Skipped: %d | Missing: %d | Built: %d | Processed: %d | Errors: %d | Duplicates: %d",
                source.getCategory(),
                source.getDescription(),
                dataJob.getParamOffset(),
                parseCounter.getNumFetched(),
                parseCounter.getNumRowErrors(),
                parseCounter.getNumSkipped(),
                parseCounter.getNumMissing(),
                parseCounter.getNumBuilt(),
                parseCounter.getNumProcessed(),
                parseCounter.getNumErrors(),
                parseCounter.getNumDuplicates());
        log.info(logStats);
    }

}
