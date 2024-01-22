package com.quirkshop.nuisancemaps.repository;
import org.springframework.data.repository.CrudRepository;
import com.quirkshop.nuisancemaps.model.DataCrime;

public interface DataCrimeRepository extends CrudRepository<DataCrime, Integer> {
        // auto implemented
}
