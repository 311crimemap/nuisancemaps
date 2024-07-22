package com.quirkshop.nuisancemaps.service;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
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

    @Autowired
    private ObjectMapper objectMapper;

    public Source updateSource(int id, Map<String, Object> updates) throws JsonMappingException {
        Source source = sourceRepository.findById(id).orElse(null);
        Mapping mapping = source.getMapping();

        // NB: need to updates.remove otherwise results in unsaved transient instance
        Map<String, Object> mappingUpdates = (Map<String, Object>) updates.remove("mapping");
        objectMapper.updateValue(mapping, mappingUpdates);
        objectMapper.updateValue(source, updates);
        return sourceRepository.save(source);
    }

    // wrap this so @Transactional throws error inside
    // the API controller scope (versus @Transactional on the controller action)
    // which would need handling outside
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

        String id = mapping.getReportNum().getPointer();
        if (id == null)
            return null;

        // replace for field name vs json path
        id = id.replaceFirst("/", "");
        String jsonResponse;
        JsonNode rootNode = null;

        try {
            String url = buildCountURL(sourceURL, id);
            jsonResponse = restTemplate.getForObject(url, String.class);
            rootNode = objectMapper.readTree(jsonResponse);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        // parse
        Integer numRecords = null;

        try {
            JsonNode node = rootNode.get(0);
            numRecords = node.at("/" + RESPONSE_PREFIX + id).asInt();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return numRecords;
    }

    public String buildCountURL(String sourceURL, String id) throws UnsupportedEncodingException {
        // 'https://data.austintexas.gov/resource/xwdj-i9he.json?$select=count(sr_number)'
        String countIdString = String.format("count('%s')", id.replaceFirst("/", ""));
        String url = UriComponentsBuilder.fromUriString(sourceURL)
                .queryParam("$select", countIdString)
                .build()
                .toUriString();

        return url;
    }
}
