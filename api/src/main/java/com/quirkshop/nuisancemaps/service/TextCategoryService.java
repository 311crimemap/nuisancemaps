package com.quirkshop.nuisancemaps.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.quirkshop.nuisancemaps.dto.TextLabelDTO;
import com.quirkshop.nuisancemaps.model.Category;
import com.quirkshop.nuisancemaps.model.TextCategory;
import com.quirkshop.nuisancemaps.repository.CategoryRepository;
import com.quirkshop.nuisancemaps.repository.PendingTextCategoryRepository;
import com.quirkshop.nuisancemaps.repository.TextCategoryRepository;
import com.quirkshop.nuisancemaps.service.dataparser.DataParser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import net.logstash.logback.argument.StructuredArguments;

@Service
public class TextCategoryService {

    private static final Logger log = LoggerFactory.getLogger(DataParser.class);

    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    TextCategoryRepository textCategoryRepository;

    @Autowired
    PendingTextCategoryRepository pendingTextCategoryRepository;

    @Autowired
    CategoryService categoryService;

    // label -> category_id (submitting labeled text categories)
    private ConcurrentHashMap<Integer, Integer> dataCrimeCategoryLabelToIdMap;
    private ConcurrentHashMap<Integer, Integer> data311CategoryLabelToIdMap;

    // text -> category_id (building data entities)
    private ConcurrentHashMap<String, Integer> dataCrimeTextToCategoryIdMap;
    private ConcurrentHashMap<String, Integer> data311TextToCategoryIdMap;
    private ConcurrentHashMap<Integer, Boolean> skipSet;

    public TextCategoryService() {
        dataCrimeCategoryLabelToIdMap = new ConcurrentHashMap<Integer, Integer>();
        data311CategoryLabelToIdMap = new ConcurrentHashMap<Integer, Integer>();
        dataCrimeTextToCategoryIdMap = new ConcurrentHashMap<String, Integer>();
        data311TextToCategoryIdMap = new ConcurrentHashMap<String, Integer>();
        skipSet = new ConcurrentHashMap<Integer, Boolean>();
    }

    /**
     * Initializes the maps used for category lookups.
     * Ensures method is called after the service is created to populate
     * the category label maps and text category ID maps.
     */
    @PostConstruct
    public void initMaps() {
        log.info("[TextCategoryService] initMap");

        clearAllMaps();

        // for label -> category_id lookup during textCategory submission
        initCategoryLabelMap(dataCrimeCategoryLabelToIdMap, "crime");
        initCategoryLabelMap(data311CategoryLabelToIdMap, "311");

        // for text -> category_id lookup during data entity creation
        refreshTextCategoryIdMap();
    }

    /**
     * Refreshes the text category ID maps and the skip set; loads the current
     * mappings.
     */
    public void refreshTextCategoryIdMap() {
        loadTextCategoryIdMap(dataCrimeTextToCategoryIdMap, "crime");
        loadTextCategoryIdMap(data311TextToCategoryIdMap, "311");
        loadCategorySkipSet("SKIP");
    }

    /**
     * Clears all maps used for category and text lookups.
     */
    public void clearAllMaps() {
        dataCrimeCategoryLabelToIdMap.clear();
        data311CategoryLabelToIdMap.clear();
        dataCrimeTextToCategoryIdMap.clear();
        data311TextToCategoryIdMap.clear();
    }

    private void initCategoryLabelMap(ConcurrentHashMap<Integer, Integer> map, String dataType) {
        List<Category> data = categoryRepository.findAllByDataType(dataType);

        for (Category category : data) {
            if (category.getLabel() == null)
                continue;
            map.put(category.getLabel(), category.getId());
        }
    }

    private void loadTextCategoryIdMap(ConcurrentHashMap<String, Integer> map, String dataType) {
        List<TextCategory> textCategories = textCategoryRepository.findAllByDataType(dataType);
        for (TextCategory textCategory : textCategories) {
            map.put(textCategory.getText(), textCategory.getCategory().getId());
        }
    }

    private void loadCategorySkipSet(String skipText) {
        List<Category> skips = categoryRepository.findAllByText(skipText);
        for (Category skip : skips) {
            skipSet.put(skip.getId(), true);
        }
    }

    /**
     * Looks up a category by its data type and text, returning the associated
     * Category object.
     *
     * @param dataType the type of data to lookup (311, crime)
     * @param text     the category text
     * @return corresponding Category object, or null
     */
    public Category lookupCategory(String dataType, String text) {

        Integer id = null;

        // default crime
        ConcurrentHashMap<String, Integer> map = dataCrimeTextToCategoryIdMap;

        if (dataType.equals("311"))
            map = data311TextToCategoryIdMap;

        id = map.getOrDefault(text, null);

        if (id == null)
            return null;

        Category c = new Category();
        c.setId(id);
        return c;
    }

    /**
     * Creates and saves text categories based on the provided list of text-label
     * DTOs: (text, label) list. Handles duplicates
     *
     * @param textLabelDTOs the list of text-label DTO tuples
     * @return a list of created TextCategory objects
     */
    public List<TextCategory> createTextCategories(List<TextLabelDTO> textLabelDTOs) {

        List<TextCategory> res = new ArrayList<TextCategory>();

        // lookup each in map
        Integer category_id = null;
        ConcurrentHashMap<Integer, Integer> mapping = null;
        for (TextLabelDTO textLabelDTO : textLabelDTOs) {

            if (textLabelDTO.getDataType().equals("crime")) {
                mapping = dataCrimeCategoryLabelToIdMap;
            } else {
                mapping = data311CategoryLabelToIdMap;
            }

            category_id = mapping.getOrDefault(textLabelDTO.getLabel(), null);
            if (category_id == null) {
                Map<String, Object> logDetails = Map.of("categoryId", textLabelDTO.getText());
                log.info("Missing category_id",
                        StructuredArguments.entries(Map.of("data", logDetails)));
                continue;
            }

            Category temp = categoryRepository.findById(category_id).orElse(null);

            TextCategory tc = new TextCategory(textLabelDTO.getDataType(),
                    textLabelDTO.getText(),
                    temp);

            try {
                textCategoryRepository.save(tc);
                res.add(tc);

                // clean up, remove from PendingTextCategory on successful add
                pendingTextCategoryRepository.deleteByDataTypeAndText(tc.getDataType(), tc.getText());

            } catch (DataIntegrityViolationException e) {
                // remove from PendingTextCategory on dupe (out of sync somehow, etc)
                pendingTextCategoryRepository.deleteByDataTypeAndText(tc.getDataType(), tc.getText());
                Map<String, Object> logError = Map.of("error", e.getMessage());
                log.error("[TextCategoryService]",
                        StructuredArguments.entries(Map.of("data", logError)));

            } catch (Exception e) {
                Map<String, Object> logError = Map.of("error", e.getMessage());
                log.error("[TextCategoryService]",
                        StructuredArguments.entries(Map.of("data", logError)));
            }

        }

        return res;
    }

    public boolean lookupIsSkip(Integer category_id) {
        return skipSet.containsKey(category_id);
    }

    public ConcurrentHashMap<Integer, Integer> getDataCrimeCategoryLabelToIdMap() {
        return dataCrimeCategoryLabelToIdMap;
    }

    public void setDataCrimeCategoryLabelToIdMap(ConcurrentHashMap<Integer, Integer> dataCrimeMap) {
        this.dataCrimeCategoryLabelToIdMap = dataCrimeMap;
    }

    public ConcurrentHashMap<Integer, Integer> getData311CategoryLabelToIdMap() {
        return data311CategoryLabelToIdMap;
    }

    public void setData311CategoryLabelToIdMap(ConcurrentHashMap<Integer, Integer> data311Map) {
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

    public ConcurrentHashMap<String, Integer> getDataCrimeTextToCategoryIdMap() {
        return dataCrimeTextToCategoryIdMap;
    }

    public void setDataCrimeTextToCategoryIdMap(ConcurrentHashMap<String, Integer> dataCrimeTextToCategoryIdMap) {
        this.dataCrimeTextToCategoryIdMap = dataCrimeTextToCategoryIdMap;
    }

    public ConcurrentHashMap<String, Integer> getData311TextToCategoryIdMap() {
        return data311TextToCategoryIdMap;
    }

    public void setData311TextToCategoryIdMap(ConcurrentHashMap<String, Integer> data311TextToCategoryIdMap) {
        this.data311TextToCategoryIdMap = data311TextToCategoryIdMap;
    }

    public ConcurrentHashMap<Integer, Boolean> getSkipSet() {
        return skipSet;
    }

}
