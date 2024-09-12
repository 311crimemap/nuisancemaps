package com.quirkshop.nuisancemaps.service.dataparser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quirkshop.nuisancemaps.NuisancemapsApplication;
import com.quirkshop.nuisancemaps.model.Category;
import com.quirkshop.nuisancemaps.model.Locale;
import com.quirkshop.nuisancemaps.model.PendingTextCategory;
import com.quirkshop.nuisancemaps.model.Source;
import com.quirkshop.nuisancemaps.model.TextCategory;
import com.quirkshop.nuisancemaps.model.datajob.DataJob;
import com.quirkshop.nuisancemaps.repository.CategoryRepository;
import com.quirkshop.nuisancemaps.repository.DataCrimeRepository;
import com.quirkshop.nuisancemaps.repository.DataJobRepository;
import com.quirkshop.nuisancemaps.repository.LocaleRepository;
import com.quirkshop.nuisancemaps.repository.MappingRepository;
import com.quirkshop.nuisancemaps.repository.PendingTextCategoryRepository;
import com.quirkshop.nuisancemaps.repository.SourceRepository;
import com.quirkshop.nuisancemaps.repository.TextCategoryRepository;
import com.quirkshop.nuisancemaps.service.GeocoderService;
import com.quirkshop.nuisancemaps.util.ParseCounter;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(classes = NuisancemapsApplication.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class APDIncidentReportDataParserTest {

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private LocaleRepository localeRepository;

    @Autowired
    private MappingRepository mappingRepository;

    @Autowired
    private SourceRepository sourceRepository;

    @Autowired
    private DataJobRepository dataJobRepository;

    @Autowired
    private DataCrimeRepository dataCrimeRepository;

    @Autowired
    private TextCategoryRepository textCategoryRepository;

    @Autowired
    private PendingTextCategoryRepository pendingTextCategoryRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @MockBean
    private GeocoderService geocoderService;

    @Autowired
    private APDIncidentReportDataParser apdIncidentReportDataParser;

    private List<Source> sources;

    @BeforeAll
    public void setUpOnce() throws IOException {

        // Source
        ObjectMapper objectMapper = new ObjectMapper();
        File sourceJSON = resourceLoader.getResource("classpath:data/source_config_archive.json").getFile();
        sources = objectMapper.readValue(sourceJSON, new TypeReference<List<Source>>() {
        });
        for (Source s : sources) {
            Locale locale = new Locale();
            localeRepository.save(locale);
            s.setLocale(locale);
            mappingRepository.save(s.getMapping());
            sourceRepository.save(s);
        }

        // require textCategory mapping to exist before successful save
        // otherwise will throw MissingCategoryException and skip
        Category cat = new Category("crime", "Public Order", 0, null);
        categoryRepository.save(cat);

        ArrayList<TextCategory> textCategories = new ArrayList<TextCategory>();
        textCategories.add(new TextCategory("crime", "TEST CRIME", cat));
        textCategories.add(new TextCategory("crime", "TEST CRIME 2", cat));
        textCategories.add(new TextCategory("crime", "TEST CRIME 3", cat));

        textCategoryRepository.saveAll(textCategories);
    }

    @AfterAll
    public void tearDown() throws IOException {
        sourceRepository.deleteAll();
        mappingRepository.deleteAll();
        localeRepository.deleteAll();
        textCategoryRepository.deleteAll();
        categoryRepository.deleteAll();
        pendingTextCategoryRepository.deleteAll();
    }

    @Test
    @Transactional
    public void formatAddressTest() {

        String input = "7918 WEST GATE BLVD, Apt # B ,    AUSTIN  78745";
        String input2 = "4825 DAVIS LN, Apt # 1214 ,    AUSTIN  78749";
        String input3 = "12424 RESEARCH BLVD SVRD SB,     AUSTIN  78759";

        assertThat(apdIncidentReportDataParser.formatAddress(input))
                .isEqualTo("7918 WEST GATE BLVD, AUSTIN 78745");

        assertThat(apdIncidentReportDataParser.formatAddress(input2))
                .isEqualTo("4825 DAVIS LN, AUSTIN 78749");

        assertThat(apdIncidentReportDataParser.formatAddress(input3))
                .isEqualTo("12424 RESEARCH BLVD SVRD SB, AUSTIN 78759");

    }

    @Test
    @Transactional
    public void parseTest() throws IOException {

        Resource htmlResource = resourceLoader.getResource("classpath:data/crime-atx.html");
        InputStream inputStream = htmlResource.getInputStream();
        ParseCounter parseCounter = new ParseCounter();

        Source s = sourceRepository.findOneBySourceConfigId(16);
        DataJob d = new DataJob(LocalDateTime.now(), s, "reportNum");
        dataJobRepository.save(d);

        List<String> mockAddresses = Arrays.asList(
                "7918 WEST GATE BLVD, Apt # B ,    AUSTIN  78745",
                "4825 DAVIS LN, Apt # 1214 ,    AUSTIN  78749",
                "12424 RESEARCH BLVD SVRD SB,     AUSTIN  78759");

        List<double[]> mockCoordinates = Arrays.asList(new double[] { 30.123456789, -90.987654321 },
                new double[] { 31.123456789, -92.987654321 },
                null);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE, MMM-dd-yyyy HH:mm");
        DateTimeFormatter outputFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        Map<String, String> map = new HashMap<String, String>() {
            {
                put("reportNum", "123");
                put("reportCategory", "TEST CRIME");
                put("address", "7918 WEST GATE BLVD, AUSTIN 78745");
                put("reportedAt", LocalDateTime.parse("Thu, Aug-01-2024 02:10", formatter)
                        .format(outputFormatter));
                put("reportedAt2", LocalDateTime.parse("Thu, Aug-01-2024 02:10", formatter)
                        .format(outputFormatter));
                put("latitude", null);
                put("longitude", null);
            }
        };

        Map<String, String> map2 = new HashMap<String, String>() {
            {
                put("reportNum", "1234");
                put("reportCategory", "TEST CRIME 2");
                put("address", "4825 DAVIS LN, AUSTIN 78749");
                put("reportedAt", LocalDateTime.parse("Fri, Aug-02-2024 02:10", formatter)
                        .format(outputFormatter));
                put("reportedAt2", LocalDateTime.parse("Fri, Aug-02-2024 02:10", formatter)
                        .format(outputFormatter));
                put("latitude", null);
                put("longitude", null);
            }
        };
        Map<String, String> map3 = new HashMap<String, String>() {
            {
                put("reportNum", "12345");
                put("reportCategory", "TEST CRIME 3");
                put("address", "12424 RESEARCH BLVD SVRD SB, AUSTIN 78759");
                put("reportedAt", LocalDateTime.parse("Sat, Aug-03-2024 02:10", formatter)
                        .format(outputFormatter));
                put("reportedAt2", LocalDateTime.parse("Sat, Aug-03-2024 02:10", formatter)
                        .format(outputFormatter));
                put("latitude", null);
                put("longitude", null);
            }
        };

        List<Map<String, String>> mockRowMaps = Arrays.asList(map, map2, map3);

        // coordinates should get applied to rowMap's above in buildRowMap()
        when(geocoderService.geocode(any(Source.class), any(List.class))).thenReturn(mockCoordinates);

        APDIncidentReportDataParser spyParser = spy(apdIncidentReportDataParser);
        doReturn(mockRowMaps).when(spyParser).parseToRowMaps(any(List.class));

        assertThat(dataCrimeRepository.count()).isEqualTo(0);

        // 1. parseToRowMaps returns mockRowMaps
        // 2. geocode() enriches mockRowMaps with mockCoordinates
        // 3. mockRowMaps are sent do dataEntityMappingService.buildDataEntity() for
        // saving
        spyParser.parse(d, inputStream, parseCounter);

        // last rowMap is missing coordinates - not saved
        assertThat(dataCrimeRepository.count()).isEqualTo(2);
    }

    @Test
    @Transactional
    public void parsePendingTextCategoryTest() throws IOException {

        Resource htmlResource = resourceLoader.getResource("classpath:data/crime-atx.html");
        InputStream inputStream = htmlResource.getInputStream();
        ParseCounter parseCounter = new ParseCounter();

        Source s = sourceRepository.findOneBySourceConfigId(16);
        DataJob d = new DataJob(LocalDateTime.now(), s, "reportNum");
        dataJobRepository.save(d);

        List<String> mockAddresses = Arrays.asList("7918 WEST GATE BLVD, Apt # B ,    AUSTIN  78745");

        List<double[]> mockCoordinates = Arrays.asList(new double[] { 30.123456789, -90.987654321 });
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE, MMM-dd-yyyy HH:mm");
        DateTimeFormatter outputFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        Map<String, String> map = new HashMap<String, String>() {
            {
                put("reportNum", "123");
                put("reportCategory", "TEST CRIME THAT DOES NOT EXIST!");
                put("address", "7918 WEST GATE BLVD, AUSTIN 78745");
                put("reportedAt", LocalDateTime.parse("Thu, Aug-01-2024 02:10", formatter)
                        .format(outputFormatter));
                put("reportedAt2", LocalDateTime.parse("Thu, Aug-01-2024 02:10", formatter)
                        .format(outputFormatter));
                put("latitude", null);
                put("longitude", null);
            }
        };

        List<Map<String, String>> mockRowMaps = Arrays.asList(map);

        // coordinates should get applied to rowMap's above in buildRowMap()
        when(geocoderService.geocode(any(Source.class), any(List.class))).thenReturn(mockCoordinates);

        APDIncidentReportDataParser spyParser = spy(apdIncidentReportDataParser);
        doReturn(mockRowMaps).when(spyParser).parseToRowMaps(any(List.class));

        assertThat(dataCrimeRepository.count()).isEqualTo(0);

        // saving
        spyParser.parse(d, inputStream, parseCounter);

        // last rowMap is missing coordinates - nothing saved
        assertThat(dataCrimeRepository.count()).isEqualTo(0);

        // check PendingTextCategory - doesn't save 1 because of missing TC
        Iterable<PendingTextCategory> ptcIter = pendingTextCategoryRepository.findAll();
        List<PendingTextCategory> ptcs = new ArrayList<PendingTextCategory>();
        ptcIter.forEach(ptcs::add);

        assertThat(ptcs.size()).isEqualTo(1);
        assertThat(ptcs.get(0).getText()).isEqualTo("TEST CRIME THAT DOES NOT EXIST!");
    }
}
