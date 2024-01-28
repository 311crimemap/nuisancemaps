package com.quirkshop.nuisancemaps.repository;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

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
    public void DataRepositoryFindByReportNum() throws Exception {
        Source s = new Source("category", "description", "url");
        sourceRepository.save(s);
        DataCrime d = new DataCrime(s);
        DataCrime d2 = new DataCrime(s);
        d.setReport_num("123");
        d2.setReport_num("abc");
        datacrime_repo.save(d);
        datacrime_repo.save(d2);
        DataCrime x = datacrime_repo.findByReportNum("123");
        DataCrime x2 = datacrime_repo.findByReportNum("abc");
        assertThat(x.getReport_num()).isEqualTo("123");
        assertThat(x2.getReport_num()).isEqualTo("abc");
    }
}
