package com.quirkshop.nuisancemaps.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quirkshop.nuisancemaps.WorkerApplication;
import com.quirkshop.nuisancemaps.model.Geocode;
import com.quirkshop.nuisancemaps.model.Source;
import com.quirkshop.nuisancemaps.repository.GeocodeRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
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

    @Autowired
    private GeocodeRepository geocodeRepository;

    private static final String MAPTILER_API_KEY = System.getenv("VITE_MAPTILER_API_KEY");
    private static final int MAPTILER_API_BATCH_SIZE = 50;
    private static final double MAPTILER_API_RELEVANCE_SCORE = .75;

    private static final Logger log = LoggerFactory.getLogger(WorkerApplication.class);

    // batchRequest
    public List<double[]> geocodeBatchRequest(Source source, List<String> addresses) {

        // address->coord map
        HashMap<String, double[]> geocodeMap = getCachedAddresses(source, addresses);

        // filter & collect new addresses with no saved coordinates
        Set<String> addressSet = new HashSet<String>(); // ensure we don't request dupes
        for (String address : addresses) {
            if (!geocodeMap.containsKey(address)) {
                addressSet.add(address);
            }
        }

        // fetchBatch call service
        List<String> newAddresses = new ArrayList<String>(addressSet);
        List<double[]> newCoordinates = fetchBatch(newAddresses);

        // update geocodeMap with fetched new coordinates
        HashMap<String, Geocode> newGeocodes = new HashMap<String, Geocode>();

        for (int i = 0; i < newCoordinates.size(); i++) {
            String address = newAddresses.get(i);
            double[] coords = newCoordinates.get(i);

            // NB: we don't return it from geoCoderService
            // but do save addresses with null coordinates to track for later
            // lookup (newGeocodes). Null values are not pulled in getCachedAddresses.
            Geocode geocode = new Geocode(source, address, null, null);

            if (coords != null) {
                geocode = new Geocode(source, address, coords[0], coords[1]);
                geocodeMap.putIfAbsent(address, coords);
            }

            newGeocodes.put(address, geocode);
        }

        try {
            geocodeRepository.saveAll(newGeocodes.values()); // add to cache
        } catch (DataIntegrityViolationException e) {
            log.info("[GeocoderService] duplicate: " + e.getMessage());
        }


        // collect addresses with coords
        // loop our original inputs and populate to ensure order (HashMap geocodeMap)
        ArrayList<double[]> results = new ArrayList<double[]>();
        for (String address : addresses) {
            results.add(geocodeMap.get(address));
        }

        return results;
    }

    public HashMap<String, double[]> getCachedAddresses(Source source, List<String> addresses) {

        List<Geocode> geocodeCached = geocodeRepository
                .findBySourceAndAddressInAndLatitudeIsNotNullAndLongitudeIsNotNull(source, addresses);

        HashMap<String, double[]> geocodeMap = new HashMap<String, double[]>();
        for (Geocode geocode : geocodeCached) {
            double[] latlng = { geocode.getLatitude(), geocode.getLongitude() };
            geocodeMap.put(geocode.getAddress(), latlng);
        }

        return geocodeMap;
    }

    public List<double[]> fetchBatch(List<String> addresses) {
        int numFetch = 1;
        log.info("[GeocoderService] fetchBatch: total num fetch: " + addresses.size());

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

                String fetchStatus = String.format("[GeocoderService] fetching batch: [%d / %d]",
                                                   numFetch, (int) Math.ceil(addresses.size() / batchURLs.size()));
                log.info(fetchStatus);

                // response
                InputStream inputStream = response.body().byteStream();
                List<double[]> coordinates = parseResponse(inputStream);
                results.addAll(coordinates);

            } catch (Exception e) {
                log.info("[GeocoderServce] geocodeBatchRequest: ERR" + e.getMessage());
                e.printStackTrace();
            }

            batchURLs.clear();
            numFetch++;
        }

        return results;
    }

    public List<double[]> parseResponse(InputStream inputStream) throws IOException {
        List<double[]> coordinates = new ArrayList<double[]>();

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode items = objectMapper.readTree(inputStream);

        log.info("[GeocoderService] parsing items: " + items.size());
        for (JsonNode item : items) {

            try {

                // empty (no) result for query
                if (!item.at("/features").has(0)) {
                    coordinates.add(null);
                    continue;
                }

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

    // to help geocoder
    // remove "BLOCK" - much more accurate to just use address
    // remove forward-slash: these are interpreted as a subpath route in api
    //
    private List<String> formatAddresses(List<String> addresses) {
        List<String> formattedAddresses = new ArrayList<String>();

        for (String address : addresses) {
            if (address == null)
                continue;

            String formattedAddress = address
                    .replaceAll("BLOCK", "")
                    .replaceAll("/", "");

            formattedAddresses.add(formattedAddress);
        }

        return formattedAddresses;
    }

    public String buildMapTilerURL(List<String> addresses, String MAPTILER_API_KEY)
            throws UnsupportedEncodingException {

        if (addresses.size() > MAPTILER_API_BATCH_SIZE) {
            throw new Error("Exceed API Batch Size");
        }

        final String centerLngLat = "-97.733330,30.266666";

        String locations = String.join(";", formatAddresses(addresses)) + ".json";

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
