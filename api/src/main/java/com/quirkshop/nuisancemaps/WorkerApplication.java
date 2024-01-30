package com.quirkshop.nuisancemaps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ConditionalOnProperty(value = "app.scheduling.enabled", matchIfMissing = true, havingValue = "true")
@EnableScheduling
public class WorkerApplication {
    private static final Logger log = LoggerFactory.getLogger(WorkerApplication.class);

    public static void main(String args[]) {
        log.info("WorkerApplication pre");
        SpringApplication.run(WorkerApplication.class, args);
        log.info("WorkerApplication post");
    }
}
