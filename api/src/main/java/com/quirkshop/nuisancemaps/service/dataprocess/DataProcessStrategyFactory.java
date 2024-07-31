package com.quirkshop.nuisancemaps.service.dataprocess;

import com.quirkshop.nuisancemaps.config.DataProcessType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DataProcessStrategyFactory {

    @Autowired
    MemoryDataProcessStrategy memoryDataProcessStrategy;

    @Autowired
    FileDataProcessStrategy fileDataProcessStrategy;

    public DataProcessStrategy getDataProcessStrategy(DataProcessType dataProcessType) {

        switch (dataProcessType) {
            case MEMORY:
                return memoryDataProcessStrategy;
            case FILE:
                return fileDataProcessStrategy;
            default:
                throw new IllegalArgumentException("Unsupported processing strategy: " + dataProcessType);
        }
    }
}
