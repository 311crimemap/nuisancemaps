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

    private final int PARAM_LIMIT = 1000;

    private static final Logger log = LoggerFactory.getLogger(WorkerApplication.class);

    @PostConstruct // method called once after beans all loaded
    public void initialize() {
        sourceLoaderService.loadJSON("data/source_config.json");
        log.info("loaded source_config.json");
    }

    @Scheduled(fixedRate = 500000) // TODO:. set cron for daily
    public void createDailyDataJobs() throws UnsupportedEncodingException {
        log.info("[createDailyDataJob]");
        HashMap<Integer, Source> sourceMap = sourceLoaderService.getSourceMap();

        log.info("source_config.json num entries: " + sourceMap.size());

        for (Map.Entry<Integer, Source> entry : sourceMap.entrySet()) {
            log.info(entry.toString());

            // NB: Anticipate modifying mapping fields, so allowing for config change of
            // same Source entity
            Map<String, Object> mapping = sourceLoaderService.getSourceMapping(entry.getKey());
            Source source = sourceRepository.findOrCreate(entry.getValue());
            source.setMapping(mapping);

            // find the last/max record - this is a previous empty result, or latest
            // queued job that hasn't run for whatever reason
            DataJob datajob = dataJobRepository.findLastDataJobBySource(source);

            // start from scratch initial crawl
            if (datajob == null) {

                String key = source.getMapping().get("report_num").toString();

                datajob = new DataJob(source, PARAM_LIMIT, 0, key);
            }

            // if found last "completed" job; we create a new job from that offset
            if (datajob.getStatus() == "completed") {
                datajob = new DataJob(source, datajob.getParam_limit(), datajob.getParam_offset(),
                        datajob.getOrder_key());
            }

            // if last job is "queued" leave it as-is, to be picked up by scheduled task
            datajob.buildURL();
            datajob.setStatus("queued");
            datajob = dataJobRepository.save(datajob);

            log.info(source.getCategory() + " " + source.getSource_config_id());
        }
    }

    // @Scheduled(fixedRate = 5000)
    public void checkDataJobQueue() throws UnsupportedEncodingException {
        // TODO: some kind of check to enabled / disable this task
        log.info("[checkDataJobQueue]");

        // TODO: clean up source - mapping - persistence
        DataJob datajob = dataJobRepository.getNextDataJob("queued");
        if (datajob == null) {
            log.info("Empty Queue");
            return;
        }

        Source source = datajob.getSource();
        Map<String, Object> mapping = sourceLoaderService.getSourceMapping(source.getSource_config_id());
        source.setMapping(mapping);

        String json = dataJobRequestService.fetchJSON(datajob);
        int num = dataservice.createData(source, json);

        log.info("retrieved: " + num);

        if (num == 0) {
            datajob.setStatus("completed");
            return;
        }

        // queue next job: new offset = offset + num
        DataJob nextJob = new DataJob(datajob.getSource(), datajob.getParam_limit(), datajob.getParam_offset() + num,
                datajob.getOrder_key());

        nextJob.buildURL();
        nextJob.setStatus("queued");
        dataJobRepository.save(nextJob);

        log.info("next job: " + nextJob.getUrl());

    }

}