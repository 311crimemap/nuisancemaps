package com.quirkshop.nuisancemaps.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfiguration {
    @Value("${ASYNC_EXECUTOR_CORE_POOL_SIZE:4}")
    private int corePoolSize;

    @Value("${ASYNC_EXECUTOR_MAX_POOL_SIZE:4}")
    private int maxPoolSize;

    @Value("${ASYNC_EXECUTOR_QUEUE_CAPACITY:8}")
    private int queueCapacity;

    @Bean(name = "asyncExecutor")
    public ThreadPoolTaskExecutor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize); // core pool size - average workload ~ CPU cores?
        executor.setMaxPoolSize(maxPoolSize); // bursty workloads; 2-3x core size
        executor.setQueueCapacity(queueCapacity); // capacity - new task but threads all busy; ~ maxPoolsize
        executor.setThreadNamePrefix("AsyncExecutorThread-");
        executor.initialize();
        return executor;
    }
}
