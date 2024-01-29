package com.quirkshop.nuisancemaps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;
import com.quirkshop.nuisancemaps.repository.SourceRepository;
import com.quirkshop.nuisancemaps.repository.DataCrimeRepository;


@SpringBootApplication
public class NuisancemapsApplication {

	private static final Logger log = LoggerFactory.getLogger(NuisancemapsApplication.class);

	/*
	 * @Autowired
	 * private EntityManager entityManager;
	 */
	public static void main(String[] args) {

		// output before "spring" logo
		System.out.println("println pre");
		log.info("log pre");

		SpringApplication.run(NuisancemapsApplication.class, args);

		// output after load
		System.out.println("println post");
		log.info("log post");
	}

	// used in DataJobRequest
	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
		return builder.build();
	}

	@Bean
	public CommandLineRunner doesntmatterwhatthisiscalled(SourceRepository srepo, DataCrimeRepository crepo) {
		return args -> {
			log.info("[NuisancemapsApplication] CommandLineRunner");
		};
	}

}
