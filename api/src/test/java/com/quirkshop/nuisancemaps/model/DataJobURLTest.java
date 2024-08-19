package com.quirkshop.nuisancemaps.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quirkshop.nuisancemaps.NuisancemapsApplication;
import com.quirkshop.nuisancemaps.model.datajob.BaseURL;
import com.quirkshop.nuisancemaps.model.datajob.DataJob;
import com.quirkshop.nuisancemaps.model.datajob.ERSIURL;
import com.quirkshop.nuisancemaps.model.datajob.OpenDataURL;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ResourceLoader;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = NuisancemapsApplication.class)
public class DataJobURLTest {

    @Autowired
    private ResourceLoader resourceLoader;

    List<Source> sources;

    @BeforeAll
    public void setUp() throws IOException {

        ObjectMapper objectMapper = new ObjectMapper();
        File sourceJSON = resourceLoader.getResource("classpath:data/source_config.json").getFile();
        sources = objectMapper.readValue(sourceJSON, new TypeReference<List<Source>>() {
        });

    }

    @Test
    public void BaseURL_buildInitURL_Test() {
        Source source = sources.get(11);
        DataJob dataJob = new DataJob(LocalDateTime.now(), source, "id");
        BaseURL baseURL = new BaseURL();

        String url = baseURL.buildInitURL(dataJob);
        String canonURL = source.getUrl();

        assertThat(url).isEqualTo(canonURL);

        // test DataJob factory
        dataJob.initURL();
        assertThat(url).isEqualTo(dataJob.getUrl());
    }

    @Test
    public void BaseURL_buildNextURL_Test() {
        Source source = sources.get(11);
        DataJob dataJob = new DataJob(LocalDateTime.now(), source, "id");
        BaseURL baseURL = new BaseURL();

        String url = baseURL.buildInitURL(dataJob);
        assertThat(url).isEqualTo(source.getUrl());

        url = baseURL.buildNextURL(dataJob);
        assertThat(url).isNull();

        // test DataJob factory
        dataJob.initURL();
        String dataJobNextURL = dataJob.buildNextURL();
        assertThat(url).isEqualTo(dataJobNextURL);
    }

    @Test
    public void OpenDataURL_buildInitURL_Test() {
        Source source = sources.get(1);
        DataJob dataJob = new DataJob(LocalDateTime.now(), source, "id");
        OpenDataURL openDataURL = new OpenDataURL();

        String url = openDataURL.buildInitURL(dataJob);

        String limit = System.getenv("WORKER_QUERY_LIMIT");
        String offset = "0";
        String order_key = "id";
        String select = openDataURL.buildURLFields(source.getMapping());
        String canonURL = source.getUrl() + "?$limit=" + limit + "&$offset=" + offset + "&$order=" +
                order_key + "&$select=" + select;

        assertThat(url).isEqualTo(canonURL);

        // test DataJob factory
        dataJob.initURL();
        assertThat(url).isEqualTo(dataJob.getUrl());
    }

    @Test
    public void OpenDataURL_buildNextURL_Test() {
        Source source = sources.get(1);
        DataJob dataJob = new DataJob(LocalDateTime.now(), source, "id");
        OpenDataURL openDataURL = new OpenDataURL();

        openDataURL.buildInitURL(dataJob); // offset: 0
        openDataURL.buildNextURL(dataJob); // offset: 1 * PARAM_LIMIT
        openDataURL.buildNextURL(dataJob); // offset: 2 * PARAM_LIMIT
        String url = openDataURL.buildNextURL(dataJob); // offset: 3 * PARAM_LIMIT

        String limit = System.getenv("WORKER_QUERY_LIMIT");
        String offset = String.valueOf(3 * Integer.parseInt(limit));
        String order_key = "id";
        String select = openDataURL.buildURLFields(source.getMapping());
        String canonURL = source.getUrl() + "?$limit=" + limit + "&$offset=" + offset + "&$order=" +
                order_key + "&$select=" + select;

        assertThat(url).isEqualTo(canonURL);

        // test DataJob factory
        dataJob.initURL();
        dataJob.buildNextURL();
        dataJob.buildNextURL();
        dataJob.buildNextURL();
        assertThat(url).isEqualTo(dataJob.getUrl());
    }

    @Test
    public void ERSIURL_BuildEndDate_Test() {
        ERSIURL ersiURL = new ERSIURL();
        LocalDate date = LocalDate.of(2024, 8, 18);
        String endDate = ersiURL.buildEndDate(date);
        assertThat(endDate).isEqualTo("08/18/2024");
    }

    @Test
    public void buildInitERSIParamsURL_Test() throws UnsupportedEncodingException {
        DataJob dataJob = new DataJob();
        ERSIURL ersiURL = new ERSIURL();
        String startDate = "07/01/2024";
        String endDate = ersiURL.buildEndDate(LocalDate.now());

        String url = ersiURL.buildInitURL(dataJob);
        String canonURL = String.format(
                "https://maps.austintexas.gov/gis/rest/APDCrimeViewer/APD_Reported_Crimes/MapServer/1/query?f=json&where=1=1 AND OCCURRENCE_DATE BETWEEN date '%s' AND date '%s' &returnGeometry=true&spatialRel=esriSpatialRelIntersects&geometry={\"rings\":[[[3018272.9057345022,10242548.737086222],[3070539.572401169,10280948.737086222],[3290272.9057345022,10247882.070419556],[3234806.2390678357,9873482.070419554],[2911606.2390678357,9984415.403752888],[3018272.9057345022,10242548.737086222]]],\"spatialReference\":{\"wkid\":102739,\"latestWkid\":2277}}&geometryType=esriGeometryPolygon&inSR=102739&outFields=*&orderByFields=OCCURRENCE_DATE&outSR=4326",
                startDate, endDate);

        assertThat(url).isEqualTo(canonURL);

        // TODO: add ERSI source
        // test DataJob factory
        // dataJob.initURL();
        // assertThat(url).isEqualTo(dataJob.getUrl());
    }

    @Test
    public void buildNextERSIParamsURL_Test() throws UnsupportedEncodingException {
        DataJob dataJob = new DataJob();
        ERSIURL ersiURL = new ERSIURL();
        String startDate = "07/01/2024";
        String endDate = ersiURL.buildEndDate(LocalDate.now());

        ersiURL.buildInitURL(dataJob); // paramMapServer: 1
        ersiURL.buildNextURL(dataJob); // paramMapServer: 2
        ersiURL.buildNextURL(dataJob); // paramMapServer: 3
        String url = ersiURL.buildNextURL(dataJob); // paramMapServer: 4

        String canonURL = String.format(
                "https://maps.austintexas.gov/gis/rest/APDCrimeViewer/APD_Reported_Crimes/MapServer/4/query?f=json&where=1=1 AND OCCURRENCE_DATE BETWEEN date '%s' AND date '%s' &returnGeometry=true&spatialRel=esriSpatialRelIntersects&geometry={\"rings\":[[[3018272.9057345022,10242548.737086222],[3070539.572401169,10280948.737086222],[3290272.9057345022,10247882.070419556],[3234806.2390678357,9873482.070419554],[2911606.2390678357,9984415.403752888],[3018272.9057345022,10242548.737086222]]],\"spatialReference\":{\"wkid\":102739,\"latestWkid\":2277}}&geometryType=esriGeometryPolygon&inSR=102739&outFields=*&orderByFields=OCCURRENCE_DATE&outSR=4326",
                startDate, endDate);

        assertThat(url).isEqualTo(canonURL);

        // TODO: add ERSI source
        // test DataJob factory
        // dataJob.initURL();
        // dataJob.buildNextURL();
        // dataJob.buildNextURL();
        // dataJob.buildNextURL();
        // String dataJobNextURL = dataJob.buildNextURL();
        // assertThat(url).isEqualTo(dataJobNextURL);
    }

}
