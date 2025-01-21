package com.quirkshop.nuisancemaps.service.geocoder;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;

import com.quirkshop.nuisancemaps.model.Source;

public interface GeocoderProvider {
    /**
     * Fetches coordinates given a list of addresses from the Provider's API.
     * This implementation should be a batched request to the Provider.
     *
     * @param source    The Source instance.
     * @param addresses A list of addresses to fetch coordinates for.
     * @return A list of double arrays where each array contains latitude and
     *         longitude.
     */
    public List<double[]> fetch(Source source, List<String> addresses);

    /**
     * Parses the response from the Provider API and extract coordinates.
     *
     * @param inputStream The input stream containing the response data.
     * @return A list of double arrays containing latitude and longitude values.
     * @throws IOException If an I/O error occurs during parsing.
     */
    public List<double[]> parseResponse(InputStream inputStream) throws IOException;

    /**
     * Builds the API URL String for making requests to the Provider Service.
     *
     * @param source    The Source instance.
     * @param addresses A list of addresses for the geocoding request.
     * @param API_KEY   The API key for authentication.
     *
     * @return The constructed API URL as a String.
     *
     * @throws UnsupportedEncodingException If the encoding is not supported.
     */

    public String buildAPIURL(Source source, List<String> addresses, String API_KEY)
            throws UnsupportedEncodingException;
}
