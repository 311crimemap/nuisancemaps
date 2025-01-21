package com.quirkshop.nuisancemaps.config;

import com.quirkshop.nuisancemaps.WorkerApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

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
        executor.setWaitForTasksToCompleteOnShutdown(true);

        // handler to quietly reject scheduled tasks that exceed queue
        executor.setRejectedExecutionHandler(new ConcurrentQueueRejectedExecutionHandler());
        executor.initialize();
        return executor;
    }
}

class ConcurrentQueueRejectedExecutionHandler implements RejectedExecutionHandler {

    private static final Logger log = LoggerFactory.getLogger(WorkerApplication.class);

    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {

        /*
         * No-op task
         * Async scheduled tasks with threads can take a random amount of time.
         * Scheduler begins to submit jobs that may exceed defined task queue size.
         * (see env values above)
         * At that point, jobs that cannot be queued are handled here as a "no-op".
         */

        log.debug("[ConcurrentQueueHandler] Queue exceed task rejected: " + r.toString());
    }
}
