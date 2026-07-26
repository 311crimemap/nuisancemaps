package com.quirkshop.nuisancemaps.service.dataparser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.quirkshop.nuisancemaps.model.DataCrime;
import com.quirkshop.nuisancemaps.model.DataEntity;
import com.quirkshop.nuisancemaps.model.Source;
import com.quirkshop.nuisancemaps.repository.DataCrimeRepository;
import com.quirkshop.nuisancemaps.util.ParseCounter;

import org.junit.jupiter.api.Test;
class DataParserTest {

    @Test
    void batchSaveDoesNotPersistAnUnchangedDuplicate() {
        Source source = source(7);
        DataCrime existing = data(source, "report-1", "original description", "100 Main St");
        existing.setId(10L);
        DataCrime incoming = data(source, "report-1", "original description", "100 Main St");

        List<List<DataEntity>> savedBatches = new ArrayList<>();
        DataCrimeRepository repository = repositoryReturning(existing, savedBatches);
        DataParser parser = parserWith(repository, incoming);
        ParseCounter parseCounter = new ParseCounter();

        parser.batchSave(source, parseCounter);

        assertThat(savedBatches).isEmpty();
        assertThat(parseCounter.getNumDuplicates()).isEqualTo(1);
        assertThat(parseCounter.getNumUnchangedDuplicates()).isEqualTo(1);
        assertThat(parseCounter.getNumInserted()).isZero();
        verify(repository).findAllBySourceIdAndReportNumIn(eq(7), any());
        verify(repository, never()).saveAllEntities(any());
    }

    @Test
    void batchSavePersistsOnlyTheExistingDuplicateWhenItHasChanged() {
        Source source = source(7);
        DataCrime existing = data(source, "report-1", "original description", "100 Main St");
        existing.setId(10L);
        DataCrime incoming = data(source, "report-1", "revised description", null);

        List<List<DataEntity>> savedBatches = new ArrayList<>();
        DataCrimeRepository repository = repositoryReturning(existing, savedBatches);
        DataParser parser = parserWith(repository, incoming);

        ParseCounter parseCounter = new ParseCounter();
        parser.batchSave(source, parseCounter);

        assertThat(savedBatches).containsExactly(List.of(existing));
        assertThat(existing.getDescription()).isEqualTo("revised description");
        assertThat(existing.getAddress()).isEqualTo("100 Main St");
        assertThat(parseCounter.getNumReplaced()).isEqualTo(1);
        assertThat(parseCounter.getNumInserted()).isZero();
    }

    @Test
    void batchSaveCountsNewRecordsAsInserted() {
        Source source = source(7);
        DataCrime incoming = data(source, "report-2", "new description", "100 Main St");
        List<List<DataEntity>> savedBatches = new ArrayList<>();
        DataCrimeRepository repository = mock(DataCrimeRepository.class);
        when(repository.findAllBySourceIdAndReportNumIn(eq(7), any())).thenReturn(List.of());
        when(repository.saveAllEntities(any())).thenAnswer(invocation -> {
            Iterable<DataEntity> entities = invocation.getArgument(0);
            List<DataEntity> saved = toList(entities);
            savedBatches.add(saved);
            return saved;
        });
        DataParser parser = parserWith(repository, incoming);
        ParseCounter parseCounter = new ParseCounter();

        parser.batchSave(source, parseCounter);

        assertThat(savedBatches).containsExactly(List.of(incoming));
        assertThat(parseCounter.getNumInserted()).isEqualTo(1);
        assertThat(parseCounter.getNumReplaced()).isZero();
        assertThat(parseCounter.getNumUnchangedDuplicates()).isZero();
    }

    @Test
    void addDataEntityDeduplicatesReportNumbersWithinABatch() {
        Source source = source(7);
        DataCrime original = data(source, "report-3", "original description", "100 Main St");
        DataCrime replacement = data(source, "report-3", "replacement description", "100 Main St");
        DataParser parser = new DataParser();

        parser.addDataEntity(original, new ParseCounter());
        parser.addDataEntity(replacement, new ParseCounter());

        assertThat(parser.reportNums).containsExactly("report-3");
        assertThat(parser.parseNewDataMap).containsEntry("report-3", replacement);
    }

    private DataParser parserWith(DataCrimeRepository repository, DataCrime incoming) {
        DataParser parser = new DataParser();
        parser.dataEntityRepository = repository;
        parser.parseNewDataMap.put(incoming.getReportNum(), incoming);
        parser.reportNums.add(incoming.getReportNum());
        return parser;
    }

    private DataCrimeRepository repositoryReturning(DataCrime existing, List<List<DataEntity>> savedBatches) {
        DataCrimeRepository repository = mock(DataCrimeRepository.class);
        when(repository.findAllBySourceIdAndReportNumIn(eq(7), any())).thenReturn(List.of(existing));
        when(repository.saveAllEntities(any())).thenAnswer(invocation -> {
            Iterable<DataEntity> entities = invocation.getArgument(0);
            List<DataEntity> saved = toList(entities);
            savedBatches.add(saved);
            return saved;
        });
        return repository;
    }

    private static List<DataEntity> toList(Iterable<DataEntity> entities) {
        List<DataEntity> result = new ArrayList<>();
        entities.forEach(result::add);
        return result;
    }

    private Source source(int id) {
        Source source = new Source();
        source.setId(id);
        return source;
    }

    private DataCrime data(Source source, String reportNum, String description, String address) {
        DataCrime data = new DataCrime(source);
        data.setReportNum(reportNum);
        data.setReportCategory("Theft");
        data.setDescription(description);
        data.setAddress(address);
        data.setLocation("Austin, TX");
        data.setLatitude(30.2672);
        data.setLongitude(-97.7431);
        data.setReportedAt(LocalDateTime.of(2026, 1, 1, 12, 0));
        return data;
    }
}
