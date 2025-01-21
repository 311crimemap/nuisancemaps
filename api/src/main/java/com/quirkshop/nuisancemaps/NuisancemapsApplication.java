package com.quirkshop.nuisancemaps;

import com.quirkshop.nuisancemaps.service.WorkerScheduleService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

@SpringBootApplication
@EnableCaching
@ComponentScan(
        // Excludes worker, @EnableScheduling annotation (api and related backend only)
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = WorkerApplication.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = WorkerScheduleService.class),
        })
public class NuisancemapsApplication {

    public static void main(String[] args) {
        SpringApplication.run(NuisancemapsApplication.class, args);
    }

}
