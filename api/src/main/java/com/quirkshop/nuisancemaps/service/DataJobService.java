package com.quirkshop.nuisancemaps.service;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.quirkshop.nuisancemaps.WorkerApplication;
import com.quirkshop.nuisancemaps.model.Source;
import com.quirkshop.nuisancemaps.model.datajob.DataJob;
import com.quirkshop.nuisancemaps.model.datajob.DataJobConfigurator;
import com.quirkshop.nuisancemaps.model.datajob.DataJobConfiguratorFactory;
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

    /**
     * Retrieves the next "earliest" QUEUED job with the specified status;
     * updates its status to START, and persists the change.
     *
     * @param status the current status of the data job to retrieve
     * @return the updated DataJob if found, otherwise null
     */
    @Transactional
    public DataJob getNextDataJob(DataJobStatus status) {
        DataJob dataJob = dataJobRepository.findTopByStatusOrderByIdAsc(status);
        if (dataJob == null)
            return null;
        dataJob.setStatus(DataJobStatus.START);
        dataJob = dataJobRepository.save(dataJob);
        return dataJob;
    }

    /**
     * Resets the status of all DataJobs currently in POLL_WAIT to QUEUED that
     * have elapsed for the specified duration.
     *
     * @param duration the time duration to check against the elapsed time of
     *                 data jobs
     * @return the number of jobs that were reset
     */

    @Transactional
    public int resetElapsedPollWait(LocalDateTime duration) {
        int count = dataJobRepository.updateElapsedJobs(duration, DataJobStatus.POLL_WAIT, DataJobStatus.QUEUED);
        if (count > 0) {
            String logStr = String.format("[resetElapsedPollWait] reset %d DataJobStatus.POLL_WAIT -> QUEUED",
                    count);
            log.info(logStr);
        }
        return count;
    }

    /**
     * Creates new data jobs if any new jobs can be created. Logs the URLs of
     * newly created jobs.
     *
     * @throws UnsupportedEncodingException
     */
    public void createNewJobs() throws UnsupportedEncodingException {

        // get all Sources
        Iterable<Source> sources = sourceRepository.findAll();

        // build source_id -> DataJob map (max session_id, max param_offset)
        List<DataJob> dataJobs = dataJobRepository.findMaxSessionIdOffsetDataJobs();

        Map<Integer, DataJob> dataJobMap = dataJobs.stream()
                .collect(Collectors.toMap(
                        dataJob -> (Integer) dataJob.getSourceId(), // Key: source_id
                        dataJob -> dataJob // Value: DataJob
                ));

        // loop and create new job if available
        for (Source source : sources) {

            DataJob nextJob = createNextDataJob(source, dataJobMap);
            if (nextJob == null)
                continue;

            String logStr = String.format("[createNewJobs] next job: %s", nextJob.getUrl());

            log.info(logStr);
        }

    }

    /**
     * Creates the next data job for a given source based on the previously created
     * jobs.
     *
     * @param source     the source instance to create a new data job
     * @param dataJobMap a lookup map of existing jobs tied to their respective
     *                   source IDs
     * @return the newly created DataJob, or null
     * @throws UnsupportedEncodingException
     */
    @Transactional
    private DataJob createNextDataJob(Source source, Map<Integer, DataJob> dataJobMap)
            throws UnsupportedEncodingException {

        DataJob maxSessionIdOffsetDataJob = dataJobMap.getOrDefault(source.getId(), null);
        // log.info("Source id: " + source.getId() + " maxSessionId: " +
        // maxSessionIdOffsetDataJob);

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

    /**
     * Creates and persists a new DataJob for a given source, optionally based on a
     * previous job.
     *
     * @param source      the source instance to create a new data job
     * @param prevDataJob the previously created DataJob to base the new job on, or
     *                    null to start fresh
     * @return the newly created DataJob, or null if it cannot be created
     * @throws UnsupportedEncodingException
     */

    @Transactional
    public DataJob createNewDataJob(Source source, DataJob prevDataJob)
            throws UnsupportedEncodingException {

        String key = source.getMapping().getOrderKey(); // NB: prevDataJob might exist
        DataJob dataJob;

        DataJobConfigurator dataJobConfigurator = DataJobConfiguratorFactory
                .create(source.getDataJobConfiguratorType());

        if (prevDataJob == null) {
            // start new 'crawl' session
            dataJob = new DataJob(LocalDateTime.now(), source, key);
            dataJob = dataJobConfigurator.initialize(dataJob);
        } else {
            prevDataJob.setSource(source); // ranked query lacks association; set here
            dataJob = dataJobConfigurator.next(new DataJob(prevDataJob));

            if (dataJob == null)
                return null;

        }

        dataJobRepository.save(dataJob);
        return dataJob;
    }

}
