package com.quirkshop.nuisancemaps.service;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;

import com.quirkshop.nuisancemaps.WorkerApplication;
import com.quirkshop.nuisancemaps.config.DataParserType;
import com.quirkshop.nuisancemaps.model.Source;
import com.quirkshop.nuisancemaps.model.datajob.DataJob;
import com.quirkshop.nuisancemaps.model.datajob.DataJobStatus;
import com.quirkshop.nuisancemaps.repository.DataJobRepository;
import com.quirkshop.nuisancemaps.repository.SourceRepository;
import com.quirkshop.nuisancemaps.service.dataparser.DataParser;
import com.quirkshop.nuisancemaps.service.dataparser.DataParserFactory;
import com.quirkshop.nuisancemaps.service.dataprocess.DataProcessStrategy;
import com.quirkshop.nuisancemaps.service.dataprocess.DataProcessStrategyFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Service
public class WorkerScheduleService {

    @Autowired
    SourceLoaderService sourceLoaderService;

    @Autowired
    DataJobRepository dataJobRepository;

    @Autowired
    SourceRepository sourceRepository;

    @Autowired
    DataParserFactory dataParserFactory;

    @Autowired
    DataProcessStrategyFactory dataProcessStrategyFactory;

    private static final int PARAM_LIMIT = Integer.parseInt(System.getenv("WORKER_QUERY_LIMIT"));

    private static final Logger log = LoggerFactory.getLogger(WorkerApplication.class);

    @PostConstruct // method called once after beans all loaded
    public void initialize() throws UnsupportedEncodingException {
        // init seed
        log.info("Init");
    }

    /*
     * SCHEDULED TASKS
     */

    @Async("asyncExecutor")
    @Scheduled(fixedDelay = 2000, initialDelay = 3000)
    public void checkDataJobQueue() throws UnsupportedEncodingException {
        String currentThreadName = Thread.currentThread().getName();
        // log.info("[checkDataJobQueue] " + currentThreadName);

        // GET / CREATE NEXT JOB
        DataJob datajob = dataJobRepository.getNextDataJob(DataJobStatus.QUEUED);
        if (datajob == null) {

            // log.info("No Jobs Queued");
            createNewJobs();
            return;
        }

        Source source = datajob.getSource();
        String prefixLog = String.format("%s | dataJob: %s | %s - %s",
                currentThreadName,
                datajob.getId(),
                source.getCategory(),
                source.getDescription());

        String logDetails = String.format("%s | offset: %s | %s",
                prefixLog, datajob.getParamOffset(), datajob.getUrl());

        log.info(String.format("[Fetching] %s", logDetails));

        // FETCH
        DataProcessStrategy dataProcessStrategy = dataProcessStrategyFactory
                .getDataProcessStrategy(source.getDataProcessType());

        InputStream inputStream = dataProcessStrategy.fetchData(datajob);

        if (datajob.getStatus() == DataJobStatus.FETCH_ERROR) {
            log.info(String.format("[FetchError] %s", logDetails));
            dataJobRepository.save(datajob);
            return;
        }

        // CREATE RECORDS
        log.info(String.format("[FetchComplete] %s", logDetails));
        datajob.setStatus(DataJobStatus.PENDING);
        dataJobRepository.save(datajob);

        log.info(String.format("createData() %s", prefixLog));

        DataParser dataParser = dataParserFactory
                .getDataParser(source.getDataParserType());

        dataProcessStrategy.process(datajob, inputStream, dataParser);

        // if high error rate, mark job as error and stop future jobs
        if (datajob.getStatus() == DataJobStatus.ERROR ||
                datajob.getStatus() == DataJobStatus.PARSE_ERROR) {

            dataJobRepository.save(datajob);
            String logError = String.format("[checkDataJobQueue] ERROR | %s | Done: %s | fetched: %s | processed: %s",
                    currentThreadName, datajob.getId(), datajob.getNumFetched(), datajob.getNumProcessed());
            log.info(logError);
            return;
        }

        datajob.setStatus(DataJobStatus.COMPLETED);
        dataJobRepository.save(datajob);
        String logDone = String.format("[checkDataJobQueue] %s | fetched: %s | processed: %s",
                logDetails,
                datajob.getNumFetched(),
                datajob.getNumProcessed());
        log.info(logDone);

        // Fetch Num records on initial session
        if (datajob.getParamOffset() == 0) {
            updateSourceNumRecords(source);
        }
    }

    public void createNewJobs() throws UnsupportedEncodingException {

        Iterable<Source> sources = sourceRepository.findAll();

        for (Source source : sources) {

            DataJob nextJob = dataJobRepository.createNextDataJob(source, PARAM_LIMIT);
            if (nextJob == null)
                continue;

            String logStr = String.format("[createNewJobs] param limit: %s | next job: %s",
                    PARAM_LIMIT,
                    nextJob.getUrl());

            log.info(logStr);
        }

    }

    /*
     * Source numRecords
     */

    public void updateSourceNumRecords(Source source) {

        if (source.getDataParserType().equals(DataParserType.CSV))
            return;

        log.info("[SourceLoaderService] FetchCount ....");

        Integer numRecords = sourceLoaderService.fetchCount(source);
        if (numRecords == null) {
            String logErr = String.format("[SourceLoaderService] FetchCount Error for Source: %s | id :%s ",
                    source.getDescription(),
                    source.getId());
            log.info(logErr);
            return;
        }

        String updateNumRecords = String.format("[SourceLoaderService] FetchCount %s -> %s",
                source.getNumRecords(),
                numRecords);
        log.info(updateNumRecords);

        if (numRecords != null) {
            source.setNumRecords(numRecords);
            source.setUpdatedAt(LocalDateTime.now());
            sourceRepository.save(source);
        }
    }

}
