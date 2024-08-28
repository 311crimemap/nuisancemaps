package com.quirkshop.nuisancemaps.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.quirkshop.nuisancemaps.NuisancemapsApplication;
import com.quirkshop.nuisancemaps.model.Locale;
import com.quirkshop.nuisancemaps.model.Source;
import com.quirkshop.nuisancemaps.repository.GeocodeRepository;
import com.quirkshop.nuisancemaps.repository.LocaleRepository;
import com.quirkshop.nuisancemaps.repository.MappingRepository;
import com.quirkshop.nuisancemaps.repository.SourceRepository;
import com.quirkshop.nuisancemaps.service.geocoder.GeoApifyGeocoderProvider;
import com.quirkshop.nuisancemaps.service.geocoder.MapTilerGeocoderProvider;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.transaction.annotation.Transactional;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

@SpringBootTest(classes = NuisancemapsApplication.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GeocoderServiceTest {

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

    @Autowired
    GeoApifyGeocoderProvider geoApifyGeocoderProvider;

    @Autowired
    GeocoderService geocoderService;

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
    public void geocodeBatchRequestMapTilerTest() throws IOException {
        Resource jsonResource = resourceLoader.getResource("classpath:data/maptiler-response.json");

        InputStream mockInputStream = jsonResource.getInputStream();
        when(response.isSuccessful()).thenReturn(true);
        when(response.body()).thenReturn(responseBody);
        when(responseBody.byteStream()).thenReturn(mockInputStream);

        when(client.newCall(Mockito.any(Request.class))).thenReturn(call);
        when(call.execute()).thenReturn(response);
        Source source = sourceRepository.findOneBySourceConfigId(1);

        List<String> addresses = Arrays.asList("5629 N LAMAR BLVD, AUSTIN 78751",
                "2921 E 12TH ST AUSTIN 78702",
                "7918 WEST GATE BLVD, AUSTIN 78745");

        List<double[]> coordinates = geocoderService
            .geocodeBatchRequest(mapTilerGeocoderProvider, source, addresses);

        List<double[]> manual_coordinates = Arrays.asList(new double[] { 30.325383, -97.726415 },
                new double[] { 30.275579, -97.706212 },
                null);
        assertThat(coordinates).usingRecursiveComparison().isEqualTo(manual_coordinates);

    }



    @Test
    @Transactional
    public void geocodeBatchRequestSizeTest() throws IOException {

        ArrayNode rootArray = objectMapper.createArrayNode();
        List<double[]> manual_coordinates = new ArrayList<double[]>();
        final AtomicInteger callCount = new AtomicInteger(0);
        int batchSize = 50;

        // Generate 150 features
        for (int i = 0; i < batchSize; i++) {
            ObjectNode feature = objectMapper.createObjectNode();
            feature.put("relevance", 0.80);

            // Create the geometry object
            ObjectNode geometry = objectMapper.createObjectNode();
            ArrayNode coordinates = objectMapper.createArrayNode();
            coordinates.add(i); // Example coordinate values
            coordinates.add(i);
            geometry.set("coordinates", coordinates);
            feature.set("geometry", geometry);

            // Add the feature to the root array
            rootArray.add(feature);

            manual_coordinates.add(new double[] { i, i });
        }

        // Creates a fresh InputStream supplier
        // inputStream gets consumed so need to create a new inputStream per iteration
        // within geocodeBatchRequest. (Otherwise it throws err that stream is
        // closed/consumed.)
        Answer<InputStream> inputStreamAnswer = new Answer<InputStream>() {
            @Override
            public InputStream answer(InvocationOnMock invocation) throws Throwable {

                String jsonString = objectMapper.writerWithDefaultPrettyPrinter()
                        .writeValueAsString(rootArray);

                callCount.incrementAndGet(); // count number of calls

                return new ByteArrayInputStream(jsonString.getBytes());
            }
        };

        when(response.isSuccessful()).thenReturn(true);
        when(response.body()).thenReturn(responseBody);
        when(responseBody.byteStream()).thenAnswer(inputStreamAnswer);

        when(client.newCall(Mockito.any(Request.class))).thenReturn(call);
        when(call.execute()).thenReturn(response);
        Source source = sourceRepository.findOneBySourceConfigId(1);

        /* TEST */
        int num = 200; // 50, 50, 50, 50 - 4 batches
        List<String> addresses = new ArrayList<String>();
        for (int i = 0; i < num; i++) {
            addresses.add(String.valueOf(i));
        }

        List<double[]> coordinates = geocoderService
            .geocodeBatchRequest(mapTilerGeocoderProvider, source, addresses);

        assertThat(coordinates.size()).isEqualTo(num);
        assertThat(geocodeRepository.count()).isEqualTo(num);
        assertThat(callCount.get()).isEqualTo(num / batchSize);
    }
}
