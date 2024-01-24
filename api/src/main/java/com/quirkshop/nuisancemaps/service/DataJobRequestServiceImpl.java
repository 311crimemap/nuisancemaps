package com.quirkshop.nuisancemaps.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.locationtech.jts.geom.GeometryFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quirkshop.nuisancemaps.model.DataCrime;
import com.quirkshop.nuisancemaps.model.DataJob;
import com.quirkshop.nuisancemaps.model.Source;
import com.quirkshop.nuisancemaps.repository.DataCrimeRepository;
import com.quirkshop.nuisancemaps.repository.SourceRepository;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class DataJobRequestServiceImpl implements DataJobRequestService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private DataCrimeRepository datacrime_repo;

    private DataJob dataJob;
    private ObjectMapper objectMapper;
    private String jsonResponse;

    public DataJobRequestServiceImpl() {
        this.objectMapper = new ObjectMapper();
        this.dataJob = null;
        this.jsonResponse = null;
    }

    @Override
    public String fetchJSON(DataJob d) {
        this.dataJob = d;

        try {
            this.dataJob.buildURL();
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            // TODO: update status - set as error
            e.printStackTrace();
        }

        String url = this.dataJob.getUrl();
        // TODO: update status pending
        this.jsonResponse = restTemplate.getForObject(url, String.class);

        // TODO: update status - set as complete
        return jsonResponse;
    }

    @Override
    public int createData() {
        int num = 0;
        List<Map<String, Object>> responseList = null;

        try {
            responseList = objectMapper.readValue(this.jsonResponse, new TypeReference<List<Map<String, Object>>>() {
            });
        } catch (JsonMappingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        Source source = this.dataJob.getSource();
        GeometryFactory geometryFactory = new GeometryFactory();

        // for each object in list
        for (Map<String, Object> responseObject : responseList) {
            // responseObject contains key/val (another obj)

            // for each key in config
            // System.out.println("K: " + responseObject.get("incident_report_number"));

            DataCrime data_crime = new DataCrime(source,
                    responseObject.get("incident_report_number").toString(),
                    responseObject.get("crime_type").toString(),
                    "", // responseObject.get("description").toString(),
                    responseObject.get("location_type").toString(),
                    geometryFactory,
                    Double.parseDouble(responseObject.get("latitude").toString()),
                    Double.parseDouble(responseObject.get("longitude").toString()),
                    LocalDateTime.parse(responseObject.get("occ_date_time").toString()));

            System.out.println(data_crime.toString());
            System.out.println("Saving");

            data_crime = datacrime_repo.save(data_crime);
            num++;
        }

        return num;

    }
}
