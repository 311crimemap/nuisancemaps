package com.quirkshop.nuisancemaps.service.geocoder;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quirkshop.nuisancemaps.WorkerApplication;
import com.quirkshop.nuisancemaps.model.Source;

import org.locationtech.jts.geom.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Request.Builder;
import okhttp3.RequestBody;
import okhttp3.Response;

@Component
public class GeoApifyGeocoderProvider implements GeocoderProvider {

    @Autowired
    private OkHttpClient client;

    private static final String GEOAPIFY_API_KEY = System.getenv("GEOAPIFY_API_KEY");

    private static final int GEOAPIFY_API_BATCH_SIZE = 50;
    private static final double GEOAPIFY_API_RELEVANCE_SCORE = .75;

    // ~ batch is slow: takes almost 20-30 seconds total for 50 entries.
    private static final long GEOAPIFY_API_POLL_DELAY = 7500;
    private static final int GEOAPIFY_API_MAX_RETRY = 20;

    private static final Logger log = LoggerFactory.getLogger(WorkerApplication.class);

    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<double[]> fetch(Source source, List<String> addresses) {
        return fetchBatch(source, formatAddresses(addresses));
    }

    @Override
    public List<double[]> parseResponse(InputStream inputStream) throws IOException {
        List<double[]> coordinates = new ArrayList<double[]>();

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode items = objectMapper.readTree(inputStream);

        log.info("[GeoApifyGeocoderProvider] parsing items: " + items.size());
        for (JsonNode item : items) {

            try {

                JsonNode feature = item;
                double relevance = feature.at("/rank/confidence").asDouble();

                // Criteria
                //
                // 1. if lacks lon/lat - no value
                //
                // 2. relevance (rank/confidence) seems indicative, should be pretty high
                // geoapify will return .25 value results
                //

                if (!feature.has("lat") || !feature.has("lon")) {
                    coordinates.add(null);
                    continue;
                }

                if (relevance > GEOAPIFY_API_RELEVANCE_SCORE) {
                    Double lat = feature.at("/lat").asDouble();
                    Double lng = feature.at("/lon").asDouble();
                    coordinates.add(new double[] { lat, lng });
                    continue;
                }

            } catch (Exception e) {
                log.info("[GeoApifyGeocoderProvider] parseResponse ERR: " + e.getMessage());
            }

            // need a placeholder to maintain alignment with batch; if
            // coordinates trip some criteria; don't exist, parsing error
            coordinates.add(null);
        }

        return coordinates;
    }

    private String fetchJobURL(Source source, List<String> batchAddresses)
            throws UnsupportedEncodingException, JsonProcessingException {
        String jobURL = null;

        String url = buildAPIURL(source, batchAddresses, GEOAPIFY_API_KEY);
        String jsonPayload = objectMapper.writeValueAsString(batchAddresses);
        RequestBody body = RequestBody.create(jsonPayload,
                MediaType.get("application/json; charset=utf-8"));

        Builder requestBuilder = new Request.Builder().url(url);
        Request request = requestBuilder
                .header("Content-Type", "application/json")
                .post(body)
                .build();

        try (Response initResponse = client.newCall(request).execute()) {

            if (!initResponse.isSuccessful()) {
                throw new IOException("Unexpected status code: " + initResponse);
            }

            // initResponse: gives worker job info
            String response = initResponse.body().string();
            JsonNode jsonResponse = objectMapper.readTree(response);

            jobURL = jsonResponse.at("/url").asText();
            // String status = jsonResponse.at("/status").asText();

            String _jobURL = UriComponentsBuilder.fromUriString(jobURL)
                    .replaceQueryParam("apiKey", "<redacted>")
                    .build()
                    .toUriString();

            log.info("[GeoApifyGeocoderProvider] jobURL: " + _jobURL);

        } catch (Exception e) {
            log.error("[GeoApifyGeocoderProvider] fetchJobURL: " + e.getMessage());
            return null;
        }

        return jobURL;

    }

    public List<double[]> fetchBatch(Source source, List<String> addresses) {
        int numFetch = 1;
        log.info("[GeoApifyGeocoderProvider] fetchBatch: total num fetch: " + addresses.size());

        List<double[]> results = new ArrayList<double[]>();

        // TODO: optimize iteration by batch size
        List<String> batchAddresses = new ArrayList<String>();
        for (int i = 0; i < addresses.size(); i++) {

            String address = addresses.get(i);
            batchAddresses.add(address);

            // build batch
            // until less than batch_size && less than total size
            if (batchAddresses.size() < GEOAPIFY_API_BATCH_SIZE &&
                    i + 1 < addresses.size())
                continue;

            // requests
            // we need to keep inputStreams open for parsing
            Response jobResponse = null;

            try {

                /*
                 * Submit Init Batch Job
                 */

                String jobURL = fetchJobURL(source, batchAddresses);
                if (jobURL == null)
                    throw new Error("fetchJobURL error");

                /*
                 * Poll Job
                 */

                String fetchStatus = String.format("[GeoApifyGeocoderProvider] fetching batch: [%d / %d]",
                        numFetch, (int) Math.ceil((double) addresses.size() / GEOAPIFY_API_BATCH_SIZE));
                log.info(fetchStatus);

                jobResponse = makePollRequest(jobURL, GEOAPIFY_API_POLL_DELAY, GEOAPIFY_API_MAX_RETRY);

                if (jobResponse == null) {
                    return new ArrayList<double[]>(addresses.size());
                }

                // parse response
                InputStream inputStream = jobResponse.body().byteStream();
                List<double[]> coordinates = parseResponse(inputStream);
                results.addAll(coordinates);

            } catch (Exception e) {
                log.info("[GeoApifyGeocoderProvider] fetchBatch: ERR" + e.getMessage());
                e.printStackTrace();
            } finally {

                log.info("[GeoApifyGeocoderProvider] closing responses");

                if (jobResponse != null) {
                    jobResponse.close();
                }
            }

            batchAddresses.clear();
            numFetch++;
        }

        return results;
    }

    @Override
    public String buildAPIURL(Source source, List<String> addresses, String GEOAPIFY_API_KEY)
            throws UnsupportedEncodingException {

        if (addresses.size() > GEOAPIFY_API_BATCH_SIZE) {
            throw new Error("Exceed API Batch Size");
        }

        // NB: both locale and proximity param is lng,lat
        Point location = source.getLocale().getLocation();
        final String centerLngLat = String.format("%f,%f", location.getX(), location.getY());

        // https://apidocs.geoapify.com/docs/geocoding/batch/#api

        String baseURL = "https://api.geoapify.com/v1/batch/geocode/search";

        String url = UriComponentsBuilder.fromUriString(baseURL)
                .queryParam("apiKey", GEOAPIFY_API_KEY)
                .queryParam("lang", "en")
                .queryParam("bias", "proximity:" + centerLngLat)
                .build()
                .encode()
                .toUriString();

        return url;
    }

    /*
     * GeoApify Specific Helpers
     */
    public Response makePollRequest(String url, long sleepMS, int maxRetries) throws InterruptedException {

        int retryCount = 0;

        // initial wait because job actually takes time to propogate on server
        TimeUnit.MILLISECONDS.sleep(sleepMS);

        while (retryCount < maxRetries) {

            Request request = new Request.Builder()
                    .url(url)
                    .build();

            Response response = null;

            try {

                response = client.newCall(request).execute();

                if (response.code() == 200) {
                    log.info("[makePollRequest] 200 OK");
                    return response;

                } else if (response.code() == 202) {
                    String logStr = String.format("[makePollRequest] 202 Accepted: retry in %d ms: attempt %d",
                            sleepMS, retryCount + 1);
                    log.info(logStr);
                    retryCount++;
                    TimeUnit.MILLISECONDS.sleep(sleepMS);
                } else {
                    // NB: 404 means job hasn't propogated on server side
                    log.error("[makePollRequest] status code: " + response.code());
                    log.error(request.url().toString());
                    break;
                }
            } catch (IOException e) {
                log.error("[makePollRequest] Request failed: " + e.getMessage());
                break;
            } finally {

                if (response != null && response.code() != 200) {
                    // log.info("[makePollRequest] closing response");
                    response.close();
                }

            }
        }

        if (retryCount >= maxRetries) {
            log.info("[makePollRequest] Exceeded max retries");
        }

        return null;
    }

    // to help geocoder
    // remove "BLOCK" - much more accurate to just use address
    // remove forward-slash: these are interpreted as a subpath route in api
    //
    private List<String> formatAddresses(List<String> addresses) {
        List<String> formattedAddresses = new ArrayList<String>();

        for (String address : addresses) {

            // maintain alignment
            if (address == null) {
                formattedAddresses.add(null);
                continue;
            }

            String formattedAddress = address
                    .replaceAll("UNKNOWN,", "")
                    .replaceAll("BLOCK", "");

            formattedAddresses.add(formattedAddress);
        }

        return formattedAddresses;
    }

    public int getBatchSize() {
        return GEOAPIFY_API_BATCH_SIZE;
    }
}
