package com.quirkshop.nuisancemaps.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import com.quirkshop.nuisancemaps.dto.FeatureCollectionDTO;
import com.quirkshop.nuisancemaps.repository.Data311Repository;
import com.quirkshop.nuisancemaps.repository.DataCrimeRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SparseQueryServiceTest {
    @Mock DataCrimeRepository crimes;
    @Mock Data311Repository reports311;
    @InjectMocks DataCrimeService crimeService;
    @InjectMocks Data311Service service311;

    private static Object[] row(LocalDateTime date) {
        return new Object[] { 1L, "report", "type", "address", "place", 30.0, -95.0,
                Timestamp.valueOf(date), 1, "crime", "text", 1, "icon", "unicode" };
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 1, 9 })
    void recentSparseCrimeAddsOnlyAvailableOlderReports(int matchCount) {
        LocalDate end = LocalDate.now(SparseQueryPolicy.ZONE);
        LocalDateTime start = end.minusMonths(1).atStartOfDay();
        LocalDateTime endExclusive = end.plusDays(1).atStartOfDay();
        List<Object[]> matches = Collections.nCopies(matchCount, row(start));
        List<Object[]> older = Collections.nCopies(SparseQueryPolicy.FALLBACK_LIMIT - matchCount,
                row(start.minusDays(1)));
        when(crimes.findAllByLatLngBoundsAndBetweenDates(0, 0, 1, 1, start, endExclusive, 20000))
                .thenReturn(matches);
        when(crimes.findAllByLatLngBoundsAndBetweenDates(0, 0, 1, 1,
                SparseQueryPolicy.fallbackStart(end), start, SparseQueryPolicy.FALLBACK_LIMIT - matchCount))
                .thenReturn(older);

        FeatureCollectionDTO result = crimeService.findAllByBoundsOrderByReportedAtDescGeoJSON(
                0, 0, 1, 1, start, end.atStartOfDay(), 20000);

        assertThat(result.getFeatures()).hasSize(SparseQueryPolicy.FALLBACK_LIMIT);
        if (matchCount > 0) {
            assertThat(result.getFeatures().get(0).getProperties().getDateMatch()).isEqualTo("within_range");
        }
        assertThat(result.getFeatures().get(matchCount).getProperties().getDateMatch()).isEqualTo("older_context");
        verify(crimes).findAllByLatLngBoundsAndBetweenDates(0, 0, 1, 1, start, endExclusive, 20000);
    }

    @Test
    void tenRecentReportsDoNotExpand() {
        LocalDate end = LocalDate.now(SparseQueryPolicy.ZONE).minusDays(1);
        LocalDateTime start = end.minusMonths(1).atStartOfDay();
        when(crimes.findAllByLatLngBoundsAndBetweenDates(0, 0, 1, 1,
                start, end.plusDays(1).atStartOfDay(), 20000))
                .thenReturn(Collections.nCopies(10, row(start)));

        FeatureCollectionDTO result = crimeService.findAllByBoundsOrderByReportedAtDescGeoJSON(
                0, 0, 1, 1, start, end.atStartOfDay(), 20000);
        assertThat(result.getFeatures()).hasSize(10);
        assertThat(result.getFeatures().get(0).getProperties().getDateMatch()).isEqualTo("within_range");
        verifyNoMoreInteractions(crimes);
    }

    @Test
    void historical311RangeStaysStrict() {
        LocalDateTime start = LocalDate.of(2024, 1, 1).atStartOfDay();
        LocalDateTime end = LocalDate.of(2024, 1, 31).atStartOfDay();
        when(reports311.findAllByLatLngBoundsAndBetweenDates(0, 0, 1, 1,
                start, end.plusDays(1), 20000)).thenReturn(Collections.singletonList(row(end)));

        FeatureCollectionDTO result = service311.findAllByBoundsOrderByReportedAtDescGeoJSON(
                0, 0, 1, 1, start, end, 20000);
        assertThat(result.getFeatures()).hasSize(1);
        assertThat(result.getFeatures().get(0).getProperties().getDateMatch()).isEqualTo("within_range");
        verify(reports311).findAllByLatLngBoundsAndBetweenDates(0, 0, 1, 1,
                start, end.plusDays(1), 20000);
        verifyNoMoreInteractions(reports311);
    }

    @Test
    void recent311UsesItsOwnSparseCountAndStopsAtSixMonths() {
        LocalDate end = LocalDate.now(SparseQueryPolicy.ZONE);
        LocalDateTime start = end.minusMonths(1).atStartOfDay();
        LocalDateTime earliest = SparseQueryPolicy.fallbackStart(end);
        when(reports311.findAllByLatLngBoundsAndBetweenDates(0, 0, 1, 1,
                start, end.plusDays(1).atStartOfDay(), 20000)).thenReturn(List.of());
        when(reports311.findAllByLatLngBoundsAndBetweenDates(0, 0, 1, 1,
                earliest, start, SparseQueryPolicy.FALLBACK_LIMIT))
                .thenReturn(Collections.singletonList(row(earliest)));

        FeatureCollectionDTO result = service311.findAllByBoundsOrderByReportedAtDescGeoJSON(
                0, 0, 1, 1, start, end.atStartOfDay(), 20000);
        assertThat(result.getFeatures()).hasSize(1);
        assertThat(result.getFeatures().get(0).getProperties().getDateMatch()).isEqualTo("older_context");
    }
}
