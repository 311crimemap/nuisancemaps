package com.quirkshop.nuisancemaps.service;

import java.io.UnsupportedEncodingException;
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
            log.info("Empty Queue");
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

        int num = dataservice.createData(source, datajob, json);

        // if high error rate, mark job as error and stop future jobs
        if (datajob.getStatus() == DataJobStatus.ERROR) {
            dataJobRepository.save(datajob);
            return;
        }

        datajob.setStatus(DataJobStatus.COMPLETED);
        datajob.setNumResults(num);
        dataJobRepository.save(datajob);

        if (num == 0) {
            return;
        }

        // queue next job: new offset = offset + num
        DataJob nextJob = new DataJob(datajob.getSource(), PARAM_LIMIT, datajob.getParamOffset() + num,
                datajob.getOrderKey());

        nextJob.buildURL();
        nextJob.setStatus(DataJobStatus.QUEUED);
        dataJobRepository.save(nextJob);

        log.info("next job: " + nextJob.getUrl());

    }

}