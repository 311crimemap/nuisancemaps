package com.quirkshop.nuisancemaps.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.quirkshop.nuisancemaps.NuisancemapsApplication;
import com.quirkshop.nuisancemaps.model.DataCrime;
import com.quirkshop.nuisancemaps.model.Source;
import com.quirkshop.nuisancemaps.model.Test;
import com.quirkshop.nuisancemaps.repository.TestRepository;

import com.quirkshop.nuisancemaps.repository.SourceRepository;
import com.quirkshop.nuisancemaps.repository.DataCrimeRepository;

@RestController
public class TestController {
    @Autowired
    private TestRepository testRepository;

    @Autowired
    private SourceRepository srepo;

    @Autowired
    private DataCrimeRepository crepo;

    // @Autowired
    private static final Logger log = LoggerFactory.getLogger(NuisancemapsApplication.class);

    @PostMapping(path = "/test/create") // Map ONLY POST Requests
    public @ResponseBody String create(@RequestParam String name, Integer age) {
        // @ResponseBody means the returned String is the response, not a view name
        // @RequestParam means it is a parameter from the GET or POST request
        // curl -X POST -d name=Howdy age=1234 localhost:8080/test/create
        Test t = new Test(name, age);
        testRepository.save(t);
        return "Saved";
    };

    // NB: terminal slash not included; path is explicit
    // likely defer this handling to nginx
    @GetMapping(path = "/tests")
    public @ResponseBody Iterable<Test> index() {
        return testRepository.findAll();
    };

    @GetMapping(path = "/tests/{id}")
    public @ResponseBody Optional<Test> get(@PathVariable(value = "id") final int id) {
        return testRepository.findById(id);
    };

    @GetMapping(path = "/tests/associations")
    public @ResponseBody String get() {

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
        d.setReportNum("2024240131387");
        d.setCategory("DWI 2nd");
        d.setDescription("abdef");
        d.setLocation("PARKING/ DROP LOT/ GARAGE");
        d.setLatitude(lat);
        d.setLongitude(lng);
        d.setPoint(point);
        d.setReportedAt(LocalDateTime.parse("2024-01-13T22:12:00.000", formatter));

        log.info("save s");
        log.info("save d");

        srepo.save(s);

        List<DataCrime> l5 = s.getDataCrimes();
        log.info("pre save s: ", l5);

        crepo.save(d);

        // entityManager.flush();

        List<DataCrime> l4 = s.getDataCrimes();
        log.info("s dpost save atacrimes: ", l4);

        for (DataCrime dl : l4) {
            log.info("[sorig] " + dl.getId() + " " + dl.getPoint());
        }

        Source s2 = d.getSource();
        log.info("S2: " + s2.getId());

        // s2Query
        Source s2QueriedEntity = srepo.findById(s2.getId()).get();
        log.info("SourceEntity: " + s2QueriedEntity.getId());
        // s2QueriedEntity.getDataCrimes().size();

        // entityManager.refresh(sourceEntity); //I think it's the refresh
        List<DataCrime> l = s2QueriedEntity.getDataCrimes();
        for (DataCrime dl : l) {
            log.info("[s2Query] " + dl.getId() + " " + dl.getPoint());
        }

        // entityManager.refresh(s2); //I think it's the refresh
        List<DataCrime> l2 = s2.getDataCrimes();
        // s2 is from d.getSource()
        // at this point s2 - shouldn't know right?
        // s2 is saved standalone. s2 id gets put into a crime.source_id
        // but the s2 object itself isn't updated

        log.info("s2 datacrimes: ", l2);

        for (DataCrime dl : l2) {
            log.info("[s2] " + dl.getId() + " " + dl.getPoint());
        }

        List<DataCrime> l3 = s.getDataCrimes();
        log.info("new s datacrimes: ", l3);

        for (DataCrime dl : l3) {
            log.info("[s3] " + dl.getId() + " " + dl.getPoint());
        }

        log.info("");

        return "Hi";
    }
}
