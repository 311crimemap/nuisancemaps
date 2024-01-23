package com.quirkshop.nuisancemaps.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.locationtech.jts.geom.GeometryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quirkshop.nuisancemaps.model.DataCrime;
import com.quirkshop.nuisancemaps.model.Source;
import com.quirkshop.nuisancemaps.repository.DataCrimeRepository;
import com.quirkshop.nuisancemaps.repository.SourceRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class DataJobRequestServiceImpl implements DataJobRequestService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private DataCrimeRepository datacrime_repo;

    @Autowired
    private SourceRepository source_repo;

    @Override
    public String fetchJSON(Source source) {
        String url = source.getUrl();
        // source = source_repo.save(source);

        // TODO: configLoader ?

        ObjectMapper objectMapper = new ObjectMapper();
        
        // String result = restTemplate.getForObject(url, String.class);
        String jsonResponse = restTemplate.getForObject(url, String.class);

        // Convert JSON string to List<Map<String, Object>>
        List<Map<String, Object>> responseList = null;

        try {
            responseList = objectMapper.readValue(jsonResponse, new TypeReference<List<Map<String, Object>>>() {
            });
        } catch (JsonMappingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        source = source_repo.save(source);
        GeometryFactory geometryFactory = new GeometryFactory();

        // for each object in list
        for (Map<String, Object> responseObject : responseList) {
            // responseObject contains key/val (another obj)

            // for each key in config
            // System.out.println("K: " + responseObject.get("incident_report_number"));

            DataCrime d = new DataCrime(source,
                    responseObject.get("incident_report_number").toString(),
                    responseObject.get("crime_type").toString(),
                    "", // responseObject.get("description").toString(),
                    responseObject.get("location_type").toString(),
                    geometryFactory,
                    Double.parseDouble(responseObject.get("latitude").toString()),
                    Double.parseDouble(responseObject.get("longitude").toString()),
                    LocalDateTime.parse(responseObject.get("occ_date_time").toString()));

            System.out.println(d.toString());
            System.out.println("Saving");
            d = datacrime_repo.save(d);
            // System.out.println("COUNT: " + datacrime_repo.count());

            System.out.println("ID: " + d.getId());
            /*
             * for (Map.Entry<String, Object> entry : responseObject.entrySet()) {
             * String fieldName = entry.getKey();
             * Object fieldValue = entry.getValue();
             * 
             * // Process the field name and value as needed
             * System.out.println("Field Name: " + fieldName + ", Field Value: " +
             * fieldValue);
             * }
             */
        }
        // TODO:
        // pluck fields of interest
        // make data
        // distinction change to source.getFetchUrl() vs. base url?

        // return result;
        return url;
    }
}
