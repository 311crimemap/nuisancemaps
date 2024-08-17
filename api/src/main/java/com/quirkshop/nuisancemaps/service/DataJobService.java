package com.quirkshop.nuisancemaps.service;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;

import com.quirkshop.nuisancemaps.WorkerApplication;
import com.quirkshop.nuisancemaps.config.DataParserType;
import com.quirkshop.nuisancemaps.model.Source;
import com.quirkshop.nuisancemaps.model.datajob.DataJob;
import com.quirkshop.nuisancemaps.model.datajob.DataJobStatus;
import com.quirkshop.nuisancemaps.repository.DataJobRepository;
import com.quirkshop.nuisancemaps.repository.SourceRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DataJobService {

    @Autowired
    SourceRepository sourceRepository;

    @Autowired
    DataJobRepository dataJobRepository;

    private static final Logger log = LoggerFactory.getLogger(WorkerApplication.class);

    private static final int PARAM_LIMIT = Integer.parseInt(System.getenv("WORKER_QUERY_LIMIT"));

    // "earliest" QUEUED job (regardless of source or session)
    @Transactional
    public DataJob getNextDataJob(DataJobStatus status) {
        DataJob dataJob = dataJobRepository.findTopByStatusOrderByIdAsc(status);
        if (dataJob == null)
            return null;
        dataJob.setStatus(DataJobStatus.START);
        dataJob = dataJobRepository.save(dataJob);
        return dataJob;
    }

    public void createNewJobs() throws UnsupportedEncodingException {

        Iterable<Source> sources = sourceRepository.findAll();

        for (Source source : sources) {

            DataJob nextJob = createNextDataJob(source);
            if (nextJob == null)
                continue;

            String logStr = String.format("[createNewJobs] next job: %s", nextJob.getUrl());

            log.info(logStr);
        }

    }

    @Transactional
    private DataJob createNextDataJob(Source source) throws UnsupportedEncodingException {

        // NB: Locked
        DataJob maxSessionIdOffsetDataJob = dataJobRepository
                .findTopBySourceIdOrderBySessionIdDescParamOffsetDesc(source.getId());

        // no job for source has ever existed, start fresh 0
        if (maxSessionIdOffsetDataJob == null) {
            DataJob newJob = createNewDataJob(source, null);
            return newJob;
        }

        // numFetched null: have a Source DataJob but yet to fetch, or in mid-fetch
        // we can wait until next round
        if (maxSessionIdOffsetDataJob.getNumFetched() == null)
            return null;

        // != 0 - has fetched so continue fetching next set until we get 0 - know for
        // sure we've reached the end.
        if (maxSessionIdOffsetDataJob.getNumFetched() != 0) {

            DataJob nextJob = createNewDataJob(source, maxSessionIdOffsetDataJob);
            return nextJob;
        }

        // all caught up, last job had num_fetched == 0 -> no new jobs
        return null;
    }

    @Transactional
    public DataJob createNewDataJob(Source source, DataJob prevDataJob)
            throws UnsupportedEncodingException {

        String key = source.getMapping().getOrderKey(); // NB: prevDataJob might exist
        DataJob dataJob;

        if (prevDataJob == null) {
            // start new 'crawl' session
            dataJob = new DataJob(LocalDateTime.now(), source, key);
            dataJob.initURL();
        } else {

            // generate dataJob and  url for next sequence of session
            //
            // NB: url can be null if DataJobURLType indicates run only once -
            // don't create next job.
            String url = prevDataJob.buildNextURL();

            if (url == null)
                return null;

            dataJob = new DataJob(prevDataJob.getSessionId(),
                    source,
                    prevDataJob.getOrderKey());

            dataJob.setUrl(url);
        }

        dataJobRepository.save(dataJob);
        return dataJob;
    }

}
