package com.quirkshop.nuisancemaps.service;

import java.util.Map;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quirkshop.nuisancemaps.model.Mapping;
import com.quirkshop.nuisancemaps.model.Source;
import com.quirkshop.nuisancemaps.repository.MappingRepository;
import com.quirkshop.nuisancemaps.repository.SourceRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

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
}
