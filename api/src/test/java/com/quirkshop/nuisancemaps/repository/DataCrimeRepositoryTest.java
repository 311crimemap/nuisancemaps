package com.quirkshop.nuisancemaps.repository;

import org.assertj.core.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

import com.quirkshop.nuisancemaps.NuisancemapsApplication;
import com.quirkshop.nuisancemaps.model.Category;
import com.quirkshop.nuisancemaps.model.DataCrime;
import com.quirkshop.nuisancemaps.model.Mapping;
import com.quirkshop.nuisancemaps.model.Source;

@SpringBootTest(classes = NuisancemapsApplication.class)
public class DataCrimeRepositoryTest {

    @Autowired
    public DataCrimeRepository datacrime_repo;

    @Autowired
    public MappingRepository mappingRepository;

    @Autowired
    public SourceRepository sourceRepository;

    @Autowired
    public CategoryRepository categoryRepository;

    private Mapping mapping;
    private Mapping mapping2;
    private Source s;
    private Source s2;

    @BeforeEach
    public void setUp() {
        mapping = new Mapping();
        mappingRepository.save(mapping);
        s = new Source("category", "description", "url");
        s.setMapping(mapping);
        sourceRepository.save(s);

        mapping2 = new Mapping();
        mappingRepository.save(mapping2);
        s2 = new Source("category", "description", "url");
        s2.setMapping(mapping2);
        sourceRepository.save(s2);
    }

    @Test
    @Transactional
    public void DataRepositoryFindByReportNumTest() throws Exception {
        DataCrime d = new DataCrime(s);
        DataCrime d2 = new DataCrime(s);
        d.setReportNum("123");
        d2.setReportNum("abc");
        datacrime_repo.save(d);
        datacrime_repo.save(d2);
        DataCrime x = datacrime_repo.findOneByReportNum("123");
        DataCrime x2 = datacrime_repo.findOneByReportNum("abc");
        assertThat(x.getReportNum()).isEqualTo("123");
        assertThat(x2.getReportNum()).isEqualTo("abc");
    }

    @Test
    @Transactional
    public void DataRepositoryFindAllByReportNumTest() throws Exception {
        DataCrime d = new DataCrime(s);
        DataCrime d2 = new DataCrime(s);
        d.setReportNum("123");
        d2.setReportNum("abc");
        datacrime_repo.save(d);
        datacrime_repo.save(d2);

        List<String> dataCrimes = new ArrayList<String>(List.of("123", "abc"));
        List<DataCrime> results = datacrime_repo.findAllByReportNumIn(dataCrimes);
        assertThat(results.get(0).getReportNum()).isEqualTo("123");
        assertThat(results.get(1).getReportNum()).isEqualTo("abc");
    }

    @Test
    @Transactional
    public void DataRepositoryFindAllBySourceIdAndReportNumTest() throws Exception {

        DataCrime d = new DataCrime(s);
        DataCrime d2 = new DataCrime(s);
        DataCrime d3 = new DataCrime(s2);
        DataCrime d4 = new DataCrime(s);

        d.setReportNum("123");
        d2.setReportNum("456");
        d3.setReportNum("abc");
        d4.setReportNum("789");

        datacrime_repo.save(d);
        datacrime_repo.save(d2);
        datacrime_repo.save(d3);
        datacrime_repo.save(d4);

        List<String> dataCrimes = new ArrayList<String>(List.of("123", "456", "789"));
        List<DataCrime> results = datacrime_repo.findAllByReportNumIn(dataCrimes);
        assertThat(results.size()).isEqualTo(3);
        assertThat(results.get(0).getReportNum()).isEqualTo("123");
        assertThat(results.get(1).getReportNum()).isEqualTo("456");
        assertThat(results.get(2).getReportNum()).isEqualTo("789");
    }

    @Test
    @Transactional
    public void DataCrimeGeoJSONQuery() throws Exception {
        //test native query
        GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), 4326);

        double latitude = 30.2944;
        double longitude = -97.7171;
        Coordinate coordinate = new Coordinate(longitude, latitude);
        Point point = geometryFactory.createPoint(coordinate);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tenDaysAgo = now.minusDays(10);
        LocalDateTime elevenDaysAgo = now.minusDays(11);

        Category parentCat = new Category("crime", "test", 0, null);
        categoryRepository.save(parentCat);
        DataCrime d = new DataCrime(s);
        Category c = new Category("crime", "test", 1, parentCat);
        categoryRepository.save(c);

        d.setReportNum("1");
        d.setOrgCategory(c);
        d.setReportCategory("test category");
        d.setPoint(point);
        d.setLatitude(latitude);
        d.setLongitude(longitude);
        d.setReportedAt(now);

        DataCrime d2 = new DataCrime(s);
        Category c2 = new Category("crime", "test2", 2, parentCat);
        categoryRepository.save(c2);
        d2.setReportNum("2");
        d2.setOrgCategory(c2);
        d2.setReportCategory("test category 2");
        d2.setPoint(point);
        d2.setLatitude(latitude);
        d2.setLongitude(longitude);
        d2.setReportedAt(tenDaysAgo);

        DataCrime d3 = new DataCrime(s);
        Category c3 = new Category("crime", "test3", 3, null);
        categoryRepository.save(c3);
        d3.setReportNum("3");
        d3.setOrgCategory(c3);
        d3.setReportCategory("test category 3");
        d3.setReportedAt(now);

        double new_latitude = 29.2944; //OUT OF RADIUS
        coordinate = new Coordinate(longitude, new_latitude);
        point = geometryFactory.createPoint(coordinate);
        d3.setPoint(point);
        d3.setLatitude(new_latitude);
        d3.setLongitude(longitude);

        datacrime_repo.save(d);
        datacrime_repo.save(d2);
        datacrime_repo.save(d3);

        // save 3 points, 2 within 1 mile radius, 1 between date
        List<DataCrime> results = datacrime_repo.findAllByLatLngDistanceAndBetweenDates(10, latitude, longitude,
                tenDaysAgo, now);

        assertThat(results.size()).isEqualTo(2);

        results = datacrime_repo.findAllByLatLngDistanceAndBetweenDates(10, latitude, longitude, now, now);
        assertThat(results.size()).isEqualTo(1);

        results = datacrime_repo.findAllByLatLngDistanceAndBetweenDates(10, new_latitude, longitude, tenDaysAgo, now);
        assertThat(results.size()).isEqualTo(1);

        //note overflow 5700 miles
        results = datacrime_repo.findAllByLatLngDistanceAndBetweenDates(100,
                                                                        new_latitude, longitude, tenDaysAgo, now);

        assertThat(results.size()).isEqualTo(3);
    }

}
