package com.quirkshop.nuisancemaps.service;

import java.io.File;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quirkshop.nuisancemaps.model.Source;
import com.quirkshop.nuisancemaps.repository.MappingRepository;
import com.quirkshop.nuisancemaps.model.Mapping;

@Service
public class SourceLoaderService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private MappingRepository mappingRepository;

    private ObjectMapper objectMapper;
    private HashMap<Integer, Source> sourceMap;

    SourceLoaderService() {
        this.objectMapper = new ObjectMapper();
        this.sourceMap = new HashMap<Integer, Source>();
    }

    public HashMap<Integer, Source> getSourceMap() {
        return this.sourceMap;
    }

    public Source findBySourceConfigID(int id) {
        if (this.sourceMap == null)
            return null;
        return this.sourceMap.get(id);
    }

    public void loadJSON(String filename) {
        File jsonFile = null;
        try {
            jsonFile = new ClassPathResource(filename).getFile();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        List<Map<String, Object>> responseList = new ArrayList<Map<String, Object>>();

        try {
            responseList = objectMapper.readValue(jsonFile, new TypeReference<List<Map<String, Object>>>() {
            });
        } catch (JsonMappingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // for each object in list
        for (Map<String, Object> responseObject : responseList) {
            // responseObject contains key/val (another obj)

            Source source = new Source();

            int source_config_id = Integer.parseInt(responseObject.get("source_config_id").toString());
            String source_config_entity = responseObject.get("source_config_entity").toString();
            String category = responseObject.get("category").toString();
            String url = responseObject.get("url").toString();
            String description = responseObject.get("description").toString();
            int numRecords = (int) responseObject.get("num_records");
            Map<String, Object> mappingJSON = (Map<String, Object>) responseObject.get("mapping");

            Mapping mapping = new Mapping(mappingJSON.get("report_num").toString(),
                                          mappingJSON.get("report_category").toString(),
                                          mappingJSON.get("description").toString(),
                                          mappingJSON.get("location").toString(),
                                          mappingJSON.get("latitude").toString(),
                                          mappingJSON.get("longitude").toString(),
                                          mappingJSON.get("reported_at").toString(),
                                          mappingJSON.get("reported_at2").toString());

            source.setSourceConfigEntity(source_config_entity);
            source.setSourceConfigId(source_config_id);
            source.setCategory(category);
            source.setUrl(url);
            source.setDescription(description);
            source.setNumRecords(numRecords);

            source.setMapping(mapping);
            mappingRepository.save(mapping);

            this.sourceMap.put(source_config_id, source);
        }
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
