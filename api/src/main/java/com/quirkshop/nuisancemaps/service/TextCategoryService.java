package com.quirkshop.nuisancemaps.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.quirkshop.nuisancemaps.dto.TextLabelDTO;
import com.quirkshop.nuisancemaps.model.Category;
import com.quirkshop.nuisancemaps.model.TextCategory;
import com.quirkshop.nuisancemaps.repository.CategoryRepository;
import com.quirkshop.nuisancemaps.repository.TextCategoryRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;

@Service
public class TextCategoryService {

    private static final Logger log = LoggerFactory.getLogger(DataService.class);

    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    TextCategoryRepository textCategoryRepository;

    @Autowired
    CategoryService categoryService;

    private HashMap<Integer, Integer> dataCrimeLabelToIdMap;
    private HashMap<Integer, Integer> data311LabeltoIdMap;

    public TextCategoryService() {
        dataCrimeLabelToIdMap = new HashMap<Integer, Integer>();
        data311LabeltoIdMap = new HashMap<Integer, Integer>();
    }

    @PostConstruct
    public void initMaps() {
        // only want labels
        initMap(dataCrimeLabelToIdMap, "crime");
        initMap(data311LabeltoIdMap, "311");
    }

    public void initMap(HashMap<Integer, Integer> map, String dataType) {
        log.info("[TextCategoryService] initMap: " + dataType);

        List<Category> data = categoryRepository.findAllByDataType(dataType);

        for (Category category : data) {
            if (category.getLabel() == null)
                continue;
            map.put(category.getLabel(), category.getId());
        }
    }

    // takes (text, label) array,
    // look up each label to get associated category id
    // save in TextCategory (text, cat_id)
    public Iterable<TextCategory> createTextCategories(List<TextLabelDTO> textLabelDTOs) {
        int num = 0;
        List<TextCategory> res = new ArrayList<TextCategory>();

        // lookup each in map
        Integer category_id = null;
        HashMap<Integer, Integer> mapping = null;
        for (TextLabelDTO textLabelDTO : textLabelDTOs) {
            if (textLabelDTO.getDataType().equals("crime")) {
                mapping = dataCrimeLabelToIdMap;
            } else {
                mapping = data311LabeltoIdMap;
            }

            category_id = mapping.getOrDefault(textLabelDTO.getLabel(), null);
            if (category_id == null) {
                // TODO: record error, continue;
                continue;
            }

            // create new TextCategory
            //Category temp = new Category();
            Category temp = categoryRepository.findById(category_id).orElse(null);

            TextCategory tc = new TextCategory(textLabelDTO.getDataType(),
                    textLabelDTO.getText(),
                    temp);

            res.add(tc);
            num++;

        }

        //marked as transactional
        return textCategoryRepository.saveAll(res);
    }

    public HashMap<Integer, Integer> getDataCrimeLabelToIdMap() {
        return dataCrimeLabelToIdMap;
    }

    public void setDataCrimeLabelToIdMap(HashMap<Integer, Integer> dataCrimeMap) {
        this.dataCrimeLabelToIdMap = dataCrimeMap;
    }

    public HashMap<Integer, Integer> getData311LabelToIdMap() {
        return data311LabeltoIdMap;
    }

    public void setData311LabelToIdMap(HashMap<Integer, Integer> data311Map) {
        this.data311LabeltoIdMap = data311Map;
    }

}
