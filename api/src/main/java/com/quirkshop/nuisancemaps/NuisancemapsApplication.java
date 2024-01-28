package com.quirkshop.nuisancemaps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import com.quirkshop.nuisancemaps.model.Source;
import com.quirkshop.nuisancemaps.model.DataCrime;

import com.quirkshop.nuisancemaps.repository.SourceRepository;
import com.quirkshop.nuisancemaps.repository.DataCrimeRepository;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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

			log.info("saving initial test instances to db");
			String url = "https://data.austintexas.gov/resource/fdj4-gpfu.json?$query=SELECT%20*%20ORDER%20BY%20%60rep_date_time%60%20DESC%20NULL%20LAST";

			Source s = new Source("crime", "Austin crime", url);
			// Create a GeometryFactory
			GeometryFactory geometryFactory = new GeometryFactory();

			// Create a Coordinate using the double values
			double lat = Double.parseDouble("30.43248411");
			double lng = Double.parseDouble("-97.7359116");
			Coordinate coordinate = new Coordinate(lat, lng);

			// Create a Point using the GeometryFactory and Coordinate
			Point point = geometryFactory.createPoint(coordinate);

			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

			DataCrime d = new DataCrime(s);
			d.setReport_num("2024240131387");
			d.setCategory("DWI 2nd");
			d.setDescription("abdef");
			d.setLocation("PARKING/ DROP LOT/ GARAGE");
			d.setLatitude(lat);
			d.setLongitude(lng);
			d.setPoint(point);
			d.setReported_at(LocalDateTime.parse("2024-01-13T22:12:00.000", formatter));

			log.info("save s");
			// log.info("save d");

			// srepo.save(s);
			// crepo.save(d);

		};
	}

}
