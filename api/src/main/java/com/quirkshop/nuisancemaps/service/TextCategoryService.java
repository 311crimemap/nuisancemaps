package com.quirkshop.nuisancemaps.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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

    private HashMap<Integer, Integer> dataCrimeCategoryLabelToIdMap;
    private HashMap<Integer, Integer> data311CategoryLabelToIdMap;
    private HashMap<String, Integer> dataCrimeTextToCategoryIdMap;
    private HashMap<String, Integer> data311TextToCategoryIdMap;
    private HashSet<Integer> skipSet;

    public TextCategoryService() {
        dataCrimeCategoryLabelToIdMap = new HashMap<Integer, Integer>();
        data311CategoryLabelToIdMap = new HashMap<Integer, Integer>();
        dataCrimeTextToCategoryIdMap = new HashMap<String, Integer>();
        data311TextToCategoryIdMap = new HashMap<String, Integer>();
        skipSet = new HashSet<Integer>();
    }

    @PostConstruct
    public void initMaps() {
        log.info("[TextCategoryService] initMap");

        // only want labels
        initCategoryLabelMap(dataCrimeCategoryLabelToIdMap, "crime");
        initCategoryLabelMap(data311CategoryLabelToIdMap, "311");

        // lookups text -> category_id during data creation
        refreshTextCategoryIdMap();
    }

    public void initCategoryLabelMap(HashMap<Integer, Integer> map, String dataType) {

        List<Category> data = categoryRepository.findAllByDataType(dataType);

        for (Category category : data) {
            if (category.getLabel() == null)
                continue;
            map.put(category.getLabel(), category.getId());
        }
    }

    public void loadCategorySkipSet(String skipText) {
        List<Category> skips = categoryRepository.findAllByText(skipText);
        for (Category skip : skips) {
            skipSet.add(skip.getId());
        }
    }

    // TODO: refactor
    public void refreshTextCategoryIdMap() {
        loadTextCategoryIdMap(dataCrimeTextToCategoryIdMap, "crime");
        loadTextCategoryIdMap(data311TextToCategoryIdMap, "311");
        loadCategorySkipSet("SKIP");
    }

    public void loadTextCategoryIdMap(HashMap<String, Integer> map, String dataType) {
        map.clear();
        List<TextCategory> textCategories = textCategoryRepository.findAllByDataType(dataType);
        for (TextCategory textCategory : textCategories) {
            map.put(textCategory.getText(), textCategory.getCategory().getId());
        }
    }

    public Category lookupCategory(String dataType, String text) {

        Integer id = null;

        // default crime
        HashMap<String, Integer> map = dataCrimeTextToCategoryIdMap;

        if (dataType.equals("311"))
            map = data311TextToCategoryIdMap;

        id = map.getOrDefault(text, null);

        if (id == null)
            return null;

        Category c = new Category();
        c.setId(id);
        return c;
    }

    // takes (text, label) array,
    // look up each label to get associated category id
    // save in TextCategory (text, cat_id)
    public List<TextCategory> createTextCategories(List<TextLabelDTO> textLabelDTOs) {

        List<TextCategory> res = new ArrayList<TextCategory>();

        // lookup each in map
        Integer category_id = null;
        HashMap<Integer, Integer> mapping = null;
        for (TextLabelDTO textLabelDTO : textLabelDTOs) {

            if (textLabelDTO.getDataType().equals("crime")) {
                mapping = dataCrimeCategoryLabelToIdMap;
            } else {
                mapping = data311CategoryLabelToIdMap;
            }

            category_id = mapping.getOrDefault(textLabelDTO.getLabel(), null);
            if (category_id == null) {
                // TODO: record error, continue;
                continue;
            }

            Category temp = categoryRepository.findById(category_id).orElse(null);

            TextCategory tc = new TextCategory(textLabelDTO.getDataType(),
                    textLabelDTO.getText(),
                    temp);

            try {
                textCategoryRepository.save(tc);
                res.add(tc);
            } catch (DataIntegrityViolationException e) {
                log.error(e.getMessage());
            }

        }

        return res;
    }

    public boolean lookupIsSkip(Integer category_id) {
        return skipSet.contains(category_id);
    }

    public HashMap<Integer, Integer> getDataCrimeCategoryLabelToIdMap() {
        return dataCrimeCategoryLabelToIdMap;
    }

    public void setDataCrimeCategoryLabelToIdMap(HashMap<Integer, Integer> dataCrimeMap) {
        this.dataCrimeCategoryLabelToIdMap = dataCrimeMap;
    }

    public HashMap<Integer, Integer> getData311CategoryLabelToIdMap() {
        return data311CategoryLabelToIdMap;
    }

    public void setData311CategoryLabelToIdMap(HashMap<Integer, Integer> data311Map) {
        this.data311CategoryLabelToIdMap = data311Map;
    }

    public static Logger getLog() {
        return log;
    }

    public CategoryRepository getCategoryRepository() {
        return categoryRepository;
    }

    public void setCategoryRepository(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public TextCategoryRepository getTextCategoryRepository() {
        return textCategoryRepository;
    }

    public void setTextCategoryRepository(TextCategoryRepository textCategoryRepository) {
        this.textCategoryRepository = textCategoryRepository;
    }

    public CategoryService getCategoryService() {
        return categoryService;
    }

    public void setCategoryService(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    public HashMap<String, Integer> getDataCrimeTextToCategoryIdMap() {
        return dataCrimeTextToCategoryIdMap;
    }

    public void setDataCrimeTextToCategoryIdMap(HashMap<String, Integer> dataCrimeTextToCategoryIdMap) {
        this.dataCrimeTextToCategoryIdMap = dataCrimeTextToCategoryIdMap;
    }

    public HashMap<String, Integer> getData311TextToCategoryIdMap() {
        return data311TextToCategoryIdMap;
    }

    public void setData311TextToCategoryIdMap(HashMap<String, Integer> data311TextToCategoryIdMap) {
        this.data311TextToCategoryIdMap = data311TextToCategoryIdMap;
    }

    public HashSet<Integer> getSkipSet() {
        return skipSet;
    }

}
