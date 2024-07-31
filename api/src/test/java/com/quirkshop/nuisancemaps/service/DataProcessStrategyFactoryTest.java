package com.quirkshop.nuisancemaps.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quirkshop.nuisancemaps.NuisancemapsApplication;
import com.quirkshop.nuisancemaps.config.DataProcessType;
import com.quirkshop.nuisancemaps.model.DataJob;
import com.quirkshop.nuisancemaps.model.DataJobStatus;
import com.quirkshop.nuisancemaps.model.Source;
import com.quirkshop.nuisancemaps.repository.DataCrimeRepository;
import com.quirkshop.nuisancemaps.repository.DataJobRepository;
import com.quirkshop.nuisancemaps.repository.SourceRepository;
import com.quirkshop.nuisancemaps.service.dataprocess.DataProcessStrategy;
import com.quirkshop.nuisancemaps.service.dataprocess.DataProcessStrategyFactory;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;

import org.springframework.util.FileCopyUtils;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

@SpringBootTest(classes = NuisancemapsApplication.class)
public class DataProcessStrategyFactoryTest {

    @Mock
    private Call call;

    @Mock
    private Response response;

    @Mock
    private ResponseBody responseBody;

    @MockBean
    private OkHttpClient client;

    @MockBean
    private DataCrimeRepository datacrime_repo;

    @MockBean
    private SourceRepository source_repo;

    @MockBean
    DataJobRepository dataJobRepository;

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private DataProcessStrategyFactory dataProcessStrategyFactory;

    @Test
    @Transactional
    void testFetchDataWithMock() throws IOException {

        Resource jsonResource = resourceLoader.getResource("classpath:data/crime-atx.json");

        // Read the content of the JSON file
        String jsonFixtureContent = new String(FileCopyUtils.copyToByteArray(jsonResource.getInputStream()),
                StandardCharsets.UTF_8);

        // Source
        ObjectMapper objectMapper = new ObjectMapper();
        File sourceJSON = resourceLoader.getResource("classpath:data/source_config.json").getFile();

        List<Source> sources = objectMapper.readValue(sourceJSON, new TypeReference<List<Source>>() {
        });

        Source s = sources.get(0);

        when(source_repo.save(Mockito.any(Source.class))).thenReturn(s);

        // DataJob
        DataJob datajob = new DataJob(LocalDateTime.now(), s, 100, 50, "id");
        datajob.buildURL();
        assertThat(datajob.getStatus()).isEqualTo(DataJobStatus.QUEUED);

        // Mock okHttpClient to return the jsonFixtureContent if it ever makes a
        // request to url; the client.newCall(), call and execute() are set to
        // return mocked response, responseBody (see @Autowire above)
        //
        // Scanner class converts inputStream to String to compare response
        // values

        InputStream mockInputStream = jsonResource.getInputStream();
        when(response.isSuccessful()).thenReturn(true);
        when(response.body()).thenReturn(responseBody);
        when(responseBody.byteStream()).thenReturn(mockInputStream);

        when(client.newCall(Mockito.any(Request.class))).thenReturn(call);
        when(call.execute()).thenReturn(response);

        // build mock save result
        // DataCrime d = new DataCrime();
        // when(datacrime_repo.save(Mockito.any(DataCrime.class))).thenReturn(d);

        DataProcessStrategy dataProcessStrategy = dataProcessStrategyFactory
                .getDataProcessStrategy(DataProcessType.MEMORY);

        InputStream inputStream2 = dataProcessStrategy.fetchData(datajob);

        try (Scanner scanner = new Scanner(inputStream2, StandardCharsets.UTF_8.name())) {
            String result = scanner.useDelimiter("\\A").next();
            assertThat(result).isEqualTo(jsonFixtureContent);
        }

        assertThat(datajob.getStatus()).isEqualTo(DataJobStatus.FETCH_COMPLETE);

        // int num = dataJobRequestService.createData();

        // num elements in fixture crime-atx
        // assertThat(num).isEqualTo(2);

    }
}
