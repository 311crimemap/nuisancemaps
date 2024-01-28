package com.quirkshop.nuisancemaps.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.quirkshop.nuisancemaps.model.Data311;

import jakarta.persistence.EntityManager;

interface Data311CustomRepository {
    public Data311 findByReportNum(String report_num);
}

class Data311CustomRepositoryImpl implements Data311CustomRepository {

    @Autowired
    private EntityManager entityManager;

    @Transactional
    public Data311 findByReportNum(String report_num) {

        Data311 d = entityManager
                .createQuery("SELECT d FROM Data311 d WHERE d.report_num = :value1", Data311.class)
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
public interface Data311Repository extends CrudRepository<Data311, Integer>, Data311CustomRepository {
    // auto implemented
}
