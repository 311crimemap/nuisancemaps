package com.quirkshop.nuisancemaps.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quirkshop.nuisancemaps.WorkerApplication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Request.Builder;
import okhttp3.Response;

@Service
public class GeocoderService {

    @Autowired
    private OkHttpClient client;

    private static final String MAPTILER_API_KEY = System.getenv("VITE_MAPTILER_API_KEY");
    private static final int MAPTILER_API_BATCH_SIZE = 50;
    private static final double MAPTILER_API_RELEVANCE_SCORE = .75;

    private static final Logger log = LoggerFactory.getLogger(WorkerApplication.class);

    // batchRequest
    public List<double[]> geocodeBatchRequest(List<String> addresses) {

        List<double[]> results = new ArrayList<double[]>();

        // TODO: optimize iteration by batch size
        List<String> batchURLs = new ArrayList<String>();
        for (int i = 0; i < addresses.size(); i++) {

            String address = addresses.get(i);
            batchURLs.add(address);

            // build batch
            // until less than batch_size && less than total size
            if (batchURLs.size() < MAPTILER_API_BATCH_SIZE &&
                    i + 1 < addresses.size())
                continue;

            // request
            try {

                String url = buildMapTilerURL(batchURLs, MAPTILER_API_KEY);
                Builder requestBuilder = new Request.Builder().url(url);
                Request request = requestBuilder.build();
                Response response = client.newCall(request).execute();

                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }

                // response
                InputStream inputStream = response.body().byteStream();
                List<double[]> coordinates = parseResponse(inputStream);
                results.addAll(coordinates);

            } catch (Exception e) {
                log.info("[GeocoderServce] geocodeBatchRequest: ERR" + e.getMessage());
                // dataJob.setStatus(DataJobStatus.FETCH_ERROR);
                // dataJobRepository.save(dataJob);
                e.printStackTrace();
            }

            batchURLs.clear();
        }

        return results;
    }

    public List<double[]> parseResponse(InputStream inputStream) throws IOException {
        List<double[]> coordinates = new ArrayList<double[]>();

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode items = objectMapper.readTree(inputStream);
        for (JsonNode item : items) {

            try {
                JsonNode feature = item.at("/features").get(0);
                double relevance = feature.at("/relevance").asDouble();

                if (relevance > MAPTILER_API_RELEVANCE_SCORE) {
                    double lng = feature.at("/geometry/coordinates/0").asDouble();
                    double lat = feature.at("/geometry/coordinates/1").asDouble();

                    coordinates.add(new double[] { lat, lng });
                    continue;
                }

            } catch (Exception e) {
                log.info("[GeocoderServce] parseResponse ERR: " + e.getMessage());
            }

            // need a placeholder to maintain alignment with batch.
            // if coordinates don't exceed relevance threshold; don't exist, or
            // there's some parsing error
            coordinates.add(null);
        }

        return coordinates;
    }

    public String buildMapTilerURL(List<String> addresses, String MAPTILER_API_KEY)
            throws UnsupportedEncodingException {

        if (addresses.size() > MAPTILER_API_BATCH_SIZE) {
            throw new Error("Exceed API Batch Size");
        }

        final String centerLngLat = "-97.733330,30.266666";

        String locations = String.join(";", addresses) + ".json";

        String baseURL = String.format("https://api.maptiler.com/geocoding/%s",
                locations);

        String url = UriComponentsBuilder.fromUriString(baseURL)
                .queryParam("language", "en")
                .queryParam("country", "us")
                .queryParam("proximity", centerLngLat)
                .queryParam("key", MAPTILER_API_KEY)
                .build()
                .encode() // , and ; are kept, rest are % encoded
                .toUriString();

        return url;
    }
}
