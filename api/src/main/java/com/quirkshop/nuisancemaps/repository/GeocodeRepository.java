package com.quirkshop.nuisancemaps.repository;

import java.util.List;

import com.quirkshop.nuisancemaps.model.Geocode;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GeocodeRepository extends CrudRepository<Geocode, Integer> {

    List<Geocode> findAllByAddressIn(List<String> address);
}
