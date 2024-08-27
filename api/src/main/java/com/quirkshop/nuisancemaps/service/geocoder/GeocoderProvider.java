package com.quirkshop.nuisancemaps.service.geocoder;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;

import com.quirkshop.nuisancemaps.model.Source;

public interface GeocoderProvider {

    public List<double[]> fetch(Source source, List<String> addresses);

    public List<double[]> parseResponse(InputStream inputStream) throws IOException;

    public String buildAPIURL(Source source, List<String> addresses, String MAPTILER_API_KEY)
            throws UnsupportedEncodingException;
}
