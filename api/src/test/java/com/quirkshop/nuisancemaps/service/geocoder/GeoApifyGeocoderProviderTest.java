package com.quirkshop.nuisancemaps.service.geocoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

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
import org.mockito.Mockito;
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
public class GeoApifyGeocoderProviderTest {

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
    GeoApifyGeocoderProvider geoApifyGeocoderProvider;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeAll
    public void setUpOnce() throws IOException {
        // Source
        GeometryFactory geometryFactory = new GeometryFactory();

        ObjectMapper objectMapper = new ObjectMapper();
        File sourceJSON = resourceLoader.getResource("classpath:data/source_config_archive.json").getFile();
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
    public void buildGeoApifyURLTest() throws IOException {
        final String GEOAPIFY_API_KEY = "abc123";
        List<String> addresses = Arrays.asList("12530 N IH 35 SB, AUSTIN 78753",
                "E RUNDBERG LN / MIDDLE FISKVILLE RD, AUSTIN 78753",
                "4901 CREEK BEND DR, AUSTIN 78744",
                "1313 AIRPORT COMMERCE DR, AUSTIN 78741",
                "100 W 6TH ST, AUSTIN 78701",
                "ajdkj lsdkj ddkfjalsdkjfalskdjfakdja");

        Source source = sourceRepository.findOneBySourceConfigId(16);
        String url = geoApifyGeocoderProvider.buildAPIURL(source, addresses, GEOAPIFY_API_KEY);

        String manualURL = "https://api.geoapify.com/v1/batch/geocode/search?apiKey="
                + GEOAPIFY_API_KEY + "&lang=en&bias=proximity:-97.733330,30.266666";

        assertThat(url).isEqualTo(manualURL);
    }

    @Test
    @Transactional
    public void buildGeoApifyURLSizeTest() throws IOException {
        final String GEOAPIFY_API_KEY = "abc123";
        int batchSize = geoApifyGeocoderProvider.getBatchSize();

        List<String> addressesValid = Arrays.asList(new String[batchSize]);
        List<String> addressesErr = Arrays.asList(new String[batchSize + 1]);

        Source source = sourceRepository.findOneBySourceConfigId(16);
        geoApifyGeocoderProvider.buildAPIURL(source, addressesValid, GEOAPIFY_API_KEY);

        assertThatThrownBy(() -> {
            geoApifyGeocoderProvider.buildAPIURL(source, addressesErr, GEOAPIFY_API_KEY);
        }).isInstanceOf(Error.class)
                .hasMessage("Exceed API Batch Size");
    }

    @Test
    @Transactional
    public void parseResponseTest() throws IOException {
        Resource jsonResource = resourceLoader.getResource("classpath:data/geoapify-response.json");
        InputStream inputStream = jsonResource.getInputStream();

        List<double[]> coordinates = geoApifyGeocoderProvider.parseResponse(inputStream);

        List<double[]> manual_coordinates = Arrays.asList(null,
                new double[] { 30.3574695, -97.686501 },
                new double[] { 30.183249, -97.74837360867699 },
                new double[] { 30.222932, -97.687178 },
                new double[] { 30.268218, -97.742992 },
                null);

        assertThat(coordinates).usingRecursiveComparison().isEqualTo(manual_coordinates);
    }

    @Test
    @Transactional
    public void parseResponseEmptyTest() throws IOException {
        // submitting input, but result not found
        // returns results array with only "query" field
        Resource jsonResource = resourceLoader.getResource("classpath:data/geoapify-empty-response.json");
        InputStream inputStream = jsonResource.getInputStream();

        List<double[]> coordinates = geoApifyGeocoderProvider.parseResponse(inputStream);

        List<double[]> manual_coordinates = Arrays.asList(null, null);
        assertThat(coordinates).usingRecursiveComparison().isEqualTo(manual_coordinates);
    }

    @Test
    @Transactional
    public void makePollRequestTest() throws IOException, InterruptedException {

        when(response.code()).thenReturn(200);
        when(client.newCall(Mockito.any(Request.class))).thenReturn(call);
        when(call.execute()).thenReturn(response);

        String url = "https://thisisastub.geoapify.com?apiKey=123";
        Response r = geoApifyGeocoderProvider.makePollRequest(url, 10, 3);
        assert (r).equals(response);
    }

    @Test
    @Transactional
    public void makePollRequestTestLoop() throws IOException, InterruptedException {

        when(response.code()).thenReturn(202);
        when(client.newCall(Mockito.any(Request.class))).thenReturn(call);
        when(call.execute()).thenReturn(response);

        String url = "https://thisisastub.geoapify.com?apiKey=123";
        Response r = geoApifyGeocoderProvider.makePollRequest(url, 10, 3);

        // mock number of times request is retried
        Mockito.verify(client, times(3)).newCall(Mockito.any(Request.class));
        assertThat(r).isNull();
    }

}
