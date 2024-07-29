package com.quirkshop.nuisancemaps.service.dataprocess;

import com.quirkshop.nuisancemaps.config.DataProcessType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DataProcessStrategyFactory {

    @Autowired
    MemoryDataProcessStrategy memoryDataProcessStrategy;

    // @Autowired
    // FileDataProcessStrategy fileDataProcessStrategy;

    public DataProcessStrategy getStrategy(DataProcessType dataProcessType) {

        switch (dataProcessType) {
            case MEMORY:
                return memoryDataProcessStrategy;
            case FILE:
                // return new FileDataProcessStrategy();
                return null;
            default:
                throw new IllegalArgumentException("Unsupported processing strategy: " + dataProcessType);
        }
    }
}
