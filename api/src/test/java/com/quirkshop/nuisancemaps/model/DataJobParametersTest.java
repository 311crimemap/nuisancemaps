package com.quirkshop.nuisancemaps.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quirkshop.nuisancemaps.NuisancemapsApplication;
import com.quirkshop.nuisancemaps.model.datajob.APDIncidentReportParameters;
import com.quirkshop.nuisancemaps.model.datajob.BaseParameters;
import com.quirkshop.nuisancemaps.model.datajob.DataJob;
import com.quirkshop.nuisancemaps.model.datajob.DataJobParameters;
import com.quirkshop.nuisancemaps.model.datajob.ERSIParameters;
import com.quirkshop.nuisancemaps.model.datajob.OpenDataParameters;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ResourceLoader;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = NuisancemapsApplication.class)
public class DataJobParametersTest {

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
    public void BaseURL_buildInit_Test() {
        Source source = sources.get(11);
        DataJob dataJob = new DataJob(LocalDateTime.now(), source, "id");
        BaseParameters baseParameters = new BaseParameters();

        String url = baseParameters.buildInitURL(dataJob);
        String canonURL = source.getUrl();

        assertThat(url).isEqualTo(canonURL);

        // test DataJob factory
        dataJob.initDataJobParameters();
        assertThat(url).isEqualTo(dataJob.getUrl());
    }

    @Test
    public void BaseURL_buildNext_Test() {
        Source source = sources.get(11);
        DataJob dataJob = new DataJob(LocalDateTime.now(), source, "id");
        BaseParameters baseParameters = new BaseParameters();

        String url = baseParameters.buildInitURL(dataJob);
        assertThat(url).isEqualTo(source.getUrl());

        url = baseParameters.buildNextURL(dataJob);
        assertThat(url).isNull();

        // test DataJob factory
        dataJob.initDataJobParameters();
        DataJobParameters dataJobNextParameters = dataJob.buildNextDataJobParameters();
        assertThat(url).isEqualTo(dataJobNextParameters);
    }

    @Test
    public void OpenDataURL_buildInitParams_Test() {
        Source source = sources.get(1);
        DataJob dataJob = new DataJob(LocalDateTime.now(), source, "id");
        OpenDataParameters openDataParameters = new OpenDataParameters();

        String url = openDataParameters.buildInitURL(dataJob);

        String limit = System.getenv("WORKER_QUERY_LIMIT");
        String offset = "0";
        String order_key = "id";
        String select = openDataParameters.buildURLFields(source.getMapping());
        String canonURL = source.getUrl() + "?$limit=" + limit + "&$offset=" + offset + "&$order=" +
                order_key + "&$select=" + select;

        assertThat(url).isEqualTo(canonURL);

        // test DataJob factory
        dataJob.initDataJobParameters();;
        assertThat(url).isEqualTo(dataJob.getUrl());
    }

    @Test
    public void OpenDataURL_buildNextParams_Test() {
        Source source = sources.get(1);
        DataJob dataJob = new DataJob(LocalDateTime.now(), source, "id");
        OpenDataParameters openDataParameters = new OpenDataParameters();

        openDataParameters.buildInitURL(dataJob); // offset: 0
        openDataParameters.buildNextURL(dataJob); // offset: 1 * PARAM_LIMIT
        openDataParameters.buildNextURL(dataJob); // offset: 2 * PARAM_LIMIT
        String url = openDataParameters.buildNextURL(dataJob); // offset: 3 * PARAM_LIMIT

        String limit = System.getenv("WORKER_QUERY_LIMIT");
        String offset = String.valueOf(3 * Integer.parseInt(limit));
        String order_key = "id";
        String select = openDataParameters.buildURLFields(source.getMapping());
        String canonURL = source.getUrl() + "?$limit=" + limit + "&$offset=" + offset + "&$order=" +
                order_key + "&$select=" + select;

        assertThat(url).isEqualTo(canonURL);

        // test DataJob factory
        dataJob.initDataJobParameters();
        dataJob.buildNextDataJobParameters();
        dataJob.buildNextDataJobParameters();
        dataJob.buildNextDataJobParameters();
        assertThat(url).isEqualTo(dataJob.getUrl());
    }

    @Test
    public void APDIncidentReport_BuildInitParamsTest() {
        Source source = sources.get(15);
        DataJob dataJob = new DataJob(LocalDateTime.now(), source, "reportNum");
        APDIncidentReportParameters apdIncidentReportParameters = new APDIncidentReportParameters();
        String url = apdIncidentReportParameters.buildInitURL(dataJob);

        assertThat(url).isEqualTo(
                "https://services.austintexas.gov/police/reports/search2.cfm?startdate=07/01/2024&numdays=6&address=&rucrext=&tract_num=&zipcode=&zone=&district=&city=&choice=criteria&Submit=Submit");

        // test DataJob factory
        dataJob.initDataJobParameters();
        assertThat(url).isEqualTo(dataJob.getUrl());
    }

    @Test
    public void APDIncidentReport_BuildNextParamsTest() {
        Source source = sources.get(15);
        DataJob dataJob = new DataJob(LocalDateTime.now(), source, "reportNum");
        HashMap<String, Object> parameters = dataJob.getParameters();
        parameters.put("paramStartDate", "07/01/2024");
        parameters.put("paramEndDate", "08/01/2024");

        // NB: numDays is inclusive
        // numDays + 1 -> (7 days) is next start date
        APDIncidentReportParameters apdIncidentReportParameters = new APDIncidentReportParameters();
        apdIncidentReportParameters.buildInitURL(dataJob); // startdate: 7/01/2024
        apdIncidentReportParameters.buildNextURL(dataJob); // + 6 + 1 -> 7/08/2024
        apdIncidentReportParameters.buildNextURL(dataJob); // + 6 + 1 -> 7/15/2024
        String url = apdIncidentReportParameters.buildNextURL(dataJob); // + 6 + 1 -> 7/22/2024

        assertThat(url).isEqualTo(
                "https://services.austintexas.gov/police/reports/search2.cfm?startdate=07/22/2024&numdays=6&address=&rucrext=&tract_num=&zipcode=&zone=&district=&city=&choice=criteria&Submit=Submit");

        apdIncidentReportParameters.buildNextURL(dataJob); // + 6 + 1 -> 7/29/2024
        apdIncidentReportParameters.buildNextURL(dataJob); // + 6 + 1 -> 8/6/2024
        url = apdIncidentReportParameters.buildNextURL(dataJob);
        assertThat(url).isNull();

        // test DataJob factory
        dataJob.buildNextDataJobParameters();
        assertThat(url).isEqualTo(dataJob.getUrl());
    }

    @Test
    public void ERSIURL_BuildEndDate_Test() {
        ERSIParameters ersiParameters = new ERSIParameters();
        LocalDate date = LocalDate.of(2024, 8, 18);
        String endDate = ersiParameters.buildEndDate(date);
        assertThat(endDate).isEqualTo("08/18/2024");
    }

    @Test
    public void buildInitERSIParams_Test() throws UnsupportedEncodingException {
        DataJob dataJob = new DataJob();
        ERSIParameters ersiParameters = new ERSIParameters();
        String startDate = "07/01/2024";
        String endDate = ersiParameters.buildEndDate(LocalDate.now());

        String url = ersiParameters.buildInitURL(dataJob);
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
    public void buildNextERSIParams_Test() throws UnsupportedEncodingException {
        DataJob dataJob = new DataJob();
        ERSIParameters ersiURL = new ERSIParameters();
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
