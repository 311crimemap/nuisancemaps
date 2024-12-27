package com.quirkshop.nuisancemaps.service;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;

import com.quirkshop.nuisancemaps.WorkerApplication;
import com.quirkshop.nuisancemaps.config.DataProcessType;
import com.quirkshop.nuisancemaps.model.Source;
import com.quirkshop.nuisancemaps.model.datajob.DataJob;
import com.quirkshop.nuisancemaps.model.datajob.DataJobStatus;
import com.quirkshop.nuisancemaps.repository.DataJobRepository;
import com.quirkshop.nuisancemaps.repository.LocaleCategoryMinMaxReportedAtRepository;
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
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;

@Service
public class WorkerScheduleService {

    @Autowired
    LocaleCategoryMinMaxReportedAtRepository localeCategoryMinMaxReportedAtRepository;

    @Autowired
    SourceLoaderService sourceLoaderService;

    @Autowired
    DataJobService dataJobService;

    @Autowired
    DataJobRepository dataJobRepository;

    @Autowired
    SourceRepository sourceRepository;

    @Autowired
    DataParserFactory dataParserFactory;

    @Autowired
    DataProcessStrategyFactory dataProcessStrategyFactory;

    private static final Logger log = LoggerFactory.getLogger(WorkerApplication.class);

    @PostConstruct // method called once after beans all loaded
    public void initialize() throws UnsupportedEncodingException {
        // init seed
        // log.info("Init");
    }

    /*
     * SCHEDULED TASKS
     */

    // schedule every 12 hours, initial 5 min delay (avoid initial hanging on deploy, restarts)
    @Scheduled(fixedRate = 4 * 60 * 60 * 1000, initialDelay = 5 * 60 * 1000)
    @Transactional
    public void refreshMaterializedView() {
        log.info("Materialized view start refresh");
        localeCategoryMinMaxReportedAtRepository.refreshMaterializedView();
        log.info("Materialized view end refresh");
    }

    @Async("asyncExecutor")
    @Scheduled(fixedDelay = 3500, initialDelay = 3000)
    public void checkDataJobQueue() throws UnsupportedEncodingException {
        String currentThreadName = Thread.currentThread().getName();
        // log.info("[checkDataJobQueue] " + currentThreadName);

        // check for any DataJobStatus.POLL_WAIT from LocaleDateTime ago
        dataJobService.resetElapsedPollWait(LocalDateTime.now().minusSeconds(10));

        // GET / CREATE NEXT JOB
        DataJob datajob = dataJobService.getNextDataJob(DataJobStatus.QUEUED);
        if (datajob == null) {

            // log.info("No Jobs Queued");
            dataJobService.createNewJobs();
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

        // CLEANUP
        datajob.setStatus(DataJobStatus.CLEANUP);
        dataJobRepository.save(datajob);
        dataProcessStrategy.cleanup(datajob);

        // COMPLETED
        datajob.setStatus(DataJobStatus.COMPLETED);
        dataJobRepository.save(datajob);
        String logDone = String.format("[checkDataJobQueue] %s | fetched: %s | processed: %s",
                logDetails,
                datajob.getNumFetched(),
                datajob.getNumProcessed());
        log.info(logDone);
    }

}
