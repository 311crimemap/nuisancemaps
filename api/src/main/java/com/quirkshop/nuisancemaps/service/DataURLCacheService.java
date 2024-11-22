package com.quirkshop.nuisancemaps.service;

import com.quirkshop.nuisancemaps.model.DataURLCache;
import com.quirkshop.nuisancemaps.repository.DataURLCacheRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

@Service
public class DataURLCacheService {

    @Autowired
    DataURLCacheRepository dataURLCacheRepository;

    @Transactional
    public void increment(DataURLCache dataURLCache) {
        if (dataURLCacheRepository.incrementCountByUrl(dataURLCache.getUrl()) == 0) {
            dataURLCacheRepository.save(dataURLCache);
        }

    }
}
