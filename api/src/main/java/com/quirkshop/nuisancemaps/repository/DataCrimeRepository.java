package com.quirkshop.nuisancemaps.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.quirkshop.nuisancemaps.model.DataCrime;

import jakarta.persistence.EntityManager;

interface DataCrimeCustomRepository {
        public DataCrime findByReportNum(String report_num);
}

class DataCrimeCustomRepositoryImpl implements DataCrimeCustomRepository {

        @Autowired
        private EntityManager entityManager;

        @Transactional
        public DataCrime findByReportNum(String report_num) {

                DataCrime d = entityManager
                                .createQuery("SELECT d FROM DataCrime d WHERE d.reportNum = :value1", DataCrime.class)
                                .setParameter("value1", report_num)
                                .setMaxResults(1)
                                .getResultList()
                                .stream()
                                .findFirst()
                                .orElse(null);

                return d;
        }
}

@Repository
public interface DataCrimeRepository extends CrudRepository<DataCrime, Integer>, DataCrimeCustomRepository {
        // auto implemented
}
