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

@Service
public class SourceLoaderService {

    @Autowired
    MappingRepository mappingRepository;

    @Autowired
    SourceRepository sourceRepository;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Updates an existing Source entity identified by the given ID with the
     * specified updates. It will also update the associated Mapping entity if
     * mapping updates are provided.
     *
     * @param id      the ID of the Source entity
     * @param updates a map containing the fields to update in the Source and
     *                Mapping
     *
     * @return the updated Source entity
     *
     * @throws JsonMappingException
     */
    public Source updateSource(int id, Map<String, Object> updates) throws JsonMappingException {
        Source source = sourceRepository.findById(id).orElse(null);
        Mapping mapping = source.getMapping();

        // NB: need to updates.remove otherwise results in unsaved transient instance
        Map<String, Object> mappingUpdates = (Map<String, Object>) updates.remove("mapping");
        objectMapper.updateValue(mapping, mappingUpdates);
        objectMapper.updateValue(source, updates);
        return sourceRepository.save(source);
    }

    /**
     * Saves a Source entity and its associated Mapping entity within a
     * transaction.
     *
     * @Transactional in service so throws error inside service scope versus
     *                controller action
     *
     * @param source the Source entity to save
     * @return the saved Source entity
     */

    @Transactional
    public Source saveTransaction(Source source) {
        mappingRepository.save(source.getMapping());
        source = sourceRepository.save(source);
        return source;
    }
}
