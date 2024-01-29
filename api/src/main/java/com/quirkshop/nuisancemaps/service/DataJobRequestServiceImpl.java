package com.quirkshop.nuisancemaps.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import java.io.UnsupportedEncodingException;
import org.springframework.beans.factory.annotation.Autowired;
import com.quirkshop.nuisancemaps.model.DataJob;
import com.quirkshop.nuisancemaps.model.DataJobStatus;
import com.quirkshop.nuisancemaps.repository.DataJobRepository;

@Service
public class DataJobRequestServiceImpl implements DataJobRequestService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    DataJobRepository dataJobRepository;

    private DataJob dataJob;
    private String jsonResponse;

    public DataJobRequestServiceImpl() {
        this.dataJob = null;
        this.jsonResponse = null;
    }

    @Override
    public String fetchJSON(DataJob d) {
        this.dataJob = d;

        try {
            this.dataJob.buildURL();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }

        String url = this.dataJob.getUrl();

        this.dataJob.setStatus(DataJobStatus.FETCH_START);
        dataJobRepository.save(dataJob);

        try {
            this.jsonResponse = restTemplate.getForObject(url, String.class);
        } catch (RestClientException e) {
            this.dataJob.setStatus(DataJobStatus.FETCH_ERROR);
            dataJobRepository.save(dataJob);
            e.printStackTrace();
            return null;
        }

        this.dataJob.setStatus(DataJobStatus.FETCH_COMPLETE);
        dataJobRepository.save(dataJob);
        return jsonResponse;
    }

}
