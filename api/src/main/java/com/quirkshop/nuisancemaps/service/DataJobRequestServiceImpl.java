package com.quirkshop.nuisancemaps.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.io.UnsupportedEncodingException;
import org.springframework.beans.factory.annotation.Autowired;
import com.quirkshop.nuisancemaps.model.DataJob;

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
            // TODO Auto-generated catch block
            // TODO: update status - set as error
            e.printStackTrace();
        }

        String url = this.dataJob.getUrl();
        // TODO: update status pending
        this.jsonResponse = restTemplate.getForObject(url, String.class);

        // TODO: update status - set as complete
        return jsonResponse;
    }

}
