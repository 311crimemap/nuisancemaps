package com.quirkshop.nuisancemaps.repository;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.quirkshop.nuisancemaps.NuisancemapsApplication;
import com.quirkshop.nuisancemaps.model.DataCrime;
import com.quirkshop.nuisancemaps.model.Source;

@SpringBootTest(classes = NuisancemapsApplication.class)
public class DataCrimeRepositoryTest {

    @Autowired
    public DataCrimeRepository datacrime_repo;

    @Autowired
    public SourceRepository sourceRepository;

    @Test
    @Transactional
    public void DataRepositoryFindByReportNumTest() throws Exception {
        Source s = new Source("category", "description", "url");
        sourceRepository.save(s);
        DataCrime d = new DataCrime(s);
        DataCrime d2 = new DataCrime(s);
        d.setReportNum("123");
        d2.setReportNum("abc");
        datacrime_repo.save(d);
        datacrime_repo.save(d2);
        DataCrime x = datacrime_repo.findOneByReportNum("123");
        DataCrime x2 = datacrime_repo.findOneByReportNum("abc");
        assertThat(x.getReportNum()).isEqualTo("123");
        assertThat(x2.getReportNum()).isEqualTo("abc");
    }

    @Test
    @Transactional
    public void DataRepositoryFindAllByReportNumTest() throws Exception {
        Source s = new Source("category", "description", "url");
        sourceRepository.save(s);
        DataCrime d = new DataCrime(s);
        DataCrime d2 = new DataCrime(s);
        d.setReportNum("123");
        d2.setReportNum("abc");
        datacrime_repo.save(d);
        datacrime_repo.save(d2);

        List<String> dataCrimes = new ArrayList<String>(List.of( "123", "abc" ));
        List<DataCrime> results = datacrime_repo.findAllByReportNumIn(dataCrimes);
        assertThat(results.get(0).getReportNum()).isEqualTo("123");
        assertThat(results.get(1).getReportNum()).isEqualTo("abc");
    }
}
