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
import com.quirkshop.nuisancemaps.model.datajob.APDIncidentReportConfigurator;
import com.quirkshop.nuisancemaps.model.datajob.BaseConfigurator;
import com.quirkshop.nuisancemaps.model.datajob.DataJob;
import com.quirkshop.nuisancemaps.model.datajob.ERSIConfigurator;
import com.quirkshop.nuisancemaps.model.datajob.OpenDataConfigurator;
import com.quirkshop.nuisancemaps.model.datajob.OpenDataDateConfigurator;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ResourceLoader;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = NuisancemapsApplication.class)
public class DataJobConfiguratorTest {

    @Autowired
    private ResourceLoader resourceLoader;

    List<Source> sources;

    @BeforeAll
    public void setUp() throws IOException {

        ObjectMapper objectMapper = new ObjectMapper();
        File sourceJSON = resourceLoader.getResource("classpath:data/source_config_archive.json").getFile();
        sources = objectMapper.readValue(sourceJSON, new TypeReference<List<Source>>() {
        });

    }

    @Test
    public void BaseConfigurator_initialize_test() {
        Source source = sources.get(3);
        DataJob dataJob = new DataJob(LocalDateTime.now(), source, "id");
        BaseConfigurator baseConfigurator = new BaseConfigurator();

        dataJob = baseConfigurator.initialize(dataJob);
        String canonURL = source.getUrl();

        assertThat(dataJob.getUrl()).isEqualTo(canonURL);
    }

    @Test
    public void BaseConfigurator_next_test() {
        Source source = sources.get(3);
        DataJob dataJob = new DataJob(LocalDateTime.now(), source, "id");
        BaseConfigurator baseConfigurator = new BaseConfigurator();

        dataJob = baseConfigurator.initialize(dataJob);
        assertThat(dataJob.getUrl()).isEqualTo(source.getUrl());

        dataJob = baseConfigurator.next(dataJob);
        assertThat(dataJob).isNull();
    }

    @Test
    public void OpenDataConfigurator_initialize_test() {
        Source source = sources.get(0);
        DataJob dataJob = new DataJob(LocalDateTime.now(), source, "id");
        OpenDataConfigurator openDataConfigurator = new OpenDataConfigurator();

        dataJob = openDataConfigurator.initialize(dataJob);

        String limit = System.getenv("WORKER_QUERY_LIMIT");
        String offset = "0";
        String order_key = "id";
        String select = openDataConfigurator.buildURLFields(source.getMapping());
        String canonURL = source.getUrl() + "?$limit=" + limit + "&$offset=" + offset + "&$order=" +
                order_key + "&$select=" + select;

        assertThat(dataJob.getUrl()).isEqualTo(canonURL);
    }

    @Test
    public void OpenDataConfigurator_next_test() {
        Source source = sources.get(0);
        DataJob dataJob = new DataJob(LocalDateTime.now(), source, "id");
        OpenDataConfigurator openDataParameters = new OpenDataConfigurator();

        dataJob = openDataParameters.initialize(dataJob); // offset: 0
        dataJob = openDataParameters.next(dataJob); // offset: 1 * PARAM_LIMIT
        dataJob = openDataParameters.next(dataJob); // offset: 2 * PARAM_LIMIT
        dataJob = openDataParameters.next(dataJob); // offset: 3 * PARAM_LIMIT

        String limit = System.getenv("WORKER_QUERY_LIMIT");
        String offset = String.valueOf(3 * Integer.parseInt(limit));
        String order_key = "id";
        String select = openDataParameters.buildURLFields(source.getMapping());
        String canonURL = source.getUrl() + "?$limit=" + limit + "&$offset=" + offset + "&$order=" +
                order_key + "&$select=" + select;

        assertThat(dataJob.getUrl()).isEqualTo(canonURL);
    }

    @Test
    public void OpenDataDateConfigurator_initialize_test() {
        Source source = sources.get(7);
        DataJob dataJob = new DataJob(LocalDateTime.now(), source, "id");
        OpenDataDateConfigurator openDataDateConfigurator = new OpenDataDateConfigurator();

        dataJob = openDataDateConfigurator.initialize(dataJob);

        String limit = System.getenv("WORKER_QUERY_LIMIT");
        String offset = "0";
        String order_key = "id";
        String select = openDataDateConfigurator.buildURLFields(source.getMapping());
        String where = openDataDateConfigurator.buildWhereField(source.getMapping());

        // NB: hardcoded in source_config_archive.json
        assertThat(where).isEqualTo("created_date >= \"2024-05-15\"");

        String canonURL = source.getUrl() + "?$limit=" + limit + "&$offset=" + offset + "&$order=" +
                order_key + "&$select=" + select + "&$where=" + where;

        assertThat(dataJob.getUrl()).isEqualTo(canonURL);
    }

    @Test
    public void APDIncidentReportConfigurator_initialize_test() {
        Source source = sources.get(6);
        DataJob dataJob = new DataJob(LocalDateTime.now(), source, "reportNum");
        APDIncidentReportConfigurator apdIncidentReportConfigurator = new APDIncidentReportConfigurator();
        dataJob = apdIncidentReportConfigurator.initialize(dataJob);

        assertThat(dataJob.getUrl()).isEqualTo(
                "https://services.austintexas.gov/police/reports/search2.cfm?startdate=08/01/2024&numdays=6&address=&rucrext=&tract_num=&zipcode=&zone=&district=&city=&choice=criteria&Submit=Submit");
    }

    @Test
    public void APDIncidentReportConfigurator_next_test() {
        Source source = sources.get(6);
        DataJob dataJob = new DataJob(LocalDateTime.now(), source, "reportNum");

        // set dataJob parameters to end sooner than .now()
        HashMap<String, Object> parameters = dataJob.getParameters();
        parameters.put("paramEndDate", "09/01/2024");

        // NB: numDays is inclusive
        // numDays + 1 -> (7 days) is next start date
        APDIncidentReportConfigurator apdIncidentReportConfigurator = new APDIncidentReportConfigurator();
        dataJob = apdIncidentReportConfigurator.initialize(dataJob); // startdate: 8/01/2024
        dataJob = apdIncidentReportConfigurator.next(dataJob); // + 6 + 1 -> 8/08/2024
        dataJob = apdIncidentReportConfigurator.next(dataJob); // + 6 + 1 -> 8/15/2024
        dataJob = apdIncidentReportConfigurator.next(dataJob); // + 6 + 1 -> 8/22/2024

        assertThat(dataJob.getUrl()).isEqualTo(
                "https://services.austintexas.gov/police/reports/search2.cfm?startdate=08/22/2024&numdays=6&address=&rucrext=&tract_num=&zipcode=&zone=&district=&city=&choice=criteria&Submit=Submit");

        dataJob = apdIncidentReportConfigurator.next(dataJob); // + 6 + 1 -> 8/29/2024
        dataJob = apdIncidentReportConfigurator.next(dataJob); // + 6 + 1 -> 9/5/2024 X end

        //this should also be null (null input, null output)
        dataJob = apdIncidentReportConfigurator.next(dataJob); // + 6 + 1 -> 9/12/2024

        assertThat(dataJob).isNull();
    }

    @Test
    public void ERSIConfigurator_buildEndDate_test() {
        ERSIConfigurator ersiConfigurator = new ERSIConfigurator();
        LocalDate date = LocalDate.of(2024, 8, 18);
        String endDate = ersiConfigurator.buildEndDate(date);
        assertThat(endDate).isEqualTo("08/18/2024");
    }

    @Test
    public void ERSIConfigurator_initialize_test() throws UnsupportedEncodingException {
        DataJob dataJob = new DataJob();
        ERSIConfigurator ersiConfigurator = new ERSIConfigurator();
        String startDate = "07/01/2024";
        String endDate = ersiConfigurator.buildEndDate(LocalDate.now());

        dataJob = ersiConfigurator.initialize(dataJob);
        String canonURL = String.format(
                "https://maps.austintexas.gov/gis/rest/APDCrimeViewer/APD_Reported_Crimes/MapServer/1/query?f=json&where=1=1 AND OCCURRENCE_DATE BETWEEN date '%s' AND date '%s' &returnGeometry=true&spatialRel=esriSpatialRelIntersects&geometry={\"rings\":[[[3018272.9057345022,10242548.737086222],[3070539.572401169,10280948.737086222],[3290272.9057345022,10247882.070419556],[3234806.2390678357,9873482.070419554],[2911606.2390678357,9984415.403752888],[3018272.9057345022,10242548.737086222]]],\"spatialReference\":{\"wkid\":102739,\"latestWkid\":2277}}&geometryType=esriGeometryPolygon&inSR=102739&outFields=*&orderByFields=OCCURRENCE_DATE&outSR=4326",
                startDate, endDate);

        assertThat(dataJob.getUrl()).isEqualTo(canonURL);
    }

    @Test
    public void ERSIConfigurator_next_test() throws UnsupportedEncodingException {
        DataJob dataJob = new DataJob();
        ERSIConfigurator ersiConfigurator = new ERSIConfigurator();
        String startDate = "07/01/2024";
        String endDate = ersiConfigurator.buildEndDate(LocalDate.now());

        dataJob = ersiConfigurator.initialize(dataJob); // paramMapServer: 1
        dataJob = ersiConfigurator.next(dataJob);       // paramMapServer: 2
        dataJob = ersiConfigurator.next(dataJob);       // paramMapServer: 3
        dataJob = ersiConfigurator.next(dataJob);       // paramMapServer: 4

        String canonURL = String.format(
                "https://maps.austintexas.gov/gis/rest/APDCrimeViewer/APD_Reported_Crimes/MapServer/4/query?f=json&where=1=1 AND OCCURRENCE_DATE BETWEEN date '%s' AND date '%s' &returnGeometry=true&spatialRel=esriSpatialRelIntersects&geometry={\"rings\":[[[3018272.9057345022,10242548.737086222],[3070539.572401169,10280948.737086222],[3290272.9057345022,10247882.070419556],[3234806.2390678357,9873482.070419554],[2911606.2390678357,9984415.403752888],[3018272.9057345022,10242548.737086222]]],\"spatialReference\":{\"wkid\":102739,\"latestWkid\":2277}}&geometryType=esriGeometryPolygon&inSR=102739&outFields=*&orderByFields=OCCURRENCE_DATE&outSR=4326",
                startDate, endDate);

        assertThat(dataJob.getUrl()).isEqualTo(canonURL);
    }

}
