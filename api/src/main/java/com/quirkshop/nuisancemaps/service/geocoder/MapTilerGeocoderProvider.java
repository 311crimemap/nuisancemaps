
package com.quirkshop.nuisancemaps.service.geocoder;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

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

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Request.Builder;
import okhttp3.Response;

@Component
public class MapTilerGeocoderProvider implements GeocoderProvider {

    @Autowired
    private OkHttpClient client;

    private static final String MAPTILER_API_KEY = System.getenv("VITE_MAPTILER_API_KEY");
    private static final int MAPTILER_API_BATCH_SIZE = 50;
    private static final double MAPTILER_API_RELEVANCE_SCORE = .75;

    private static final Logger log = LoggerFactory.getLogger(WorkerApplication.class);

    @Override
    public List<double[]> fetch(Source source, List<String> addresses) {
        return fetchBatch(source, addresses);
    }

    @Override
    public List<double[]> parseResponse(InputStream inputStream) throws IOException {
        List<double[]> coordinates = new ArrayList<double[]>();

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode items = objectMapper.readTree(inputStream);

        log.info("[MapTilerGeocoderProvider] parsing items: " + items.size());
        for (JsonNode item : items) {

            try {

                // empty (no) result for query
                if (!item.at("/features").has(0)) {
                    coordinates.add(null);
                    continue;
                }

                JsonNode feature = item.at("/features").get(0);
                double relevance = feature.at("/relevance").asDouble();

                // Criteria
                //
                // 1. has "address" field - otherwise accuracy suffers wildly
                // even though correct "street"
                //
                // 2. precision <= 6-8 coordinate digits: anything greater is a derived
                // coordinate; answer is almost always off
                //
                // *. Relevance is more about answer quality versus relevance to
                // query. 95% relevance score on street, but without numeric
                // address field it's still too inaccurate.

                if (!feature.has("address")) {
                    coordinates.add(null);
                    continue;
                }

                String latString = feature.at("/geometry/coordinates/0").asText();
                String lngString = feature.at("/geometry/coordinates/1").asText();

                if (!(validPrecision(latString) && validPrecision(lngString))) {
                    coordinates.add(null);
                    continue;
                }

                if (relevance > MAPTILER_API_RELEVANCE_SCORE) {
                    double lng = feature.at("/geometry/coordinates/0").asDouble();
                    double lat = feature.at("/geometry/coordinates/1").asDouble();

                    coordinates.add(new double[] { lat, lng });
                    continue;
                }

            } catch (Exception e) {
                log.info("[MapTilerGeocoderProvider] parseResponse ERR: " + e.getMessage());
            }

            // need a placeholder to maintain alignment with batch; if
            // coordinates trip some criteria; don't exist, parsing error
            coordinates.add(null);
        }

        return coordinates;
    }

    public List<double[]> fetchBatch(Source source, List<String> addresses) {
        int numFetch = 1;
        log.info("[MapTilerGeocoderProvider] fetchBatch: total num fetch: " + addresses.size());

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

                String url = buildAPIURL(source, batchURLs, MAPTILER_API_KEY);
                Builder requestBuilder = new Request.Builder().url(url);
                Request request = requestBuilder.build();
                Response response = client.newCall(request).execute();

                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }

                String fetchStatus = String.format("[MapTilerGeocoderProvider] fetching batch: [%d / %d]",
                        numFetch, (int) Math.ceil(addresses.size() / batchURLs.size()));
                log.info(fetchStatus);

                // response
                InputStream inputStream = response.body().byteStream();
                List<double[]> coordinates = parseResponse(inputStream);
                results.addAll(coordinates);

            } catch (Exception e) {
                log.info("[MapTilerGeocoderProvider] geocode: ERR" + e.getMessage());
                e.printStackTrace();
            }

            batchURLs.clear();
            numFetch++;
        }

        return results;
    }

    @Override
    public String buildAPIURL(Source source, List<String> addresses, String MAPTILER_API_KEY)
            throws UnsupportedEncodingException {

        if (addresses.size() > MAPTILER_API_BATCH_SIZE) {
            throw new Error("Exceed API Batch Size");
        }

        // NB: both locale and proximity param is lng,lat
        Point location = source.getLocale().getLocation();
        final String centerLngLat = String.format("%f,%f", location.getX(), location.getY());

        String queryAddresses = String.join(";", formatAddresses(addresses)) + ".json";

        String baseURL = String.format("https://api.maptiler.com/geocoding/%s",
                queryAddresses);

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

    /*
     * MapTiler Specific Helpers
     */

    // result is almost always better when coordinates are less precise;
    // implies entity wasn't calculated / averaged
    private boolean validPrecision(String coord) {
        final int MAX_PRECISION = 8;
        int precision = 0;
        int decimalIndex = coord.indexOf(".");

        if (decimalIndex > 0) {
            precision = coord.length() - decimalIndex - 1;
        }

        return precision <= MAX_PRECISION;
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
                    .replaceAll("BLOCK", "")
                    .replaceAll("/", "");

            formattedAddresses.add(formattedAddress);
        }

        return formattedAddresses;
    }

}
