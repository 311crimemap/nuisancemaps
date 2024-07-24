package com.quirkshop.nuisancemaps.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import java.io.UnsupportedEncodingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.quirkshop.nuisancemaps.WorkerApplication;
import com.quirkshop.nuisancemaps.model.DataJob;
import com.quirkshop.nuisancemaps.model.DataJobStatus;
import com.quirkshop.nuisancemaps.model.Source;
import com.quirkshop.nuisancemaps.repository.DataJobRepository;

@Service
public class DataJobRequestServiceImpl implements DataJobRequestService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    DataJobRepository dataJobRepository;

    private static final Logger log = LoggerFactory.getLogger(WorkerApplication.class);

    @Override
    public String fetchJSON(DataJob dataJob) {
        String jsonResponse = null;
        String currentThreadName = Thread.currentThread().getName();
        Source source = dataJob.getSource();
        String prefixLog = String.format("%s | dataJob: %s | %s - %s",
                currentThreadName,
                dataJob.getId(),
                source.getCategory(),
                source.getDescription());
        String logDetails = String.format("%s | offset: %s | %s",
                prefixLog, dataJob.getParamOffset(), dataJob.getUrl());

        try {
            dataJob.buildURL();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }

        String url = dataJob.getUrl();

        dataJob.setStatus(DataJobStatus.FETCH_START);
        dataJobRepository.save(dataJob);

        try {
            jsonResponse = restTemplate.getForObject(url, String.class);
        } catch (Exception e) {
            dataJob.setStatus(DataJobStatus.FETCH_ERROR);
            dataJobRepository.save(dataJob);
            e.printStackTrace();
            return null;
        }

        dataJob.setStatus(DataJobStatus.FETCH_COMPLETE);
        dataJobRepository.save(dataJob);
        return jsonResponse;
    }

}
