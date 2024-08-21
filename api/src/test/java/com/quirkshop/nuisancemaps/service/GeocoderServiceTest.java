package com.quirkshop.nuisancemaps.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quirkshop.nuisancemaps.NuisancemapsApplication;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
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
    GeocoderService geocoderService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @Transactional
    public void buildMapTilerURLTest() throws IOException {
        final String MAPTILER_API_KEY = "abc123";
        List<String> addresses = Arrays.asList("5629 N LAMAR BLVD, AUSTIN 78751",
                "2921 E 12TH ST AUSTIN 78702",
                "7918 WEST GATE BLVD, AUSTIN 78745");

        String url = geocoderService.buildMapTilerURL(addresses, MAPTILER_API_KEY);

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

        geocoderService.buildMapTilerURL(addressesValid, MAPTILER_API_KEY);

        assertThatThrownBy(() -> {
            geocoderService.buildMapTilerURL(addressesErr, MAPTILER_API_KEY);
        }).isInstanceOf(Error.class)
                .hasMessage("Exceed API Batch Size");
    }

    @Test
    @Transactional
    public void parseResponseTest() throws IOException {
        Resource jsonResource = resourceLoader.getResource("classpath:data/maptiler-response.json");
        InputStream inputStream = jsonResource.getInputStream();

        List<double[]> coordinates = geocoderService.parseResponse(inputStream);

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

        List<double[]> coordinates = geocoderService.parseResponse(inputStream);

        List<double[]> manual_coordinates = Arrays.asList(null, null);
        assertThat(coordinates).usingRecursiveComparison().isEqualTo(manual_coordinates);
    }

    @Test
    @Transactional
    public void geocodeBatchRequestTest() throws IOException {
        Resource jsonResource = resourceLoader.getResource("classpath:data/maptiler-response.json");

        InputStream mockInputStream = jsonResource.getInputStream();
        when(response.isSuccessful()).thenReturn(true);
        when(response.body()).thenReturn(responseBody);
        when(responseBody.byteStream()).thenReturn(mockInputStream);

        when(client.newCall(Mockito.any(Request.class))).thenReturn(call);
        when(call.execute()).thenReturn(response);

        List<String> addresses = Arrays.asList("5629 N LAMAR BLVD, AUSTIN 78751",
                "2921 E 12TH ST AUSTIN 78702",
                "7918 WEST GATE BLVD, AUSTIN 78745");

        List<double[]> coordinates = geocoderService.geocodeBatchRequest(addresses);

        List<double[]> manual_coordinates = Arrays.asList(new double[] { 30.325383, -97.726415 },
                new double[] { 30.275579, -97.706212 },
                null);
        assertThat(coordinates).usingRecursiveComparison().isEqualTo(manual_coordinates);

    }

    @Test
    @Transactional
    public void geocodeBatchRequestSizeTest() throws IOException {
        Resource jsonResource = resourceLoader.getResource("classpath:data/maptiler-response.json");

        InputStream mockInputStream = jsonResource.getInputStream();

        // Create a fresh InputStream supplier
        // inputStream gets consumed so need to create a new inputStream per iteration
        // within geocodeBatchRequest. (Otherwise it throws err that stream is
        // closed/consumed.)
        Answer<InputStream> inputStreamAnswer = new Answer<InputStream>() {
            @Override
            public InputStream answer(InvocationOnMock invocation) throws Throwable {
                return jsonResource.getInputStream(); // Return a new InputStream each time
            }
        };

        when(response.isSuccessful()).thenReturn(true);
        when(response.body()).thenReturn(responseBody);
        when(responseBody.byteStream()).thenAnswer(inputStreamAnswer);

        when(client.newCall(Mockito.any(Request.class))).thenReturn(call);
        when(call.execute()).thenReturn(response);

        List<String> addresses = Arrays.asList(new String[151]); // 4 batches (50, 50, 50, 1)

        List<double[]> coordinates = geocoderService.geocodeBatchRequest(addresses);

        List<double[]> manual_coordinates = Arrays.asList(new double[] { 30.325383, -97.726415 },
                new double[] { 30.275579, -97.706212 },
                null);

        // test batches given array of addresses size 151 - result in 4 batches
        // each batch api response mocked with return of set of 3 manual_coordinates
        // (because I'm lazy)
        // yes 50 requests -> 3 response, but its fine. Test is to counting batches.
        assertThat(coordinates.size()).isEqualTo(4 * 3);
        assertThat(coordinates.subList(0, 3)).usingRecursiveComparison().isEqualTo(manual_coordinates);
        assertThat(coordinates.subList(3, 6)).usingRecursiveComparison().isEqualTo(manual_coordinates);
        assertThat(coordinates.subList(6, 9)).usingRecursiveComparison().isEqualTo(manual_coordinates);
        assertThat(coordinates.subList(9, 12)).usingRecursiveComparison().isEqualTo(manual_coordinates);

    }
}
