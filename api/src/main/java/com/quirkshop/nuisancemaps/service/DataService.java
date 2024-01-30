package com.quirkshop.nuisancemaps.service;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;

import org.apache.commons.lang3.StringUtils;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quirkshop.nuisancemaps.model.Data311;
import com.quirkshop.nuisancemaps.model.DataCrime;
import com.quirkshop.nuisancemaps.model.DataError;
import com.quirkshop.nuisancemaps.model.DataJob;
import com.quirkshop.nuisancemaps.model.DataJobStatus;
import com.quirkshop.nuisancemaps.model.Source;
import com.quirkshop.nuisancemaps.repository.Data311Repository;
import com.quirkshop.nuisancemaps.repository.DataCrimeRepository;
import com.quirkshop.nuisancemaps.repository.DataErrorRepository;

import java.util.List;
import java.util.Map;

@Service
public class DataService {

    private ObjectMapper objectMapper;
    private static final Logger log = LoggerFactory.getLogger(DataService.class);
    private final int LOG_NUM = 3000;
    private final int ERROR_RATE = 5;

    @Autowired
    private DataCrimeRepository datacrime_repo;

    @Autowired
    private Data311Repository data311_repo;

    @Autowired
    private DataErrorRepository dataErrorRepository;

    public DataService() {
        this.objectMapper = new ObjectMapper();
    }

    public int createData(Source source, DataJob dataJob, String jsonResponse) {
        int num = 0;
        int errors = 0;
        List<Map<String, Object>> responseList = null;
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

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
            try {

                switch (source.getCategory()) {
                    case "crime":
                        createDataCrime(source, responseObject, geometryFactory);
                        break;
                    case "311":
                        createData311(source, responseObject, geometryFactory);
                        break;
                    default:
                        break;
                }

                num++;

                if (num % LOG_NUM == 0) {
                    log.info(source.getCategory() + ": " + source.getUrl() + ": Processed " + num);
                }

            } catch (Exception e) {
                log.info("[DataService] createData error");
                errors++;
                e.printStackTrace(pw);

                String error_msg = StringUtils.substring(sw.toString(), 0, 255);
                String content = StringUtils.substring(responseObject.toString(), 0, 255);

                DataError dataError = new DataError(dataJob, content, error_msg);
                dataErrorRepository.save(dataError);

                log.info(content);
                log.info(error_msg);
            }
        }

        // 5% error rate, mark job as failed to figure out consistent error
        if (errors > (num / ERROR_RATE)) {
            dataJob.setStatus(DataJobStatus.ERROR);
        }

        return num;
    }

    public boolean createDataCrime(Source source, Map<String, Object> responseObject, GeometryFactory geometryFactory) {

        Map<String, Object> mapping = source.getMapping();
        String report_num = responseObject.get(mapping.get("report_num")).toString();
        String category = responseObject.get(mapping.get("category")).toString();
        String description = responseObject.getOrDefault(mapping.get("description"), "").toString();
        String location = responseObject.getOrDefault(mapping.get("location"), "").toString();
        String lat = responseObject.getOrDefault(mapping.get("latitude"), "").toString();
        String lng = responseObject.getOrDefault(mapping.get("longitude"), "").toString();

        Double latitude = lat.isEmpty() ? null : Double.parseDouble(lat);
        Double longitude = lng.isEmpty() ? null : Double.parseDouble(lng);
        Coordinate coordinate = null;
        Point point = null;

        if (!lat.isEmpty() && !lng.isEmpty()) {
            coordinate = new Coordinate(latitude, longitude);
            point = geometryFactory.createPoint(coordinate);
        }

        LocalDateTime reported_at = LocalDateTime.parse(responseObject.get(mapping.get("reported_at")).toString());

        DataCrime data_crime = datacrime_repo.findByReportNum(report_num);

        if (data_crime == null) {
            data_crime = new DataCrime(source);
        }

        data_crime.setReport_num(report_num);
        data_crime.setCategory(category);
        data_crime.setDescription(description.isEmpty() ? null : description);
        data_crime.setLocation(location.isEmpty() ? null : location);
        data_crime.setLatitude(latitude);
        data_crime.setLongitude(longitude);
        data_crime.setPoint(point);
        data_crime.setReported_at(reported_at);
        data_crime.setUpdated_at(LocalDateTime.now());

        data_crime = datacrime_repo.save(data_crime);

        return true;
    }

    public boolean createData311(Source source, Map<String, Object> responseObject, GeometryFactory geometryFactory) {
        Map<String, Object> mapping = source.getMapping();

        String report_num = responseObject.get(mapping.get("report_num")).toString();
        String category = responseObject.get(mapping.get("category")).toString();
        String description = responseObject.getOrDefault(mapping.get("description"), "").toString();
        String location = responseObject.getOrDefault(mapping.get("location"), "").toString();
        String lat = responseObject.getOrDefault(mapping.get("latitude"), "").toString();
        String lng = responseObject.getOrDefault(mapping.get("longitude"), "").toString();

        Double latitude = lat.isEmpty() ? null : Double.parseDouble(lat);
        Double longitude = lng.isEmpty() ? null : Double.parseDouble(lng);
        Coordinate coordinate = null;
        Point point = null;

        if (!lat.isEmpty() && !lng.isEmpty()) {
            coordinate = new Coordinate(latitude, longitude);
            point = geometryFactory.createPoint(coordinate);
        }

        LocalDateTime reported_at = LocalDateTime.parse(responseObject.get(mapping.get("reported_at")).toString());

        Data311 data_311 = data311_repo.findByReportNum(report_num);

        if (data_311 == null) {
            data_311 = new Data311(source);
        }

        data_311.setReport_num(report_num);
        data_311.setCategory(category);
        data_311.setDescription(description.isEmpty() ? null : description);
        data_311.setLocation(location.isEmpty() ? null : location);
        data_311.setLatitude(latitude);
        data_311.setLongitude(longitude);
        data_311.setPoint(point);
        data_311.setReported_at(reported_at);
        data_311.setUpdated_at(LocalDateTime.now());

        data_311 = data311_repo.save(data_311);

        return true;
    }

}
