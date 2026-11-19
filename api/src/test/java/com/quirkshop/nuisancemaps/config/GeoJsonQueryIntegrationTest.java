package com.quirkshop.nuisancemaps.config;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.quirkshop.nuisancemaps.NuisancemapsApplication;
import com.quirkshop.nuisancemaps.model.Category;
import com.quirkshop.nuisancemaps.model.Data311;
import com.quirkshop.nuisancemaps.model.DataCrime;
import com.quirkshop.nuisancemaps.model.DataEntity;
import com.quirkshop.nuisancemaps.model.Locale;
import com.quirkshop.nuisancemaps.model.Mapping;
import com.quirkshop.nuisancemaps.model.Source;
import com.quirkshop.nuisancemaps.model.datajob.DataJobConfiguratorType;
import com.quirkshop.nuisancemaps.service.SparseQueryPolicy;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.cache.CacheManager;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(classes = NuisancemapsApplication.class)
@AutoConfigureMockMvc
@Transactional
class GeoJsonQueryIntegrationTest {
    @Autowired EntityManager entityManager;
    @Autowired JdbcTemplate jdbc;
    @Autowired MockMvc mvc;
    @Autowired CacheManager cacheManager;

    @ParameterizedTest
    @CsvSource({"crime, false", "311, false", "crime, true", "311, true"})
    void populatedNativeQueriesReturnGeoJsonWithReportedAt(String dataType, boolean olderContext) throws Exception {
        cacheManager.getCache("dataCrimeControllerCache").clear();
        cacheManager.getCache("data311ControllerCache").clear();

        var point = new GeometryFactory(new PrecisionModel(), 4326).createPoint(new Coordinate(-95, 30));
        var locale = new Locale();
        locale.setLocation(point);
        locale.setEnabled(true);
        entityManager.persist(locale);
        var mapping = new Mapping();
        entityManager.persist(mapping);
        var source = new Source(locale, dataType, "timestamp regression", "https://example.invalid",
                DataParserType.JSON, DataProcessType.MEMORY, DataJobConfiguratorType.BASE);
        source.setMapping(mapping);
        entityManager.persist(source);
        var category = new Category(dataType, "timestamp regression " + dataType, 1, null);
        entityManager.persist(category);

        LocalDate end = LocalDate.now(SparseQueryPolicy.ZONE);
        LocalDate start = end.minusDays(7);
        LocalDateTime reportedAt = (olderContext ? start.minusDays(3) : start.plusDays(1))
                .atTime(12, 30, 45, 123456000);
        DataEntity report = dataType.equals("crime") ? new DataCrime(source) : new Data311(source);
        report.setReportNum("timestamp-regression");
        report.setReportCategory("test category");
        report.setOrgCategory(category);
        report.setLatitude(30.0);
        report.setLongitude(-95.0);
        report.setPoint(point);
        report.setReportedAt(reportedAt);
        entityManager.persist(report);
        entityManager.flush();
        // Populate the anchor used by the actual older-context native query.
        jdbc.execute("REFRESH MATERIALIZED VIEW locale_category_min_max_reported_at");

        mvc.perform(get(dataType.equals("crime") ? "/datacrimes.geojson" : "/data311s.geojson")
                .param("sw_lat", "29.99").param("sw_lng", "-95.01")
                .param("ne_lat", "30.01").param("ne_lng", "-94.99")
                .param("startDate", start.toString()).param("endDate", end.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("FeatureCollection"))
                .andExpect(jsonPath("$.features", hasSize(1)))
                .andExpect(jsonPath("$.features[0].properties.reportNum").value("timestamp-regression"))
                .andExpect(jsonPath("$.features[0].properties.reportedAt").value(reportedAt.toString()))
                .andExpect(jsonPath("$.features[0].properties.dateMatch")
                        .value(olderContext ? "older_context" : "within_range"));
    }
}
