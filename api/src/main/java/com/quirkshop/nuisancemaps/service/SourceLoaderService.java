package com.quirkshop.nuisancemaps.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quirkshop.nuisancemaps.model.Source;

@Service
public class SourceLoaderService {

    private ObjectMapper objectMapper;
    private HashMap<Integer, Source> sourceMap;

    SourceLoaderService() {
        this.objectMapper = new ObjectMapper();
        this.sourceMap = new HashMap<Integer, Source>();
    }

    public Map<String, Object> getSourceMapping(int source_config_id) {
        if (this.sourceMap.containsKey(source_config_id)) {
            return this.sourceMap.get(source_config_id).getMapping();
        }
        return null;
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

        List<Map<String, Object>> responseList = null;

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
            List<Map<String, Object>> mappings = (List<Map<String, Object>>) responseObject.get("mappings");

            source.setSourceConfigEntity(source_config_entity);
            source.setSourceConfigId(source_config_id);
            source.setCategory(category);
            source.setUrl(url);
            source.setDescription(description);

            // TODO: first of mapping list - not sure how to handle multiple config
            // from same url (e.g. data changes)
            Map<String, Object> mapping = (Map<String, Object>) mappings.get(0).get("mapping");
            source.setMapping(mapping);

            this.sourceMap.put(source_config_id, source);
        }

    }
}