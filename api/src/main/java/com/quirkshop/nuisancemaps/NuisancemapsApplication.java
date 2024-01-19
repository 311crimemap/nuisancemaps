package com.quirkshop.nuisancemaps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import com.quirkshop.nuisancemaps.model.Source;
import com.quirkshop.nuisancemaps.model.DataCrime;
import com.quirkshop.nuisancemaps.model.Test;
import com.quirkshop.nuisancemaps.repository.TestRepository;

import jakarta.persistence.EntityManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import com.quirkshop.nuisancemaps.repository.SourceRepository;
import com.quirkshop.nuisancemaps.repository.DataCrimeRepository;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@SpringBootApplication
public class NuisancemapsApplication {

	private static final Logger log = LoggerFactory.getLogger(NuisancemapsApplication.class);

	@Autowired
	private EntityManager entityManager;
	@Autowired
	private TransactionTemplate transactionTemplate;

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
			transactionTemplate.execute(status -> {
				executeTransactionalLogic(srepo, crepo);
				return null;
			});
		};
	}

	private void executeTransactionalLogic(SourceRepository srepo, DataCrimeRepository crepo) {

		// save a few Tests
		// this is useful for seeding the DB when hibernate.ddl-auto is create
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

		DataCrime d = new DataCrime(s,
				"2024240131387",
				"DWI 2nd", "abdef", "PARKING/ DROP LOT/ GARAGE",
				lat, lng, point, LocalDateTime.parse("2024-01-13T22:12:00.000", formatter));

		srepo.save(s);
		crepo.save(d);
		
		entityManager.flush();

		Source s2 = d.getSource();
		log.info("S2: " + s2.getId());

		Source sourceEntity = srepo.findById(s2.getId()).get();
		log.info("SourceEntity: " + sourceEntity.getId());
		entityManager.refresh(sourceEntity);   //I think it's the refresh
		List<DataCrime> l = sourceEntity.getDataCrimes();
		for (DataCrime dl : l) {
			log.info(dl.getId() + " " + dl.getPoint());
		}

		entityManager.refresh(s2);   //I think it's the refresh
		List<DataCrime> l2 = s2.getDataCrimes();
		log.info("ID: " + s.getId());

		for (DataCrime dl : l2) {
			log.info(dl.getId() + " " + dl.getPoint());
		}

		// repository.save(new Test("testopresto", 1));
		// repository.save(new Test("test", 2));
		log.info("");
	}
}
