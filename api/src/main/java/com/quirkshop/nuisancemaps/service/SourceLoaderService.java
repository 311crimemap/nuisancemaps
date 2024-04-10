package com.quirkshop.nuisancemaps.service;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quirkshop.nuisancemaps.model.Mapping;
import com.quirkshop.nuisancemaps.model.Source;
import com.quirkshop.nuisancemaps.repository.MappingRepository;
import com.quirkshop.nuisancemaps.repository.SourceRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class SourceLoaderService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    MappingRepository mappingRepository;

    @Autowired
    SourceRepository sourceRepository;

    private ObjectMapper objectMapper;

    SourceLoaderService() {
        this.objectMapper = new ObjectMapper();
    }

    //wrap this so @Transactional throws error inside
    //the API controller scope (versus @Transactional on the controller action)
    //which would need handling outside
    @Transactional
    public Source saveTransaction(Source source) {
        mappingRepository.save(source.getMapping());
        source = sourceRepository.save(source);
        return source;
    }

    public Integer fetchCount(Source source) {
        final String RESPONSE_PREFIX = "count_";

        String sourceURL = source.getUrl();

        Mapping mapping = source.getMapping();
        if (mapping == null)
            return null;

        String id = mapping.getReportNum();
        if (id == null)
            return null;

        String jsonResponse;
        List<Map<String, Object>> responseList = new ArrayList<Map<String, Object>>();

        try {
            String url = buildCountURL(sourceURL, id);
            jsonResponse = restTemplate.getForObject(url, String.class);
            responseList = objectMapper.readValue(jsonResponse, new TypeReference<List<Map<String, Object>>>() {
            });
        } catch (RestClientException e) {
            e.printStackTrace();
            return null;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        } catch (JsonMappingException e) {
            e.printStackTrace();
            return null;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }

        // parse
        Integer numRecords = null;

        try {
            Map<String, Object> responseObject = responseList.get(0);
            numRecords = Integer.parseInt(responseObject.get(RESPONSE_PREFIX + id).toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return numRecords;
    }

    public String buildCountURL(String sourceURL, String id) throws UnsupportedEncodingException {
        // 'https://data.austintexas.gov/resource/xwdj-i9he.json?$select=count(sr_number)'
        String countIdString = String.format("count('%s')", id);
        String url = UriComponentsBuilder.fromUriString(sourceURL)
                .queryParam("$select", countIdString)
                .build()
                .toUriString();

        return url;
    }
}
