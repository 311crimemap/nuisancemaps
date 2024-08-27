package com.quirkshop.nuisancemaps.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.quirkshop.nuisancemaps.WorkerApplication;
import com.quirkshop.nuisancemaps.model.Geocode;
import com.quirkshop.nuisancemaps.model.Source;
import com.quirkshop.nuisancemaps.repository.GeocodeRepository;
import com.quirkshop.nuisancemaps.service.geocoder.MapTilerGeocoderProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
public class GeocoderService {

    @Autowired
    private GeocodeRepository geocodeRepository;

    @Autowired
    private MapTilerGeocoderProvider mapTilerGeocoderProvider;

    private static final Logger log = LoggerFactory.getLogger(WorkerApplication.class);

    // batchRequest
    public List<double[]> geocodeBatchRequest(Source source, List<String> addresses) {

        /*
         * PREP
         */

        // address -> coord map
        HashMap<String, double[]> geocodeMap = getCachedAddresses(source, addresses);
        HashSet<String> missingCoordinates = getAddressesNoCoordinates(source, addresses);

        // 1. filter out addresses with null coordinates (we've tried, no valid data)
        // 2. collect any new addresses
        Set<String> addressSet = new HashSet<String>(); // ensure we don't request dupes
        for (String address : addresses) {

            // Skip address: we've tried this address, but there were no viable
            // coordinates
            if (missingCoordinates.contains(address))
                continue;

            // new address, add for fetch
            if (!geocodeMap.containsKey(address)) {
                addressSet.add(address);
            }
        }

        String logRecords = String.format(
                "[GeocoderService] Batch Dataset: numInitial: %d | numCached: %d | numFiltered: %d | " +
                        "numQuery: %d | (NB: does not count dupes)",
                addresses.size(), geocodeMap.size(), missingCoordinates.size(), addressSet.size());
        log.info(logRecords);

        /*
         * FETCH
         * fetchBatch: calls service
         * addressSet: set of new addresses to be geocoded
         */
        List<String> newAddresses = new ArrayList<String>(addressSet);

        List<double[]> newCoordinates = mapTilerGeocoderProvider.fetch(source, newAddresses);

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
            geocodeRepository.saveAll(newGeocodes.values()); // add to Geocode cache table
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

    public HashSet<String> getAddressesNoCoordinates(Source source, List<String> addresses) {

        List<Geocode> geocodes = geocodeRepository
                .findBySourceAndAddressInAndLatitudeIsNullAndLongitudeIsNull(source, addresses);

        HashSet<String> missingCoordinates = new HashSet<String>();
        for (Geocode geocode : geocodes) {
            missingCoordinates.add(geocode.getAddress());
        }

        return missingCoordinates;
    }

}
