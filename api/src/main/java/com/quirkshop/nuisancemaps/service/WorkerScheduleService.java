package com.quirkshop.nuisancemaps.service;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.quirkshop.nuisancemaps.WorkerApplication;
import com.quirkshop.nuisancemaps.model.DataJob;
import com.quirkshop.nuisancemaps.model.DataJobStatus;
import com.quirkshop.nuisancemaps.model.Source;
import com.quirkshop.nuisancemaps.repository.DataJobRepository;
import com.quirkshop.nuisancemaps.repository.SourceRepository;

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
    DataJobRequestService dataJobRequestService;

    @Autowired
    DataService dataservice;

    private final int PARAM_LIMIT = 10000;

    private static final Logger log = LoggerFactory.getLogger(WorkerApplication.class);

    @PostConstruct // method called once after beans all loaded
    public void initialize() throws UnsupportedEncodingException {
        sourceLoaderService.loadJSON("data/source_config.json");
        log.info("loaded source_config.json");

        // init seed
        if (dataJobRepository.count() == 0) {
            log.info("Initial Seed Jobs");
            createDailyDataJobs();
        }
    }



    /*
     * SCHEDULED TASKS
     */

    @Scheduled(cron = "@daily")
    public void fetchAndUpdateNumSourceRecords() {
        HashMap<Integer, Source> sourceMap = sourceLoaderService.getSourceMap();

        for (Map.Entry<Integer, Source> entry : sourceMap.entrySet()) {

            Source source = sourceRepository.findOrCreate(entry.getValue());
            // NB: Single Lock
            LocalDateTime nowMinusHours = LocalDateTime.now().minusHours(1);
            boolean needsUpdate = sourceRepository.needsUpdateAndTouch(source, nowMinusHours);
            if (!needsUpdate)
                continue;

            updateSourceNumRecords(source);
        }
    }

    @Scheduled(cron = "@daily")
    public void createDailyDataJobs() throws UnsupportedEncodingException {
        log.info("[createDailyDataJob]");
        HashMap<Integer, Source> sourceMap = sourceLoaderService.getSourceMap();

        log.info("source_config.json num entries: " + sourceMap.size());

        for (Map.Entry<Integer, Source> entry : sourceMap.entrySet()) {

            // NB: Anticipate modifying mapping fields, so allowing for config change of
            // same Source entity
            Map<String, Object> mapping = sourceLoaderService.getSourceMapping(entry.getKey());
            Source source = sourceRepository.findOrCreate(entry.getValue());
            source.setMapping(mapping);

            // find the last dataJob: a previous empty result (DataJobStatus.COMPLETED), or
            // latest queued job (DataJobStatus.QUEUED)
            //
            // This is slightly different from createNewJobs(): we want to redo the last job
            // parameter offset because new records could be added the next day that are still within the
            // same fetch range
            LocalDateTime cutOffTime = LocalDateTime.now().minusHours(3);
            DataJob datajob = dataJobRepository.createLastDataJobBySource(source, PARAM_LIMIT, cutOffTime);
            if (datajob == null) return;

            String dailyJob = String.format("[createDailyDataJob] id: %s | category: %s | offset %s",
                                            source.getSourceConfigId(), source.getCategory(), datajob.getParamOffset());
            log.info(dailyJob);
        }
    }

    @Scheduled(fixedDelay = 2000, initialDelay = 3000)
    public void checkDataJobQueue() throws UnsupportedEncodingException {
        log.info("[checkDataJobQueue]");

        // NB: lock
        DataJob datajob = dataJobRepository.getNextDataJob(DataJobStatus.QUEUED);
        if (datajob == null) {
            log.info("No Jobs Queued");
            createNewJobs();
            return;
        }

        Source source = datajob.getSource();
        Map<String, Object> mapping = sourceLoaderService.getSourceMapping(source.getSourceConfigId());
        source.setMapping(mapping);

        String prefixLog = String.format("%s - %s", source.getCategory(), source.getDescription());
        String logDetails = String.format("%s | offset: %s | %s",
                                          prefixLog, datajob.getParamOffset(), datajob.getUrl());

        log.info(String.format("[Fetching] %s", logDetails));

        String json = dataJobRequestService.fetchJSON(datajob);
        if (datajob.getStatus() == DataJobStatus.FETCH_ERROR) {
            log.info(String.format("[FetchError] %s", logDetails));
            dataJobRepository.save(datajob);
            return;
        }

        log.info(String.format("[FetchComplete] %s", logDetails));
        datajob.setStatus(DataJobStatus.PENDING);
        dataJobRepository.save(datajob);
        log.info("createData() " + prefixLog);

        dataservice.createData(source, datajob, json);

        // if high error rate, mark job as error and stop future jobs
        if (datajob.getStatus() == DataJobStatus.ERROR || datajob.getStatus() == DataJobStatus.PARSE_ERROR) {
            dataJobRepository.save(datajob);
            return;
        }

        datajob.setStatus(DataJobStatus.COMPLETED);
        dataJobRepository.save(datajob);
        String logDone = String.format("[checkDataJobQueue] Done: %s | fetched: %s | processed: %s",
                                       datajob.getId(), datajob.getNumFetched(), datajob.getNumProcessed());
        log.info(logDone);
    }


    public void createNewJobs() throws UnsupportedEncodingException {

        HashMap<Integer, Source> sourceMap = sourceLoaderService.getSourceMap();

        for (Map.Entry<Integer, Source> entry : sourceMap.entrySet()) {
            Map<String, Object> mapping = sourceLoaderService.getSourceMapping(entry.getKey());
            Source source = sourceRepository.findOrCreate(entry.getValue());
            source.setMapping(mapping);

            // NB: lock
            DataJob nextJob = dataJobRepository.createNextDataJob(source, PARAM_LIMIT);
            log.info("[createNewJobs] next job: " + nextJob.getUrl());
        }

    }

    public void updateSourceNumRecords(Source source) {
        log.info("[SourceLoaderService] FetchCount ....");

        Integer numRecords = sourceLoaderService.fetchCount(source);
        if (numRecords == null) {
            log.info("[SourceLoaderService] FetchCount Error for source: " + source.getSourceConfigId());
            return;
        }

        String updateNumRecords = String.format("[SourceLoaderService] FetchCount %s -> %s", source.getNumRecords(),
                numRecords);
        log.info(updateNumRecords);

        if (numRecords != null) {
            source.setNumRecords(numRecords);
            source.setUpdatedAt(LocalDateTime.now());
            sourceRepository.save(source);
        }
    }

}
