package com.quirkshop.nuisancemaps.service.dataprocess;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.util.concurrent.TimeUnit;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.quirkshop.nuisancemaps.WorkerApplication;
import com.quirkshop.nuisancemaps.model.DataJob;
import com.quirkshop.nuisancemaps.model.DataJobStatus;
import com.quirkshop.nuisancemaps.repository.DataJobRepository;
import com.quirkshop.nuisancemaps.service.dataparser.DataParser;

@Service
public class MemoryDataProcessStrategy implements DataProcessStrategy {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private OkHttpClient client;

    @Autowired
    DataJobRepository dataJobRepository;

    private static final Logger log = LoggerFactory.getLogger(WorkerApplication.class);

    @Override
    public InputStream fetchData(DataJob dataJob) {
        InputStream inputStream = null;

        try {
            dataJob.buildURL();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }

        String url = dataJob.getUrl();

        dataJob.setStatus(DataJobStatus.FETCH_START);
        dataJobRepository.save(dataJob);

        Request request = new Request.Builder().url(url).build();

        try {
            Response response = client.newCall(request).execute();
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
            inputStream = response.body().byteStream();
        } catch (IOException e) {
            System.err.println("Error fetchData: " + e.getMessage());
            dataJob.setStatus(DataJobStatus.FETCH_ERROR);
            dataJobRepository.save(dataJob);
            e.printStackTrace();
            return null;
        }

        dataJob.setStatus(DataJobStatus.FETCH_COMPLETE);
        dataJobRepository.save(dataJob);

        return inputStream;
    }

    @Override
    public void process(DataJob dataJob, InputStream inputStream, DataParser dataParser) {
        dataParser.parse(dataJob, inputStream);
    }

}
