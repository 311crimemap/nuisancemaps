package com.quirkshop.nuisancemaps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.quirkshop.nuisancemaps.model.Test;
import com.quirkshop.nuisancemaps.repository.TestRepository;

@SpringBootApplication
public class NuisancemapsApplication {

	private static final Logger log = LoggerFactory.getLogger(NuisancemapsApplication.class);

	public static void main(String[] args) {

		// doesn't work
		System.out.println("println pre");
		log.info("log pre");

		SpringApplication.run(NuisancemapsApplication.class, args);

		// works
		System.out.println("println post");
		log.info("log post");
	}

	@Bean
	public CommandLineRunner doesntmatterwhatthisiscalled(TestRepository repository) {

		return (args) -> {

			// save a few Tests
			// this is useful for seeding the DB when hibernate.ddl-auto is create
			log.info("saving initial test instances to db");
			repository.save(new Test("testopresto"));
			repository.save(new Test("test"));
			log.info("");
		};
	}
}
