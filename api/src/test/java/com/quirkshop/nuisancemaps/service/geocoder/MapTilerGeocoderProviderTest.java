package com.quirkshop.nuisancemaps.service.geocoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quirkshop.nuisancemaps.NuisancemapsApplication;
import com.quirkshop.nuisancemaps.model.Locale;
import com.quirkshop.nuisancemaps.model.Source;
import com.quirkshop.nuisancemaps.repository.GeocodeRepository;
import com.quirkshop.nuisancemaps.repository.LocaleRepository;
import com.quirkshop.nuisancemaps.repository.MappingRepository;
import com.quirkshop.nuisancemaps.repository.SourceRepository;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.transaction.annotation.Transactional;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.ResponseBody;

@SpringBootTest(classes = NuisancemapsApplication.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MapTilerGeocoderProviderTest {

    @Autowired
    private ResourceLoader resourceLoader;

    @Mock
    private Call call;

    @Mock
    private Response response;

    @Mock
    private ResponseBody responseBody;

    @MockBean
    private OkHttpClient client;

    @Autowired
    private LocaleRepository localeRepository;

    @Autowired
    private MappingRepository mappingRepository;

    @Autowired
    private SourceRepository sourceRepository;

    @Autowired
    GeocodeRepository geocodeRepository;

    @Autowired
    MapTilerGeocoderProvider mapTilerGeocoderProvider;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeAll
    public void setUpOnce() throws IOException {
        // Source
        GeometryFactory geometryFactory = new GeometryFactory();

        ObjectMapper objectMapper = new ObjectMapper();
        File sourceJSON = resourceLoader.getResource("classpath:data/source_config.json").getFile();
        List<Source> sources = objectMapper.readValue(sourceJSON, new TypeReference<List<Source>>() {
        });

        for (Source s : sources) {
            Locale locale = new Locale();
            Coordinate coordinate = new Coordinate(-97.733330, 30.266666);
            Point point = geometryFactory.createPoint(coordinate);
            locale.setLocation(point);
            localeRepository.save(locale);
            s.setLocale(locale);

            mappingRepository.save(s.getMapping());
            sourceRepository.save(s);
        }
    }

    @AfterAll
    public void tearDown() throws IOException {
        sourceRepository.deleteAll();
        mappingRepository.deleteAll();
        localeRepository.deleteAll();
    }

    @Test
    @Transactional
    public void buildMapTilerURLTest() throws IOException {
        final String MAPTILER_API_KEY = "abc123";
        List<String> addresses = Arrays.asList("5629 N LAMAR BLVD, AUSTIN 78751",
                "2921 E 12TH ST AUSTIN 78702",
                "7918 WEST GATE BLVD, AUSTIN 78745");

        Source source = sourceRepository.findOneBySourceConfigId(16);
        String url = mapTilerGeocoderProvider.buildAPIURL(source, addresses, MAPTILER_API_KEY);

        String manualURL = "https://api.maptiler.com/geocoding/5629%20N%20LAMAR%20BLVD,%20AUSTIN%2078751;2921%20E%2012TH%20ST%20AUSTIN%2078702;7918%20WEST%20GATE%20BLVD,%20AUSTIN%2078745.json?language=en&country=us&proximity=-97.733330,30.266666&key="
                + MAPTILER_API_KEY;

        assertThat(url).isEqualTo(manualURL);
    }

    @Test
    @Transactional
    public void buildMapTilerURLSizeTest() throws IOException {
        final String MAPTILER_API_KEY = "abc123";
        List<String> addressesValid = Arrays.asList(new String[50]);
        List<String> addressesErr = Arrays.asList(new String[51]);

        Source source = sourceRepository.findOneBySourceConfigId(16);
        mapTilerGeocoderProvider.buildAPIURL(source, addressesValid, MAPTILER_API_KEY);

        assertThatThrownBy(() -> {
            mapTilerGeocoderProvider.buildAPIURL(source, addressesErr, MAPTILER_API_KEY);
        }).isInstanceOf(Error.class)
                .hasMessage("Exceed API Batch Size");
    }

    @Test
    @Transactional
    public void parseResponseTest() throws IOException {
        Resource jsonResource = resourceLoader.getResource("classpath:data/maptiler-response.json");
        InputStream inputStream = jsonResource.getInputStream();

        List<double[]> coordinates = mapTilerGeocoderProvider.parseResponse(inputStream);

        List<double[]> manual_coordinates = Arrays.asList(new double[] { 30.325383, -97.726415 },
                new double[] { 30.275579, -97.706212 },
                null);
        assertThat(coordinates).usingRecursiveComparison().isEqualTo(manual_coordinates);

    }

    @Test
    @Transactional
    public void parseResponseEmptyTest() throws IOException {
        Resource jsonResource = resourceLoader.getResource("classpath:data/maptiler-empty-response.json");
        InputStream inputStream = jsonResource.getInputStream();

        List<double[]> coordinates = mapTilerGeocoderProvider.parseResponse(inputStream);

        List<double[]> manual_coordinates = Arrays.asList(null, null);
        assertThat(coordinates).usingRecursiveComparison().isEqualTo(manual_coordinates);
    }

}
