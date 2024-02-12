package com.quirkshop.nuisancemaps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import com.quirkshop.nuisancemaps.repository.SourceRepository;
import com.quirkshop.nuisancemaps.service.WorkerScheduleService;
import com.quirkshop.nuisancemaps.repository.DataCrimeRepository;

@SpringBootApplication
@ComponentScan(
        // Excludes worker, @EnableScheduling annotation (api and related backend only)
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = WorkerApplication.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = WorkerScheduleService.class),
        })
public class NuisancemapsApplication {

    private static final Logger log = LoggerFactory.getLogger(NuisancemapsApplication.class);

    public static void main(String[] args) {

        // output before "spring" logo
        System.out.println("println pre");
        log.info("log pre");

        SpringApplication.run(NuisancemapsApplication.class, args);

        // output after load
        System.out.println("println post");
        log.info("log post");
    }

    @Bean
    public CommandLineRunner doesntmatterwhatthisiscalled(SourceRepository srepo, DataCrimeRepository crepo) {
        return args -> {
            log.info("[NuisancemapsApplication] CommandLineRunner");
        };
    }

}
