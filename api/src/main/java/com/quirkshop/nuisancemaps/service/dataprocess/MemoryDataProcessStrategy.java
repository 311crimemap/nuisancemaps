package com.quirkshop.nuisancemaps.service.dataprocess;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import com.quirkshop.nuisancemaps.model.Source;
import com.quirkshop.nuisancemaps.model.datajob.DataJob;
import com.quirkshop.nuisancemaps.model.datajob.DataJobStatus;
import com.quirkshop.nuisancemaps.repository.DataJobRepository;
import com.quirkshop.nuisancemaps.service.dataparser.DataParser;
import com.quirkshop.nuisancemaps.util.ParseCounter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Request.Builder;

@Service
public class MemoryDataProcessStrategy implements DataProcessStrategy {

    @Autowired
    private OkHttpClient client;

    @Autowired
    DataJobRepository dataJobRepository;

    @Override
    public InputStream fetchData(DataJob dataJob) {
        InputStream inputStream = null;

        String url = dataJob.getUrl();
        Source source = dataJob.getSource();
        String cookie = source.getCookie();
        Builder requestBuilder = new Request.Builder().url(url);
        if (cookie != null) {
            requestBuilder.addHeader("Cookie", cookie);
        }
        Request request = requestBuilder.build();

        dataJob.setStatus(DataJobStatus.FETCH_START);
        dataJobRepository.save(dataJob);

        try {
            Response response = client.newCall(request).execute();

            // 202 accept: typically indicates start of background job, requires
            // periodic poll check for generated requested file. Set status and
            // defer back to queue.
            if (response.code() == 202) {
                dataJob.setStatus(DataJobStatus.POLL_WAIT);
                dataJobRepository.save(dataJob);
                return null;
            }

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
        // in memory <-- this implementation here, nothing needed due to parse
        // objectMapper

        // parse
        // dataParser.parseData(dataJob, inputStream);
        ParseCounter parseCounter = new ParseCounter();

        dataParser.parse(dataJob, inputStream, parseCounter);

        setJobStatus(dataJob.getSource(), dataJob, parseCounter);
    }

    @Override
    public void cleanup(DataJob dataJob) {
        return; //no-op
    }
}
