package com.quirkshop.nuisancemaps.service;

import java.time.LocalDateTime;

import org.locationtech.jts.geom.GeometryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quirkshop.nuisancemaps.model.DataCrime;
import com.quirkshop.nuisancemaps.model.Source;
import com.quirkshop.nuisancemaps.repository.DataCrimeRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class DataService {

    private ObjectMapper objectMapper;

    @Autowired
    private DataCrimeRepository datacrime_repo;

    public DataService() {
        this.objectMapper = new ObjectMapper();
    }

    public int createData(Source source, String jsonResponse) {
        int num = 0;
        List<Map<String, Object>> responseList = null;

        try {
            responseList = objectMapper.readValue(jsonResponse,
                    new TypeReference<List<Map<String, Object>>>() {
                    });
        } catch (JsonMappingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        GeometryFactory geometryFactory = new GeometryFactory();

        // for each object in list
        for (Map<String, Object> responseObject : responseList) {
            // responseObject contains key/val (another obj)
            switch (source.getCategory()) {
                case "crime":
                    createDataCrime(source, responseObject, geometryFactory);
                    break;
                case "311":
                    // createData311(source, responseObject, geometryFactory);
                    break;
                default:
                    break;
            }
            num++;
        }

        return num;
    }

    public boolean createDataCrime(Source source, Map<String, Object> responseObject, GeometryFactory geometryFactory) {
        Map<String, Object> mapping = source.getMapping();
        String location = responseObject.getOrDefault(mapping.get("location"), "").toString();
        String description = responseObject.getOrDefault(mapping.get("description"), "").toString();

        DataCrime data_crime = new DataCrime(source,
                responseObject.get(mapping.get("report_num")).toString(),
                responseObject.get(mapping.get("category")).toString(),
                description.isEmpty() ? null : description,
                location.isEmpty() ? null : location,
                geometryFactory,
                Double.parseDouble(responseObject.get(mapping.get("latitude")).toString()),
                Double.parseDouble(responseObject.get(mapping.get("longitude")).toString()),
                LocalDateTime.parse(responseObject.get(mapping.get("reported_at")).toString()));

        data_crime = datacrime_repo.save(data_crime);

        return true;

    }
}
