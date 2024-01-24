package com.quirkshop.nuisancemaps.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.transaction.annotation.Transactional;

import com.quirkshop.nuisancemaps.NuisancemapsApplication;
import com.quirkshop.nuisancemaps.model.DataCrime;
import com.quirkshop.nuisancemaps.model.DataJob;
import com.quirkshop.nuisancemaps.model.Source;
import com.quirkshop.nuisancemaps.repository.DataCrimeRepository;
import com.quirkshop.nuisancemaps.repository.SourceRepository;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.util.FileCopyUtils;
import org.springframework.web.client.RestTemplate;

@SpringBootTest(classes = NuisancemapsApplication.class)
public class DataJobRequestServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private DataCrimeRepository datacrime_repo;

    @Mock
    private SourceRepository source_repo;

    @Autowired
    private ResourceLoader resourceLoader;

    @InjectMocks
    private DataJobRequestServiceImpl dataJobRequestService;

    @Test
    @Transactional
    void testFetchDataWithMock() throws IOException {

        Resource jsonResource = resourceLoader.getResource("classpath:data/crime-atx.json");

        // Read the content of the JSON file
        String jsonFixtureContent = new String(FileCopyUtils.copyToByteArray(jsonResource.getInputStream()),
                StandardCharsets.UTF_8);

        // Source
        Source s = new Source("test", "testDescription", "https://data.austintexas.gov/resource/fdj4-gpfu.json");
        s.setId(1);
        when(source_repo.save(Mockito.any(Source.class))).thenReturn(s);

        // DataJob
        DataJob datajob = new DataJob(s, 100, 50, "id");
        datajob.buildURL();

        // Mock restTemplate to return the jsonFixtureContent if it ever makes a request
        // to url
        // this @Mock restTemplate is D.I'd into dataJobRequestService.fetchJSON(s)
        // below
        when(restTemplate.getForObject(datajob.getUrl(), String.class))
                .thenReturn(jsonFixtureContent);

        // build mock save result
        // DataCrime d = new DataCrime();
        // when(datacrime_repo.save(Mockito.any(DataCrime.class))).thenReturn(d);

        String result = dataJobRequestService.fetchJSON(datajob);

        assertThat(result).isEqualTo(jsonFixtureContent);

        int num = dataJobRequestService.createData();

        // num elements in fixture crime-atx
        assertThat(num).isEqualTo(2);

        // verify(restTemplate).getForObject(datajob.getUrl(), String.class);

    }
}
