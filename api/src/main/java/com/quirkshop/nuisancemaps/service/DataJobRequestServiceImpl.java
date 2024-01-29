package com.quirkshop.nuisancemaps.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import java.io.UnsupportedEncodingException;
import org.springframework.beans.factory.annotation.Autowired;
import com.quirkshop.nuisancemaps.model.DataJob;
import com.quirkshop.nuisancemaps.model.DataJobStatus;

@Service
public class DataJobRequestServiceImpl implements DataJobRequestService {

    @Autowired
    private RestTemplate restTemplate;

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

        try {
            this.jsonResponse = restTemplate.getForObject(url, String.class);
        } catch (RestClientException e) {
            this.dataJob.setStatus(DataJobStatus.FETCH_ERROR);
            e.printStackTrace();
            return null;
        }

        this.dataJob.setStatus(DataJobStatus.FETCH_COMPLETE);
        return jsonResponse;
    }

}
