package com.quirkshop.nuisancemaps.service;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
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

        // Remote address
        log.info(InetAddress.getLoopbackAddress().getHostAddress());
        log.info(InetAddress.getLoopbackAddress().getHostName());
    }

    public void updateSourceNumRecords(Source source) {
        // TODO: early terminate if not worker_1 hostname
        // get pod name - restrict this to initial worker. e.g worker_1,
        // log.info("getProp2: " + env.getProperty("HOSTNAME"));
        // log.info( System.getenv("HOSTNAME"));

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
            sourceRepository.save(source);
        }
    }

    /*
     * SCHEDULED TASKS
     */

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

            // Fetch Count and Update
            updateSourceNumRecords(source);

            // find the last dataJob: a previous empty result (DataJobStatus.COMPLETED), or
            // latest queued job (DataJobStatus.QUEUED)
            DataJob datajob = dataJobRepository.findLastDataJobBySource(source.getId());

            // start from scratch initial crawl
            if (datajob == null) {

                String key = source.getMapping().get("report_num").toString();

                datajob = new DataJob(source, PARAM_LIMIT, 0, key);
            }

            // if found last "completed" job; we create a new job from that offset
            if (datajob.getStatus().equals(DataJobStatus.COMPLETED)) {
                datajob = new DataJob(source, PARAM_LIMIT, datajob.getParamOffset(),
                        datajob.getOrderKey());
            }

            // if last job is "queued" leave it as-is, to be picked up by scheduled task
            datajob.buildURL();
            datajob.setStatus(DataJobStatus.QUEUED);
            datajob = dataJobRepository.save(datajob);

            log.info("category: " + source.getCategory() + " id: " + source.getSourceConfigId());
        }
    }

    @Scheduled(fixedRate = 5000, initialDelay = 3000)
    public void checkDataJobQueue() throws UnsupportedEncodingException {
        log.info("[checkDataJobQueue]");

        DataJob datajob = dataJobRepository.getNextDataJob(DataJobStatus.QUEUED);
        if (datajob == null) {
            log.info("No Jobs Queued");
            return;
        }

        Source source = datajob.getSource();
        Map<String, Object> mapping = sourceLoaderService.getSourceMapping(source.getSourceConfigId());
        source.setMapping(mapping);

        log.info("fetching: " + datajob.getUrl());
        String json = dataJobRequestService.fetchJSON(datajob);

        datajob.setStatus(DataJobStatus.PENDING);
        dataJobRepository.save(datajob);
        log.info("createData()");

        dataservice.createData(source, datajob, json);

        // if high error rate, mark job as error and stop future jobs
        if (datajob.getStatus() == DataJobStatus.ERROR) {
            dataJobRepository.save(datajob);
            return;
        }

        datajob.setStatus(DataJobStatus.COMPLETED);
        dataJobRepository.save(datajob);

        if (datajob.getNumFetched() == 0) {
            return;
        }

        // queue next job: new offset = offset + PARAM_LIMIT
        // get next batch of N (LIMIT) records.
        // Look at dataJob.numResults to determine any errors, but this worker just
        // grabs at each clip.
        DataJob nextJob = new DataJob(datajob.getSource(), PARAM_LIMIT, datajob.getParamOffset() + PARAM_LIMIT,
                datajob.getOrderKey());

        nextJob.buildURL();
        nextJob.setStatus(DataJobStatus.QUEUED);
        dataJobRepository.save(nextJob);

        log.info("next job: " + nextJob.getUrl());

    }

}