package com.quirkshop.nuisancemaps.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quirkshop.nuisancemaps.NuisancemapsApplication;
import com.quirkshop.nuisancemaps.model.Geocode;
import com.quirkshop.nuisancemaps.model.Locale;
import com.quirkshop.nuisancemaps.model.Source;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ResourceLoader;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceException;

@SpringBootTest(classes = NuisancemapsApplication.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GeocodeRepositoryTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    public LocaleRepository localeRepository;

    @Autowired
    public SourceRepository sourceRepository;

    @Autowired
    public MappingRepository mappingRepository;

    @Autowired
    public GeocodeRepository geocodeRepository;

    @BeforeAll
    public void setUpOnce() throws IOException {

        // Source
        ObjectMapper objectMapper = new ObjectMapper();
        File sourceJSON = resourceLoader.getResource("classpath:data/source_config_archive.json").getFile();
        List<Source> sources = objectMapper.readValue(sourceJSON, new TypeReference<List<Source>>() {
        });
        for (Source s : sources) {
            Locale locale = new Locale();
            localeRepository.save(locale);
            mappingRepository.save(s.getMapping());
            s.setLocale(locale);
            sourceRepository.save(s);
        }
    }

    @AfterAll
    public void tearDown() throws IOException {
        sourceRepository.deleteAll();
        mappingRepository.deleteAll();
        localeRepository.deleteAll();
        geocodeRepository.deleteAll();
    }

    @Test
    @Transactional
    public void GeocodeRepositoryFindGeocodesByAddressesTest() {
        List<String> addresses = Arrays.asList("1600 Pennsylvania Ave, Washington DC",
                "123 New York City, NY 11104",
                "Non existent address2, TX 78702",
                "ABC Los Angeles, CA 90210",
                "Non existent address, TX 78701");

        Source s = sourceRepository.findOneBySourceConfigId(16);
        Geocode g1 = new Geocode(s, addresses.get(0), 30.000, 50.000);
        Geocode g2 = new Geocode(s, addresses.get(1), 31.000, 51.000);
        Geocode g3 = new Geocode(s, addresses.get(3), 32.000, 52.000);

        geocodeRepository.saveAll(List.of(g1, g2, g3));
        assertThat(geocodeRepository.count()).isEqualTo(3);

        // skip the non-existent
        List<Geocode> results = geocodeRepository
                .findBySourceAndAddressInAndLatitudeIsNotNullAndLongitudeIsNotNull(s, addresses);

        assertThat(results.get(0).getAddress()).isEqualTo(addresses.get(0));
        assertThat(results.get(1).getAddress()).isEqualTo(addresses.get(1));
        assertThat(results.get(2).getAddress()).isEqualTo(addresses.get(3));

    }

    @Test
    @Transactional
    public void EnforceUniquenessConstraint() {
        List<String> addresses = Arrays.asList("1600 Pennsylvania Ave, Washington DC",
                "123 New York City, NY 11104");

        Source s1 = sourceRepository.findOneBySourceConfigId(1);
        Source s2 = sourceRepository.findOneBySourceConfigId(16);

        Geocode g1 = new Geocode(s1, addresses.get(0), 30.000, 50.000);
        Geocode g2 = new Geocode(s2, addresses.get(0), 31.000, 51.000);
        Geocode g3 = new Geocode(s1, addresses.get(1), 32.000, 52.000);
        Geocode g4 = new Geocode(s2, addresses.get(1), 32.000, 52.000);

        // violates uniqueness
        Geocode g5 = new Geocode(s2, addresses.get(1), 32.000, 52.000);

        assertThat(geocodeRepository.count()).isEqualTo(0);
        geocodeRepository.saveAll(List.of(g1, g2, g3, g4));
        assertThat(geocodeRepository.count()).isEqualTo(4);
        entityManager.flush();

        assertThrows(PersistenceException.class, () -> {
            geocodeRepository.save(g5);
            entityManager.flush();
        });

        // NB: transaction state is in flux after throws
        // subsequent repo actions will break
    }
}
